package org.mifosplatform.portfolio.loanaccount.guarantor.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.guarantor.GuarantorConstants.GUARANTOR_JSON_INPUT_PARAMS;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_guarantor")
public class Guarantor extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "type_enum", nullable = false)
    private Integer gurantorType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "firstname", length = 50)
    private String firstname;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

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

    protected Guarantor() {

    }

    public static Guarantor createNew(Loan loan, Integer gurantorType, Long entityId, String firstname, String lastname, Date dateOfBirth,
            String addressLine1, String addressLine2, String city, String state, String country, String zip, String housePhoneNumber,
            String mobilePhoneNumber, String comment) {
        return new Guarantor(loan, gurantorType, entityId, firstname, lastname, dateOfBirth, addressLine1, addressLine2, city, state,
                country, zip, housePhoneNumber, mobilePhoneNumber, comment);
    }

    public Guarantor(Loan loan, Integer gurantorType, Long entityId, String firstname, String lastname, Date dateOfBirth,
            String addressLine1, String addressLine2, String city, String state, String country, String zip, String housePhoneNumber,
            String mobilePhoneNumber, String comment) {
        this.loan = loan;
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
    }

    public static Guarantor fromJson(final Loan loan, final JsonCommand command) {
        Integer gurantorType = command.integerValueSansLocaleOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.GUARANTOR_TYPE_ID.getValue());
        Long entityId = command.longValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.ENTITY_ID.getValue());

        if (GuarantorType.EXTERNAL.getValue().equals(gurantorType)) {
            String firstname = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.FIRSTNAME.getValue());
            String lastname = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.LASTNAME.getValue());
            Date dateOfBirth = command.DateValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.DATE_OF_BIRTH.getValue());
            String addressLine1 = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_1.getValue());
            String addressLine2 = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_2.getValue());
            String city = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.CITY.getValue());
            String state = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.STATE.getValue());
            String country = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.COUNTRY.getValue());
            String zip = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.ZIP.getValue());
            String housePhoneNumber = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.PHONE_NUMBER.getValue());
            String mobilePhoneNumber = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.MOBILE_NUMBER.getValue());
            String comment = command.stringValueOfParameterNamed(GUARANTOR_JSON_INPUT_PARAMS.COMMENT.getValue());

            return new Guarantor(loan, gurantorType, entityId, firstname, lastname, dateOfBirth, addressLine1, addressLine2, city, state,
                    country, zip, housePhoneNumber, mobilePhoneNumber, comment);
        }

        return new Guarantor(loan, gurantorType, entityId, null, null, null, null, null, null, null, null, null, null, null, null);

    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);

        handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.GUARANTOR_TYPE_ID.getValue(), this.gurantorType, true);
        handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.ENTITY_ID.getValue(), this.entityId);

        if (this.isExternalGuarantor()) {
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.FIRSTNAME.getValue(), this.firstname);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.LASTNAME.getValue(), this.lastname);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.DATE_OF_BIRTH.getValue(), this.dateOfBirth);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_1.getValue(), this.addressLine1);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_2.getValue(), this.addressLine2);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.CITY.getValue(), this.city);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.STATE.getValue(), this.state);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.COUNTRY.getValue(), this.country);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.ZIP.getValue(), this.zip);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.PHONE_NUMBER.getValue(), this.housePhoneNumber);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.MOBILE_NUMBER.getValue(), this.mobilePhoneNumber);
            handlePropertyUpdate(command, actualChanges, GUARANTOR_JSON_INPUT_PARAMS.COMMENT.getValue(), this.comment);
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
            Integer propertyToBeUpdated, boolean sansLocale) {
        if (command.isChangeInIntegerParameterNamed(paramName, propertyToBeUpdated)) {
            Integer newValue = null;
            if (sansLocale) {
                newValue = command.integerValueSansLocaleOfParameterNamed(paramName);
            } else {
                newValue = command.integerValueOfParameterNamed(paramName);
            }
            actualChanges.put(paramName, newValue);
            propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.GUARANTOR_TYPE_ID.getValue())) {
                this.gurantorType = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            String propertyToBeUpdated) {
        if (command.isChangeInStringParameterNamed(paramName, propertyToBeUpdated)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.FIRSTNAME.getValue())) {
                this.firstname = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.LASTNAME.getValue())) {
                this.lastname = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_1.getValue())) {
                this.addressLine1 = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.ADDRESS_LINE_2.getValue())) {
                this.addressLine2 = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.CITY.getValue())) {
                this.city = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.STATE.getValue())) {
                this.state = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.COUNTRY.getValue())) {
                this.country = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.ZIP.getValue())) {
                this.zip = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.PHONE_NUMBER.getValue())) {
                this.housePhoneNumber = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.MOBILE_NUMBER.getValue())) {
                this.mobilePhoneNumber = newValue;
            } else if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.COMMENT.getValue())) {
                this.comment = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            Long propertyToBeUpdated) {
        if (command.isChangeInLongParameterNamed(paramName, propertyToBeUpdated)) {
            final Long newValue = command.longValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.ENTITY_ID.getValue())) {
                this.entityId = newValue;
            }
        }
    }

    private void handlePropertyUpdate(final JsonCommand command, final Map<String, Object> actualChanges, final String paramName,
            Date propertyToBeUpdated) {
        if (command.isChangeInDateParameterNamed(paramName, propertyToBeUpdated)) {
            final Date newValue = command.DateValueOfParameterNamed(paramName);
            actualChanges.put(paramName, newValue);
            propertyToBeUpdated = newValue;

            // now update actual property
            if (paramName.equals(GUARANTOR_JSON_INPUT_PARAMS.DATE_OF_BIRTH.getValue())) {
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

}