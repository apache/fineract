package org.mifosplatform.portfolio.client.api;

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

import org.mifosplatform.infrastructure.core.api.ApiConstants;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.exception.ImageNotFoundException;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.codec.Base64;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

// FIXME - kw- image should be images to keep consitent with rest of API - even if we only support one image per client.
@Path("/clients/{clientId}/image")
@Component
@Scope("singleton")
public class ClientImagesApiResource {

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ClientWritePlatformService clientWritePlatformService;
    private final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer;

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
    public String addNewClientImage(@PathParam("clientId") final Long clientId, @HeaderParam("Content-Length") Long fileSize,
            @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") FormDataBodyPart bodyPart) {

        // TODO: vishwas might need more advances validation (like reading magic
        // number) for handling malicious clients
        // and clients not setting mime type
        FileUtils.validateClientImageNotEmpty(fileDetails.getFileName());
        FileUtils.validateImageMimeType(bodyPart.getMediaType().toString());
        FileUtils.validateFileSizeWithinPermissibleRange(fileSize, fileDetails.getFileName(), ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);

        final CommandProcessingResult result = this.clientWritePlatformService.saveOrUpdateClientImage(clientId, fileDetails.getFileName(),
                inputStream);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String addNewClientImage(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {

        final Base64EncodedImage base64EncodedImage = FileUtils.extractImageFromDataURL(jsonRequestBody);

        final CommandProcessingResult result = this.clientWritePlatformService.saveOrUpdateClientImage(clientId, base64EncodedImage);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Returns a base 64 encoded client image
     */
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClientImage(@PathParam("clientId") final Long clientId) {

        context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");

        final ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);

        if (clientData.imageKeyDoesNotExist()) { throw new ImageNotFoundException("clients", clientId); }

        return Base64.encodeFromFile(clientData.imageKey());
    }

    /**
     * This method is added only for consistency with other URL patterns and for
     * maintaining consistency of usage of the HTTP "verb" at the client side
     */
    @PUT
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientImage(@PathParam("clientId") final Long clientId, @HeaderParam("Content-Length") Long fileSize,
            @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") FormDataBodyPart bodyPart) {

        return addNewClientImage(clientId, fileSize, inputStream, fileDetails, bodyPart);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteClientImage(@PathParam("clientId") final Long clientId) {
        this.clientWritePlatformService.deleteClientImage(clientId);

        return this.toApiJsonSerializer.serialize(new CommandProcessingResult(clientId));
    }

    /**
     * This method is added only for consistency with other URL patterns and for
     * maintaining consistency of usage of the HTTP "verb" at the client side
     * 
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClientImage(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {
        return addNewClientImage(clientId, jsonRequestBody);
    }
}