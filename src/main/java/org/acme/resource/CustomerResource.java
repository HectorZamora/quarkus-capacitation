package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.acme.dto.request.CreateCustomerRequest;
import org.acme.dto.response.CustomerResponse;
import org.acme.service.CustomerService;

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @Context
    UriInfo uriInfo;

    @POST
    public Response create(@Valid CreateCustomerRequest request) {
        CustomerResponse customer = customerService.create(request);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(customer.id().toString()).build())
                .entity(customer)
                .build();
    }

    @GET
    @Path("/{id}")
    public CustomerResponse findById(@PathParam("id") Long id) {
        return customerService.findById(id);
    }
}
