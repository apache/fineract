package org.mifosng.platform.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.platform.WritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/protected/note")
@Component
@Scope("singleton")
public class NoteResource {

	@Autowired
	private WritePlatformService writePlatformService;
	
	@POST
	@Path("{noteId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateClientAccount(@PathParam("noteId") final Long noteId, final NoteCommand command) {
		
		EntityIdentifier identifier = this.writePlatformService.updateNote(command);

		return Response.ok().entity(identifier).build();
	}
}