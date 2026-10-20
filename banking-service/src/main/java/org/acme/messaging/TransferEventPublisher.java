package org.acme.messaging;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
public class TransferEventPublisher {

    @Inject
    @Channel("transfers-out")
    Emitter<TransferCompletedEvent> emitter;

    void publishAfterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) TransferCompletedEvent event) {
        // La cuenta origen como clave garantiza que sus transferencias caigan en la misma partición
        // y el consumidor antifraude las procese en orden.
        Message<TransferCompletedEvent> message = Message.of(event)
                .addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
                        .withKey(event.sourceAccountNumber())
                        .build());
        emitter.send(message);
    }
}
