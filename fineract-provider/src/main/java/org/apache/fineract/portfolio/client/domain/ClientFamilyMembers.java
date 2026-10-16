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
package org.apache.fineract.portfolio.client.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Getter
@Setter
@Entity
@Table(name = "m_family_members")
public class ClientFamilyMembers extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "middlename")
    private String middleName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "age")
    private Long age;

    @Column(name = "is_dependent")
    private Boolean isDependent;

    @ManyToOne
    @JoinColumn(name = "relationship_cv_id")
    private CodeValue relationship;

    @ManyToOne
    @JoinColumn(name = "marital_status_cv_id")
    private CodeValue maritalStatus;

    @ManyToOne
    @JoinColumn(name = "gender_cv_id")
    private CodeValue gender;

    @ManyToOne
    @JoinColumn(name = "profession_cv_id")
    private CodeValue profession;

    @Column(name = "date_of_birth", nullable = true)
    private LocalDate dateOfBirth;

    public static ClientFamilyMembers fromJson(final Client client, final String firstName, final String middleName, final String lastName,
            final String qualification, final String mobileNumber, final Long age, final Boolean isDependent, final CodeValue relationship,
            final CodeValue maritalStatus, final CodeValue gender, final LocalDate dateOfBirth, final CodeValue profession) {
        ClientFamilyMembers entity = new ClientFamilyMembers();
        entity.setClient(client);
        entity.setFirstName(firstName);
        entity.setMiddleName(middleName);
        entity.setLastName(lastName);
        entity.setQualification(qualification);
        entity.setMobileNumber(mobileNumber);
        entity.setAge(age);
        entity.setIsDependent(isDependent);
        entity.setRelationship(relationship);
        entity.setMaritalStatus(maritalStatus);
        entity.setGender(gender);
        entity.setDateOfBirth(dateOfBirth);
        entity.setProfession(profession);
        return entity;
    }
}
