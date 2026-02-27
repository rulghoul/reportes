package com.organizame.reportes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organizame.reportes.dto.request.CalculoISANRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestPropertySource(locations = "classpath:application-test.properties")
class ISANControllerTest {

    @Autowired
    private MockMvc mockMvc;           // 3. Herramienta para simular HTTP

    @Autowired
    private ObjectMapper mapper;
    // ============================================
    // PRUEBAS DE ENDPOINTS GET
    // ============================================

    @Test
    void debeRetornarParametros() throws Exception {
        mockMvc.perform(get("/isan/parametros"))
                .andExpect(status().isOk())              // Espera HTTP 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());     // Verifica que sea un array JSON
    }

    @Test
    void debeRetornarTarifas() throws Exception {
        mockMvc.perform(get("/isan/tarifas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void debeLimpiarCache() throws Exception {
        mockMvc.perform(get("/isan/limpia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    // ============================================
    // PRUEBAS DE ENDPOINT POST (CÁLCULO)
    // ============================================

    @Test
    void debeCalcularISAN_Exitoso() throws Exception {
        // 1. Preparar el cuerpo de la petición (DTO)
        CalculoISANRequest request = new CalculoISANRequest();
        request.setMonto(new BigDecimal("350000.00"));
        request.setAnio(2024);

        // 2. Ejecutar petición POST
        mockMvc.perform(post("/isan/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))  // Convierte a JSON

                // 3. Validar respuesta
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value("350000.00"))
                .andExpect(jsonPath("$.anio").value(2024))
                .andExpect(jsonPath("$.isan").exists())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void debeCalcularISAN_Exento() throws Exception {
        CalculoISANRequest request = new CalculoISANRequest();
        request.setMonto(new BigDecimal("200000.00"));  // Menor al límite de exención
        request.setAnio(2024);

        mockMvc.perform(post("/isan/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isan").value("0.00"))
                .andExpect(jsonPath("$.mensaje").value("El monto está exento del ISAN"));
    }

    // ============================================
    // PRUEBAS DE VALIDACIÓN (400 Bad Request)
    // ============================================

    @Test
    void debeRechazarMontoNulo() throws Exception {
        String jsonInvalido = "{\"anio\": 2024}";  // Falta el monto

        mockMvc.perform(post("/isan/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());  // Espera HTTP 400
    }

    @Test
    void debeRechazarAnioNulo() throws Exception {
        String jsonInvalido = "{\"monto\": 350000.00}";  // Falta el año

        mockMvc.perform(post("/isan/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debeRechazarMontoCeroONegativo() throws Exception {
        String jsonInvalido = "{\"monto\": 0, \"anio\": 2024}";

        mockMvc.perform(post("/isan/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }
}