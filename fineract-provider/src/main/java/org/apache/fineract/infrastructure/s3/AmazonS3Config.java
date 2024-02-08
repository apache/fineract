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
package org.apache.fineract.infrastructure.s3;

import java.util.List;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.dataqueries.service.export.S3DatatableReportExportServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@Conditional(AmazonS3ConfigCondition.class)
public class AmazonS3Config {

    @Bean
    public DefaultCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public AwsRegionProvider awsRegionProvider() {
        return DefaultAwsRegionProviderChain.builder().build();
    }

    @Bean("s3Client")
    public S3Client s3Client(DefaultCredentialsProvider awsCredentialsProvider, AwsRegionProvider awsRegionProvider,
            List<S3ClientCustomizer> customizers) {
        S3ClientBuilder builder = S3Client.builder().credentialsProvider(awsCredentialsProvider).region(awsRegionProvider.getRegion());
        customizers.forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    @Bean
    @ConditionalOnBean(S3Client.class)
    public S3DatatableReportExportServiceImpl s3DatatableReportExportServiceImpl(ReadReportingService reportServiceImpl,
            ConfigurationDomainService configurationDomainService, S3Client s3Client, FineractProperties fineractProperties) {
        return new S3DatatableReportExportServiceImpl(reportServiceImpl, configurationDomainService, s3Client, fineractProperties);
    }

}
