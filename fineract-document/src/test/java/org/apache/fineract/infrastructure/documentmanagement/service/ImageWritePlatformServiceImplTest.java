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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.contentstore.data.ContentStoreType;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.documentmanagement.adapter.EntityImageIdAdapter;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;
import org.apache.fineract.infrastructure.documentmanagement.domain.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageWritePlatformServiceImplTest {

    @Mock
    private EntityImageIdAdapter clientImageIdAdapter;
    @Mock
    private EntityImageIdAdapter staffImageIdAdapter;
    @Mock
    private ContentStoreService contentStoreService;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ContentDetectorManager contentDetectorManager;

    private ImageWritePlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new ImageWritePlatformServiceImpl(List.of(clientImageIdAdapter, staffImageIdAdapter), contentStoreService,
                imageRepository, contentDetectorManager);

        when(contentStoreService.getDelimiter()).thenReturn("/");
        when(contentStoreService.getType()).thenReturn(ContentStoreType.FILE_SYSTEM);
        when(contentStoreService.upload(anyString(), any(InputStream.class), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image image = invocation.getArgument(0);
            image.setId(99L);
            return image;
        });
    }

    @Test
    void createImageShouldStoreStaffImageUnderStaffDirectory() {
        Long entityId = 7L;
        when(staffImageIdAdapter.accept("staff")).thenReturn(true);
        when(staffImageIdAdapter.get(entityId)).thenReturn(Optional.empty());
        when(staffImageIdAdapter.set(eq(entityId), anyLong())).thenReturn(Optional.empty());
        when(clientImageIdAdapter.accept(anyString())).thenReturn(false);

        ImageCreateRequest request = ImageCreateRequest.builder().entityType("STAFF").entityId(entityId).fileName("profile.png")
                .type("image/png").stream(new ByteArrayInputStream(new byte[] { 1, 2, 3 })).build();

        var response = underTest.createImage(request);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(contentStoreService).upload(pathCaptor.capture(), any(InputStream.class), eq("image/png"));
        assertEquals("images/staff/7/profile.png", pathCaptor.getValue());
        assertEquals(99L, response.getResourceId());
    }

    @Test
    void createImageShouldStoreClientImageUnderClientsDirectory() {
        Long entityId = 7L;
        when(clientImageIdAdapter.accept("clients")).thenReturn(true);
        when(clientImageIdAdapter.get(entityId)).thenReturn(Optional.empty());
        when(clientImageIdAdapter.set(eq(entityId), anyLong())).thenReturn(Optional.empty());

        ImageCreateRequest request = ImageCreateRequest.builder().entityType("CLIENTS").entityId(entityId).fileName("profile.png")
                .type("image/png").stream(new ByteArrayInputStream(new byte[] { 1, 2, 3 })).build();

        var response = underTest.createImage(request);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(contentStoreService).upload(pathCaptor.capture(), any(InputStream.class), eq("image/png"));
        assertEquals("images/clients/7/profile.png", pathCaptor.getValue());
        assertEquals(99L, response.getResourceId());
    }
}
