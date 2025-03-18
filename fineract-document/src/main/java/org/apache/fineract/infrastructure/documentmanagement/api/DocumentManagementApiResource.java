/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.documentmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/{entityType}/{entityId}/documents")
@Tag(name = "Documents", description = "Multiple Documents (a combination of a name, description and a file) may be attached to different Entities like Clients, Groups, Staff, Loans, Savings and Client Identifiers in the system\n"
        + "\n" + "Note: The currently allowed Entities are\n" + "\n" + "Clients: URL Pattern as clients\n" + "Staff: URL Pattern as staff\n"
        + "Loans: URL Pattern as loans\n" + "Savings: URL Pattern as savings\n" + "Client Identifiers: URL Pattern as client_identifiers\n"
        + "Groups: URL Pattern as groups")
@RequiredArgsConstructor
public class DocumentManagementApiResource {

    private static final String SYSTEM_ENTITY_TYPE = "DOCUMENT";

    private final PlatformSecurityContext context;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final DocumentWritePlatformService documentWritePlatformService;
    private final FileUploadValidator fileUploadValidator;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List documents", description = "Example Requests:\n" + "\n" + "clients/1/documents\n" + "\n"
            + "client_identifiers/1/documents\n" + "\n" + "loans/1/documents?fields=name,description")
    public List<DocumentData> retrieveAllDocuments(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId) {

        context.authenticatedUser().validateHasReadPermission(SYSTEM_ENTITY_TYPE);
        return documentReadPlatformService.retrieveAllDocuments(entityType, entityId);
    }

    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(description = "Create document", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = DocumentManagementApiResourceSwagger.DocumentUploadRequest.class)) })
    @Operation(summary = "Create a Document", description = "Note: A document is created using a Multi-part form upload \n" + "\n"
            + "Body Parts\n" + "\n" + "name : \n" + "Name or summary of the document\n" + "\n" + "description : \n"
            + "Description of the document\n" + "\n" + "file : \n" + "The file to be uploaded\n" + "\n" + "Mandatory Fields : \n"
            + "file and description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Not Shown (multi-part form data)", content = @Content(schema = @Schema(implementation = DocumentManagementApiResourceSwagger.PostEntityTypeEntityIdDocumentsResponse.class))) })
    public CommandProcessingResult createDocument(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @HeaderParam("Content-Length") @Parameter(description = "Content-Length") final Long fileSize,
            @FormDataParam("file") final InputStream inputStream, @FormDataParam("file") final FormDataContentDisposition fileDetails,
            @FormDataParam("file") final FormDataBodyPart bodyPart, @FormDataParam("name") final String name,
            @FormDataParam("description") final String description) {

        // TODO: stop reading from stream after max size is reached to protect against malicious clients
        // TODO: need to extract the actual file type and determine if they are permissible

        fileUploadValidator.validate(fileSize, inputStream, fileDetails, bodyPart);
        final DocumentCommand documentCommand = new DocumentCommand(null, null, entityType, entityId, name, fileDetails.getFileName(),
                fileSize, bodyPart.getMediaType().toString(), description, null);
        final Long documentId = documentWritePlatformService.createDocument(documentCommand, inputStream);
        return CommandProcessingResult.resourceResult(documentId);
    }

    @PUT
    @Path("{documentId}")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(description = "Update document", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = DocumentManagementApiResourceSwagger.DocumentUploadRequest.class)) })
    @Operation(summary = "Update a Document", description = "Note: A document is updated using a Multi-part form upload \n" + "Body Parts\n"
            + "name\n" + "Name or summary of the document\n" + "description\n" + "Description of the document\n" + "file\n"
            + "The file to be uploaded")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Not Shown (multi-part form data)", content = @Content(schema = @Schema(implementation = DocumentManagementApiResourceSwagger.PutEntityTypeEntityIdDocumentsResponse.class))) })
    public CommandProcessingResult updateDocument(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @PathParam("documentId") @Parameter(description = "documentId") final Long documentId,
            @HeaderParam("Content-Length") @Parameter(description = "Content-Length") final Long fileSize,
            @FormDataParam("file") final InputStream inputStream, @FormDataParam("file") final FormDataContentDisposition fileDetails,
            @FormDataParam("file") final FormDataBodyPart bodyPart, @FormDataParam("name") final String name,
            @FormDataParam("description") final String description) {

        final Set<String> modifiedParams = new HashSet<>();
        modifiedParams.add("name");
        modifiedParams.add("description");

        /***
         * Populate Document command based on whether a file has also been passed in as a part of the update
         ***/
        DocumentCommand documentCommand;
        if (inputStream != null && fileDetails.getFileName() != null) {
            fileUploadValidator.validate(fileSize, inputStream, fileDetails, bodyPart);
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
        /***
         * TODO: does not return list of changes, should be done for consistency with rest of API
         **/
        return documentWritePlatformService.updateDocument(documentCommand, inputStream);
    }

    @GET
    @Path("{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Document", description = "Example Requests:\n" + "\n" + "clients/1/documents/1\n" + "\n" + "\n"
            + "loans/1/documents/1\n" + "\n" + "\n" + "client_identifiers/1/documents/1?fields=name,description")
    public DocumentData getDocument(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @PathParam("documentId") @Parameter(description = "documentId") final Long documentId) {

        context.authenticatedUser().validateHasReadPermission(SYSTEM_ENTITY_TYPE);
        return documentReadPlatformService.retrieveDocument(entityType, entityId, documentId);
    }

    @GET
    @Path("{documentId}/attachment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary = "Retrieve Binary File associated with Document", description = "Request used to download the file associated with the document\n"
            + "\n" + "Example Requests:\n" + "\n" + "clients/1/documents/1/attachment\n" + "\n" + "\n" + "loans/1/documents/1/attachment")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Not Shown: The corresponding Binary file") })
    public Response downloadFile(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @PathParam("documentId") @Parameter(description = "documentId") final Long documentId) {

        context.authenticatedUser().validateHasReadPermission(SYSTEM_ENTITY_TYPE);
        final FileData fileData = documentReadPlatformService.retrieveFileData(entityType, entityId, documentId);
        return ContentResources.fileDataToResponse(fileData, "attachment");
    }

    @DELETE
    @Path("{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Remove a Document", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = DocumentManagementApiResourceSwagger.DeleteEntityTypeEntityIdDocumentsResponse.class))) })
    public CommandProcessingResult deleteDocument(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId,
            @PathParam("documentId") @Parameter(description = "documentId") final Long documentId) {

        final DocumentCommand documentCommand = new DocumentCommand(null, documentId, entityType, entityId, null, null, null, null, null,
                null);

        return documentWritePlatformService.deleteDocument(documentCommand);
    }
}
