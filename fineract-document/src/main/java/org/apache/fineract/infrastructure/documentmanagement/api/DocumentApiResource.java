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

import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_DESCRIPTION;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_DOCUMENT_ID;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_ENTITY_ID;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_ENTITY_TYPE;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_FILE;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_NAME;
import static org.apache.fineract.util.StreamResponseUtil.DISPOSITION_TYPE_ATTACHMENT;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorContext;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCreateCommand;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentDeleteCommand;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentUpdateCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentDeleteRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentDeleteResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentUpdateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentUpdateResponse;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.util.StreamResponseUtil;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Path("/v1/{entityType}/{entityId}/documents")
@Tag(name = "Documents", description = """
        Multiple Documents (a combination of a name, description and a file) may be attached to different entities like clients, groups, staff, loans, savings and client identifiers in the system

        Note: the currently allowed entities are

        - clients: URL pattern as clients
        - staff: URL pattern as staff
        - loans: URL pattern as loans
        - savings: URL pattern as savings
        - client identifiers: URL pattern as client_identifiers
        - groups: URL pattern as groups
        """)
@RequiredArgsConstructor
public class DocumentApiResource {

    private final DocumentReadPlatformService documentReadPlatformService;
    private final FileUploadValidator fileUploadValidator;
    private final ContentDetectorManager contentDetectorManager;
    private final CommandPipeline commandPipeline;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List documents", description = """
            Example Requests:

            - clients/1/documents
            - client_identifiers/1/documents
            - loans/1/documents?fields=name,description
            """)
    public List<DocumentData> retrieveAllDocuments(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId) {

        return documentReadPlatformService.retrieveAllDocuments(entityType, entityId);
    }

    @GET
    @Path("{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Document", description = """
            Example Requests:

            - clients/1/documents/1
            - loans/1/documents/1
            - client_identifiers/1/documents/1?fields=name,description
            """)
    public DocumentData getDocument(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId,
            @PathParam(DOCUMENT_API_PARAM_DOCUMENT_ID) final Long documentId) {

        return documentReadPlatformService.retrieveDocument(entityType, entityId, documentId);
    }

    @GET
    @Path("{documentId}/attachment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @Operation(summary = "Retrieve Binary File associated with Document", description = """
            Request used to download the file associated with the document

            Example Requests:

            - clients/1/documents/1/attachment
            - loans/1/documents/1/inline
            """)
    @ApiResponse(responseCode = "200", description = "Not Shown: The corresponding Binary file")
    public Response downloadFile(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId,
            @PathParam(DOCUMENT_API_PARAM_DOCUMENT_ID) final Long documentId) {

        final var content = documentReadPlatformService.retrieveDocumentContent(entityType, entityId, documentId);

        return StreamResponseUtil
                .ok(StreamResponseUtil.StreamResponseData.builder().type(content.getContentType()).fileName(content.getFileName())
                        .dispositionType(DISPOSITION_TYPE_ATTACHMENT).stream(content.getStream()).size(content.getSize()).build());
    }

    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Document", description = """
            Note: A document is created using a Multi-part form upload

            Body parts

            - name : name or summary of the document
            - description : description of the document
            - file : the file to be uploaded

            Mandatory fields :

            - file
            - description
            """)
    @ApiResponse(responseCode = "200", description = "Not Shown (multi-part form data)")
    public DocumentCreateResponse createDocument(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId, @HeaderParam(CONTENT_LENGTH) final Long fileSize,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final InputStream is,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataContentDisposition fileDetails,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataBodyPart filePart,
            @FormDataParam(DOCUMENT_API_PARAM_NAME) final String name,
            @FormDataParam(DOCUMENT_API_PARAM_DESCRIPTION) final String description) {

        fileUploadValidator.validate(fileSize, is, fileDetails, filePart);

        final var command = new DocumentCreateCommand();

        var type = Optional.ofNullable(filePart.getMediaType()).map(MediaType::toString)
                .or(() -> Optional.of(contentDetectorManager
                        .detect(ContentDetectorContext.builder().fileName(fileDetails.getFileName()).build()).getMimeType()))
                .orElse(APPLICATION_OCTET_STREAM_VALUE);

        command.setPayload(DocumentCreateRequest.builder().entityId(entityId).entityType(entityType).name(name).description(description)
                .fileName(fileDetails.getFileName()).size(fileSize).type(type).stream(is).build());

        final Supplier<DocumentCreateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @PUT
    @Path("{documentId}")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    // @RequestBody(description = "Update document", content = { @Content(mediaType = MediaType.MULTIPART_FORM_DATA) })
    @Operation(summary = "Update a Document", description = """
            Note: A document is updated using a Multi-part form upload

            Body Parts

            - name: name or summary of the document
            - description: description of the document
            - file: the file to be uploaded
            """)
    @ApiResponse(responseCode = "200", description = "Not Shown (multi-part form data)")
    public DocumentUpdateResponse updateDocument(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId, @PathParam(DOCUMENT_API_PARAM_DOCUMENT_ID) final Long documentId,
            @HeaderParam(CONTENT_LENGTH) final Long fileSize, @FormDataParam(DOCUMENT_API_PARAM_FILE) final InputStream is,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataContentDisposition fileDetails,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataBodyPart filePart,
            @FormDataParam(DOCUMENT_API_PARAM_NAME) final String name,
            @FormDataParam(DOCUMENT_API_PARAM_DESCRIPTION) final String description) {

        final var command = new DocumentUpdateCommand();

        final var request = DocumentUpdateRequest.builder().id(documentId).entityId(entityId).entityType(entityType).name(name)
                .description(description).stream(is);

        if (fileDetails != null) {
            request.fileName(fileDetails.getFileName()).type(fileDetails.getType()).size(fileSize);
        }

        command.setPayload(DocumentUpdateRequest.builder().id(documentId).entityId(entityId).entityType(entityType).name(name)
                .description(description).stream(is).build());

        final Supplier<DocumentUpdateResponse> response = commandPipeline.send(command);

        // TODO: does not return list of changes, should be done for consistency with rest of API
        return response.get();
    }

    @DELETE
    @Path("{documentId}")
    @Operation(summary = "Remove a Document", description = """
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public DocumentDeleteResponse deleteDocument(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId,
            @PathParam(DOCUMENT_API_PARAM_DOCUMENT_ID) final Long documentId) {

        final var command = new DocumentDeleteCommand();

        command.setPayload(DocumentDeleteRequest.builder().id(documentId).entityId(entityId).entityType(entityType).build());

        final Supplier<DocumentDeleteResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
