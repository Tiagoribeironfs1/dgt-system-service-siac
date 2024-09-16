package com.siac.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Service
public class ProductSyncService {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncService.class);

    @Value("${api.base.url}")
    private String API_BASE_URL;

    @Value("${snapStore.tenantId}")
    private String TENANT_ID;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RestTemplate restTemplate = new RestTemplate();

    // Executa o serviço periodicamente (por exemplo, a cada 5 minutos)
    @PostConstruct
    @Scheduled(fixedRate = 300000) // 300000 ms = 5 minutos
    public void syncNewProducts() {
        int limit = 500;  // Tamanho do lote para buscar novos produtos
    
        // Obter o lastCodpro a partir da resposta da API
        String lastCodpro = getLastCodproFromApi();
    
        log.info("Iniciando a verificação de produtos a partir do CODPRO: {}", lastCodpro);
    
        // Obter as lojas
        List<Map<String, Object>> stores = getStoresFromApi();
        if (stores.isEmpty()) {
            log.warn("Nenhuma loja foi encontrada.");
            return;
        }
    
        // Filtrar os IDs de store existentes
        Set<String> storeIds = stores.stream()
                .map(store -> (String) store.get("storeId"))
                .collect(Collectors.toSet());
    
        boolean hasMoreProducts = true;
    
        while (hasMoreProducts) {
            // Construa a consulta SQL com base nas lojas existentes
            String sql = buildSqlQuery(limit, lastCodpro, storeIds);
    
            List<Map<String, Object>> products = jdbcTemplate.queryForList(sql);
            if (products.isEmpty()) {
                hasMoreProducts = false;
                break;
            }
    
            for (Map<String, Object> product : products) {
                // Construir o corpo da requisição para a API
                Map<String, Object> productJson = buildProductJson(product);
    
                // Log do conteúdo de productJson antes de enviar para a API
                log.info("Enviando produto para API: {}", productJson);
    
                // Enviar produto para API
                sendProductToApi(productJson);
    
                // Criar o estoque para cada loja após criar o produto
                createStorageForStores(product, stores);
    
                // Atualizar lastCodpro com o valor do último produto processado
                lastCodpro = (String) product.get("CODPRO");
    
                // // Salvar o último CODPRO para continuar a partir daí
                // CodproManager.saveLastCodpro(lastCodpro);
            }
        }
    }

    // Constrói a consulta SQL, incluindo apenas as colunas de estoque das lojas existentes
    private String buildSqlQuery(int limit, String lastCodpro, Set<String> storeIds) {
        StringBuilder estoqueColumns = new StringBuilder();
        for (String storeId : storeIds) {
            estoqueColumns.append("PRODUTO.ESTOQUE").append(storeId).append(", ");
        }

        // Remove a última vírgula e espaço
        String estoqueColumnsFinal = estoqueColumns.substring(0, estoqueColumns.length() - 2);

        return "SELECT TOP " + limit + " " +
                "PRODUTO.CODPRO, PRODUTO.NUM_FAB, PRODUTO.NUM_ORIG, PRODUTO.UNIDADE, PRODUTO.PRODUTO, PRODUTO.NACIONAL, " +
                estoqueColumnsFinal + ", " +
                "P_PRECOS.P_CUSTO, SECAO.CODSEC, SECAO.SECAO, GRUPO.CODGRU, GRUPO.GRUPO, " +
                "PRODUTO.NUM_ORIG, PRODUTO.BARRAEAN, PRODUTO.CLASSEFISC, PRODUTO.CEST, P_PRECOS.P_VENDA, PROD_APL.TX_APLICA " +
                "FROM PRODUTO " +
                "INNER JOIN P_PRECOS ON PRODUTO.CODPRO = P_PRECOS.CODPRO " +
                "INNER JOIN PROD_APL ON PRODUTO.CODPRO = PROD_APL.CODPRO " +
                "INNER JOIN SECAO ON PRODUTO.CODSEC = SECAO.CODSEC " +
                "INNER JOIN GRUPO ON PRODUTO.CODGRU = GRUPO.CODGRU " +
                "WHERE P_PRECOS.CATEGORIA = 'A' AND SECAO.SECAO <> 'SERVICOS' AND PRODUTO.CODPRO > '" + lastCodpro + "' " +
                "ORDER BY PRODUTO.CODPRO ASC";
    }

    private Map<String, Object> buildProductJson(Map<String, Object> product) {
        Map<String, Object> productJson = new HashMap<>();
        productJson.put("tenantId", TENANT_ID);
        productJson.put("productId", product.get("CODPRO"));
        productJson.put("productName", product.get("PRODUTO"));
        productJson.put("originalNumber", product.get("NUM_ORIG"));
        productJson.put("factoryNumber", product.get("NUM_FAB"));
        productJson.put("brandId", product.get("CODSEC"));
        productJson.put("brandName", product.get("SECAO"));
        productJson.put("groupId", product.get("CODGRU"));
        productJson.put("groupName", product.get("GRUPO"));

        // Transformar o campo TX_APLICA (do tipo MEMO) em String[]
        String txAplica = (String) product.get("TX_APLICA");
        if (txAplica != null) {
            String[] applicationArray = txAplica.split("\\r?\\n");
            productJson.put("application", applicationArray);
            log.info("applicationArray para o produto {}: {}", product.get("CODPRO"), applicationArray);
        } else {
            productJson.put("application", new String[0]); // Se não houver aplicação, envia um array vazio
        }

        return productJson;
    }

    private void sendProductToApi(Map<String, Object> productJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(productJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(API_BASE_URL + "/v1/warehouse/products", HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Produto enviado com sucesso: {}", productJson.get("productId"));
            } else {
                log.error("Erro ao enviar produto: {} - {}", productJson.get("productId"), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar produto: {} - {}", productJson.get("productId"), e.getMessage(), e);
        }
    }

    // Obter as lojas da API
    private List<Map<String, Object>> getStoresFromApi() {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(API_BASE_URL + "/v1/stores/tenant/" + TENANT_ID, HttpMethod.GET, null, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && (Boolean) response.getBody().get("ok")) {
                return (List<Map<String, Object>>) response.getBody().get("data");
            } else {
                log.error("Erro ao buscar lojas: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Erro ao buscar lojas: {}", e.getMessage(), e);
        }
        return List.of();  // Retornar uma lista vazia caso ocorra erro
    }

    // Criar o estoque para cada loja
    private void createStorageForStores(Map<String, Object> product, List<Map<String, Object>> stores) {
        String productId = (String) product.get("CODPRO");

        for (Map<String, Object> store : stores) {
            String storeId = (String) store.get("storeId");
            String tenantId = (String) store.get("tenantId");

            // Obter o estoque correto para a loja
            String estoqueColumn = "ESTOQUE" + storeId;
            BigDecimal stockDecimal = (BigDecimal) product.get(estoqueColumn); // Tratar como BigDecimal
            Integer stock = (stockDecimal != null) ? stockDecimal.intValue() : null; // Converter para Integer


            if (stock != null && stock > 0) {
                // Construir o JSON de estoque
                Map<String, Object> storageJson = new HashMap<>();
                storageJson.put("tenantId", tenantId);
                storageJson.put("storeId", store.get("id"));
                storageJson.put("productId", productId);
                storageJson.put("stock", stock);

                // Enviar o estoque para a API
                sendStorageToApi(storageJson);
            }
        }
    }

    private void sendStorageToApi(Map<String, Object> storageJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(storageJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(API_BASE_URL + "/v1/storages/tenant/" + TENANT_ID, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Estoque enviado com sucesso para o produto: {}", storageJson.get("productId"));
            } else {
                log.error("Erro ao enviar estoque: {} - {}", storageJson.get("productId"), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar estoque: {} - {}", storageJson.get("productId"), e.getMessage(), e);
        }
    }

    private String getLastCodproFromApi() {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = API_BASE_URL + "/v1/warehouse/products/tenant/"+ TENANT_ID +"/products/lastproduct";
    
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);
    
            // Verificar se a resposta foi bem-sucedida e contém os dados
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Map<String, Object> data = (Map<String, Object>) body.get("data");
    
                if (data != null && data.get("productId") != null) {
                    // Retornar o productId como lastCodpro
                    return (String) data.get("productId");
                }
            }
        } catch (Exception e) {
            log.error("Erro ao buscar lastCodpro da API: {}", e.getMessage());
        }
    
        // Retornar um valor padrão ou o último salvo se houver erro
        return CodproManager.loadLastCodpro();
    }

}
