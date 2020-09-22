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

import java.util.Map;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public class InteropKycResponseData extends CommandProcessingResult {

    private String nationality;
    private String dateOfBirth;
    private String contactPhone;
    private String gender;

    private IdDocument[] idDocument;

    private PostalAddress postalAddress;

    private SubjectName subjectName;

    private String emailAddress;
    private String birthCountry;

    public InteropKycResponseData(Long resourceId, Long officeId, Long commandId, Map<String, Object> changesOnly, String nationality,
            String dateOfBirth, String contactPhone, String gender, IdDocument[] idDocument, PostalAddress postalAddress,
            SubjectName subjectName, String emailAddress, String birthCountry) {
        super(resourceId, officeId, commandId, changesOnly);
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.contactPhone = contactPhone;
        this.gender = gender;
        this.idDocument = idDocument;
        this.postalAddress = postalAddress;
        this.subjectName = subjectName;
        this.emailAddress = emailAddress;
        this.birthCountry = birthCountry;
    }

    public InteropKycResponseData(String nationality, String dateOfBirth, String contactPhone, String gender, IdDocument[] idDocument,
            PostalAddress postalAddress, SubjectName subjectName, String emailAddress, String birthCountry) {
        this(null, null, null, null, nationality, dateOfBirth, contactPhone, gender, idDocument, postalAddress, subjectName, emailAddress,
                birthCountry);
    }

    public static InteropKycResponseData build(InteropKycData accountKyc) {

        PostalAddress postalAddress = new PostalAddress(accountKyc.getAddressLine1(), accountKyc.getAddressLine2(), accountKyc.getCity(),
                accountKyc.getStateProvince(), accountKyc.getPostalCode(), accountKyc.getCountry());
        IdDocument idDocument = new IdDocument(accountKyc.getIdType(), accountKyc.getIdNo(), accountKyc.getCountry(),
                accountKyc.getDescription());
        SubjectName subjectName = new SubjectName(accountKyc.getFirstName(), accountKyc.getMiddleName(), accountKyc.getLastName(),
                accountKyc.getDisplayName());

        return new InteropKycResponseData(accountKyc.getNationality(), accountKyc.getDateOfBirth(), accountKyc.getContactPhone(),
                accountKyc.getGender(), new IdDocument[] { idDocument }, postalAddress, subjectName, accountKyc.getEmail(),
                accountKyc.getCountry());
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public IdDocument[] getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(IdDocument[] idDocument) {
        this.idDocument = idDocument;
    }

    public PostalAddress getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(PostalAddress postalAddress) {
        this.postalAddress = postalAddress;
    }

    public SubjectName getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(SubjectName subjectName) {
        this.subjectName = subjectName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

}
