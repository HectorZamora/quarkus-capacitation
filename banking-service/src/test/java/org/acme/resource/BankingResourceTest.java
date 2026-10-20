package org.acme.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.acme.client.ExchangeRateClient;
import org.acme.dto.response.ExchangeRatesResponse;
import org.acme.entity.Currency;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
class BankingResourceTest {

    @InjectMock
    @RestClient
    ExchangeRateClient exchangeRateClient;

    @Test
    void validatesCustomerRequestsAndRejectsDuplicateDocuments() {
        given()
                .auth().oauth2(adminToken())
                .contentType("application/json")
                .body(Map.of("name", "", "document", "12345678", "email", "invalid-email"))
                .when().post("/customers")
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_ERROR"));

        String unique = UUID.randomUUID().toString();
        Map<String, String> request = customerRequest(unique, "12345678");
        long customerId = ((Number) given().auth().oauth2(adminToken()).contentType("application/json").body(request)
                .when().post("/customers")
                .then().statusCode(201)
                .extract().path("id")).longValue();

        given().auth().oauth2(adminToken()).contentType("application/json")
                .body(customerRequest(unique, "12345678"))
                .when().post("/customers")
                .then()
                .statusCode(409)
                .body("code", is("CUSTOMER_DOCUMENT_ALREADY_EXISTS"));

        given().auth().oauth2(adminToken())
                .contentType("application/json")
                .body(customerRequest(unique + "-updated", "12345678"))
                .when().put("/customers/{id}", customerId)
                .then().statusCode(200)
                .body("name", is("Test Customer " + unique + "-updated"));

        given().auth().oauth2(adminToken())
                .when().delete("/customers/{id}", customerId)
                .then().statusCode(204);
    }

    @Test
    void depositsAndTransfersWithinTheSameCurrency() {
        when(exchangeRateClient.getLatestRates("PEN"))
                .thenReturn(new ExchangeRatesResponse("success", "PEN",
                        Map.of("USD", new BigDecimal("0.29000000"))));
        long sourceCustomerId = createCustomer();
        long destinationCustomerId = createCustomer();
        String sourceNumber = openAccount(sourceCustomerId, Currency.PEN);
        String destinationNumber = openAccount(destinationCustomerId, Currency.PEN);

        given()
                .auth().oauth2(customerToken())
                .contentType("application/json")
                .body(Map.of("amount", new BigDecimal("100.00"), "description", "Initial deposit"))
                .when().post("/accounts/{number}/deposits", sourceNumber)
                .then()
                .statusCode(201)
                .body("type", is("DEPOSIT"))
                .body("destinationAccountNumber", is(sourceNumber));

        Number transactionId = given()
                .auth().oauth2(customerToken())
                .contentType("application/json")
                .body(Map.of(
                        "destinationAccountNumber", destinationNumber,
                        "amount", new BigDecimal("30.00"),
                        "description", "Test transfer"))
                .when().post("/accounts/{number}/transfers", sourceNumber)
                .then()
                .statusCode(201)
                .body("type", is("TRANSFER"))
                .body("sourceAccountNumber", is(sourceNumber))
                .body("destinationAccountNumber", is(destinationNumber))
                .extract().path("id");

        given()
                .auth().oauth2(adminToken())
                .when().get("/transactions/{id}", transactionId.longValue())
                .then()
                .statusCode(200)
                .body("type", is("TRANSFER"));

        assertBalance(sourceNumber, "70.00");
        assertBalance(destinationNumber, "30.00");
    }

    @Test
    void rejectsTransfersWithInsufficientBalance() {
        long customerId = createCustomer();
        String penAccount = openAccount(customerId, Currency.PEN);
        String anotherPenAccount = openAccount(createCustomer(), Currency.PEN);

        given()
                .auth().oauth2(customerToken())
                .contentType("application/json")
                .body(Map.of("destinationAccountNumber", anotherPenAccount, "amount", new BigDecimal("1.00")))
                .when().post("/accounts/{number}/transfers", penAccount)
                .then()
                .statusCode(422)
                .body("code", is("INSUFFICIENT_BALANCE"));

        assertBalance(penAccount, "0.00");
        assertBalance(anotherPenAccount, "0.00");
    }

