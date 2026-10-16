package org.acme.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class TransferEventPublisher {

    @Inject
    @Channel("transfers-out")
    Emitter<TransferCompletedEvent> emitter;

    void publishAfterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) TransferCompletedEvent event) {
        emitter.send(event);
    }
}
