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
package org.apache.fineract.portfolio.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long clientId;

    @NotBlank(message = "{org.apache.fineract.portfolio.client.familymember.firstname.not-blank}")
    @Length(max = 100, message = "{org.apache.fineract.portfolio.client.familymember.firstname.max}")
    private String firstName;

    @Length(max = 100, message = "{org.apache.fineract.portfolio.client.familymember.middlename.max}")
    private String middleName;

    @Length(max = 100, message = "{org.apache.fineract.portfolio.client.familymember.lastname.max}")
    private String lastName;

    @Length(max = 100, message = "{org.apache.fineract.portfolio.client.familymember.qualification.max}")
    private String qualification;

    @Length(max = 100, message = "{org.apache.fineract.portfolio.client.familymember.mobile-number.max}")
    private String mobileNumber;

    @Positive(message = "{org.apache.fineract.portfolio.client.familymember.age.positive}")
    private Long age;

    @JsonProperty("isDependent")
    private Boolean isDependent;

    @NotNull(message = "{org.apache.fineract.portfolio.client.familymember.relationship-id.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.client.familymember.relationship-id.positive}")
    private Long relationshipId;

    @Positive(message = "{org.apache.fineract.portfolio.client.familymember.marital-status-id.positive}")
    private Long maritalStatusId;

    @Positive(message = "{org.apache.fineract.portfolio.client.familymember.gender-id.positive}")
    private Long genderId;

    @Positive(message = "{org.apache.fineract.portfolio.client.familymember.profession-id.positive}")
    private Long professionId;

    // Kept as a String, parsed with dateFormat/locale in FamilyMemberCreateRequestMapper (like StaffCreateRequestMapper
    // does for joiningDate) so clients sending a formatted date don't hit a Jackson deserialization error. The
    // business-date check (must not be in the future) runs in FamilyMemberWriteServiceImpl after parsing, since it
    // needs to compare against the business date rather than the system clock.
    private String dateOfBirth;

    private String locale;
    private String dateFormat;
}