    @Test
    void convertsCrossCurrencyTransfersAndStoresTheAppliedRate() {
        long sourceCustomerId = createCustomer();
        String sourceNumber = openAccount(sourceCustomerId, Currency.PEN);
        String destinationNumber = openAccount(createCustomer(), Currency.USD);

        given()
                .auth().oauth2(customerToken())
                .contentType("application/json")
                .body(Map.of("amount", new BigDecimal("500.00")))
                .when().post("/accounts/{number}/deposits", sourceNumber)
                .then().statusCode(201);

        when(exchangeRateClient.getLatestRates("PEN"))
                .thenReturn(new ExchangeRatesResponse("success", "PEN",
                        Map.of("USD", new BigDecimal("0.29000000"))));

        Number transactionId = given()
                .auth().oauth2(customerToken())
                .contentType("application/json")
                .body(Map.of("destinationAccountNumber", destinationNumber, "amount", new BigDecimal("100.00")))
                .when().post("/accounts/{number}/transfers", sourceNumber)
                .then()
                .statusCode(201)
                .body("sourceCurrency", is("PEN"))
                .body("destinationCurrency", is("USD"))
                .extract().path("id");

        given()
                .auth().oauth2(adminToken())
                .when().get("/transactions/{id}", transactionId.longValue())
                .then()
                .statusCode(200)
                .body("exchangeRate", is(0.29f))
                .body("destinationAmount", is(29.0f));

        assertBalance(sourceNumber, "400.00");
        assertBalance(destinationNumber, "29.00");
    }

    @Test
    void leavesBalancesUnchangedWhenExchangeRateProviderFails() {
        String sourceNumber = openAccount(createCustomer(), Currency.PEN);
        String destinationNumber = openAccount(createCustomer(), Currency.USD);

        given()
                .auth().oauth2(customerToken())
                .contentType("application/json")
                .body(Map.of("amount", new BigDecimal("100.00")))
                .when().post("/accounts/{number}/deposits", sourceNumber)
                .then().statusCode(201);

        when(exchangeRateClient.getLatestRates("PEN"))
                .thenThrow(new IllegalStateException("provider unavailable"));

        given()
                .auth().oauth2(customerToken())
                .contentType("application/json")
                .body(Map.of("destinationAccountNumber", destinationNumber, "amount", new BigDecimal("50.00")))
                .when().post("/accounts/{number}/transfers", sourceNumber)
                .then()
                .statusCode(503)
                .body("code", is("EXCHANGE_RATE_UNAVAILABLE"));

        assertBalance(sourceNumber, "100.00");
        assertBalance(destinationNumber, "0.00");
    }

    @Test
    void closesEmptyAccountsAndReturnsUniformNotFoundErrors() {
        String accountNumber = openAccount(createCustomer(), Currency.USD);

        given()
                .auth().oauth2(adminToken())
                .when().delete("/accounts/{number}", accountNumber)
                .then().statusCode(204);

        given()
                .auth().oauth2(adminToken())
                .when().get("/accounts/{number}", accountNumber)
                .then()
                .statusCode(200)
                .body("active", is(false));

        given()
                .auth().oauth2(adminToken())
                .when().get("/accounts/{number}", "MISSING-ACCOUNT")
                .then()
                .statusCode(404)
                .body("code", is("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void loginIssuesRoleSpecificTokensAndProtectsAdminRoutes() {
        given()
                .when().get("/customers")
                .then().statusCode(401);

        String adminToken = adminToken();
        String customerToken = customerToken();

        given()
                .contentType("application/json")
                .body(Map.of("username", "admin", "password", "wrong-password"))
                .when().post("/auth/login")
                .then()
                .statusCode(401)
                .body("code", is("INVALID_CREDENTIALS"));

        given()
                .auth().oauth2(customerToken)
                .when().get("/customers")
                .then().statusCode(403);

        given()
                .auth().oauth2(adminToken)
                .when().get("/customers")
                .then().statusCode(200);

        given()
                .auth().oauth2(customerToken)
                .when().get("/transactions")
                .then().statusCode(403);

        given()
                .auth().oauth2(adminToken)
                .when().get("/transactions")
                .then().statusCode(200);
    }

    private long createCustomer() {
        String unique = UUID.randomUUID().toString();
        return ((Number) given()
                .auth().oauth2(adminToken())
                .contentType("application/json")
                .body(customerRequest(unique, unique.replace("-", "").substring(0, 16)))
                .when().post("/customers")
                .then()
                .statusCode(201)
                .extract().path("id")).longValue();
    }

    private String openAccount(long customerId, Currency currency) {
        String number = "TEST-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        given()
                .auth().oauth2(adminToken())
                .contentType("application/json")
                .body(Map.of("number", number, "customerId", customerId, "currency", currency.name()))
                .when().post("/accounts")
                .then().statusCode(201);
        return number;
    }

    private Map<String, String> customerRequest(String unique, String document) {
        return Map.of(
                "name", "Test Customer " + unique,
                "document", document,
                "email", "customer-" + unique + "@example.test");
    }

    private void assertBalance(String accountNumber, String expected) {
        String balance = given()
                .auth().oauth2(adminToken())
                .when().get("/accounts/{number}", accountNumber)
                .then().statusCode(200)
                .extract().path("balance").toString();
        assertEquals(0, new BigDecimal(balance).compareTo(new BigDecimal(expected)));
    }

    private String adminToken() {
        return token("admin", "admin123");
    }

    private String customerToken() {
        return token("customer", "customer123");
    }

    private String token(String username, String password) {
        return given()
                .contentType("application/json")
                .body(Map.of("username", username, "password", password))
                .when().post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");
    }
}
