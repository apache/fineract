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
import org.mifosng.platform.api.infrastructure.ApiConstants;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.documentmanagement.service.DocumentReadPlatformService;
import org.mifosng.platform.documentmanagement.service.DocumentWritePlatformService;
import org.mifosng.platform.infrastructure.FileUtils;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.security.PlatformSecurityContext;
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
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

	private static final Set<String> typicalResponseParameters = new HashSet<String>(
			Arrays.asList("id", "parentEntityType", "parentEntityId", "name",
					"fileName", "type", "size", "description"));

	private final String SystemEntityType = "DOCUMENT";
    @Autowired
    private PlatformSecurityContext context;
	
	/**
	 * @param uriInfo
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retreiveAllDocuments(@Context final UriInfo uriInfo,
			@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId) {

    	context.authenticatedUser().validateHasReadPermission(SystemEntityType);
    	
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

	/**
	 * @param entityType
	 * @param entityId
	 * @param fileSize
	 * @param inputStream
	 * @param fileDetails
	 * @param bodyPart
	 * @param name
	 * @param description
	 * @return
	 */
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createDocument(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@HeaderParam("Content-Length") Long fileSize,
			@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetails,
			@FormDataParam("file") FormDataBodyPart bodyPart,
			@FormDataParam("name") String name,
			@FormDataParam("description") String description) {

		FileUtils.validateFileSizeWithinPermissibleRange(fileSize, name, ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);

		/**
		 * TODO: also need to have a backup and stop reading from stream after
		 * max size is reached to protect against malicious clients
		 **/

		/**
		 * TODO: need to extract the actual file type and determine if they are
		 * permissable
		 **/
		DocumentCommand documentCommand = new DocumentCommand(null, null,
				entityType, entityId, name, fileDetails.getFileName(),
				fileSize, bodyPart.getMediaType().toString(), description, null);

		Long documentId = this.documentWritePlatformService.createDocument(
				documentCommand, inputStream);

		return Response.ok().entity(new EntityIdentifier(documentId)).build();
	}

	/**
	 * @param entityType
	 * @param entityId
	 * @param documentId
	 * @param fileSize
	 * @param inputStream
	 * @param fileDetails
	 * @param bodyPart
	 * @param name
	 * @param description
	 * @return
	 */
	@PUT
	@Path("{documentId}")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateDocument(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@PathParam("documentId") Long documentId,
			@HeaderParam("Content-Length") Long fileSize,
			@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetails,
			@FormDataParam("file") FormDataBodyPart bodyPart,
			@FormDataParam("name") String name,
			@FormDataParam("description") String description) {

		FileUtils.validateFileSizeWithinPermissibleRange(fileSize, name, ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);

		Set<String> modifiedParams = new HashSet<String>();
		modifiedParams.add("name");
		modifiedParams.add("description");

		/***
		 * Populate Document command based on whether a file has also been
		 * passed in as a part of the update
		 ***/
		DocumentCommand documentCommand = null;
		if (inputStream != null && fileDetails.getFileName() != null) {
			modifiedParams.add("fileName");
			modifiedParams.add("size");
			modifiedParams.add("type");
			modifiedParams.add("location");
			documentCommand = new DocumentCommand(modifiedParams, documentId,
					entityType, entityId, name, fileDetails.getFileName(),
					fileSize, bodyPart.getMediaType().toString(), description,
					null);
		} else {
			documentCommand = new DocumentCommand(modifiedParams, documentId,
					entityType, entityId, name, null, null, null, description,
					null);
		}
		EntityIdentifier identifier = this.documentWritePlatformService
				.updateDocument(documentCommand, inputStream);

		return Response.ok().entity(identifier).build();
	}


	/**
	 * @param entityType
	 * @param entityId
	 * @param documentId
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Path("{documentId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getDocument(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@PathParam("documentId") Long documentId,
			@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(SystemEntityType);
    	
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

	/**
	 * @param entityType
	 * @param entityId
	 * @param documentId
	 * @return
	 */
	@GET
	@Path("{documentId}/attachment")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response downloadFile(@PathParam("entityType") String entityType,
			@PathParam("entityId") Long entityId,
			@PathParam("documentId") Long documentId) {

    	context.authenticatedUser().validateHasReadPermission(SystemEntityType);
    	
		DocumentData documentData = this.documentReadPlatformService
				.retrieveDocument(entityType, entityId, documentId);
		File file = new File(documentData.getLocation());
		ResponseBuilder response = Response.ok(file);
		response.header("Content-Disposition", "attachment; filename=\""
				+ documentData.getFileName() + "\"");
		response.header("Content-Type", documentData.getType());
		return response.build();
	}

	/**
	 * @param entityType
	 * @param entityId
	 * @param documentId
	 * @return
	 */
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
