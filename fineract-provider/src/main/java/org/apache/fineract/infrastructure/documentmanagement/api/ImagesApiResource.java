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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils.ImageFileExtension;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageResizer;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.apache.fineract.infrastructure.documentmanagement.exception.InvalidEntityTypeForImageManagementException;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageReadPlatformService;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Path("{entity}/{entityId}/images")

public class ImagesApiResource {

    private final PlatformSecurityContext context;
    private final ImageReadPlatformService imageReadPlatformService;
    private final ImageWritePlatformService imageWritePlatformService;
    private final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final FileUploadValidator fileUploadValidator;
    private final ImageResizer imageResizer;

    @Autowired
    public ImagesApiResource(final PlatformSecurityContext context, final ImageReadPlatformService readPlatformService,
            final ImageWritePlatformService imageWritePlatformService, final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer,
            final FileUploadValidator fileUploadValidator, final ImageResizer imageResizer) {
        this.context = context;
        this.imageReadPlatformService = readPlatformService;
        this.imageWritePlatformService = imageWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.fileUploadValidator = fileUploadValidator;
        this.imageResizer = imageResizer;
    }

    /**
     * Upload images through multi-part form upload
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RequestBody(description = "Upload new client image", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String addNewClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart bodyPart) {
        validateEntityTypeforImage(entityName);
        fileUploadValidator.validate(fileSize, inputStream, fileDetails, bodyPart);
        // TODO: vishwas might need more advances validation (like reading magic
        // number) for handling malicious clients
        // and clients not setting mime type
        ContentRepositoryUtils.validateClientImageNotEmpty(fileDetails.getFileName());
        ContentRepositoryUtils.validateImageMimeType(bodyPart.getMediaType().toString());

        final CommandProcessingResult result = this.imageWritePlatformService.saveOrUpdateImage(entityName, entityId,
                fileDetails.getFileName(), inputStream, fileSize);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @POST
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            final String jsonRequestBody) {
        validateEntityTypeforImage(entityName);

        final Base64EncodedImage base64EncodedImage = ContentRepositoryUtils.extractImageFromDataURL(jsonRequestBody);

        final CommandProcessingResult result = this.imageWritePlatformService.saveOrUpdateImage(entityName, entityId, base64EncodedImage);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Returns a images, either as Base64 encoded text/plain or as inline or attachment with image MIME type as
     * Content-Type.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    // FINERACT-1265: Do NOT specify @Produces(TEXT_PLAIN) here - it may actually not (if it calls the next methods it's
    // octet-stream)
    public Response retrieveImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @QueryParam("maxWidth") final Integer maxWidth, @QueryParam("maxHeight") final Integer maxHeight,
            @QueryParam("output") final String output, @HeaderParam("Accept") String acceptHeader) {
        validateEntityTypeforImage(entityName);
        if (EntityTypeForImages.CLIENTS.toString().equalsIgnoreCase(entityName)) {
            this.context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");
        } else if (EntityTypeForImages.STAFF.toString().equalsIgnoreCase(entityName)) {
            this.context.authenticatedUser().validateHasReadPermission("STAFFIMAGE");
        }

        final FileData imageData = this.imageReadPlatformService.retrieveImage(entityName, entityId);
        final FileData resizedImage = imageResizer.resize(imageData, maxWidth, maxHeight);

        // If client wants (Accept header) octet-stream, or output="octet" or "inline_octet", then send that instead of
        // text
        if ("application/octet-stream".equalsIgnoreCase(acceptHeader)
                || (output != null && (output.equals("octet") || output.equals("inline_octet")))) {
            return ContentResources.fileDataToResponse(resizedImage, resizedImage.name() + ImageFileExtension.JPEG,
                    "inline_octet".equals(output) ? "inline" : "attachment");
        }

        // Else return response with Base64 encoded
        // TODO: Need a better way of determining image type
        String imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.JPEG.getValue();
        if (StringUtils.endsWith(imageData.name(), ContentRepositoryUtils.ImageFileExtension.GIF.getValue())) {
            imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.GIF.getValue();
        } else if (StringUtils.endsWith(imageData.name(), ContentRepositoryUtils.ImageFileExtension.PNG.getValue())) {
            imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.PNG.getValue();
        }

        try {
            byte[] resizedImageBytes = resizedImage.getByteSource().read();
            final String clientImageAsBase64Text = imageDataURISuffix + Base64.getMimeEncoder().encodeToString(resizedImageBytes);
            return Response.ok(clientImageAsBase64Text, MediaType.TEXT_PLAIN_TYPE).build();
        } catch (IOException e) {
            throw new ContentManagementException(imageData.name(), e.getMessage(), e);
        }
    }

    /**
     * This method is added only for consistency with other URL patterns and for maintaining consistency of usage of the
     * HTTP "verb" at the client side
     */
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RequestBody(description = "Update client image", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String updateClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart bodyPart) {
        return addNewClientImage(entityName, entityId, fileSize, inputStream, fileDetails, bodyPart);
    }

    /**
     * This method is added only for consistency with other URL patterns and for maintaining consistency of usage of the
     * HTTP "verb" at the client side
     *
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @PUT
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String updateClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            final String jsonRequestBody) {
        return addNewClientImage(entityName, entityId, jsonRequestBody);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId) {
        validateEntityTypeforImage(entityName);
        this.imageWritePlatformService.deleteImage(entityName, entityId);
        return this.toApiJsonSerializer.serialize(new CommandProcessingResult(entityId));
    }

    /*** Entities for document Management **/
    public enum EntityTypeForImages {

        STAFF, CLIENTS;

        @Override
        public String toString() {
            return name().toString().toLowerCase();
        }
    }

    private void validateEntityTypeforImage(final String entityName) {
        if (!checkValidEntityType(entityName)) {
            throw new InvalidEntityTypeForImageManagementException(entityName);
        }
    }

    private static boolean checkValidEntityType(final String entityType) {
        for (final EntityTypeForImages entities : EntityTypeForImages.values()) {
            if (entities.name().equalsIgnoreCase(entityType)) {
                return true;
            }
        }
        return false;
    }
}
