package org.mifosng.platform.api;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.DocumentCommand;
import org.mifosng.platform.api.data.DocumentData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.documentmanagement.service.DocumentReadPlatformService;
import org.mifosng.platform.documentmanagement.service.DocumentWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("{entityType}/{entityId}/documents")
@Component
@Scope("singleton")
public class DocumentManagementApiResource {

	@Autowired
	private DocumentReadPlatformService documentReadPlatformService;

	@Autowired
	private DocumentWritePlatformService documentWritePlatformService;

	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

	private static final Set<String> typicalResponseParameters = new HashSet<String>(
			Arrays.asList("id", "parentEntityType", "parentEntityId", "name",
					"fileName", "type", "size", "description"));

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retreiveAllDocuments(@Context final UriInfo uriInfo,
			@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId) {

		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());

		Collection<DocumentData> documentDatas = this.documentReadPlatformService
				.retrieveAllDocuments(entityType, entityId);

		return this.apiJsonSerializerService.serializeDocumentDataToJson(
				prettyPrint, responseParameters, documentDatas);
	}

	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createDocument(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetails,
			@FormDataParam("file") FormDataBodyPart bodyPart,
			@HeaderParam("Content-Length") Long fileSize) {

		// TODO: Using Content-Length gives me size of the entire request, need
		// to filter out only the size of the file

		// TODO: Need to add a Fast fail for checking size of file depending on
		// the size sent by client, also need to have a backup and stop reading
		// from stream after size is reached to protect against malicious
		// clients
		DocumentCommand documentCommand = new DocumentCommand(null, null,
				entityType, entityId, "test", fileDetails.getFileName(),
				fileSize, bodyPart.getMediaType().toString(), "description",
				null);

		bodyPart.getMediaType();

		Long documentId = this.documentWritePlatformService.createDocument(
				documentCommand, inputStream);

		return Response.ok().entity(new EntityIdentifier(documentId)).build();
	}

	@GET
	@Path("{documentId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDocument(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@PathParam("documentId") Long documentId,
			@Context final UriInfo uriInfo) {

		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}

		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());

		DocumentData documentData = this.documentReadPlatformService
				.retrieveDocument(entityType, entityId, documentId);

		return this.apiJsonSerializerService.serializeDocumentDataToJson(
				prettyPrint, responseParameters, documentData);
	}

	@GET
	@Path("{documentId}/attachment")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response downloadFile(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@PathParam("documentId") Long documentId) {

		DocumentData documentData = this.documentReadPlatformService
				.retrieveDocument(entityType, entityId, documentId);
		File file = new File(documentData.getLocation());
		ResponseBuilder response = Response.ok(file);
		response.header("Content-Disposition", "attachment; filename=\""
				+ documentData.getFileName() + "\"");
		response.header("Content-Type", documentData.getType());
		return response.build();
	}

	@PUT
	@Path("{documentId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateDocument(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@PathParam("documentId") Long documentId) {

		// TODO
		// EntityIdentifier identifier =
		// this.documentWritePlatformService.updateDocument(documentCommand,
		// inputStream, fileDetails)

		return Response.ok().entity(null).build();
	}

	@DELETE
	@Path("{documentId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteDocument(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@PathParam("documentId") Long documentId) {
		DocumentCommand documentCommand = new DocumentCommand(null, documentId,
				entityType, entityId, null, null, null, null, null, null);

		EntityIdentifier documentIdentifier = this.documentWritePlatformService
				.deleteDocument(documentCommand);

		return Response.ok(documentIdentifier).build();
	}

}
