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
package org.apache.fineract.infrastructure.documentmanagement.service;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorContext;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.documentmanagement.adapter.EntityImageIdAdapter;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageDeleteRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageDeleteResponse;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;
import org.apache.fineract.infrastructure.documentmanagement.domain.ImageRepository;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentInvalidRequestException;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageWritePlatformServiceImpl implements ImageWritePlatformService {

    private static final String STORE_PREFIX = "images";
    private static final String DEFAULT_ENTITY_TYPE = "clients";
    private static final String DEFAULT_EXTENSION = ".jpg";

    @Deprecated
    private final List<EntityImageIdAdapter> imageIdAdapters;
    private final ContentStoreService storeService;
    private final ImageRepository imageRepository;
    private final ContentDetectorManager contentDetectorManager;

    @Transactional
    @Override
    public ImageCreateResponse createImage(final ImageCreateRequest request) {
        try {
            if (StringUtils.isEmpty(request.getEntityType())) {
                // TODO: keeping the path segment always "clients" not consistent how this works with documents
                request.setEntityType(DEFAULT_ENTITY_TYPE);
            }
            final var imageEntityType = normalizeImageEntityType(request.getEntityType());

            if (StringUtils.isEmpty(request.getFileName())) {
                // NOTE: defacto limiting the uploads to JPEG files, same behavior as before
                request.setFileName(UUID.randomUUID() + DEFAULT_EXTENSION);
            }

            if (StringUtils.isEmpty(request.getType())) {
                final var type = Optional
                        .of(contentDetectorManager.detect(ContentDetectorContext.builder().fileName(request.getFileName()).build()))
                        .filter(contentDetectorContext -> StringUtils.isNotEmpty(contentDetectorContext.getMimeType()))
                        .filter(contentDetectorContext -> !APPLICATION_OCTET_STREAM_VALUE
                                .equalsIgnoreCase(contentDetectorContext.getMimeType()))
                        .map(ContentDetectorContext::getMimeType).orElse(APPLICATION_OCTET_STREAM_VALUE);

                request.setType(type);
            }

            // TODO: make "prefix" configurable?
            var path = getPath(STORE_PREFIX, imageEntityType, request.getEntityId(), request.getFileName(), storeService.getDelimiter());

            final var imagePath = storeService.upload(path, request.getStream(), request.getType());

            final var result = imageIdAdapters.stream().filter(imageIdAdapter -> imageIdAdapter.accept(imageEntityType)).findFirst()
                    .flatMap(imageIdAdapter -> imageIdAdapter.get(request.getEntityId()))
                    .flatMap(imageIdResult -> imageRepository.findById(imageIdResult.getId())).map(image -> {
                        // delete old image
                        storeService.delete(image.getLocation());

                        return image;
                    }).or(() -> Optional.of(new Image()))
                    .map(image -> image.setLocation(imagePath).setStorageType(storeService.getType().getValue())).map(imageRepository::save)
                    .map(image -> ImageCreateResponse.builder().resourceId(image.getId()).build());

            imageIdAdapters.stream().filter(imageIdAdapter -> imageIdAdapter.accept(imageEntityType)).findFirst()
                    .ifPresent(imageIdAdapter -> result.ifPresent(
                            imageCreateResponse -> imageIdAdapter.set(request.getEntityId(), imageCreateResponse.getResourceId())));

            return result.orElseThrow(() -> new DocumentNotFoundException(request.getEntityType(), request.getEntityId(), -1L));
        } catch (final Exception e) {
            throw new DocumentInvalidRequestException(e);
        }
    }

    @Transactional
    @Override
    public ImageDeleteResponse deleteImage(final ImageDeleteRequest request) {
        try {
            return delete(request.getEntityType(), request.getEntityId())
                    .orElseThrow(() -> new DocumentNotFoundException(request.getEntityType(), request.getEntityId(), -1L));
        } catch (final Exception e) {
            throw new DocumentInvalidRequestException(e);
        }
    }

    private Optional<ImageDeleteResponse> delete(final String entityType, final Long entityId) {
        return imageIdAdapters.stream().filter(imageIdAdapter -> imageIdAdapter.accept(entityType)).findFirst()
                .flatMap(imageIdAdapter -> imageIdAdapter.set(entityId, null))
                .flatMap(imageIdResult -> imageRepository.findById(imageIdResult.getId())).map(image -> {
                    requireNonNull(image.getId(), "Image ID required");
                    requireNonNull(image.getLocation(), "Image location required");

                    storeService.delete(image.getLocation());

                    imageRepository.deleteById(image.getId());

                    return ImageDeleteResponse.builder().resourceId(entityId).resourceIdentifier(entityType).build();
                });
    }

    private String normalizeImageEntityType(final String entityType) {
        if ("staff".equalsIgnoreCase(entityType)) {
            return "staff";
        }
        if ("clients".equalsIgnoreCase(entityType)) {
            return DEFAULT_ENTITY_TYPE;
        }
        return entityType;
    }

    private String getPath(final String prefix, final String entityType, final Long entityId, final String fileName, String delimiter) {
        requireNonNull(prefix);
        requireNonNull(entityType);
        requireNonNull(entityId);
        requireNonNull(fileName);
        requireNonNull(delimiter);

        return String.join(delimiter, prefix, entityType, String.valueOf(entityId), fileName);
    }
}
