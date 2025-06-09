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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "1")
    @NotNull(message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.officeId.NotNull}")
    private Long officeId;
    @Schema(example = "John")
    @NotBlank(message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.firstname.NotBlank}")
    @Size(max = 50, message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.firstname.Size}")
    private String firstname;
    @Schema(example = "Doe")
    @NotBlank(message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.lastname.NotBlank}")
    @Size(max = 50, message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.lastname.Size}")
    private String lastname;
    @Schema(example = "true")
    private Boolean isLoanOfficer;
    @Schema(example = "17H")
    @Size(max = 100, message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.externalId.Size}")
    private String externalId;
    @Schema(example = "+353851239876")
    @Size(max = 50, message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.mobileNo.Size}")
    private String mobileNo;
    @Schema(example = "true")
    private Boolean isActive;
    @Schema(example = "01 January 2009")
    @NotBlank(message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.joiningDate.NotBlank}")
    private String joiningDate;
    @Schema(example = "en")
    @NotBlank(message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.locale.NotBlank}")
    private String locale;
    @Schema(example = "dd MMMM yyyy")
    @NotBlank(message = "{org.apache.fineract.organisation.staff.data.CreateStaffRequest.dateFormat.NotBlank}")
    private String dateFormat;
    @Schema(example = "true")
    private Boolean forceStatus;
}
