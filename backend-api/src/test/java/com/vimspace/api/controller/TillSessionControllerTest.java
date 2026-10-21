package com.vimspace.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vimspace.contract.OpenTillSessionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the HTTP contract of the till-session endpoints, in particular the
 * status codes the desktop clients rely on.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TillSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @DisplayName("Opening a session returns 201 with the session payload")
    void openSessionReturnsCreated() throws Exception {
        String body = mapper.writeValueAsString(
                new OpenTillSessionRequest("POS-HTTP-1", "cashier", 30.0));

        mockMvc.perform(post("/api/till-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.terminalId").value("POS-HTTP-1"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.expectedCash").value(30.0));
    }

    @Test
    @DisplayName("Opening a second session for the same terminal returns 409")
    void duplicateOpenReturnsConflict() throws Exception {
        String body = mapper.writeValueAsString(
                new OpenTillSessionRequest("POS-HTTP-2", "cashier", 30.0));

        mockMvc.perform(post("/api/till-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/till-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("A missing terminalId returns 400 with a structured error")
    void missingTerminalReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/till-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cashierId\":\"cashier\",\"openingFloat\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("An unknown session returns 404")
    void unknownSessionReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/till-sessions/987654/reconciliation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("The active-session list and history are both reachable")
    void listEndpointsRespond() throws Exception {
        mockMvc.perform(get("/api/till-sessions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/till-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/till-sessions?status=CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
