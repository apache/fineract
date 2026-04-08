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

import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.condition.PropertiesCondition;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

public class AmazonS3ConfigCondition extends PropertiesCondition {

    @Override
    protected boolean matches(FineractProperties properties) {
        FineractProperties.FineractExportS3Properties s3ReportExportProperties = properties.getReport().getExport().getS3();
        return s3ReportExportProperties.getEnabled() && StringUtils.isNotBlank(s3ReportExportProperties.getBucketName())
                && isAwsCredentialValid();
    }

    private boolean isAwsCredentialValid() {
        try {
            // The credentials provider is intentionally not closed here since Spring will close it at the
            // context closure event
            DefaultCredentialsProvider.create().resolveCredentials();
            DefaultAwsRegionProviderChain.builder().build().getRegion();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
