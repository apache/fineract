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
package org.apache.fineract.infrastructure.contentstore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.apache.fineract.infrastructure.contentstore.exception.ContentStoreException;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultDeleteContentPolicy;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultDownloadContentPolicy;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultPostUploadContentPolicy;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultPreUploadContentPolicy;
import org.apache.fineract.infrastructure.contentstore.util.ContentPathSanitizer;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ContentStoreServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private ContentPathSanitizer pathSanitizer;
    @Mock
    private DefaultDownloadContentPolicy downloadContentPolicy;
    @Mock
    private DefaultPreUploadContentPolicy preUploadContentPolicy;
    @Mock
    private DefaultPostUploadContentPolicy postUploadContentPolicy;
    @Mock
    private DefaultDeleteContentPolicy deleteContentPolicy;
    @Mock
    private ContentDetectorManager contentDetectorManager;

    private S3ContentStoreService service;

    @BeforeEach
    void setUp() {
        final var s3Properties = new FineractProperties.FineractContentS3Properties();
        s3Properties.setBucketName("bucket");
        final var content = new FineractProperties.FineractContentProperties();
        content.setS3(s3Properties);
        final var properties = new FineractProperties();
        properties.setContent(content);

        when(pathSanitizer.sanitize(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        service = new S3ContentStoreService(s3Client, pathSanitizer, downloadContentPolicy, preUploadContentPolicy, postUploadContentPolicy,
                deleteContentPolicy, contentDetectorManager, properties);
        setTenant("tenant-a");
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.clearTenant();
    }

    @Test
    void uploadStoresNewObjectsBelowTheTenantPrefix() {
        stubGetObject();

        final var location = service.upload("documents/clients/1/file.txt", new ByteArrayInputStream(new byte[] { 1 }), "text/plain");

        assertEquals("tenants/tenant-a/documents/clients/1/file.txt", location);
        assertEquals("tenants/tenant-a/documents/clients/1/file.txt", capturedPutKey());
    }

    @Test
    void uploadDoesNotPrefixALocationTwice() {
        stubGetObject();

        final var location = service.upload("tenants/tenant-a/documents/clients/1/file.txt", new ByteArrayInputStream(new byte[] { 1 }),
                "text/plain");

        assertEquals("tenants/tenant-a/documents/clients/1/file.txt", location);
    }

    @Test
    void uploadCannotWriteToAnotherTenantPrefix() {
        assertThrows(ContentPolicyException.class,
                () -> service.upload("tenants/tenant-b/documents/x.txt", new ByteArrayInputStream(new byte[] { 1 }), "text/plain"));

        verify(s3Client, never()).putObject(any(Consumer.class), any(RequestBody.class));
    }

    @Test
    void downloadOfALocationWithoutPrefixStillResolvesToTheStoredKey() {
        stubGetObject();

        service.download("documents/clients/1/legacy.txt");

        final var captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture(), any(ResponseTransformer.class));
        assertEquals("documents/clients/1/legacy.txt", captor.getValue().key());
    }

    @Test
    void downloadOfTheCurrentTenantsLocationResolvesToTheStoredKey() {
        stubGetObject();

        service.download("tenants/tenant-a/documents/clients/1/file.txt");

        final var captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture(), any(ResponseTransformer.class));
        assertEquals("tenants/tenant-a/documents/clients/1/file.txt", captor.getValue().key());
    }

    @Test
    void downloadAndDeleteRejectAnotherTenantsLocation() {
        assertThrows(ContentPolicyException.class, () -> service.download("tenants/tenant-b/documents/clients/1/file.txt"));
        assertThrows(ContentPolicyException.class, () -> service.delete("tenants/tenant-b/documents/clients/1/file.txt"));

        verify(s3Client, never()).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
        verify(s3Client, never()).deleteObject(any(Consumer.class));
    }

    @Test
    void rejectsLocationsEscapingTheTenantPrefix() {
        assertThrows(ContentPolicyException.class, () -> service.download("../tenant-b/documents/file.txt"));
        assertThrows(ContentPolicyException.class, () -> service.delete("/tenants/tenant-b/documents/file.txt"));
    }

    @Test
    void deleteOfALegacyLocationUsesTheStoredKey() {
        service.delete("documents/clients/1/legacy.txt");

        final ArgumentCaptor<Consumer<DeleteObjectRequest.Builder>> captor = ArgumentCaptor.captor();
        verify(s3Client).deleteObject(captor.capture());
        final var builder = DeleteObjectRequest.builder();
        captor.getValue().accept(builder);
        assertEquals("documents/clients/1/legacy.txt", builder.build().key());
    }

    @Test
    void uploadRejectsATenantIdentifierThatCannotBeUsedAsKeyPrefix() {
        setTenant("a/../b");

        assertThrows(ContentStoreException.class,
                () -> service.upload("documents/clients/1/file.txt", new ByteArrayInputStream(new byte[] { 1 }), "text/plain"));
    }

    @Test
    void uploadWithoutATenantIsRejectedWithAStoreException() {
        ThreadLocalContextUtil.clearTenant();

        assertThrows(ContentStoreException.class,
                () -> service.upload("documents/clients/1/file.txt", new ByteArrayInputStream(new byte[] { 1 }), "text/plain"));
        verify(s3Client, never()).putObject(any(Consumer.class), any(RequestBody.class));
    }

    @Test
    void downloadOfAPrefixedLocationWithoutATenantIsRejectedWithAStoreException() {
        ThreadLocalContextUtil.clearTenant();

        assertThrows(ContentStoreException.class, () -> service.download("tenants/tenant-a/documents/clients/1/file.txt"));
    }

    private String capturedPutKey() {
        final ArgumentCaptor<Consumer<PutObjectRequest.Builder>> captor = ArgumentCaptor.captor();
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        final var builder = PutObjectRequest.builder();
        captor.getValue().accept(builder);
        return builder.build().key();
    }

    private void stubGetObject() {
        doReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[] { 1 })).when(s3Client)
                .getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }

    private static void setTenant(final String identifier) {
        ThreadLocalContextUtil.setTenant(FineractPlatformTenant.builder().id(1L).name(identifier).tenantIdentifier(identifier).build());
    }
}
