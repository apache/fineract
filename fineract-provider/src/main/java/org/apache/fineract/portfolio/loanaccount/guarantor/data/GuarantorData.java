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
package org.apache.fineract.portfolio.loanaccount.guarantor.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorEnumerations;
import org.joda.time.LocalDate;

public class GuarantorData {

    private final Long id;
    private final Long loanId;
    private final CodeValueData clientRelationshipType;
    private final EnumOptionData guarantorType;

    private final String firstname;
    private final String lastname;

    /*** Fields for current customers/staff serving as guarantors **/
    private final Long entityId;
    private final String externalId;
    private final String officeName;
    private final LocalDate joinedDate;

    /*** Fields for external persons serving as guarantors ***/

    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String zip;
    private final String country;
    private final String mobileNumber;
    private final String housePhoneNumber;
    private final String comment;
    private final LocalDate dob;
    private final Collection<GuarantorFundingData> guarantorFundingDetails;
    private final boolean status;

    // template
    @SuppressWarnings("unused")
    private final List<EnumOptionData> guarantorTypeOptions;
    private final Collection<CodeValueData> allowedClientRelationshipTypes;
    private final Collection<PortfolioAccountData> accountLinkingOptions;

    //import fields
    private Integer guarantorTypeId;
    private Integer clientRelationshipTypeId;
    private Integer savingsId;
    private BigDecimal amount;
    private transient Long accountId;
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;

