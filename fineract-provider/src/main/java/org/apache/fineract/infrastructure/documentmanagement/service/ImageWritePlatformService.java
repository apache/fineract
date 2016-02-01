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

import java.io.InputStream;

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ImageWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE','CREATE_STAFFIMAGE')")
    CommandProcessingResult saveOrUpdateImage(String entityName, Long entityId, String imageName, InputStream inputStream, Long fileSize);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CLIENTIMAGE','CREATE_STAFFIMAGE')")
    CommandProcessingResult saveOrUpdateImage(String entityName, Long entityId, Base64EncodedImage encodedImage);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_CLIENTIMAGE','DELETE_STAFFIMAGE')")
    CommandProcessingResult deleteImage(String entityName, Long entityId);
}