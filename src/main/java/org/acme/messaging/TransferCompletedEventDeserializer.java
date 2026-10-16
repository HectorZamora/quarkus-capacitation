package org.acme.messaging;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class TransferCompletedEventDeserializer extends ObjectMapperDeserializer<TransferCompletedEvent> {

    public TransferCompletedEventDeserializer() {
        super(TransferCompletedEvent.class);
    }
}
