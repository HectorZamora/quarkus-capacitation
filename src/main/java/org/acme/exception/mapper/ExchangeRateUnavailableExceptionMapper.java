package org.acme.exception.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.dto.response.ErrorResponse;
import org.acme.exception.ExchangeRateUnavailableException;

import java.time.Instant;

@Provider
public class ExchangeRateUnavailableExceptionMapper implements ExceptionMapper<ExchangeRateUnavailableException> {

    @Override
    public Response toResponse(ExchangeRateUnavailableException exception) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse("EXCHANGE_RATE_UNAVAILABLE", exception.getMessage(), Instant.now()))
                .build();
    }
}
