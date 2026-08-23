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
package org.apache.fineract.portfolio.group.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class GroupCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{org.apache.fineract.portfolio.group.create.assertion.name-required}")
    @Size(max = 100, message = "{org.apache.fineract.portfolio.group.create.assertion.name-max-length}")
    private String name;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.group.create.assertion.external-id-max-length}")
    private String externalId;

    private Long centerId;

    @NotNull(message = "{org.apache.fineract.portfolio.group.create.assertion.office-id-required}")
    @Positive(message = "{org.apache.fineract.portfolio.group.create.assertion.office-id-positive}")
    private Long officeId;

    @Positive(message = "{org.apache.fineract.portfolio.group.create.assertion.staff-id-positive}")
    private Long staffId;

    @NotNull(message = "{org.apache.fineract.portfolio.group.create.assertion.active-required}")
    private Boolean active;

    private String activationDate;

    private String submittedOnDate;

    private Set<Long> clientMembers;

    private String locale;

    private String dateFormat;

    @Valid
    private List<DatatableEntry> datatables;

    @JsonIgnore
    @AssertTrue(message = "{org.apache.fineract.portfolio.group.create.assertion.activation-date-required-when-active}")
    public boolean isActivationDateValidWhenActive() {
        if (Boolean.TRUE.equals(active)) {
            return activationDate != null && !activationDate.isBlank();
        }
        return true;
    }
}