    public static GuarantorData importInstance(Integer guarantorTypeId,Integer clientRelationshipTypeId,Long entityId,String firstName,
            String lastName,String addressLine1,String addressLine2,String city,LocalDate dob, String zip,
            Integer savingsId, BigDecimal amount,Integer rowIndex,Long accountId,String locale,String dateFormat){
        return new GuarantorData(guarantorTypeId,clientRelationshipTypeId,entityId,firstName, lastName,
                addressLine1, addressLine2,city,dob,zip,savingsId,amount, rowIndex, accountId,locale,dateFormat);
    }
    private GuarantorData(Integer guarantorTypeId,Integer clientRelationshipTypeId,Long entityId,String firstname,
            String lastname,String addressLine1,String addressLine2,String city,LocalDate dob, String zip,
            Integer savingsId, BigDecimal amount,Integer rowIndex,Long accountId,String locale,String dateFormat) {
        this.rowIndex=rowIndex;
        this.firstname = firstname;
        this.lastname = lastname;
        this.entityId = entityId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.zip = zip;
        this.dob = dob;
        this.guarantorTypeId = guarantorTypeId;
        this.clientRelationshipTypeId = clientRelationshipTypeId;
        this.savingsId = savingsId;
        this.amount = amount;
        this.accountId = accountId;
        this.dateFormat=dateFormat;
        this.locale= locale;
        this.clientRelationshipType = null;
        this.guarantorType = null;
        this.id = null;
        this.loanId = null;
        this.externalId = null;
        this.officeName = null;
        this.joinedDate = null;
        this.state = null;
        this.country = null;
        this.mobileNumber = null;
        this.housePhoneNumber = null;
        this.comment = null;
        this.guarantorFundingDetails = null;
        this.status = false;
        this.guarantorTypeOptions = null;
        this.allowedClientRelationshipTypes = null;
        this.accountLinkingOptions = null;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public static GuarantorData template(final List<EnumOptionData> guarantorTypeOptions,
            final Collection<CodeValueData> allowedClientRelationshipTypes, Collection<PortfolioAccountData> accountLinkingOptions) {
        final Collection<GuarantorFundingData> guarantorFundingDetails = null;
        final boolean status = false;
        return new GuarantorData(null, null, null, null, GuarantorEnumerations.guarantorType(GuarantorType.CUSTOMER), null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, status, guarantorFundingDetails,
                guarantorTypeOptions, allowedClientRelationshipTypes, accountLinkingOptions);
    }

    public static GuarantorData templateOnTop(final GuarantorData guarantorData, final List<EnumOptionData> guarantorTypeOptions,
            final Collection<CodeValueData> allowedClientRelationshipTypes, Collection<PortfolioAccountData> accountLinkingOptions) {
        return new GuarantorData(guarantorData.id, guarantorData.loanId, guarantorData.clientRelationshipType, guarantorData.entityId,
                guarantorData.guarantorType, guarantorData.firstname, guarantorData.lastname, guarantorData.dob,
                guarantorData.addressLine1, guarantorData.addressLine2, guarantorData.city, guarantorData.state, guarantorData.zip,
                guarantorData.country, guarantorData.mobileNumber, guarantorData.housePhoneNumber, guarantorData.comment,
                guarantorData.officeName, guarantorData.joinedDate, guarantorData.externalId, guarantorData.status,
                guarantorData.guarantorFundingDetails, guarantorTypeOptions, allowedClientRelationshipTypes, accountLinkingOptions);
    }

    public static GuarantorData mergeClientData(final ClientData clientData, final GuarantorData guarantorData) {
        return new GuarantorData(guarantorData.id, guarantorData.loanId, guarantorData.clientRelationshipType, guarantorData.entityId,
                guarantorData.guarantorType, clientData.getFirstname(), clientData.getLastname(), null, null, null, null, null, null, null,
                null, null, null, clientData.officeName(), clientData.getActivationDate(), clientData.getExternalId(),
                guarantorData.status, guarantorData.guarantorFundingDetails, null, guarantorData.allowedClientRelationshipTypes,
                guarantorData.accountLinkingOptions);
    }

    public static GuarantorData mergeStaffData(final StaffData staffData, final GuarantorData guarantorData) {
        return new GuarantorData(guarantorData.id, guarantorData.loanId, guarantorData.clientRelationshipType, guarantorData.entityId,
                guarantorData.guarantorType, staffData.getFirstname(), staffData.getLastname(), null, null, null, null, null, null, null,
                null, null, null, staffData.getOfficeName(), null, null, guarantorData.status, guarantorData.guarantorFundingDetails, null,
                guarantorData.allowedClientRelationshipTypes, guarantorData.accountLinkingOptions);
    }

    public GuarantorData(final Long id, final Long loanId, final CodeValueData clientRelationshipType, final Long entityId,
            final EnumOptionData guarantorType, final String firstname, final String lastname, final LocalDate dob,
            final String addressLine1, final String addressLine2, final String city, final String state, final String zip,
            final String country, final String mobileNumber, final String housePhoneNumber, final String comment, final String officeName,
            final LocalDate joinedDate, final String externalId, final boolean status,
            Collection<GuarantorFundingData> guarantorFundingDetails, final List<EnumOptionData> guarantorTypeOptions,
            final Collection<CodeValueData> allowedClientRelationshipTypes, final Collection<PortfolioAccountData> accountLinkingOptions) {
        this.id = id;
        this.loanId = loanId;
        this.clientRelationshipType = clientRelationshipType;
        this.guarantorType = guarantorType;
        this.entityId = entityId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dob = dob;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.mobileNumber = mobileNumber;
        this.housePhoneNumber = housePhoneNumber;
        this.comment = comment;
        this.officeName = officeName;
        this.joinedDate = joinedDate;
        this.externalId = externalId;
        this.status = status;
        this.guarantorFundingDetails = guarantorFundingDetails;
        this.guarantorTypeOptions = guarantorTypeOptions;
        this.allowedClientRelationshipTypes = allowedClientRelationshipTypes;
        this.accountLinkingOptions = accountLinkingOptions;
    }

    public boolean isExternalGuarantor() {
        return GuarantorType.EXTERNAL.getValue().equals(this.guarantorType.getId().intValue());
    }

    public boolean isExistingClient() {
        return GuarantorType.CUSTOMER.getValue().equals(this.guarantorType.getId().intValue());
    }

    public boolean isStaffMember() {
        return GuarantorType.STAFF.getValue().equals(this.guarantorType.getId().intValue());
    }

    public Long getEntityId() {
        return this.entityId;
    }

}