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
package org.apache.fineract.portfolio.loanaccount.guarantor.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants.GuarantorJSONinputParams;

@Entity
@Table(name = "m_guarantor")
public class Guarantor extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "client_reln_cv_id", nullable = false)
    private CodeValue clientRelationshipType;

    @Column(name = "type_enum", nullable = false)
    private Integer gurantorType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "firstname", length = 50)
    private String firstname;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "address_line_1", length = 500)
    private String addressLine1;

    @Column(name = "address_line_2", length = 500)
    private String addressLine2;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "zip", length = 20)
    private String zip;

    @Column(name = "house_phone_number", length = 20)
    private String housePhoneNumber;

    @Column(name = "mobile_number", length = 20)
    private String mobilePhoneNumber;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "guarantor", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GuarantorFundingDetails> guarantorFundDetails = new ArrayList<>();

    protected Guarantor() {

    }

    private Guarantor(final Loan loan, final CodeValue clientRelationshipType, final Integer gurantorType, final Long entityId,
            final String firstname, final String lastname, final LocalDate dateOfBirth, final String addressLine1,
            final String addressLine2, final String city, final String state, final String country, final String zip,
            final String housePhoneNumber, final String mobilePhoneNumber, final String comment, final boolean active,
            final List<GuarantorFundingDetails> guarantorFundDetails) {
        this.loan = loan;
        this.clientRelationshipType = clientRelationshipType;
        this.gurantorType = gurantorType;
        this.entityId = entityId;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.dateOfBirth = dateOfBirth;
        this.addressLine1 = StringUtils.defaultIfEmpty(addressLine1, null);
        this.addressLine2 = StringUtils.defaultIfEmpty(addressLine2, null);
        this.city = StringUtils.defaultIfEmpty(city, null);
        this.state = StringUtils.defaultIfEmpty(state, null);
        this.country = StringUtils.defaultIfEmpty(country, null);
        this.zip = StringUtils.defaultIfEmpty(zip, null);
        this.housePhoneNumber = StringUtils.defaultIfEmpty(housePhoneNumber, null);
        this.mobilePhoneNumber = StringUtils.defaultIfEmpty(mobilePhoneNumber, null);
        this.comment = StringUtils.defaultIfEmpty(comment, null);
        this.active = active;
        this.guarantorFundDetails.addAll(guarantorFundDetails);
    }

    public static Guarantor fromJson(final Loan loan, final CodeValue clientRelationshipType, final JsonCommand command,
            final List<GuarantorFundingDetails> fundingDetails) {
        final Integer gurantorType = command.integerValueSansLocaleOfParameterNamed(GuarantorJSONinputParams.GUARANTOR_TYPE_ID.getValue());
        final Long entityId = command.longValueOfParameterNamed(GuarantorJSONinputParams.ENTITY_ID.getValue());
        final boolean active = true;
        if (GuarantorType.EXTERNAL.getValue().equals(gurantorType)) {
            final String firstname = command.stringValueOfParameterNamed(GuarantorJSONinputParams.FIRSTNAME.getValue());
            final String lastname = command.stringValueOfParameterNamed(GuarantorJSONinputParams.LASTNAME.getValue());
            final LocalDate dateOfBirth = command.localDateValueOfParameterNamed(GuarantorJSONinputParams.DATE_OF_BIRTH.getValue());
            final String addressLine1 = command.stringValueOfParameterNamed(GuarantorJSONinputParams.ADDRESS_LINE_1.getValue());
            final String addressLine2 = command.stringValueOfParameterNamed(GuarantorJSONinputParams.ADDRESS_LINE_2.getValue());
            final String city = command.stringValueOfParameterNamed(GuarantorJSONinputParams.CITY.getValue());
            final String state = command.stringValueOfParameterNamed(GuarantorJSONinputParams.STATE.getValue());
            final String country = command.stringValueOfParameterNamed(GuarantorJSONinputParams.COUNTRY.getValue());
            final String zip = command.stringValueOfParameterNamed(GuarantorJSONinputParams.ZIP.getValue());
            final String housePhoneNumber = command.stringValueOfParameterNamed(GuarantorJSONinputParams.PHONE_NUMBER.getValue());
            final String mobilePhoneNumber = command.stringValueOfParameterNamed(GuarantorJSONinputParams.MOBILE_NUMBER.getValue());
            final String comment = command.stringValueOfParameterNamed(GuarantorJSONinputParams.COMMENT.getValue());

            return new Guarantor(loan, clientRelationshipType, gurantorType, entityId, firstname, lastname, dateOfBirth, addressLine1,
                    addressLine2, city, state, country, zip, housePhoneNumber, mobilePhoneNumber, comment, active, fundingDetails);
        }

        return new Guarantor(loan, clientRelationshipType, gurantorType, entityId, null, null, null, null, null, null, null, null, null,
                null, null, null, active, fundingDetails);

    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.CLIENT_RELATIONSHIP_TYPE_ID.getValue(), 0, true);

        if (isExternalGuarantor()) {
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.FIRSTNAME.getValue(), this.firstname);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.LASTNAME.getValue(), this.lastname);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.DATE_OF_BIRTH.getValue(), this.dateOfBirth);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.ADDRESS_LINE_1.getValue(), this.addressLine1);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.ADDRESS_LINE_2.getValue(), this.addressLine2);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.CITY.getValue(), this.city);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.STATE.getValue(), this.state);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.COUNTRY.getValue(), this.country);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.ZIP.getValue(), this.zip);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.PHONE_NUMBER.getValue(), this.housePhoneNumber);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.MOBILE_NUMBER.getValue(), this.mobilePhoneNumber);
            handlePropertyUpdate(command, actualChanges, GuarantorJSONinputParams.COMMENT.getValue(), this.comment);
            updateExistingEntityToNull();
        }

        return actualChanges;
    }

    public boolean isExistingCustomer() {
        return GuarantorType.CUSTOMER.getValue().equals(this.gurantorType);
    }

    public boolean isExistingEmployee() {
        return GuarantorType.STAFF.getValue().equals(this.gurantorType);
    }

    public boolean isExternalGuarantor() {
        return GuarantorType.EXTERNAL.getValue().equals(this.gurantorType);
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            Integer propertyToBeUpdated, final boolean sansLocale) {
        if (command.isChangeInIntegerParameterNamed(paramName, propertyToBeUpdated)) {
            Integer newValue = null;
            if (sansLocale) {
                newValue = command.integerValueSansLocaleOfParameterNamed(paramName);
            } else {
                newValue = command.integerValueOfParameterNamed(paramName);
            }
            actualChanges.put(paramName, newValue);
            // propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(GuarantorJSONinputParams.GUARANTOR_TYPE_ID.getValue())) {
                this.gurantorType = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            String propertyToBeUpdated) {
        if (command.isChangeInStringParameterNamed(paramName, propertyToBeUpdated)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(GuarantorJSONinputParams.FIRSTNAME.getValue())) {
                this.firstname = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.LASTNAME.getValue())) {
                this.lastname = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.ADDRESS_LINE_1.getValue())) {
                this.addressLine1 = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.ADDRESS_LINE_2.getValue())) {
                this.addressLine2 = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.CITY.getValue())) {
                this.city = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.STATE.getValue())) {
                this.state = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.COUNTRY.getValue())) {
                this.country = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.ZIP.getValue())) {
                this.zip = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.PHONE_NUMBER.getValue())) {
                this.housePhoneNumber = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.MOBILE_NUMBER.getValue())) {
                this.mobilePhoneNumber = newValue;
            } else if (paramName.equals(GuarantorJSONinputParams.COMMENT.getValue())) {
                this.comment = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            LocalDate propertyToBeUpdated) {
        if (command.isChangeInDateParameterNamed(paramName, propertyToBeUpdated)) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            // propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(GuarantorJSONinputParams.DATE_OF_BIRTH.getValue())) {
                this.dateOfBirth = newValue;
            }
        }
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public Long getLoanId() {
        return this.loan.getId();
    }

    public Long getClientId() {
        return this.loan.getClientId();
    }

    public Long getOfficeId() {
        return this.loan.getOfficeId();
    }

    public CodeValue getClientRelationshipType() {
        return this.clientRelationshipType;
    }

    public void updateClientRelationshipType(final CodeValue clientRelationshipType) {
        this.clientRelationshipType = clientRelationshipType;
    }

    private void updateExistingEntityToNull() {
        this.entityId = null;
    }

    public Integer getGurantorType() {
        return this.gurantorType;
    }

    public boolean isActive() {
        return this.active;
    }

    public void updateStatus(final boolean status) {
        this.active = status;
    }

    public void addFundingDetails(final List<GuarantorFundingDetails> fundingDetails) {
        this.guarantorFundDetails.addAll(fundingDetails);
    }

    public void updateStatus(final GuarantorFundingDetails guarantorFundingDetails, final GuarantorFundStatusType fundStatusType) {
        guarantorFundingDetails.updateStatus(fundStatusType);
        updateStatus();
    }

    public GuarantorFundingDetails getGuarantorFundingDetail(final Long fundingDetailId) {
        GuarantorFundingDetails guarantorFundingDetails = null;
        for (GuarantorFundingDetails fundingDetails : this.guarantorFundDetails) {
            if (fundingDetails.getId().equals(fundingDetailId)) {
                guarantorFundingDetails = fundingDetails;
                break;
            }
        }
        return guarantorFundingDetails;
    }

    private void updateStatus() {
        boolean isActive = false;
        for (GuarantorFundingDetails guarantorFundingDetails : this.guarantorFundDetails) {
            if (guarantorFundingDetails.getStatus().isActive()) {
                isActive = true;
                break;
            }
        }
        this.active = isActive;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public List<GuarantorFundingDetails> getGuarantorFundDetails() {
        return this.guarantorFundDetails;
    }

    public boolean hasGuarantor(Long savingsId) {
        if (savingsId == null) {
            return false;
        }
        boolean hasGuarantee = false;
        for (GuarantorFundingDetails guarantorFundingDetails : this.guarantorFundDetails) {
            if (guarantorFundingDetails.getStatus().isActive()
                    && savingsId.equals(guarantorFundingDetails.getLinkedSavingsAccount().getId())) {
                hasGuarantee = true;
                break;
            }
        }
        return hasGuarantee;
    }

    public boolean isSelfGuarantee() {
        boolean isSelf = false;
        if (isExistingCustomer() && getEntityId().equals(getClientId())) {
            isSelf = true;
        }
        return isSelf;
    }
}
