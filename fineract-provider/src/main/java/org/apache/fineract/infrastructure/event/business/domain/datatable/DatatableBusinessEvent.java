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
package org.apache.fineract.infrastructure.event.business.domain.datatable;

import java.util.Map;
import lombok.Getter;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.event.business.domain.AbstractBusinessEvent;

@Getter
public abstract class DatatableBusinessEvent extends AbstractBusinessEvent<Map<String, Object>> {

    private static final String CATEGORY = "Datatable";

    private final EntityTables entityType;
    private final Long entityId;
    private final String datatableName;
    private final Long appTableId;

    public DatatableBusinessEvent(final Map<String, Object> value, final EntityTables entityType, final Long entityId,
            final String datatableName, final Long appTableId) {
        super(value);
        this.entityType = entityType;
        this.entityId = entityId;
        this.datatableName = datatableName;
        this.appTableId = appTableId;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Long getAggregateRootId() {
        return this.entityId;
    }
}
