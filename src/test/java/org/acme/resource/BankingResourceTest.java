package org.acme.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.entity.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class BankingResourceTest {

    @Test
    void validatesCustomerRequestsAndRejectsDuplicateDocuments() {
        given()
                .contentType("application/json")
                .body(Map.of("name", "", "document", "12345678", "email", "invalid-email"))
                .when().post("/customers")
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_ERROR"));

        String unique = UUID.randomUUID().toString();
        Map<String, String> request = customerRequest(unique, "12345678");
        given().contentType("application/json").body(request)
                .when().post("/customers")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(customerRequest(unique, "12345678"))
                .when().post("/customers")
                .then()
                .statusCode(409)
                .body("code", is("CUSTOMER_DOCUMENT_ALREADY_EXISTS"));
    }

    @Test
    void depositsAndTransfersWithinTheSameCurrency() {
        long sourceCustomerId = createCustomer();
        long destinationCustomerId = createCustomer();
        String sourceNumber = openAccount(sourceCustomerId, Currency.PEN);
        String destinationNumber = openAccount(destinationCustomerId, Currency.PEN);

        given()
                .contentType("application/json")
                .body(Map.of("amount", new BigDecimal("100.00"), "description", "Initial deposit"))
                .when().post("/accounts/{number}/deposits", sourceNumber)
                .then()
                .statusCode(201)
                .body("type", is("DEPOSIT"))
                .body("destinationAccountNumber", is(sourceNumber));

        Number transactionId = given()
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
                .when().get("/transactions/{id}", transactionId.longValue())
                .then()
                .statusCode(200)
                .body("type", is("TRANSFER"));

        assertBalance(sourceNumber, "70.00");
        assertBalance(destinationNumber, "30.00");
    }

    @Test
    void rejectsInsufficientBalanceAndCrossCurrencyTransfers() {
        long customerId = createCustomer();
        String penAccount = openAccount(customerId, Currency.PEN);
        String anotherPenAccount = openAccount(createCustomer(), Currency.PEN);
        String usdAccount = openAccount(createCustomer(), Currency.USD);

        given()
                .contentType("application/json")
                .body(Map.of("destinationAccountNumber", anotherPenAccount, "amount", new BigDecimal("1.00")))
                .when().post("/accounts/{number}/transfers", penAccount)
                .then()
                .statusCode(422)
                .body("code", is("INSUFFICIENT_BALANCE"));

        given()
                .contentType("application/json")
                .body(Map.of("destinationAccountNumber", usdAccount, "amount", new BigDecimal("1.00")))
                .when().post("/accounts/{number}/transfers", penAccount)
                .then()
                .statusCode(422)
                .body("code", is("CURRENCY_CONVERSION_NOT_AVAILABLE"));

        assertBalance(penAccount, "0.00");
        assertBalance(anotherPenAccount, "0.00");
    }

    @Test
    void closesEmptyAccountsAndReturnsUniformNotFoundErrors() {
        String accountNumber = openAccount(createCustomer(), Currency.USD);

        given()
                .when().delete("/accounts/{number}", accountNumber)
                .then().statusCode(204);

        given()
                .when().get("/accounts/{number}", accountNumber)
                .then()
                .statusCode(200)
                .body("active", is(false));

        given()
                .when().get("/accounts/{number}", "MISSING-ACCOUNT")
                .then()
                .statusCode(404)
                .body("code", is("ACCOUNT_NOT_FOUND"));
    }

    private long createCustomer() {
        String unique = UUID.randomUUID().toString();
        return ((Number) given()
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
                .when().get("/accounts/{number}", accountNumber)
                .then().statusCode(200)
                .extract().path("balance").toString();
        assertEquals(0, new BigDecimal(balance).compareTo(new BigDecimal(expected)));
    }
}
