package com.siac.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ConnectSiacService {

    private static final Logger log = LoggerFactory.getLogger(ConnectSiacService.class);

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;

    @Value("${api.base.url}")
    private String API_BASE_URL;

    @Value("${snapStore.tenantId}")
    private String TENANT_ID;

    @Autowired
    public ConnectSiacService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = new RestTemplate(); // Inicializando RestTemplate
    }

    private static final String SQL = 
            "SELECT " +
            "    loja AS storeId, " +
            "    cgc AS businessId, " +
            "    fantasia AS name, " +
            "    cidade AS location " +
            "FROM tpd001";

    // Agendamento para segunda a sexta, das 08:00 às 19:00
    @Scheduled(cron = "0 */5 8-19 * * MON-FRI")
    public void realizarOperacoesBancoDadosDiasUteis() {
        executarConsulta();
    }

    // Agendamento para sábado, das 08:00 às 15:00
    @Scheduled(cron = "0 */5 8-15 * * SAT")
    public void realizarOperacoesBancoDadosSabado() {
        executarConsulta();
    }

    // Método comum para executar a consulta e enviar os dados para a API
    private void executarConsulta() {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(SQL);
            result.forEach(row -> {
                // Construir o JSON para cada store
                Map<String, Object> storeJson = buildStoreJson(row);

                // Enviar o JSON para a API
                sendStoreToApi(storeJson);
            });
        } catch (Exception e) {
            log.error("Erro ao executar consulta SQL: {}", e.getMessage(), e);
        }
    }

    // Método para construir o JSON no formato correto
    private Map<String, Object> buildStoreJson(Map<String, Object> store) {
        Map<String, Object> storeJson = new HashMap<>();
        storeJson.put("tenantId", TENANT_ID);
        storeJson.put("storeId", store.get("storeId")); // mapeado de 'loja' para 'storeId'
        storeJson.put("businessId", store.get("businessId")); // mapeado de 'cgc'
        storeJson.put("name", store.get("name")); // mapeado de 'fantasia'
        storeJson.put("location", store.get("location")); // mapeado de 'cidade'

        return storeJson;
    }

    // Método para verificar se a loja já está cadastrada e enviar o JSON para a API se não estiver
    private void sendStoreToApi(Map<String, Object> storeJson) {
        String storeId = (String) storeJson.get("storeId");

        // Verificar se a loja já existe
        if (checkIfStoreExists(TENANT_ID, storeId)) {
            log.info("Loja já cadastrada: {}", storeId);
        } else {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(storeJson, headers);
                ResponseEntity<String> response = restTemplate.exchange(API_BASE_URL + "/v1/stores", HttpMethod.POST, request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Store enviada com sucesso: {}", storeJson.get("storeId"));
                } else {
                    log.error("Erro ao enviar store: {} - Status: {}", storeJson.get("storeId"), response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Erro ao enviar store: {} - {}", storeJson.get("storeId"), e.getMessage(), e);
            }
        }
    }

    // Método para verificar se a loja já existe
    private boolean checkIfStoreExists(String tenantId, String storeId) {
        try {
            String url = API_BASE_URL + "/v1/stores/tenant/" + tenantId + "/store/" + storeId;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Se a resposta for 2xx, a loja existe
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            // Se a loja não for encontrada, retorna false
            return false;
        } catch (Exception e) {
            log.error("Erro ao verificar se a loja existe: {}", e.getMessage(), e);
            return false;
        }
    }
}
