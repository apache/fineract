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

import static java.util.Objects.requireNonNull;
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor.DATA_URL_ENCODE_PARAM_CONTENT_TYPE;
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor.DATA_URL_ENCODE_PARAM_ENCODING;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_FORMAT;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_MAX_HEIGHT;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_MAX_WIDTH;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_ENTITY_ID;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_ENTITY_TYPE;
import static org.apache.fineract.infrastructure.documentmanagement.api.DocumentApiConstants.DOCUMENT_API_PARAM_FILE;
import static org.apache.fineract.infrastructure.documentmanagement.api.ImageApiConstants.IMAGE_API_PARAM_MAX_HEIGHT;
import static org.apache.fineract.infrastructure.documentmanagement.api.ImageApiConstants.IMAGE_API_PARAM_MAX_WIDTH;
import static org.apache.fineract.infrastructure.documentmanagement.api.ImageApiConstants.IMAGE_API_PARAM_OUTPUT;
import static org.apache.fineract.infrastructure.documentmanagement.api.ImageApiConstants.IMAGE_API_VALUE_ENCODING_BASE64;
import static org.apache.fineract.infrastructure.documentmanagement.api.ImageApiConstants.IMAGE_API_VALUE_OUTPUT_INLINE_OCTET;
import static org.apache.fineract.util.StreamResponseUtil.DISPOSITION_TYPE_ATTACHMENT;
import static org.apache.fineract.util.StreamResponseUtil.DISPOSITION_TYPE_INLINE;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.contentstore.processor.Base64DecoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.Base64EncoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.ContentProcessorContext;
import org.apache.fineract.infrastructure.contentstore.processor.DataUrlDecoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor;
import org.apache.fineract.infrastructure.documentmanagement.command.ImageCreateCommand;
import org.apache.fineract.infrastructure.documentmanagement.command.ImageDeleteCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageDeleteRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageDeleteResponse;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageReadPlatformService;
import org.apache.fineract.util.StreamResponseUtil;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

// NOTE: left for backward compatibility only, could be unified with documents
@Deprecated
@Slf4j
@RequiredArgsConstructor
@Component
@Path("/v1/{entityType}/{entityId}/images")
public class ImagesApiResource {

