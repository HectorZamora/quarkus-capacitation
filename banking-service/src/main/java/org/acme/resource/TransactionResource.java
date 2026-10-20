package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.TransactionResponse;
import org.acme.security.SecurityRoles;
import org.acme.service.TransactionService;

import java.util.List;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.ADMIN)
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @GET
    public List<TransactionResponse> listAll() {
        return transactionService.listAll();
    }

    @GET
    @Path("/{id}")
    public TransactionResponse findById(@PathParam("id") Long id) {
        return transactionService.findById(id);
    }
}
