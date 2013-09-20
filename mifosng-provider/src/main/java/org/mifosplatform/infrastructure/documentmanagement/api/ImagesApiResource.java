/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.api;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.mifosplatform.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils.IMAGE_FILE_EXTENSION;
import org.mifosplatform.infrastructure.documentmanagement.data.ImageData;
import org.mifosplatform.infrastructure.documentmanagement.service.ImageReadPlatformService;
import org.mifosplatform.infrastructure.documentmanagement.service.ImageWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.codec.Base64;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * TODO Vishwas need to change the url to entity/entityId/images to make the
 * image management API's generic for other entities like staff etc
 **/
@Path("{clients}/{clientId}/images")
@Component
@Scope("singleton")
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
    public String addNewClientImage(@PathParam("clientId") final Long clientId, @HeaderParam("Content-Length") final Long fileSize,
            @FormDataParam("file") final InputStream inputStream, @FormDataParam("file") final FormDataContentDisposition fileDetails,
            @FormDataParam("file") final FormDataBodyPart bodyPart) {

        // TODO: vishwas might need more advances validation (like reading magic
        // number) for handling malicious clients
        // and clients not setting mime type
        ContentRepositoryUtils.validateClientImageNotEmpty(fileDetails.getFileName());
        ContentRepositoryUtils.validateImageMimeType(bodyPart.getMediaType().toString());

        final CommandProcessingResult result = this.imageWritePlatformService.saveOrUpdateClientImage(clientId, fileDetails.getFileName(),
                inputStream, fileSize);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @POST
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String addNewClientImage(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {

        final Base64EncodedImage base64EncodedImage = ContentRepositoryUtils.extractImageFromDataURL(jsonRequestBody);

        final CommandProcessingResult result = this.imageWritePlatformService.saveOrUpdateClientImage(clientId, base64EncodedImage);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Returns a base 64 encoded client image Data URI
     */
    @GET
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    public String retrieveClientImage(@PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");

        final ImageData imageData = this.imageReadPlatformService.retrieveClientImage(clientId);

        // TODO: Need a better way of determining image type
        String imageDataURISuffix = ContentRepositoryUtils.IMAGE_DATA_URI_SUFFIX.JPEG.getValue();
        if (StringUtils.endsWith(imageData.location(), ContentRepositoryUtils.IMAGE_FILE_EXTENSION.GIF.getValue())) {
            imageDataURISuffix = ContentRepositoryUtils.IMAGE_DATA_URI_SUFFIX.GIF.getValue();
        } else if (StringUtils.endsWith(imageData.location(), ContentRepositoryUtils.IMAGE_FILE_EXTENSION.PNG.getValue())) {
            imageDataURISuffix = ContentRepositoryUtils.IMAGE_DATA_URI_SUFFIX.PNG.getValue();
        }

        final String clientImageAsBase64Text = imageDataURISuffix + Base64.encodeBytes(imageData.getContent());
        return clientImageAsBase64Text;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response downloadClientImage(@PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");
        final ImageData imageData = this.imageReadPlatformService.retrieveClientImage(clientId);

        final ResponseBuilder response = Response.ok(imageData.getContent());
        response.header("Content-Disposition", "attachment; filename=\"" + imageData.getEntityDisplayName() + IMAGE_FILE_EXTENSION.JPEG
                + "\"");

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
    public String updateClientImage(@PathParam("clientId") final Long clientId, @HeaderParam("Content-Length") final Long fileSize,
            @FormDataParam("file") final InputStream inputStream, @FormDataParam("file") final FormDataContentDisposition fileDetails,
            @FormDataParam("file") final FormDataBodyPart bodyPart) {
        return addNewClientImage(clientId, fileSize, inputStream, fileDetails, bodyPart);
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
    public String updateClientImage(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {
        return addNewClientImage(clientId, jsonRequestBody);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteClientImage(@PathParam("clientId") final Long clientId) {
        this.imageWritePlatformService.deleteClientImage(clientId);
        return this.toApiJsonSerializer.serialize(new CommandProcessingResult(clientId));
    }

}