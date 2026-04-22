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
package org.apache.fineract.portfolio.search.data;

import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Getter
public class SearchData {

    private final Long entityId;
    private final String entityAccountNo;
    private final String entityExternalId;
    private final String entityName;
    private final String entityType;
    private final Long parentId;
    private final String parentName;
    private final String entityMobileNo;
    private final EnumOptionData entityStatus;
    private final String parentType;
    private final String subEntityType;

    public SearchData(final Long entityId, final String entityAccountNo, final String entityExternalId, final String entityName,
            final String entityType, final Long parentId, final String parentName, final String parentType, final String entityMobileNo,
            final EnumOptionData entityStatus, final String subEntityType) {

        this.entityId = entityId;
        this.entityAccountNo = entityAccountNo;
        this.entityExternalId = entityExternalId;
        this.entityName = entityName;
        this.entityType = entityType;
        this.parentId = parentId;
        this.parentName = parentName;
        this.parentType = parentType;
        this.entityMobileNo = entityMobileNo;
        this.entityStatus = entityStatus;
        this.subEntityType = subEntityType;
    }

}
