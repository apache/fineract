package org.mifosng.platform.api.commands;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a Guarantor.
 */
public class GuarantorCommand {

    private final boolean externalGuarantor;

    /*** Fields for current customers serving as guarantors **/
    private final Long existingClientId;

    /*** Fields for external persons serving as guarantors ***/
    // FIXME - kw - what the details for collecting guarantors change? we are using datatable approach but fixing here?
    private final String firstname;
    private final String lastname;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String zip;
    private final String country;
    private final String mobileNumber;
    private final String housePhoneNumber;
    private final String comment;
    private final String dob;

    /** Feilds for passing Locale info to generic data service api **/
    private String dateFormat;
    private String locale;

    private final Set<String> modifiedParameters;

    public GuarantorCommand(final Set<String> modifiedParameters, final Long existingClientId, final String firstname,
            final String lastname, final Boolean externalGuarantor, final String addressLine1, final String addressLine2,
            final String city, final String state, final String zip, final String country, final String mobileNumber,
            final String housePhoneNumber, final String comment, final String dob) {
        this.modifiedParameters = modifiedParameters;

        this.externalGuarantor = externalGuarantor;

        /*** Fields for current customers serving as guarantors **/
        this.existingClientId = existingClientId;

        /*** Fields for external persons serving as guarantors ***/
        this.firstname = firstname;
        this.lastname = lastname;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.mobileNumber = mobileNumber;
        this.housePhoneNumber = housePhoneNumber;
        this.comment = comment;
        this.dob = dob;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public Boolean isExternalGuarantor() {
        return externalGuarantor;
    }

    public Long getExistingClientId() {
        return existingClientId;
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

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getCountry() {
        return country;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getHousePhoneNumber() {
        return housePhoneNumber;
    }

    public String getComment() {
        return comment;
    }

    public Set<String> getModifiedParameters() {
        return modifiedParameters;
    }

    public String getDob() {
        return dob;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

}