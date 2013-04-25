/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.api;

import com.lowagie.text.pdf.codec.Base64;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.ImageUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.exception.ImageNotFoundException;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.InputStream;

@Path("/clients/{clientId}/images")
@Component
@Scope("singleton")
public class ClientImagesApiResource {

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ClientWritePlatformService clientWritePlatformService;
    private final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final static Logger logger = LoggerFactory.getLogger(ClientImagesApiResource.class);

    @Autowired
    public ClientImagesApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
                                   final ClientWritePlatformService clientWritePlatformService, final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer) {
        this.context = context;
        this.clientReadPlatformService = readPlatformService;
        this.clientWritePlatformService = clientWritePlatformService;
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
        ImageUtils.validateClientImageNotEmpty(fileDetails.getFileName());
        ImageUtils.validateImageMimeType(bodyPart.getMediaType().toString());

        final CommandProcessingResult result = this.clientWritePlatformService.saveOrUpdateClientImage(clientId, fileDetails.getFileName(),
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

        final Base64EncodedImage base64EncodedImage = ImageUtils.extractImageFromDataURL(jsonRequestBody);

        final CommandProcessingResult result = this.clientWritePlatformService.saveOrUpdateClientImage(clientId, base64EncodedImage);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Returns a base 64 encoded client image Data URI
     */
    @GET
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    public String retrieveClientImage(@PathParam("clientId") final Long clientId) {

        context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");

        final ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);

        if (clientData.imageKeyDoesNotExist()) { throw new ImageNotFoundException("clients", clientId); }

        // TODO: Need a better way of determining image type
        String imageDataURISuffix = ImageUtils.IMAGE_DATA_URI_SUFFIX.JPEG.getValue();
        if (StringUtils.endsWith(clientData.imageKey(), ImageUtils.IMAGE_FILE_EXTENSION.GIF.getValue())) {
            imageDataURISuffix = ImageUtils.IMAGE_DATA_URI_SUFFIX.GIF.getValue();
        } else if (StringUtils.endsWith(clientData.imageKey(), ImageUtils.IMAGE_FILE_EXTENSION.PNG.getValue())) {
            imageDataURISuffix = ImageUtils.IMAGE_DATA_URI_SUFFIX.PNG.getValue();
        }

        String clientImageAsBase64Text = imageDataURISuffix + Base64.encodeBytes(clientData.image());
        return clientImageAsBase64Text;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response downloadClientImage(@PathParam("clientId") final Long clientId) {

        context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");
        final ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);

        if (clientData.imageKeyDoesNotExist()) { throw new ImageNotFoundException("clients", clientId); }

        ResponseBuilder response = Response.ok(clientData.image());
        response.header("Content-Disposition", "attachment; filename=\"" + clientData.imageName() + "\"");

        // TODO: Need a better way of determining image type

        response.header("Content-Type", clientData.imageContentType());
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
        this.clientWritePlatformService.deleteClientImage(clientId);
        return this.toApiJsonSerializer.serialize(new CommandProcessingResult(clientId));
    }

}