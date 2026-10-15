package org.acme.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.ExchangeRatesResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v6/latest")
@RegisterRestClient(configKey = "exchange-rate")
@Produces(MediaType.APPLICATION_JSON)
public interface ExchangeRateClient {

    @GET
    @Path("/{baseCurrency}")
    ExchangeRatesResponse getLatestRates(@PathParam("baseCurrency") String baseCurrency);
}
