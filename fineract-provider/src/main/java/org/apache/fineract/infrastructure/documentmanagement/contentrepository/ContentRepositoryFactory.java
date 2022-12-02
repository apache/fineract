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
package org.apache.fineract.infrastructure.documentmanagement.contentrepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentRepositoryFactory {

    // TODO: all configuration should be really moved to application.properties
    private final ConfigurationDomainService configurationService;
    private final List<ContentRepository> contentRepositories;

    public ContentRepository getRepository() {
        if (configurationService.isAmazonS3Enabled()) {
            return getRepository(StorageType.S3);
        }
        return getRepository(StorageType.FILE_SYSTEM);
    }

    public ContentRepository getRepository(StorageType storageType) {
        return contentRepositories.stream().filter(cr -> cr.getStorageType().equals(storageType)).findFirst().orElseThrow();
    }
}
