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
package org.apache.fineract.infrastructure.event.business.domain;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BulkBusinessEvent extends AbstractBusinessEvent<List<BusinessEvent<?>>> {

    private static final String CATEGORY = "Bulk";
    public static final String TYPE = "BulkBusinessEvent";

    public BulkBusinessEvent(List<BusinessEvent<?>> value) {
        super(value);
        verifySameAggregate(value);
    }

    private void verifySameAggregate(List<BusinessEvent<?>> events) {
        Set<Long> aggregateRootIds = events.stream().map(BusinessEvent::getAggregateRootId).filter(Objects::nonNull).collect(toSet());
        if (aggregateRootIds.size() > 1) {
            throw new IllegalArgumentException("The business events are related to multiple aggregate roots which is not allowed");
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Long getAggregateRootId() {
        return get().iterator().next().getAggregateRootId();
    }
}
