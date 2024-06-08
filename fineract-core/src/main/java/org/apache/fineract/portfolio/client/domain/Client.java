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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Getter
@Setter
@Table(name = "m_client", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "account_no_UNIQUE"), //
        @UniqueConstraint(columnNames = { "mobile_no" }, name = "mobile_no_UNIQUE") })
public class Client extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "transfer_to_office_id")
    private Office transferToOffice;

    @OneToOne(optional = true)
    @JoinColumn(name = "image_id")
    private Image image;

    /**
     * A value from {@link ClientStatus}.
     */
    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_status")
    private CodeValue subStatus;

    @Column(name = "activation_date")
    private LocalDate activationDate;

    @Column(name = "office_joining_date")
    private LocalDate officeJoiningDate;

    @Column(name = "firstname", length = 50)
    private String firstname;

    @Column(name = "middlename", length = 50)
    private String middlename;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "fullname", length = 160)
    private String fullname;

    @Column(name = "display_name", length = 160, nullable = false)
    private String displayName;

    @Column(name = "mobile_no", length = 50, unique = true)
    private String mobileNo;

    @Column(name = "email_address", length = 50, unique = true)
    private String emailAddress;

    @Column(name = "is_staff", nullable = false)
    private boolean isStaff;

    @Column(name = "external_id", length = 100, unique = true)
    private ExternalId externalId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_cv_id")
    private CodeValue gender;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "m_group_client", joinColumns = @JoinColumn(name = "client_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups;

    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closure_reason_cv_id")
    private CodeValue closureReason;

    @Column(name = "closedon_date")
    private LocalDate closureDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reject_reason_cv_id")
    private CodeValue rejectionReason;

    @Column(name = "rejectedon_date")
    private LocalDate rejectionDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rejectedon_userid")
    private AppUser rejectedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "withdraw_reason_cv_id")
    private CodeValue withdrawalReason;

    @Column(name = "withdrawn_on_date")
    private LocalDate withdrawalDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "withdraw_on_userid")
    private AppUser withdrawnBy;

    @Column(name = "reactivated_on_date")
    private LocalDate reactivateDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "reactivated_on_userid")
    private AppUser reactivatedBy;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "closedon_userid")
    private AppUser closedBy;

    @Column(name = "submittedon_date")
    private LocalDate submittedOnDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activatedon_userid")
    private AppUser activatedBy;

    @Column(name = "default_savings_product")
    private Long savingsProductId;

    @Column(name = "default_savings_account")
    private Long savingsAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_type_cv_id")
    private CodeValue clientType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_classification_cv_id")
    private CodeValue clientClassification;

    @Column(name = "legal_form_enum")
    private Integer legalForm;

    @Column(name = "reopened_on_date")
    private LocalDate reopenedDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "reopened_by_userid")
    private AppUser reopenedBy;

    @Column(name = "proposed_transfer_date")
    private LocalDate proposedTransferDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client", orphanRemoval = true, fetch = FetchType.LAZY)
    protected Set<ClientIdentifier> identifiers = new HashSet<>();

    public static Client instance(final AppUser currentUser, final ClientStatus status, final Office office, final Group clientParentGroup,
            final String accountNo, final String firstname, final String middlename, final String lastname, final String fullname,
            final LocalDate activationDate, final LocalDate officeJoiningDate, final ExternalId externalId, final String mobileNo,
            final String emailAddress, final Staff staff, final LocalDate submittedOnDate, final Long savingsProductId,
            final Long savingsAccountId, final LocalDate dateOfBirth, final CodeValue gender, final CodeValue clientType,
            final CodeValue clientClassification, final Integer legalForm, final Boolean isStaff) {
        return new Client(currentUser, status, office, clientParentGroup, accountNo, firstname, middlename, lastname, fullname,
                activationDate, officeJoiningDate, externalId, mobileNo, emailAddress, staff, submittedOnDate, savingsProductId,
                savingsAccountId, dateOfBirth, gender, clientType, clientClassification, legalForm, isStaff);
    }

    protected Client() {}

    private Client(final AppUser currentUser, final ClientStatus status, final Office office, final Group clientParentGroup,
            final String accountNo, final String firstname, final String middlename, final String lastname, final String fullname,
            final LocalDate activationDate, final LocalDate officeJoiningDate, final ExternalId externalId, final String mobileNo,
            final String emailAddress, final Staff staff, final LocalDate submittedOnDate, final Long savingsProductId,
            final Long savingsAccountId, final LocalDate dateOfBirth, final CodeValue gender, final CodeValue clientType,
            final CodeValue clientClassification, final Integer legalForm, final Boolean isStaff) {

        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }

        this.submittedOnDate = submittedOnDate;

        this.status = status.getValue();
        this.office = office;
        this.externalId = externalId;

        if (StringUtils.isNotBlank(mobileNo)) {
            this.mobileNo = mobileNo.trim();
        }

        if (StringUtils.isNotBlank(emailAddress)) {
            this.emailAddress = emailAddress.trim();
        }

        if (activationDate != null) {
            this.activationDate = activationDate;
            this.activatedBy = currentUser;
        }

        this.officeJoiningDate = officeJoiningDate;

        if (StringUtils.isNotBlank(firstname)) {
            this.firstname = firstname.trim();
        }

        if (StringUtils.isNotBlank(middlename)) {
            this.middlename = middlename.trim();
        }

        if (StringUtils.isNotBlank(lastname)) {
            this.lastname = lastname.trim();
        }

        if (StringUtils.isNotBlank(fullname)) {
            this.fullname = fullname.trim();
        }

        if (clientParentGroup != null) {
            this.groups = new HashSet<>();
            this.groups.add(clientParentGroup);
        }

        this.staff = staff;
        this.savingsProductId = savingsProductId;
        this.savingsAccountId = savingsAccountId;

        if (gender != null) {
            this.gender = gender;
        }

        this.dateOfBirth = dateOfBirth;

        this.clientType = clientType;
        this.clientClassification = clientClassification;
        this.setLegalForm(legalForm);

        deriveDisplayName();
        validate();
    }

    private void validate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        validateNameParts(dataValidationErrors);
        validateActivationDate(dataValidationErrors);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    public void validateUpdate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        // Not validating name parts while update request as firstname/lastname
        // can be along with fullname
        // when we change clientType from Individual to Organisation or
        // vice-cersa
        validateActivationDate(dataValidationErrors);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

    }

    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }

    public void setAccountNumberRequiresAutoGeneration(final boolean accountNumberRequiresAutoGeneration) {
        this.accountNumberRequiresAutoGeneration = accountNumberRequiresAutoGeneration;
    }

    public boolean identifiedBy(final Long clientId) {
        return getId().equals(clientId);
    }

    public void updateAccountNo(final String accountIdentifier) {
        this.accountNumber = accountIdentifier;
        this.accountNumberRequiresAutoGeneration = false;
    }

    public void activate(final AppUser currentUser, final DateTimeFormatter formatter, final LocalDate activationLocalDate) {

        if (isActive()) {
            final String defaultUserMessage = "Cannot activate client. Client is already active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.clients.already.active", defaultUserMessage,
                    ClientApiConstants.activationDateParamName, activationLocalDate.format(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        this.activationDate = activationLocalDate;
        this.activatedBy = currentUser;
        this.officeJoiningDate = this.activationDate;
        this.status = ClientStatus.ACTIVE.getValue();

        // in case a closed client is being re open
        this.closureDate = null;
        this.closureReason = null;
        this.closedBy = null;

        validate();
    }

    public boolean isNotActive() {
        return !isActive();
    }

    public boolean isActive() {
        return ClientStatus.fromInt(this.status).isActive();
    }

    public boolean isClosed() {
        return ClientStatus.fromInt(this.status).isClosed();
    }

    public boolean isTransferInProgress() {
        return ClientStatus.fromInt(this.status).isTransferInProgress();
    }

    public boolean isTransferOnHold() {
        return ClientStatus.fromInt(this.status).isTransferOnHold();
    }

    public boolean isTransferInProgressOrOnHold() {
        return isTransferInProgress() || isTransferOnHold();
    }

    public boolean isNotPending() {
        return !isPending();
    }

    public boolean isPending() {
        return ClientStatus.fromInt(this.status).isPending();
    }

    public boolean isRejected() {
        return ClientStatus.fromInt(this.status).isRejected();
    }

    public boolean isWithdrawn() {
        return ClientStatus.fromInt(this.status).isWithdrawn();
    }

    private void validateNameParts(final List<ApiParameterError> dataValidationErrors) {
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");

        if (StringUtils.isNotBlank(this.fullname)) {

            baseDataValidator.reset().parameter(ClientApiConstants.firstnameParamName).value(this.firstname)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.fullnameParamName, this.fullname);

            baseDataValidator.reset().parameter(ClientApiConstants.middlenameParamName).value(this.middlename)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.fullnameParamName, this.fullname);

            baseDataValidator.reset().parameter(ClientApiConstants.lastnameParamName).value(this.lastname)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.fullnameParamName, this.fullname);
        } else {

            baseDataValidator.reset().parameter(ClientApiConstants.firstnameParamName).value(this.firstname).notBlank()
                    .notExceedingLengthOf(50);
            baseDataValidator.reset().parameter(ClientApiConstants.middlenameParamName).value(this.middlename).ignoreIfNull()
                    .notExceedingLengthOf(50);
            baseDataValidator.reset().parameter(ClientApiConstants.lastnameParamName).value(this.lastname).notBlank()
                    .notExceedingLengthOf(50);
        }
    }

    private void validateActivationDate(final List<ApiParameterError> dataValidationErrors) {
        if (getSubmittedOnDate() != null && DateUtils.isDateInTheFuture(getSubmittedOnDate())) {
            final String defaultUserMessage = "submitted date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.clients.submittedOnDate.in.the.future",
                    defaultUserMessage, ClientApiConstants.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (getActivationDate() != null && DateUtils.isAfter(getSubmittedOnDate(), getActivationDate())) {
            final String defaultUserMessage = "submitted date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.clients.submittedOnDate.after.activation.date",
                    defaultUserMessage, ClientApiConstants.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }
        if (getActivationDate() != null && DateUtils.isAfter(getReopenedDate(), getActivationDate())) {
            final String defaultUserMessage = "reopened date cannot be after the submittedon date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.clients.submittedOnDate.after.reopened.date",
                    defaultUserMessage, ClientApiConstants.reopenedDateParamName, this.reopenedDate);

            dataValidationErrors.add(error);
        }
        if (DateUtils.isDateInTheFuture(getActivationDate())) {
            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.clients.activationDate.in.the.future",
                    defaultUserMessage, ClientApiConstants.activationDateParamName, getActivationDate());

            dataValidationErrors.add(error);
        }
        if (getActivationDate() != null) {
            if (this.office.isOpeningDateAfter(getActivationDate())) {
                final String defaultUserMessage = "Client activation date cannot be a date before the office opening date.";
                final ApiParameterError error = ApiParameterError.parameterError(
                        "error.msg.clients.activationDate.cannot.be.before.office.activation.date", defaultUserMessage,
                        ClientApiConstants.activationDateParamName, getActivationDate());
                dataValidationErrors.add(error);
            }
        }
    }

    public void deriveDisplayName() {
        if (StringUtils.isNotBlank(this.fullname)) {
            this.displayName = this.fullname;
        } else {
            StringBuilder nameBuilder = new StringBuilder();
            if (legalForm == null || LegalForm.fromInt(legalForm).isPerson()) {
                if (StringUtils.isNotBlank(this.firstname)) {
                    nameBuilder.append(this.firstname);
                }
                if (StringUtils.isNotBlank(this.middlename)) {
                    if (!nameBuilder.isEmpty()) {
                        nameBuilder.append(' ');
                    }
                    nameBuilder.append(this.middlename);
                }
                if (StringUtils.isNotBlank(this.lastname)) {
                    if (!nameBuilder.isEmpty()) {
                        nameBuilder.append(' ');
                    }
                    nameBuilder.append(this.lastname);
                }
            }
            this.displayName = nameBuilder.toString();
        }
    }

    public boolean isOfficeIdentifiedBy(final Long officeId) {
        return this.office.identifiedBy(officeId);
    }

    public Long officeId() {
        return this.office.getId();
    }

    public void setImage(final Image image) {
        this.image = image;
    }

    public String mobileNo() {
        return this.mobileNo;
    }

    public String emailAddress() {
        return this.emailAddress;
    }

    public void setMobileNo(final String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public boolean isNotStaff() {
        return !isStaff();
    }

    public boolean isStaff() {
        return this.isStaff;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void updateOffice(final Office office) {
        this.office = office;
    }

    public void updateTransferToOffice(final Office office) {
        this.transferToOffice = office;
    }

    public void updateOfficeJoiningDate(final LocalDate date) {
        this.officeJoiningDate = date;
    }

    public Long staffId() {
        Long staffId = null;
        if (this.staff != null) {
            staffId = this.staff.getId();
        }
        return staffId;
    }

    public void updateStaff(final Staff staff) {
        this.staff = staff;
    }

    public void unassignStaff() {
        this.staff = null;
    }

    public void assignStaff(final Staff staff) {
        this.staff = staff;
    }

    public void close(final AppUser currentUser, final CodeValue closureReason, final LocalDate closureDate) {
        this.closureReason = closureReason;
        this.closureDate = closureDate;
        this.closedBy = currentUser;
        this.status = ClientStatus.CLOSED.getValue();
    }

    public CodeValue subStatus() {
        return this.subStatus;
    }

    public Long subStatusId() {
        Long subStatusId = null;
        if (this.subStatus != null) {
            subStatusId = this.subStatus.getId();
        }
        return subStatusId;
    }

    public boolean isActivatedAfter(final LocalDate submittedOn) {
        return DateUtils.isAfter(getActivationDate(), submittedOn);
    }

    public boolean isChildOfGroup(final Long groupId) {
        if (groupId != null && this.groups != null && !this.groups.isEmpty()) {
            for (final Group group : this.groups) {
                if (group.getId().equals(groupId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Long savingsProductId() {
        return this.savingsProductId;
    }

    public void updateSavingsProduct(final Long savingsProductId) {
        this.savingsProductId = savingsProductId;
    }

    public AppUser activatedBy() {
        return this.activatedBy;
    }

    public Long savingsAccountId() {
        return this.savingsAccountId;
    }

    public void updateSavingsAccount(Long savingsAccountId) {
        this.savingsAccountId = savingsAccountId;
    }

    public Long genderId() {
        Long genderId = null;
        if (this.gender != null) {
            genderId = this.gender.getId();
        }
        return genderId;
    }

    public Long clientTypeId() {
        Long clientTypeId = null;
        if (this.clientType != null) {
            clientTypeId = this.clientType.getId();
        }
        return clientTypeId;
    }

    public Long clientClassificationId() {
        Long clientClassificationId = null;
        if (this.clientClassification != null) {
            clientClassificationId = this.clientClassification.getId();
        }
        return clientClassificationId;
    }

    public LocalDate getRejectedDate() {
        return this.rejectionDate;
    }

    public CodeValue gender() {
        return this.gender;
    }

    public CodeValue clientType() {
        return this.clientType;
    }

    public void updateClientType(CodeValue clientType) {
        this.clientType = clientType;
    }

    public CodeValue clientClassification() {
        return this.clientClassification;
    }

    public void updateClientClassification(CodeValue clientClassification) {
        this.clientClassification = clientClassification;
    }

    public void updateGender(CodeValue gender) {
        this.gender = gender;
    }

    public LocalDate dateOfBirth() {
        return this.dateOfBirth;
    }

    public LocalDate dateOfBirthLocalDate() {
        return this.dateOfBirth;
    }

    public void reject(AppUser currentUser, CodeValue rejectionReason, LocalDate rejectionDate) {
        this.rejectionReason = rejectionReason;
        this.rejectionDate = rejectionDate;
        this.rejectedBy = currentUser;
        this.status = ClientStatus.REJECTED.getValue();

    }

    public void withdraw(AppUser currentUser, CodeValue withdrawalReason, LocalDate withdrawalDate) {
        this.withdrawalReason = withdrawalReason;
        this.withdrawalDate = withdrawalDate;
        this.withdrawnBy = currentUser;
        this.status = ClientStatus.WITHDRAWN.getValue();

    }

    public void reActivate(AppUser currentUser, LocalDate reactivateDate) {
        this.closureDate = null;
        this.closureReason = null;
        this.reactivateDate = reactivateDate;
        this.reactivatedBy = currentUser;
        this.status = ClientStatus.PENDING.getValue();
    }

    public void reOpened(AppUser currentUser, LocalDate reopenedDate) {
        this.reopenedDate = reopenedDate;
        this.reopenedBy = currentUser;
        this.status = ClientStatus.PENDING.getValue();
    }

    public void setLegalForm(Integer legalForm) {
        this.legalForm = legalForm;
    }

    public void loadLazyCollections() {
        this.groups.size();
    }

    public LocalDate getProposedTransferDate() {
        return proposedTransferDate;
    }

    public void updateProposedTransferDate(LocalDate proposedTransferDate) {
        this.proposedTransferDate = proposedTransferDate;
    }

    public void resetDerivedNames(final LegalForm legalForm) {
        if (legalForm.isPerson()) {
            setFullname(null);
        } else if (legalForm.isEntity()) {
            setFirstname(null);
            setLastname(null);
            setDisplayName(null);
        }
    }
}
