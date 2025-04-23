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
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@FieldNameConstants
public class StaffRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "1")
    private Long officeId;
    @Schema(example = "John")
    private String firstname;
    @Schema(example = "Doe")
    private String lastname;
    @Schema(example = "true")
    private Boolean isLoanOfficer;
    @Schema(example = "17H")
    private String externalId;
    @Schema(example = "+353851239876")
    private String mobileNo;
    @Schema(example = "true")
    private Boolean isActive;
    @Schema(example = "01 January 2009")
    private String joiningDate;
    @Schema(example = "en")
    private String locale;
    @Schema(example = "dd MMMM yyyy")
    private String dateFormat;
    @Schema(example = "true")
    private Boolean forceStatus;
}
