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

import com.google.common.base.Strings;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Slf4j
@Configuration
public class ContentS3Config {

    @Bean
    @ConditionalOnProperty("fineract.content.s3.enabled")
    public S3Client contentS3Client(FineractProperties fineractProperties) {
        S3ClientBuilder builder = S3Client.builder().credentialsProvider(getCredentialProvider(fineractProperties.getContent().getS3()));

        if (!Strings.isNullOrEmpty(fineractProperties.getContent().getS3().getRegion())) {
            builder.region(Region.of(fineractProperties.getContent().getS3().getRegion()));
        }
        if (!Strings.isNullOrEmpty(fineractProperties.getContent().getS3().getEndpoint())) {
            builder.endpointOverride(URI.create(fineractProperties.getContent().getS3().getEndpoint()))
                    .forcePathStyle(fineractProperties.getContent().getS3().getPathStyleAddressingEnabled());
        }

        return builder.build();
    }

    private AwsCredentialsProvider getCredentialProvider(FineractProperties.FineractContentS3Properties s3Properties) {
        if (Strings.isNullOrEmpty(s3Properties.getAccessKey()) || Strings.isNullOrEmpty(s3Properties.getSecretKey())) {
            return DefaultCredentialsProvider.create();
        }

        return StaticCredentialsProvider.create(AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey()));
    }
}
