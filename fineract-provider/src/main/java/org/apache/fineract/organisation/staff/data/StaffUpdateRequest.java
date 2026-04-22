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
package org.apache.fineract.organisation.staff.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.organisation.staff.validation.StaffForceStatus;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@StaffForceStatus(message = "{org.apache.fineract.organisation.staff.force-id.staff-assigned}")
public class StaffUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long id;
    // @Min(value = 1, message = "{org.apache.fineract.organisation.staff.office-id.min}")
    // @NotNull(message = "{org.apache.fineract.organisation.staff.office-id.not-null}")
    private Long officeId;
    @Length(max = 50, message = "{org.apache.fineract.organisation.staff.firstname.max}")
    // @NotNull(message = "{org.apache.fineract.organisation.staff.firstname.not-null}")
    private String firstname;
    @Length(max = 50, message = "{org.apache.fineract.organisation.staff.lastname.max}")
    // @NotNull(message = "{org.apache.fineract.organisation.staff.lastname.not-null}")
    private String lastname;
    @JsonProperty("isLoanOfficer")
    private Boolean isLoanOfficer;
    @Length(max = 100, message = "{org.apache.fineract.organisation.staff.external-id.max}")
    // @NotBlank(message = "{org.apache.fineract.organisation.staff.external-id.not-blank}")
    private String externalId;
    @Length(max = 50, message = "{org.apache.fineract.organisation.staff.email.max}")
    private String emailAddress;
    @Length(max = 50, message = "{org.apache.fineract.organisation.staff.mobile-no.max}")
    // @NotBlank(message = "{org.apache.fineract.organisation.staff.mobile-no.not-blank}")
    private String mobileNo;
    @JsonProperty("isActive")
    private Boolean isActive;
    // @NotBlank(message = "{org.apache.fineract.organisation.staff.joining-date.not-blank}")
    private String joiningDate;
    private Boolean forceStatus;
}
