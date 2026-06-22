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
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
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
public class GroupUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long groupId;

    @NotBlank(message = "{org.apache.fineract.portfolio.group.update.assertion.name-required}")
    @Size(max = 100, message = "{org.apache.fineract.portfolio.group.update.assertion.name-max-length}")
    private String name;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.group.update.assertion.external-id-max-length}")
    private String externalId;

    @Positive(message = "{org.apache.fineract.portfolio.group.update.assertion.office-id-positive}")
    private Long officeId;

    @Positive(message = "{org.apache.fineract.portfolio.group.update.assertion.staff-id-positive}")
    private Long staffId;

    private Long centerId;

    private Boolean active;

    private String activationDate;

    private String submittedOnDate;

    private String locale;

    private String dateFormat;

    @JsonIgnore
    @AssertTrue(message = "{org.apache.fineract.portfolio.group.update.assertion.activation-date-required-when-active-or-present}")
    public boolean isActivationDateValid() {
        if (Boolean.TRUE.equals(active)) {
            return activationDate != null && !activationDate.isBlank();
        }
        return true;
    }
}
