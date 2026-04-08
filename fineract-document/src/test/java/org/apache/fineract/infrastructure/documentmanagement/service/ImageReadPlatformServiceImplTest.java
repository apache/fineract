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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.documentmanagement.adapter.EntityImageIdAdapter;
import org.apache.fineract.infrastructure.documentmanagement.domain.ImageRepository;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageReadPlatformServiceImplTest {

    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ContentStoreService storeService;
    @Mock
    private ContentDetectorManager contentDetectorManager;
    @Mock
    private EntityImageIdAdapter imageIdAdapter;

    private ImageReadPlatformServiceImpl imageReadPlatformService;

    @BeforeEach
    void setUp() {
        List<EntityImageIdAdapter> imageIdAdapters = Collections.singletonList(imageIdAdapter);
        imageReadPlatformService = new ImageReadPlatformServiceImpl(imageIdAdapters, storeService, imageRepository, contentDetectorManager);
    }

    @Test
    void testRetrieveImage_NotFound_ThrowsDocumentNotFoundException() {
        // Arrange
        String entityType = "clients";
        Long entityId = 1L;

        when(imageIdAdapter.accept(anyString())).thenReturn(true);
        when(imageIdAdapter.get(entityId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentNotFoundException.class, () -> {
            imageReadPlatformService.retrieveImage(entityType, entityId);
        });
    }
}
