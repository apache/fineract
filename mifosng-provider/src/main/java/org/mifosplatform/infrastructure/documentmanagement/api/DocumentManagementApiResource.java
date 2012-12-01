package org.mifosplatform.infrastructure.documentmanagement.api;

import java.io.File;
import java.io.InputStream;
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

import org.mifosplatform.infrastructure.core.api.ApiConstants;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.mifosplatform.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
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

    private final String SystemEntityType = "DOCUMENT";
    
    private final PlatformSecurityContext context;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final DocumentWritePlatformService documentWritePlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;

    @Autowired
    public DocumentManagementApiResource(final PlatformSecurityContext context,
            final DocumentReadPlatformService documentReadPlatformService, final DocumentWritePlatformService documentWritePlatformService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService) {
        this.context = context;
        this.documentReadPlatformService = documentReadPlatformService;
        this.documentWritePlatformService = documentWritePlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveAllDocuments(@Context final UriInfo uriInfo, @PathParam("entityType") String entityType,
            @PathParam("entityId") Long entityId) {

        context.authenticatedUser().validateHasReadPermission(SystemEntityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<DocumentData> documentDatas = this.documentReadPlatformService.retrieveAllDocuments(entityType, entityId);

        return this.apiJsonSerializerService.serializeDocumentDataToJson(prettyPrint, responseParameters, documentDatas);
    }

    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response createDocument(@PathParam("entityType") String entityType, @PathParam("entityId") Long entityId,
            @HeaderParam("Content-Length") Long fileSize, @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetails, @FormDataParam("file") FormDataBodyPart bodyPart,
            @FormDataParam("name") String name, @FormDataParam("description") String description) {

        FileUtils.validateFileSizeWithinPermissibleRange(fileSize, name, ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);

        /**
         * TODO: also need to have a backup and stop reading from stream after
         * max size is reached to protect against malicious clients
         **/

        /**
         * TODO: need to extract the actual file type and determine if they are
         * permissable
         **/
        DocumentCommand documentCommand = new DocumentCommand(null, null, entityType, entityId, name, fileDetails.getFileName(), fileSize,
                bodyPart.getMediaType().toString(), description, null);

        Long documentId = this.documentWritePlatformService.createDocument(documentCommand, inputStream);

        return Response.ok().entity(new EntityIdentifier(documentId)).build();
    }

    @PUT
    @Path("{documentId}")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateDocument(@PathParam("entityType") String entityType, @PathParam("entityId") Long entityId,
            @PathParam("documentId") Long documentId, @HeaderParam("Content-Length") Long fileSize,
            @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") FormDataBodyPart bodyPart, @FormDataParam("name") String name,
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
            documentCommand = new DocumentCommand(modifiedParams, documentId, entityType, entityId, name, fileDetails.getFileName(),
                    fileSize, bodyPart.getMediaType().toString(), description, null);
        } else {
            documentCommand = new DocumentCommand(modifiedParams, documentId, entityType, entityId, name, null, null, null, description,
                    null);
        }
        EntityIdentifier identifier = this.documentWritePlatformService.updateDocument(documentCommand, inputStream);

        return Response.ok().entity(identifier).build();
    }

    @GET
    @Path("{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getDocument(@PathParam("entityType") String entityType, @PathParam("entityId") Long entityId,
            @PathParam("documentId") Long documentId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(SystemEntityType);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final DocumentData documentData = this.documentReadPlatformService.retrieveDocument(entityType, entityId, documentId);

        return this.apiJsonSerializerService.serializeDocumentDataToJson(prettyPrint, responseParameters, documentData);
    }

    @GET
    @Path("{documentId}/attachment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response downloadFile(@PathParam("entityType") String entityType, @PathParam("entityId") Long entityId,
            @PathParam("documentId") Long documentId) {

        context.authenticatedUser().validateHasReadPermission(SystemEntityType);

        DocumentData documentData = this.documentReadPlatformService.retrieveDocument(entityType, entityId, documentId);
        File file = new File(documentData.fileLocation());
        ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"" + documentData.fileName() + "\"");
        response.header("Content-Type", documentData.contentType());
        return response.build();
    }

    @DELETE
    @Path("{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteDocument(@PathParam("entityType") String entityType, @PathParam("entityId") Long entityId,
            @PathParam("documentId") Long documentId) {

        DocumentCommand documentCommand = new DocumentCommand(null, documentId, entityType, entityId, null, null, null, null, null, null);

        EntityIdentifier documentIdentifier = this.documentWritePlatformService.deleteDocument(documentCommand);

        return Response.ok(documentIdentifier).build();
    }
}