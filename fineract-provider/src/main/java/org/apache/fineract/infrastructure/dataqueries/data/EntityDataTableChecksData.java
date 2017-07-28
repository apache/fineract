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
package org.apache.fineract.infrastructure.dataqueries.data;

import java.io.Serializable;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object for role data.
 */
public class EntityDataTableChecksData implements Serializable {

    private final long id;
    private final String entity;
    private final EnumOptionData status;
    private final String datatableName;
    private final boolean systemDefined;
    private final Long order;
    private final Long productId;
    private final String productName;

    public EntityDataTableChecksData(final long id, final String entity, final EnumOptionData status, final String datatableName,
            final boolean systemDefined, final Long loanProductId, final String productName) {
        this.id = id;
        this.entity = entity;
        this.status = status;
        this.datatableName = datatableName;
        this.systemDefined = systemDefined;
        this.order = id;
        this.productId = loanProductId;
        this.productName = productName;
    }

}