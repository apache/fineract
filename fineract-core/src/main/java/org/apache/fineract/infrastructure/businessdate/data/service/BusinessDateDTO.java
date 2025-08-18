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
package org.apache.fineract.infrastructure.businessdate.data.service;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String description;
    private BusinessDateType type;
    private LocalDate date;
    private Map<BusinessDateType, LocalDate> changes;

    public void addChange(final BusinessDateType businessDateType, final LocalDate date) {
        if (this.changes == null) {
            this.changes = new HashMap<>();
        }

        changes.put(businessDateType, date);
    }

    public void addAllChanges(final Map<BusinessDateType, LocalDate> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        for (final Map.Entry<BusinessDateType, LocalDate> entry : changes.entrySet()) {
            addChange(entry.getKey(), entry.getValue());
        }
    }
}
