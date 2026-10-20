package org.acme.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.inject.Inject;
import org.acme.entity.Currency;
import org.acme.messaging.TransferCompletedEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AlertResourceTest {

    private static final AtomicLong TRANSACTION_SEQUENCE = new AtomicLong(System.nanoTime());

    @Inject
    @Channel("transfers-test-out")
    Emitter<TransferCompletedEvent> emitter;

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void createsAnAlertForAHighValueTransfer() throws InterruptedException {
        long transactionId = TRANSACTION_SEQUENCE.incrementAndGet();
        publish(transactionId, "PEN-HIGH-VALUE", new BigDecimal("20000.00"),
                new BigDecimal("6000.00"), LocalDateTime.now());

        String reason = awaitAlertReason(transactionId);
        assertTrue(reason.contains("Transfer exceeds USD 5000"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADMIN")
    void createsAnAlertForFiveTransfersWithinFiveMinutes() throws InterruptedException {
        long lastTransactionId = 0;
        LocalDateTime now = LocalDateTime.now();
        for (int index = 0; index < 5; index++) {
            lastTransactionId = TRANSACTION_SEQUENCE.incrementAndGet();
            publish(lastTransactionId, "PEN-BURST", BigDecimal.ONE, BigDecimal.ONE,
                    now.plusSeconds(index));
        }

        String reason = awaitAlertReason(lastTransactionId);
        assertTrue(reason.contains("Five or more transfers from the same account within five minutes"));
    }

    @Test
    void rejectsRequestsWithoutAToken() {
        given()
                .when().get("/alerts")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "customer", roles = "CUSTOMER")
    void rejectsCustomers() {
        given()
                .when().get("/alerts")
                .then().statusCode(403);
    }

    private void publish(long transactionId, String sourceAccountNumber,
                         BigDecimal amount, BigDecimal amountInUsd, LocalDateTime occurredAt) {
        TransferCompletedEvent event = new TransferCompletedEvent(
                transactionId,
                sourceAccountNumber,
                "PEN-DESTINATION",
                amount,
                amount,
                Currency.PEN,
                Currency.PEN,
                amountInUsd,
                occurredAt
        );
        emitter.send(Message.of(event)
                .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                        .withKey(sourceAccountNumber)
                        .build()));
    }

    private String awaitAlertReason(long transactionId) throws InterruptedException {
        long deadline = System.nanoTime() + 20_000_000_000L;
        while (System.nanoTime() < deadline) {
            List<Map<String, Object>> alerts = given()
                    .when().get("/alerts")
                    .then().statusCode(200)
                    .extract().jsonPath().getList("");
            for (Map<String, Object> alert : alerts) {
                if (((Number) alert.get("transactionId")).longValue() == transactionId) {
                    return alert.get("reason").toString();
                }
            }
            Thread.sleep(200);
        }
        throw new AssertionError("No antifraud alert was created for transaction " + transactionId);
    }
}
