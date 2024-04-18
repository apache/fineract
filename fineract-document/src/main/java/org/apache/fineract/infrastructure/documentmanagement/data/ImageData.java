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
package org.apache.fineract.infrastructure.documentmanagement.data;

import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils.ImageMIMEtype;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;

public class ImageData {

    private final String location;
    private final StorageType storageType;
    private final String entityDisplayName;
    private final ContentRepositoryUtils.ImageMIMEtype contentType;

    public ImageData(final String location, final StorageType storageType, final String entityDisplayName) {
        this.location = location;
        this.storageType = storageType;
        this.entityDisplayName = entityDisplayName;
        this.contentType = ContentRepositoryUtils.ImageMIMEtype
                .fromFileExtension(ContentRepositoryUtils.imageExtensionFromFileName(location));
    }

    public ImageMIMEtype contentType() {
        return this.contentType;
    }

    public StorageType storageType() {
        return this.storageType;
    }

    public String location() {
        return this.location;
    }

    public String getEntityDisplayName() {
        return this.entityDisplayName;
    }
}
