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

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_family_members")
public class ClientFamilyMembers extends AbstractPersistableCustom {

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

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "address_type_id")
    private CodeValue addressType;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "address3")
    private String address3;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private CodeValue city;

    @ManyToOne
    @JoinColumn(name = "state_province_id")
    private CodeValue stateProvince;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CodeValue country;

    @Column(name = "postal_code")
    private String postalCode;

    private ClientFamilyMembers(final Client client, final String firstName, final String middleName, final String lastName,
            final String qualification, final String mobileNumber, final Long age, final Boolean isDependent, final CodeValue relationship,
            final CodeValue maritalStatus, final CodeValue gender, final LocalDate dateOfBirth, final CodeValue profession, String email,
            CodeValue addressType, String address1, String address2, String address3, CodeValue city, CodeValue stateProvince,
            CodeValue country, String postalCode) {

        this.client = client;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.qualification = qualification;
        this.age = age;
        this.mobileNumber = mobileNumber;
        this.isDependent = isDependent;
        this.relationship = relationship;
        this.maritalStatus = maritalStatus;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.profession = profession;
        this.email = email;
        this.addressType = addressType;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.city = city;
        this.stateProvince = stateProvince;
        this.country = country;
        this.postalCode = postalCode;
    }

    public ClientFamilyMembers() {

    }

    public static ClientFamilyMembers fromJson(final Client client, final String firstName, final String middleName, final String lastName,
            final String qualification, final String mobileNumber, final Long age, final Boolean isDependent, final CodeValue relationship,
            final CodeValue maritalStatus, final CodeValue gender, final LocalDate dateOfBirth, final CodeValue profession, String email,
            CodeValue addressType, String address1, String address2, String address3, CodeValue city, CodeValue stateProvince,
            CodeValue country, String postalCode) {
        return new ClientFamilyMembers(client, firstName, middleName, lastName, qualification, mobileNumber, age, isDependent, relationship,
                maritalStatus, gender, dateOfBirth, profession, email, addressType, address1, address2, address3, city, stateProvince,
                country, postalCode);
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getQualification() {
        return this.qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public CodeValue getRelationship() {
        return this.relationship;
    }

    public void setRelationship(CodeValue relationship) {
        this.relationship = relationship;
    }

    public CodeValue getMaritalStatus() {
        return this.maritalStatus;
    }

    public void setMaritalStatus(CodeValue maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public CodeValue getGender() {
        return this.gender;
    }

    public void setGender(CodeValue gender) {
        this.gender = gender;
    }

    public CodeValue getProfession() {
        return this.profession;
    }

    public void setProfession(CodeValue profession) {
        this.profession = profession;
    }

    public LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Long getAge() {
        return this.age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public Boolean getIsDependent() {
        return this.isDependent;
    }

    public void setIsDependent(Boolean isDependent) {
        this.isDependent = isDependent;
    }

    public Boolean getDependent() {
        return isDependent;
    }

    public void setDependent(Boolean dependent) {
        isDependent = dependent;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CodeValue getAddressType() {
        return addressType;
    }

    public void setAddressType(CodeValue addressType) {
        this.addressType = addressType;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public CodeValue getCity() {
        return city;
    }

    public void setCity(CodeValue city) {
        this.city = city;
    }

    public CodeValue getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(CodeValue stateProvince) {
        this.stateProvince = stateProvince;
    }

    public CodeValue getCountry() {
        return country;
    }

    public void setCountry(CodeValue country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
