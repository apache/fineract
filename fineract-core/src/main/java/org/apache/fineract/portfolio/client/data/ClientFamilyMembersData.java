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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

@Getter
@Builder
public final class ClientFamilyMembersData implements Serializable {

    private final Long id;

    private final Long clientId;

    private final String firstName;

    private final String middleName;

    private final String lastName;

    private final String qualification;

    private final Long relationshipId;

    private final String relationship;

    private final Long maritalStatusId;

    private final String maritalStatus;

    private final Long genderId;

    private final String gender;

    private final LocalDate dateOfBirth;

    private final Long professionId;

    private final String profession;

    private final String mobileNumber;

    private final Long age;

    private final Boolean isDependent;

    // template holder
    private final Collection<CodeValueData> relationshipIdOptions;
    private final Collection<CodeValueData> genderIdOptions;
    private final Collection<CodeValueData> maritalStatusIdOptions;
    private final Collection<CodeValueData> professionIdOptions;

}
