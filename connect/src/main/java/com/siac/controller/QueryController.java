package com.siac.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public QueryController(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody String json) {
        try {
            // Desserializar o JSON para um objeto ou estrutura de dados adequada
            Map<String, Object> jsonData = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

            // Validação básica de SQL (melhoraria adicionar mais validação específica)
            if (!jsonData.containsKey("sql")) {
                log.error("SQL não fornecido no corpo da requisição.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Campo 'sql' é obrigatório."));
            }

            String sql = (String) jsonData.get("sql");

            log.info("Executando consulta SQL: {}", sql);

            // Executa a consulta e retorna o resultado
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            int totalCount = result.size();

            Map<String, Object> response = new HashMap<>();
            response.put("total_count", totalCount);
            response.put("result", result);

            log.info("Consulta executada com sucesso. Total de registros: {}", totalCount);
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            log.error("Erro ao processar o JSON: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erro ao processar o JSON."));
        } catch (Exception e) {
            log.error("Erro ao executar a consulta SQL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao executar a consulta."));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }
}
