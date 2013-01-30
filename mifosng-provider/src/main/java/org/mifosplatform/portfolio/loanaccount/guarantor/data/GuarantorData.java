package org.mifosplatform.portfolio.loanaccount.guarantor.data;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.mifosplatform.portfolio.loanaccount.guarantor.service.GuarantorEnumerations;

public class GuarantorData {

    private final Long id;
    private final Long loanId;
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

    // template
    @SuppressWarnings("unused")
    private final List<EnumOptionData> guarantorTypeOptions;

    public static GuarantorData template(final List<EnumOptionData> guarantorTypeOptions) {
        return new GuarantorData(null, null, null, GuarantorEnumerations.guarantorType(GuarantorType.CUSTOMER), null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, guarantorTypeOptions);
    }

    public static GuarantorData templateOnTop(final GuarantorData guarantorData, final List<EnumOptionData> guarantorTypeOptions) {
        return new GuarantorData(guarantorData.id, guarantorData.loanId, guarantorData.entityId, guarantorData.guarantorType,
                guarantorData.firstname, guarantorData.lastname, guarantorData.dob, guarantorData.addressLine1, guarantorData.addressLine2,
                guarantorData.city, guarantorData.state, guarantorData.zip, guarantorData.country, guarantorData.mobileNumber,
                guarantorData.housePhoneNumber, guarantorData.comment, guarantorData.officeName, guarantorData.joinedDate,
                guarantorData.externalId, guarantorTypeOptions);
    }

    public static GuarantorData mergeClientData(ClientData clientData, GuarantorData guarantorData) {
        return new GuarantorData(guarantorData.id, guarantorData.loanId, guarantorData.entityId, guarantorData.guarantorType,
                clientData.getFirstname(), clientData.getLastname(), null, null, null, null, null, null, null, null, null, null,
                clientData.getOfficeName(), clientData.getJoinedDate(), clientData.getExternalId(), null);
    }

    public static GuarantorData mergeStaffData(StaffData staffData, GuarantorData guarantorData) {
        return new GuarantorData(guarantorData.id, guarantorData.loanId, guarantorData.entityId, guarantorData.guarantorType,
                staffData.getFirstname(), staffData.getLastname(), null, null, null, null, null, null, null, null, null, null,
                staffData.getOfficeName(), null, null, null);
    }

    public GuarantorData(Long id, Long loanId, Long entityId, EnumOptionData guarantorType, String firstname, String lastname,
            LocalDate dob, String addressLine1, String addressLine2, String city, String state, String zip, String country,
            String mobileNumber, String housePhoneNumber, String comment, String officeName, LocalDate joinedDate, String externalId,
            List<EnumOptionData> guarantorTypeOptions) {
        this.id = id;
        this.loanId = loanId;
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
        this.guarantorTypeOptions = guarantorTypeOptions;
    }

    public boolean isExternalGuarantor() {
        return GuarantorType.EXTERNAL.getValue().equals(guarantorType.getId().intValue());
    }

    public boolean isExistingClient() {
        return GuarantorType.CUSTOMER.getValue().equals(guarantorType.getId().intValue());
    }

    public boolean isStaffMember() {
        return GuarantorType.STAFF.getValue().equals(guarantorType.getId().intValue());
    }

    public Long getEntityId() {
        return this.entityId;
    }

}