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

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@Builder
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
public class ClientFamilyMemberRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String lastName;
    private String firstName;
    private String middleName;
    private Long clientId;
    private String dateFormat;
    private String mobileNumber;
    private Long genderId;
    private Boolean isDependent;
    private String dateOfBirth;
    private Long relationshipId;
    private String locale;
    private String familyMembers;
    private String qualification;
    private Long maritalStatusId;
    private Long id;
    private Long age;
    private Long professionId;
}
