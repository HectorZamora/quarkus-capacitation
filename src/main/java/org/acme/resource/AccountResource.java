package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.acme.dto.request.DepositRequest;
import org.acme.dto.request.OpenAccountRequest;
import org.acme.dto.request.TransferRequest;
import org.acme.dto.response.AccountResponse;
import org.acme.dto.response.TransactionResponse;
import org.acme.service.AccountService;
import org.acme.service.TransactionService;

@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    @Inject
    TransactionService transactionService;

    @Context
    UriInfo uriInfo;

    @POST
    public Response open(@Valid OpenAccountRequest request) {
        AccountResponse account = accountService.open(request);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(account.number()).build())
                .entity(account)
                .build();
    }

    @GET
    @Path("/{number}")
    public AccountResponse findByNumber(@PathParam("number") String number) {
        return accountService.findByNumber(number);
    }

    @DELETE
    @Path("/{number}")
    public Response close(@PathParam("number") String number) {
        accountService.close(number);
        return Response.noContent().build();
    }

    @POST
    @Path("/{number}/deposits")
    public Response deposit(@PathParam("number") String number, @Valid DepositRequest request) {
        TransactionResponse transaction = transactionService.deposit(number, request);
        return transactionCreated(transaction);
    }

    @POST
    @Path("/{number}/transfers")
    public Response transfer(@PathParam("number") String number, @Valid TransferRequest request) {
        TransactionResponse transaction = transactionService.transfer(number, request);
        return transactionCreated(transaction);
    }

    private Response transactionCreated(TransactionResponse transaction) {
        return Response.created(uriInfo.getBaseUriBuilder()
                        .path(TransactionResource.class)
                        .path(transaction.id().toString())
                        .build())
                .entity(transaction)
                .build();
    }
}
