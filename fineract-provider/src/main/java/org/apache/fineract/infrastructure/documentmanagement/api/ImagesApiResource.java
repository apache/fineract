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

import java.io.InputStream;

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
import javax.ws.rs.core.Response.ResponseBuilder;

import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils.IMAGE_FILE_EXTENSION;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.infrastructure.documentmanagement.exception.InvalidEntityTypeForImageManagementException;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageReadPlatformService;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.codec.Base64;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("{entity}/{entityId}/images")
@Component
@Scope("singleton")
@Api(value = "DomainName//api//v1//{entity}//{entityId}//images", description = "")
public class ImagesApiResource {

    private final PlatformSecurityContext context;
    private final ImageReadPlatformService imageReadPlatformService;
    private final ImageWritePlatformService imageWritePlatformService;
    private final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer;

    @Autowired
    public ImagesApiResource(final PlatformSecurityContext context, final ImageReadPlatformService readPlatformService,
            final ImageWritePlatformService imageWritePlatformService, final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer) {
        this.context = context;
        this.imageReadPlatformService = readPlatformService;
        this.imageWritePlatformService = imageWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    /**
     * Upload images through multi-part form upload
     */
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String addNewClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart bodyPart) {
        validateEntityTypeforImage(entityName);
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
    @Produces({ MediaType.APPLICATION_JSON })
    public String addNewClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            final String jsonRequestBody) {
        validateEntityTypeforImage(entityName);
        final Base64EncodedImage base64EncodedImage = ContentRepositoryUtils.extractImageFromDataURL(jsonRequestBody);

        final CommandProcessingResult result = this.imageWritePlatformService.saveOrUpdateImage(entityName, entityId, base64EncodedImage);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Returns a base 64 encoded client image Data URI
     */
    @GET
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    public Response retrieveImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @QueryParam("maxWidth") final Integer maxWidth, @QueryParam("maxHeight") final Integer maxHeight,
            @QueryParam("output") final String output) {
        validateEntityTypeforImage(entityName);
        if (ENTITY_TYPE_FOR_IMAGES.CLIENTS.toString().equalsIgnoreCase(entityName)) {
            this.context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");
        } else if (ENTITY_TYPE_FOR_IMAGES.STAFF.toString().equalsIgnoreCase(entityName)) {
            this.context.authenticatedUser().validateHasReadPermission("STAFFIMAGE");
        }

        if (output != null && (output.equals("octet") || output.equals("inline_octet"))) { return downloadClientImage(entityName, entityId,
                maxWidth, maxHeight, output); }

        final ImageData imageData = this.imageReadPlatformService.retrieveImage(entityName, entityId);

        // TODO: Need a better way of determining image type
        String imageDataURISuffix = ContentRepositoryUtils.IMAGE_DATA_URI_SUFFIX.JPEG.getValue();
        if (StringUtils.endsWith(imageData.location(), ContentRepositoryUtils.IMAGE_FILE_EXTENSION.GIF.getValue())) {
            imageDataURISuffix = ContentRepositoryUtils.IMAGE_DATA_URI_SUFFIX.GIF.getValue();
        } else if (StringUtils.endsWith(imageData.location(), ContentRepositoryUtils.IMAGE_FILE_EXTENSION.PNG.getValue())) {
            imageDataURISuffix = ContentRepositoryUtils.IMAGE_DATA_URI_SUFFIX.PNG.getValue();
        }

        final String clientImageAsBase64Text = imageDataURISuffix + Base64.encodeBytes(imageData.getContentOfSize(maxWidth, maxHeight));
        return Response.ok(clientImageAsBase64Text).build();
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response downloadClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @QueryParam("maxWidth") final Integer maxWidth, @QueryParam("maxHeight") final Integer maxHeight,
            @QueryParam("output") String output) {
        validateEntityTypeforImage(entityName);
        if (ENTITY_TYPE_FOR_IMAGES.CLIENTS.toString().equalsIgnoreCase(entityName)) {
            this.context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");
        } else if (ENTITY_TYPE_FOR_IMAGES.STAFF.toString().equalsIgnoreCase(entityName)) {
            this.context.authenticatedUser().validateHasReadPermission("STAFFIMAGE");
        }

        final ImageData imageData = this.imageReadPlatformService.retrieveImage(entityName, entityId);

        final ResponseBuilder response = Response.ok(imageData.getContentOfSize(maxWidth, maxHeight));
        String dispositionType = "inline_octet".equals(output) ? "inline" : "attachment";
        response.header("Content-Disposition", dispositionType + "; filename=\"" + imageData.getEntityDisplayName()
                + IMAGE_FILE_EXTENSION.JPEG + "\"");

        // TODO: Need a better way of determining image type

        response.header("Content-Type", imageData.contentType());
        return response.build();
    }

    /**
     * This method is added only for consistency with other URL patterns and for
     * maintaining consistency of usage of the HTTP "verb" at the client side
     */
    @PUT
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart bodyPart) {
        return addNewClientImage(entityName, entityId, fileSize, inputStream, fileDetails, bodyPart);
    }

    /**
     * This method is added only for consistency with other URL patterns and for
     * maintaining consistency of usage of the HTTP "verb" at the client side
     * 
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @PUT
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            final String jsonRequestBody) {
        return addNewClientImage(entityName, entityId, jsonRequestBody);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId) {
        validateEntityTypeforImage(entityName);
        this.imageWritePlatformService.deleteImage(entityName, entityId);
        return this.toApiJsonSerializer.serialize(new CommandProcessingResult(entityId));
    }

    /*** Entities for document Management **/
    public static enum ENTITY_TYPE_FOR_IMAGES {
        STAFF, CLIENTS;

        @Override
        public String toString() {
            return name().toString().toLowerCase();
        }
    }

    private void validateEntityTypeforImage(final String entityName) {
        if (!checkValidEntityType(entityName)) { throw new InvalidEntityTypeForImageManagementException(entityName); }
    }

    private static boolean checkValidEntityType(final String entityType) {
        for (final ENTITY_TYPE_FOR_IMAGES entities : ENTITY_TYPE_FOR_IMAGES.values()) {
            if (entities.name().equalsIgnoreCase(entityType)) { return true; }
        }
        return false;
    }

}