    private final ImageReadPlatformService imageReadPlatformService;
    private final CommandPipeline commandPipeline;
    private final ImageResizeContentProcessor imageResizeContentProcessor;
    private final Base64EncoderContentProcessor base64EncoderContentProcessor;
    private final DataUrlEncoderContentProcessor dataUrlEncoderContentProcessor;
    private final Base64DecoderContentProcessor base64DecoderContentProcessor;
    private final DataUrlDecoderContentProcessor dataUrlDecoderContentProcessor;
    private final FileUploadValidator fileUploadValidator;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    // FINERACT-1265: Do NOT specify @Produces(TEXT_PLAIN) here - it may actually not (if it calls the next methods it's
    // octet-stream)
    public Response retrieveImage(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityName,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId, @QueryParam(IMAGE_API_PARAM_MAX_WIDTH) final Integer maxWidth,
            @QueryParam(IMAGE_API_PARAM_MAX_HEIGHT) final Integer maxHeight, @QueryParam(IMAGE_API_PARAM_OUTPUT) String output,
            @HeaderParam(ACCEPT) String acceptHeader) {

        // TODO: pass resize information here and do all the processing in the service
        final var content = imageReadPlatformService.retrieveImage(entityName, entityId);

        String dispositionType = null;
        ContentProcessorContext ctx = new ContentProcessorContext(content.getStream());
        String type;

        // prevent clients sending non-sense
        if (!"octet".equalsIgnoreCase(output) && !"inline_octet".equalsIgnoreCase(output)) {
            output = null;
        }

        if ((StringUtils.isNotEmpty(output) && output.contains("octet")) || APPLICATION_OCTET_STREAM_VALUE.equalsIgnoreCase(acceptHeader)) {
            if (maxWidth != null && maxHeight != null) {
                ctx = imageResizeContentProcessor.process(content.getStream(), Map.of(IMAGE_RESIZE_PARAM_MAX_WIDTH, maxWidth,
                        IMAGE_RESIZE_PARAM_MAX_HEIGHT, maxHeight, IMAGE_RESIZE_PARAM_FORMAT, content.getFormat()));
            }

            if (IMAGE_API_VALUE_OUTPUT_INLINE_OCTET.equalsIgnoreCase(output)) {
                dispositionType = DISPOSITION_TYPE_INLINE;
            } else {
                dispositionType = DISPOSITION_TYPE_ATTACHMENT;
            }

            type = content.getContentType();
        } else {
            // else stream base64 encoded original format
            if (maxWidth != null && maxHeight != null) {
                ctx = imageResizeContentProcessor.then(base64EncoderContentProcessor).then(dataUrlEncoderContentProcessor).process(
                        content.getStream(),
                        Map.of(IMAGE_RESIZE_PARAM_MAX_WIDTH, maxWidth, IMAGE_RESIZE_PARAM_MAX_HEIGHT, maxHeight, IMAGE_RESIZE_PARAM_FORMAT,
                                content.getFormat(), DATA_URL_ENCODE_PARAM_CONTENT_TYPE, content.getContentType(),
                                DATA_URL_ENCODE_PARAM_ENCODING, IMAGE_API_VALUE_ENCODING_BASE64));
            } else {
                ctx = base64EncoderContentProcessor.then(dataUrlEncoderContentProcessor).process(content.getStream(),
                        Map.of(DATA_URL_ENCODE_PARAM_CONTENT_TYPE, content.getContentType(), DATA_URL_ENCODE_PARAM_ENCODING,
                                IMAGE_API_VALUE_ENCODING_BASE64));
            }

            type = TEXT_PLAIN_VALUE;
        }

        // make sure we use the transformed input stream ("ctx.getInputStream()")
        return StreamResponseUtil.ok(StreamResponseUtil.StreamResponseData.builder().fileName(content.getDisplayName()).type(type)
                .stream(ctx.getInputStream()).dispositionType(dispositionType).build());
    }

    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponse(responseCode = "200", description = "Not Shown (multi-part form data)")
    public ImageCreateResponse createImage(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId, @HeaderParam(CONTENT_LENGTH) final Long fileSize,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final InputStream is,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataContentDisposition fileDetails,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataBodyPart filePart) {

        fileUploadValidator.validate(fileSize, is, fileDetails, filePart);

        final var command = new ImageCreateCommand();

        command.setPayload(
                ImageCreateRequest.builder().entityId(entityId).entityType(entityType).fileName(fileDetails.getFileName()).size(fileSize)
                        .type(Optional.ofNullable(filePart.getMediaType()).map(MediaType::toString).orElse(null)).stream(is).build());

        final Supplier<ImageCreateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @POST
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    public ImageCreateResponse createImage(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId, final InputStream body) {

        requireNonNull(body, "Missing input stream");

        final var command = new ImageCreateCommand();

        var ctx = dataUrlDecoderContentProcessor.then(base64DecoderContentProcessor).process(new ContentProcessorContext(body));

        command.setPayload(ImageCreateRequest.builder().entityId(entityId).entityType(entityType).stream(ctx.getInputStream()).build());

        final Supplier<ImageCreateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Update image", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "object")) })
    public ImageCreateResponse updateImage(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityName,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId, @HeaderParam(CONTENT_LENGTH) final Long fileSize,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final InputStream inputStream,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataContentDisposition fileDetails,
            @FormDataParam(DOCUMENT_API_PARAM_FILE) final FormDataBodyPart bodyPart) {
        return createImage(entityName, entityId, fileSize, inputStream, fileDetails, bodyPart);
    }

    @PUT
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    public ImageCreateResponse updateImage(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityName,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId, final InputStream body) {
        return createImage(entityName, entityId, body);
    }

    @DELETE
    public ImageDeleteResponse deleteImage(@PathParam(DOCUMENT_API_PARAM_ENTITY_TYPE) final String entityType,
            @PathParam(DOCUMENT_API_PARAM_ENTITY_ID) final Long entityId) {

        final var command = new ImageDeleteCommand();

        command.setPayload(ImageDeleteRequest.builder().entityId(entityId).entityType(entityType).build());

        final Supplier<ImageDeleteResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
