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
package org.apache.fineract.interoperation.data;

import java.io.Serializable;

public class InteropKycData implements Serializable {

    private String nationality;
    private String dateOfBirth;
    private String contactPhone;
    private String gender;
    private String email;
    private String idType;
    private String idNo;
    private String description;
    private String country;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String displayName;

    public InteropKycData(String nationality, String dateOfBirth, String contactPhone, String gender, String email, String idType,
            String idNo, String description, String country, String addressLine1, String addressLine2, String city, String stateProvince,
            String postalCode, String firstName, String middleName, String lastName, String displayName) {
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.contactPhone = contactPhone;
        this.gender = gender;
        this.email = email;
        this.idType = idType;
        this.idNo = idNo;
        this.description = description;
        this.country = country;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.displayName = displayName;
    }

    public static InteropKycData instance(String nationality, String dateOfBirth, String contactPhone, String gender, String email,
            String idType, String idNo, String description, String country, String address_line_1, String address_line_2, String city,
            String stateProvince, String postalCode, String firstName, String middleName, String lastName, String displayName) {
        return new InteropKycData(nationality, dateOfBirth, contactPhone, gender, email, idType, idNo, description, country, address_line_1,
                address_line_2, city, stateProvince, postalCode, firstName, middleName, lastName, displayName);
    }

    public String getNationality() {
        return nationality;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdNo() {
        return idNo;
    }

    public String getDescription() {
        return description;
    }

    public String getCountry() {
        return country;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
