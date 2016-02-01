/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.data;

import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.account.data.PortfolioAccountData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.mifosplatform.portfolio.loanaccount.guarantor.service.GuarantorEnumerations;

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