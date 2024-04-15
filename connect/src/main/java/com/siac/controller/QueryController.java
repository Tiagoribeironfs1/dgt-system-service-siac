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

@RestController
@RequestMapping("/api/query")
public class QueryController {
      private final JdbcTemplate jdbcTemplate;

    @Autowired
    public QueryController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
      public ResponseEntity<Map<String, Object>> executeQuery(@RequestBody String json) {
          ObjectMapper objectMapper = new ObjectMapper();

          try {
              // Desserializar o JSON para um objeto ou estrutura de dados adequada
              Map<String, Object> jsonData = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});

              // Agora você pode acessar os dados do JSON
              String sql = (String) jsonData.get("sql");

              System.out.println(sql);
              
              // Execute a consulta com o SQL fornecido
              List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
              int totalCount = result.size();

              Map<String, Object> response = new HashMap<>();
              response.put("total_count", totalCount);
              response.put("result", result);


              // Retorna a lista de resultados como resposta
              return ResponseEntity.ok(response);
          } catch (JsonProcessingException e) {
              e.printStackTrace();
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
          }
      }
}