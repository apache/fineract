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

import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable data object representing a user document being managed on the platform.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentData {

    private Long id;
    private String parentEntityType;
    private Long parentEntityId;
    private String name;
    private String fileName;
    private Long size;
    private String type;
    private String location;
    private String description;
    private Integer storageType;

    public StorageType storageType() {
        return StorageType.fromInt(this.storageType);
    }
}
