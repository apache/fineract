package org.mifosng.platform.rest;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.OrganisationList;
import org.mifosng.data.OrganisationReadModel;
import org.mifosng.platform.ReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/protected/org")
@Component
@Scope("singleton")
public class TenantResource {

    @Autowired
	private ReadPlatformService readPlatformService;

    @GET
	@Path("view")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response retrieveExistingOrganisations() {

		Collection<OrganisationReadModel> organisations = this.readPlatformService
				.retrieveAll();
		return Response.ok().entity(new OrganisationList(organisations))
				.build();
    }
}