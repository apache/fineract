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

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorContext;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.documentmanagement.adapter.EntityImageIdAdapter;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentContent;
import org.apache.fineract.infrastructure.documentmanagement.domain.ImageRepository;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageReadPlatformServiceImpl implements ImageReadPlatformService {

    @Deprecated
    private final List<EntityImageIdAdapter> imageIdAdapters;
    private final ContentStoreService storeService;
    private final ImageRepository imageRepository;
    private final ContentDetectorManager contentDetectorManager;

    @Override
    public DocumentContent retrieveImage(final String entityType, final Long entityId) {
        return imageIdAdapters.stream().filter(imageIdAdapter -> imageIdAdapter.accept(entityType)).findFirst()
                .flatMap(imageIdAdapter -> imageIdAdapter.get(entityId))
                .flatMap(imageIdResult -> imageRepository.findById(imageIdResult.getId()).map(image -> DocumentContent.builder()
                        .fileName(FilenameUtils.getName(image.getLocation())).format(FilenameUtils.getExtension(image.getLocation()))
                        .displayName(imageIdResult.getDisplayName())
                        .contentType(contentDetectorManager
                                .detect(ContentDetectorContext.builder().fileName(FilenameUtils.getName(image.getLocation())).build())
                                .getMimeType())
                        .stream(storeService.download(image.getLocation())).build()))
                .orElseThrow(() -> new DocumentNotFoundException(entityType, entityId, -1L));
    }
}
