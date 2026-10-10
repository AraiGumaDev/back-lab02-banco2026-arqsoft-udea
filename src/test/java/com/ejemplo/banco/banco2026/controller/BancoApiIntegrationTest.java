package com.ejemplo.banco.banco2026.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class BancoApiIntegrationTest {

    // Credenciales definidas en src/test/resources/application.properties
    private static final RequestPostProcessor USER = httpBasic("user", "user-test");
    private static final RequestPostProcessor ADMIN = httpBasic("admin", "admin-test");

    @Autowired
    private MockMvc mockMvc;

    private String customerJson(String account, Double balance) {
        return """
                {"firstName":"Ana","lastName":"Lopez","accountNumber":"%s","balance":%s}
                """.formatted(account, balance);
    }

    private String transactionJson(String sender, String receiver, double amount) {
        return """
                {"senderAccountNumber":"%s","receiverAccountNumber":"%s","amount":%s}
                """.formatted(sender, receiver, amount);
    }

    private long createCustomer(String account, double balance) throws Exception {
        String body = mockMvc.perform(post("/api/customers").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson(account, balance)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    // ---------- Seguridad ----------

    @Test
    void swaggerDocsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithoutCredentialsIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithWrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/customers").with(httpBasic("admin", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRoleCannotWrite() throws Exception {
        mockMvc.perform(post("/api/customers").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("SEC-001", 100.0)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userRoleCanRead() throws Exception {
        mockMvc.perform(get("/api/customers").with(USER))
                .andExpect(status().isOk());
    }

    // ---------- Clientes ----------

    @Test
    void customerCrudFlow() throws Exception {
        long id = createCustomer("CUS-001", 100.0);

        mockMvc.perform(get("/api/customers/{id}", id).with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("CUS-001"))
                .andExpect(jsonPath("$.balance").value(100.0));

        mockMvc.perform(put("/api/customers/{id}", id).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Maria\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Maria"))
                .andExpect(jsonPath("$.lastName").value("Lopez"));

        mockMvc.perform(delete("/api/customers/{id}", id).with(ADMIN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", id).with(USER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void createCustomerWithoutBalanceIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/customers").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson("CUS-002", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    // ---------- Transacciones ----------

    @Test
    void transactionFlowUpdatesBalances() throws Exception {
        long senderId = createCustomer("TRX-001", 100.0);
        long receiverId = createCustomer("TRX-002", 0.0);

        String body = mockMvc.perform(post("/api/transactions").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson("TRX-001", "TRX-002", 40.0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(40.0))
                .andReturn().getResponse().getContentAsString();
        long trxId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mockMvc.perform(get("/api/customers/{id}", senderId).with(USER))
                .andExpect(jsonPath("$.balance").value(60.0));
        mockMvc.perform(get("/api/customers/{id}", receiverId).with(USER))
                .andExpect(jsonPath("$.balance").value(40.0));

        mockMvc.perform(get("/api/transactions/{id}", trxId).with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderAccountNumber").value("TRX-001"));
        mockMvc.perform(get("/api/transactions").with(USER))
                .andExpect(status().isOk());

        // Se reduce el monto de 40 a 10: se devuelven 30 al remitente
        mockMvc.perform(put("/api/transactions/{id}", trxId).with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson("TRX-001", "TRX-002", 10.0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(10.0));
        mockMvc.perform(get("/api/customers/{id}", senderId).with(USER))
                .andExpect(jsonPath("$.balance").value(90.0));

        // Se anula la transacción: los saldos vuelven a su estado inicial
        mockMvc.perform(delete("/api/transactions/{id}", trxId).with(ADMIN))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/customers/{id}", senderId).with(USER))
                .andExpect(jsonPath("$.balance").value(100.0));
        mockMvc.perform(get("/api/customers/{id}", receiverId).with(USER))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void transferWithInsufficientBalanceIsBadRequest() throws Exception {
        createCustomer("TRX-003", 10.0);
        createCustomer("TRX-004", 0.0);

        mockMvc.perform(post("/api/transactions").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson("TRX-003", "TRX-004", 500.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Sender Balance not enough"));
    }

    @Test
    void unknownTransactionIsNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", 9999).with(USER))
                .andExpect(status().isNotFound());
    }
}
