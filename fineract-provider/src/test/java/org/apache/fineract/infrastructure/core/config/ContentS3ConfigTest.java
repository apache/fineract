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

package org.apache.fineract.infrastructure.core.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
class ContentS3ConfigTest {

    @InjectMocks
    private ContentS3Config contentS3Config;

    @Mock
    private FineractProperties fineractProperties;

    @Mock
    private FineractProperties.FineractContentProperties contentProperties;

    @Mock
    private FineractProperties.FineractContentS3Properties s3Properties;

    @BeforeEach
    void setUp() {
        when(fineractProperties.getContent()).thenReturn(contentProperties);
        when(contentProperties.getS3()).thenReturn(s3Properties);
    }

    @AfterEach
    void tearDown() {
        reset(fineractProperties);
        reset(contentProperties);
        reset(s3Properties);
    }

    @Test
    void testContentS3Client_WithCredentialsAndNoEndpoint() {

        String accessKey = "test-access-key";
        String secretKey = "test-secret-key";
        String region = "us-east-1";

        when(s3Properties.getAccessKey()).thenReturn(accessKey);
        when(s3Properties.getSecretKey()).thenReturn(secretKey);
        when(s3Properties.getRegion()).thenReturn(region);
        when(s3Properties.getEndpoint()).thenReturn(null);

        S3Client s3Client = contentS3Config.contentS3Client(fineractProperties);

        assertNotNull(s3Client);

        verify(s3Properties, times(2)).getAccessKey();
        verify(s3Properties, times(2)).getSecretKey();
        verify(s3Properties, times(2)).getRegion();
        verify(s3Properties).getEndpoint();
    }

    @Test
    void testContentS3Client_WithNoCredentials() {

        String region = "us-west-2";

        when(s3Properties.getAccessKey()).thenReturn(null);
        when(s3Properties.getRegion()).thenReturn(region);
        when(s3Properties.getEndpoint()).thenReturn(null);

        S3Client s3Client = contentS3Config.contentS3Client(fineractProperties);

        assertNotNull(s3Client);

        verify(s3Properties, times(1)).getAccessKey();
        verify(s3Properties, times(0)).getSecretKey();
        verify(s3Properties, times(2)).getRegion();
        verify(s3Properties).getEndpoint();
    }

    @Test
    void testContentS3Client_WithCredentialsAndEndpoint() {

        String accessKey = "test-access-key";
        String secretKey = "test-secret-key";
        String region = "eu-west-1";
        String endpoint = "http://localhost:4566";
        boolean pathStyleAddressing = true;

        when(s3Properties.getAccessKey()).thenReturn(accessKey);
        when(s3Properties.getSecretKey()).thenReturn(secretKey);
        when(s3Properties.getRegion()).thenReturn(region);
        when(s3Properties.getEndpoint()).thenReturn(endpoint);
        when(s3Properties.getPathStyleAddressingEnabled()).thenReturn(pathStyleAddressing);

        S3Client s3Client = contentS3Config.contentS3Client(fineractProperties);

        assertNotNull(s3Client);

        verify(s3Properties, times(2)).getAccessKey();
        verify(s3Properties, times(2)).getSecretKey();
        verify(s3Properties, times(2)).getRegion();
        verify(s3Properties, times(2)).getEndpoint();
        verify(s3Properties).getPathStyleAddressingEnabled();
    }

    @Test
    void testContentS3Client_WithEmptyCredentials() {

        String accessKey = "";
        String region = "ap-southeast-1";

        when(s3Properties.getAccessKey()).thenReturn(accessKey);
        when(s3Properties.getEndpoint()).thenReturn(null);
        when(s3Properties.getRegion()).thenReturn(region);

        S3Client s3Client = contentS3Config.contentS3Client(fineractProperties);

        assertNotNull(s3Client);

        verify(s3Properties, times(1)).getAccessKey();
        verify(s3Properties, times(0)).getSecretKey();
        verify(s3Properties, times(2)).getRegion();
        verify(s3Properties).getEndpoint();
    }

    @Test
    void testContentS3Client_WithEmptyEndpoint() {

        String accessKey = "test-access-key";
        String secretKey = "test-secret-key";
        String region = "ca-central-1";
        String endpoint = "";

        when(s3Properties.getAccessKey()).thenReturn(accessKey);
        when(s3Properties.getSecretKey()).thenReturn(secretKey);
        when(s3Properties.getRegion()).thenReturn(region);
        when(s3Properties.getEndpoint()).thenReturn(endpoint);

        S3Client s3Client = contentS3Config.contentS3Client(fineractProperties);

        assertNotNull(s3Client);

        verify(s3Properties, times(2)).getAccessKey();
        verify(s3Properties, times(2)).getSecretKey();
        verify(s3Properties, times(2)).getRegion();
        verify(s3Properties).getEndpoint();

        verify(s3Properties, never()).getPathStyleAddressingEnabled();
    }

    @Test
    void testContentS3Client_WithOnlyAccessKey() {
        String accessKey = "test-access-key";
        String region = "sa-east-1";

        when(s3Properties.getAccessKey()).thenReturn(accessKey);
        when(s3Properties.getSecretKey()).thenReturn(null);
        when(s3Properties.getRegion()).thenReturn(region);
        when(s3Properties.getEndpoint()).thenReturn(null);

        S3Client s3Client = contentS3Config.contentS3Client(fineractProperties);

        assertNotNull(s3Client);

        verify(s3Properties, times(1)).getAccessKey();
        verify(s3Properties, times(1)).getSecretKey();
        verify(s3Properties, times(2)).getRegion();
        verify(s3Properties).getEndpoint();
    }
}
