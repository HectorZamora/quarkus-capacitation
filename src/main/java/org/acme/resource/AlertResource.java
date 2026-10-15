package org.acme.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.response.AlertResponse;
import org.acme.security.SecurityRoles;
import org.acme.service.AlertService;

import java.util.List;

@Path("/alerts")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SecurityRoles.ADMIN)
public class AlertResource {

    @Inject
    AlertService alertService;

    @GET
    public List<AlertResponse> listAll() {
        return alertService.listAll();
    }
}
