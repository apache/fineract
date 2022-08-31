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
package org.apache.fineract.commands.service;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.self.pockets.api.PocketApiConstants;
import org.apache.fineract.useradministration.api.PasswordPreferencesApiConstants;

public class CommandWrapperBuilder {

    private Long officeId;
    private Long groupId;
    private Long clientId;
    private Long loanId;
    private Long savingsId;
    private String actionName;
    private String entityName;
    private Long entityId;
    private Long subentityId;
    private String href;
    private String json = "{}";
    private String transactionId;
    private Long productId;
    private Long templateId;
    private Long creditBureauId;
    private Long organisationCreditBureauId;

    public CommandWrapper build() {
        return new CommandWrapper(this.officeId, this.groupId, this.clientId, this.loanId, this.savingsId, this.actionName, this.entityName,
                this.entityId, this.subentityId, this.href, this.json, this.transactionId, this.productId, this.templateId,
                this.creditBureauId, this.organisationCreditBureauId);
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCreditBureau() {
        this.actionName = "UPDATE";
        this.entityName = "ORGANISATIONCREDITBUREAU";
        this.entityId = null;
        this.href = "/creditBureauConfiguration/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCreditBureauLoanProductMapping() {
        this.actionName = "UPDATE";
        this.entityName = "CREDITBUREAU_LOANPRODUCT_MAPPING";
        this.entityId = null;
        this.href = "/creditBureauConfiguration/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder addOrganisationCreditBureau(final long organisationCreditBureauId) {
        this.actionName = "CREATE";
        this.entityName = "ORGANISATIONCREDITBUREAU";
        this.entityId = organisationCreditBureauId;
        this.href = "/creditBureauConfiguration/organizationCreditBureau/template";
        this.organisationCreditBureauId = organisationCreditBureauId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder getCreditReport() {
        this.actionName = "GET";
        this.entityName = "CREDITREPORT";
        this.entityId = null;
        this.href = "/getCreditReport/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder saveCreditReport(final long creditBureauId, final String nationalId) {
        this.actionName = "SAVE";
        this.entityName = "CREDITREPORT";
        this.entityId = creditBureauId;
        this.transactionId = nationalId;
        this.href = "/saveCreditReport/";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCreditReport(final Long creditBureauId) {
        this.actionName = "DELETE";
        this.entityName = "CREDITREPORT";
        this.entityId = creditBureauId;
        this.href = "/deleteCreditReport/";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCreditBureauLoanProductMapping(final long organisationCreditBureauId) {
        this.actionName = "CREATE";
        this.entityName = "CREDITBUREAU_LOANPRODUCT_MAPPING";
        this.entityId = creditBureauId;
        this.href = "/creditBureauConfiguration/template";
        this.organisationCreditBureauId = organisationCreditBureauId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder addCreditBureauConfiguration(final long creditBureauId) {
        this.actionName = "CREATE";
        this.entityName = "CREDITBUREAU_CONFIGURATION";
        this.entityId = creditBureauId;
        this.href = "/addCreditBureauConfigurationData/";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCreditBureauConfiguration(final long configurationId) {
        this.actionName = "UPDATE";
        this.entityName = "CREDITBUREAU_CONFIGURATION";
        this.entityId = configurationId;
        this.href = "/updateCreditBureauConfigurationData/";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder addClientAddress(final long clientId, final long addressTypeId) {
        this.actionName = "CREATE";
        this.entityName = "ADDRESS";
        this.entityId = addressTypeId;
        this.href = "/clients/" + clientId + "/addresses";
        this.clientId = clientId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateClientAddress(final long clientId) {
        this.actionName = "UPDATE";
        this.entityName = "ADDRESS";
        this.href = "/clients/" + clientId + "/addresses";
        this.clientId = clientId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder addFamilyMembers(final long clientId) {
        this.actionName = "CREATE";
        this.entityName = "FAMILYMEMBERS";
        this.href = "/clients/" + clientId + "/familymembers";
        this.clientId = clientId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateFamilyMembers(final long familyMemberId) {
        this.actionName = "UPDATE";
        this.entityName = "FAMILYMEMBERS";
        this.href = "/clients/" + clientId + "/familymembers";
        this.entityId = familyMemberId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteFamilyMembers(final long familyMemberId) {
        this.actionName = "DELETE";
        this.entityName = "FAMILYMEMBERS";
        this.href = "/clients/" + clientId + "/familymembers";
        this.entityId = familyMemberId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withLoanId(final Long withLoanId) {
        this.loanId = withLoanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withSavingsId(final Long withSavingsId) {
        this.savingsId = withSavingsId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withClientId(final Long withClientId) {
        this.clientId = withClientId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withGroupId(final Long withGroupId) {
        this.groupId = withGroupId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withEntityName(final String withEntityName) {
        this.entityName = withEntityName;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withSubEntityId(final Long withSubEntityId) {
        this.subentityId = withSubEntityId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withJson(final String withJson) {
        this.json = withJson;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withNoJsonBody() {
        this.json = null;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateGlobalConfiguration(final Long configId) {
        this.actionName = "UPDATE";
        this.entityName = "CONFIGURATION";
        this.entityId = configId;

        this.href = "/configurations/" + configId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updatePermissions() {
        this.actionName = "UPDATE";
        this.entityName = "PERMISSION";
        this.entityId = null;
        this.href = "/permissions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createRole() {
        this.actionName = "CREATE";
        this.entityName = "ROLE";
        this.href = "/roles/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateRole(final Long roleId) {
        this.actionName = "UPDATE";
        this.entityName = "ROLE";
        this.entityId = roleId;
        this.href = "/roles/" + roleId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateRolePermissions(final Long roleId) {
        this.actionName = "PERMISSIONS";
        this.entityName = "ROLE";
        this.entityId = roleId;
        this.href = "/roles/" + roleId + "/permissions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createUser() {
        this.actionName = "CREATE";
        this.entityName = "USER";
        this.entityId = null;
        this.href = "/users/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateUser(final Long userId) {
        this.actionName = "UPDATE";
        this.entityName = "USER";
        this.entityId = userId;
        this.href = "/users/" + userId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteUser(final Long userId) {
        this.actionName = "DELETE";
        this.entityName = "USER";
        this.entityId = userId;
        this.href = "/users/" + userId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createOffice() {
        this.actionName = "CREATE";
        this.entityName = "OFFICE";
        this.entityId = null;
        this.href = "/offices/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateOffice(final Long officeId) {
        this.actionName = "UPDATE";
        this.entityName = "OFFICE";
        this.entityId = officeId;
        this.href = "/offices/" + officeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createOfficeTransaction() {
        this.actionName = "CREATE";
        this.entityName = "OFFICETRANSACTION";
        this.href = "/officetransactions/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteOfficeTransaction(final Long transactionId) {
        this.actionName = "DELETE";
        this.entityName = "OFFICETRANSACTION";
        this.entityId = transactionId;
        this.href = "/officetransactions/" + transactionId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createStaff() {
        this.actionName = "CREATE";
        this.entityName = "STAFF";
        this.entityId = null;
        this.href = "/staff/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateStaff(final Long staffId) {
        this.actionName = "UPDATE";
        this.entityName = "STAFF";
        this.entityId = staffId;
        this.href = "/staff/" + staffId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createGuarantor(final Long loanId) {
        this.actionName = "CREATE";
        this.entityName = "GUARANTOR";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder recoverFromGuarantor(final Long loanId) {
        this.actionName = "RECOVERGUARANTEES";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "?command=recoverGuarantees";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateGuarantor(final Long loanId, final Long guarantorId) {
        this.actionName = "UPDATE";
        this.entityName = "GUARANTOR";
        this.entityId = guarantorId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors/" + guarantorId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteGuarantor(final Long loanId, final Long guarantorId, final Long guarantorFundingId) {
        this.actionName = "DELETE";
        this.entityName = "GUARANTOR";
        this.entityId = guarantorId;
        this.subentityId = guarantorFundingId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors/" + guarantorId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createFund() {
        this.actionName = "CREATE";
        this.entityName = "FUND";
        this.entityId = null;
        this.href = "/funds/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateFund(final Long fundId) {
        this.actionName = "UPDATE";
        this.entityName = "FUND";
        this.entityId = fundId;
        this.href = "/funds/" + fundId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createReport() {
        this.actionName = "CREATE";
        this.entityName = "REPORT";
        this.entityId = null;
        this.href = "/reports/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateReport(final Long id) {
        this.actionName = "UPDATE";
        this.entityName = "REPORT";
        this.entityId = id;
        this.href = "/reports/" + id;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteReport(final Long id) {
        this.actionName = "DELETE";
        this.entityName = "REPORT";
        this.entityId = id;
        this.href = "/reports/" + id;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCurrencies() {
        this.actionName = "UPDATE";
        this.entityName = "CURRENCY";
        this.href = "/currencies";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createSms() {
        this.actionName = "CREATE";
        this.entityName = "SMS";
        this.entityId = null;
        this.href = "/sms/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateSms(final Long resourceId) {
        this.actionName = "UPDATE";
        this.entityName = "SMS";
        this.entityId = resourceId;
        this.href = "/sms/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteSms(final Long resourceId) {
        this.actionName = "DELETE";
        this.entityName = "SMS";
        this.entityId = resourceId;
        this.href = "/sms/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCode() {
        this.actionName = "CREATE";
        this.entityName = "CODE";
        this.entityId = null;
        this.href = "/codes/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCode(final Long codeId) {
        this.actionName = "UPDATE";
        this.entityName = "CODE";
        this.entityId = codeId;
        this.href = "/codes/" + codeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCode(final Long codeId) {
        this.actionName = "DELETE";
        this.entityName = "CODE";
        this.entityId = codeId;
        this.href = "/codes/" + codeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createHook() {
        this.actionName = "CREATE";
        this.entityName = "HOOK";
        this.entityId = null;
        this.href = "/hooks/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateHook(final Long hookId) {
        this.actionName = "UPDATE";
        this.entityName = "HOOK";
        this.entityId = hookId;
        this.href = "/hooks/" + hookId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteHook(final Long hookId) {
        this.actionName = "DELETE";
        this.entityName = "HOOK";
        this.entityId = hookId;
        this.href = "/hooks/" + hookId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCharge() {
        this.actionName = "CREATE";
        this.entityName = "CHARGE";
        this.entityId = null;
        this.href = "/charges/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCollateral() {
        this.actionName = "CREATE";
        this.entityId = null;
        this.entityName = "COLLATERAL_PRODUCT";
        this.href = "/collateral-product";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCharge(final Long chargeId) {
        this.actionName = "UPDATE";
        this.entityName = "CHARGE";
        this.entityId = chargeId;
        this.href = "/charges/" + chargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCharge(final Long chargeId) {
        this.actionName = "DELETE";
        this.entityName = "CHARGE";
        this.entityId = chargeId;
        this.href = "/charges/" + chargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createLoanProduct() {
        this.actionName = "CREATE";
        this.entityName = "LOANPRODUCT";
        this.entityId = null;
        this.href = "/loanproducts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateLoanProduct(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "LOANPRODUCT";
        this.entityId = productId;
        this.href = "/loanproducts/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createClientIdentifier(final Long clientId) {
        this.actionName = "CREATE";
        this.entityName = "CLIENTIDENTIFIER";
        this.entityId = null;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        this.actionName = "UPDATE";
        this.entityName = "CLIENTIDENTIFIER";
        this.entityId = clientIdentifierId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/" + clientIdentifierId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        this.actionName = "DELETE";
        this.entityName = "CLIENTIDENTIFIER";
        this.entityId = clientIdentifierId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/" + clientIdentifierId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createClient() {
        this.actionName = "CREATE";
        this.entityName = "CLIENT";
        this.href = "/clients/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder activateClient(final Long clientId) {
        this.actionName = "ACTIVATE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=activate&template=true";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeClient(final Long clientId) {
        this.actionName = "CLOSE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=close&template=true";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectClient(final Long clientId) {
        this.actionName = "REJECT";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=reject&template=true";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withdrawClient(final Long clientId) {
        this.actionName = "WITHDRAW";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=withdraw&template=true";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder reActivateClient(final Long clientId) {
        this.actionName = "REACTIVATE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=reactivate&template=true";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder proposeClientTransfer(final Long clientId) {
        this.actionName = "PROPOSETRANSFER";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=proposeTransfer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder proposeAndAcceptClientTransfer(final Long clientId) {
        this.actionName = "PROPOSEANDACCEPTTRANSFER";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=proposeAndAcceptTransfer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withdrawClientTransferRequest(final Long clientId) {
        this.actionName = "WITHDRAWTRANSFER";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=withdrawTransfer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder acceptClientTransfer(final Long clientId) {
        this.actionName = "ACCEPTTRANSFER";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=acceptTransfer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectClientTransfer(final Long clientId) {
        this.actionName = "REJECTTRANSFER";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=rejectTransfer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateClient(final Long clientId) {
        this.actionName = "UPDATE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteClient(final Long clientId) {
        this.actionName = "DELETE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId;
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createDBDatatable(final String json) {
        this.actionName = "CREATE";
        this.entityName = "DATATABLE";
        this.entityId = null;
        this.href = "/datatables/";
        this.json = json;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateDBDatatable(final String datatable, final String json) {
        this.actionName = "UPDATE";
        this.entityName = "DATATABLE";
        this.entityId = null;
        this.href = "/datatables/" + datatable;
        this.json = json;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteDBDatatable(final String datatable, final String json) {
        this.actionName = "DELETE";
        this.entityName = "DATATABLE";
        this.entityId = null;
        this.href = "/datatables/" + datatable;
        this.json = json;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoRejection(final Long clientId) {
        this.actionName = "UNDOREJECT";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=undoRejection";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoWithdrawal(final Long clientId) {
        this.actionName = "UNDOWITHDRAWAL";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=undoWithdrawal";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = "CREATE";
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = "UPDATE";
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = "DELETE";
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

    private void commonDatatableSettings(final String datatable, final Long apptableId, final Long datatableId) {

        this.entityName = datatable;
        this.entityId = apptableId;
        this.subentityId = datatableId;
        if (datatableId == null) {
            this.href = "/datatables/" + datatable + "/" + apptableId;
        } else {
            this.href = "/datatables/" + datatable + "/" + apptableId + "/" + datatableId;
        }
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createLoanCharge(final Long loanId) {
        this.actionName = "CREATE";
        this.entityName = "LOANCHARGE";
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = "UPDATE";
        this.entityName = "LOANCHARGE";
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder waiveLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = "WAIVE";
        this.entityName = "LOANCHARGE";
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder payLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = "PAY";
        this.entityName = "LOANCHARGE";
        this.entityId = loanChargeId;
        this.loanId = loanId;
        if (loanChargeId == null) {
            this.href = "/loans/" + loanId;
        } else {
            this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        }
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = "DELETE";
        this.entityName = "LOANCHARGE";
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder loanRepaymentTransaction(final Long loanId) {
        this.actionName = "REPAYMENT";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=repayment";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder loanMerchantIssuedRefundTransaction(final Long loanId) {
        this.actionName = "MERCHANTISSUEDREFUND";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=merchantissuedrefund";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder loanPayoutRefundTransaction(final Long loanId) {
        this.actionName = "PAYOUTREFUND";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=payoutrefund";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder loanGoodwillCreditTransaction(final Long loanId) {
        this.actionName = "GOODWILLCREDIT";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=goodwillcredit";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder refundLoanCharge(final Long loanId) {
        this.actionName = "CHARGEREFUND";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=chargerefund";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder loanRecoveryPaymentTransaction(final Long loanId) {
        this.actionName = "RECOVERYPAYMENT";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=recoverypayment";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder waiveInterestPortionTransaction(final Long loanId) {
        this.actionName = "WAIVEINTERESTPORTION";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=waiveinterest";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder writeOffLoanTransaction(final Long loanId) {
        this.actionName = "WRITEOFF";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=writeoff";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoWriteOffLoanTransaction(final Long loanId) {
        this.actionName = "UNDOWRITEOFF";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=undowriteoff";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeLoanAsRescheduledTransaction(final Long loanId) {
        this.actionName = "CLOSEASRESCHEDULED";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=close-rescheduled";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeLoanTransaction(final Long loanId) {
        this.actionName = "CLOSE";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder adjustTransaction(final Long loanId, final Long transactionId) {
        this.actionName = "ADJUST";
        this.entityName = "LOAN";
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/" + transactionId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder refundLoanTransactionByCash(final Long loanId) {
        this.actionName = "REFUNDBYCASH";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=refundbycash";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder loanForeclosure(final Long loanId) {
        this.actionName = "FORECLOSURE";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=foreclosure";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder creditBalanceRefund(final Long loanId) {
        this.actionName = "CREDITBALANCEREFUND";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=creditBalanceRefund";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoWaiveChargeTransaction(final Long loanId, final Long transactionId) {
        this.actionName = "UNDO";
        this.entityName = "WAIVECHARGE";
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=undo";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createLoanApplication() {
        this.actionName = "CREATE";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = null;
        this.href = "/loans";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updatePostDatedCheck(final Long id, final Long loanId) {
        this.actionName = "UPDATE";
        this.entityName = "REPAYMENT_WITH_POSTDATEDCHECKS";
        this.entityId = id;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/repaymentwithpostdatedchecks/" + id;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder bouncedCheck(final Long id, final Long loanId) {
        this.actionName = "BOUNCE";
        this.entityName = "REPAYMENT_WITH_POSTDATEDCHECKS";
        this.entityId = id;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/repaymentwithpostdatedchecks/" + id;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deletePostDatedCheck(final Long id, final Long loanId) {
        this.actionName = "DELETE";
        this.entityName = "REPAYMENT_WITH_POSTDATEDCHECKS";
        this.entityId = id;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/repaymentwithpostdatedchecks/" + id;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateLoanApplication(final Long loanId) {
        this.actionName = "UPDATE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateDisbusementDate(final Long loanId, final Long disbursementId) {
        this.actionName = "UPDATE";
        this.entityName = "DISBURSEMENTDETAIL";
        this.entityId = disbursementId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/disbursementdetail/" + disbursementId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder addAndDeleteDisbursementDetails(final Long loanId) {
        this.actionName = "UPDATE";
        this.entityName = "DISBURSEMENTDETAIL";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/editdisbursementdetails/";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteLoanApplication(final Long loanId) {
        this.actionName = "DELETE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectLoanApplication(final Long loanId) {
        this.actionName = "REJECT";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectGLIMApplication(final Long glimId) {
        this.actionName = "REJECT";
        this.entityName = "GLIMLOAN";
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withdrawLoanApplication(final Long loanId) {
        this.actionName = "WITHDRAW";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveLoanApplication(final Long loanId) {
        this.actionName = "APPROVE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveGLIMLoanApplication(final Long glimId) {
        this.actionName = "APPROVE";
        this.entityName = "GLIMLOAN";
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder disburseGlimLoanApplication(final Long glimId) {
        this.actionName = "DISBURSE";
        this.entityName = "GLIMLOAN";
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder repaymentGlimLoanApplication(final Long glimId) {
        this.actionName = "REPAYMENT";
        this.entityName = "GLIMLOAN";
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoGLIMLoanDisbursal(final Long glimId) {
        this.actionName = "UNDODISBURSAL";
        this.entityName = "GLIMLOAN";
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoGLIMLoanApproval(final Long glimId) {
        this.actionName = "UNDOAPPROVAL";
        this.entityName = "GLIMLOAN";
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder disburseLoanApplication(final Long loanId) {
        this.actionName = "DISBURSE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder disburseLoanToSavingsApplication(final Long loanId) {
        this.actionName = "DISBURSETOSAVINGS";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoLoanApplicationApproval(final Long loanId) {
        this.actionName = "APPROVALUNDO";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoLoanApplicationDisbursal(final Long loanId) {
        this.actionName = "DISBURSALUNDO";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoLastDisbursalLoanApplication(final Long loanId) {
        this.actionName = "DISBURSALLASTUNDO";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder assignLoanOfficer(final Long loanId) {
        this.actionName = "UPDATELOANOFFICER";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unassignLoanOfficer(final Long loanId) {
        this.actionName = "REMOVELOANOFFICER";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder assignLoanOfficersInBulk() {
        this.actionName = "BULKREASSIGN";
        this.entityName = "LOAN";
        this.href = "/loans/loanreassignment";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder assignDelinquency(final Long loanId) {
        this.actionName = "UPDATEDELINQUENCY";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCodeValue(final Long codeId) {
        this.actionName = "CREATE";
        this.entityName = "CODEVALUE";
        this.entityId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCodeValue(final Long codeId, final Long codeValueId) {
        this.actionName = "UPDATE";
        this.entityName = "CODEVALUE";
        this.subentityId = codeValueId;
        this.entityId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCodeValue(final Long codeId, final Long codeValueId) {
        this.actionName = "DELETE";
        this.entityName = "CODEVALUE";
        this.subentityId = codeValueId;
        this.entityId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createGLClosure() {
        this.actionName = "CREATE";
        this.entityName = "GLCLOSURE";
        this.entityId = null;
        this.href = "/glclosures/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateGLClosure(final Long glClosureId) {
        this.actionName = "UPDATE";
        this.entityName = "GLCLOSURE";
        this.entityId = glClosureId;
        this.href = "/glclosures/" + glClosureId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteGLClosure(final Long glClosureId) {
        this.actionName = "DELETE";
        this.entityName = "GLCLOSURE";
        this.entityId = glClosureId;
        this.href = "/glclosures/" + glClosureId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder excuteAccrualAccounting() {
        this.actionName = "EXECUTE";
        this.entityName = "PERIODICACCRUALACCOUNTING";
        this.entityId = null;
        this.href = "/accrualaccounting";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createGLAccount() {
        this.actionName = "CREATE";
        this.entityName = "GLACCOUNT";
        this.entityId = null;
        this.href = "/glaccounts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateGLAccount(final Long glAccountId) {
        this.actionName = "UPDATE";
        this.entityName = "GLACCOUNT";
        this.entityId = glAccountId;
        this.href = "/glaccounts/" + glAccountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteGLAccount(final Long glAccountId) {
        this.actionName = "DELETE";
        this.entityName = "GLACCOUNT";
        this.entityId = glAccountId;
        this.href = "/glaccounts/" + glAccountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createJournalEntry() {
        this.actionName = "CREATE";
        this.entityName = "JOURNALENTRY";
        this.entityId = null;
        this.href = "/journalentries/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder reverseJournalEntry(final String transactionId) {
        this.actionName = "REVERSE";
        this.entityName = "JOURNALENTRY";
        this.entityId = null;
        this.transactionId = transactionId;
        this.href = "/journalentries/" + transactionId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateRunningBalanceForJournalEntry() {
        this.actionName = "UPDATERUNNINGBALANCE";
        this.entityName = "JOURNALENTRY";
        this.entityId = null;
        this.href = "/journalentries/update";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder defineOpeningBalanceForJournalEntry() {
        this.actionName = "DEFINEOPENINGBALANCE";
        this.entityName = "JOURNALENTRY";
        this.entityId = null;
        this.href = "/journalentries/update";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateOpeningBalanceForJournalEntry() {
        this.actionName = "UPDATEOPENINGBALANCE";
        this.entityName = "JOURNALENTRY";
        this.entityId = null;
        this.href = "/journalentries/update";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createSavingProduct() {
        this.actionName = "CREATE";
        this.entityName = "SAVINGSPRODUCT";
        this.entityId = null;
        this.href = "/savingsproducts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateSavingProduct(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "SAVINGSPRODUCT";
        this.entityId = productId;
        this.href = "/savingsproducts/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteSavingProduct(final Long productId) {
        this.actionName = "DELETE";
        this.entityName = "SAVINGSPRODUCT";
        this.entityId = productId;
        this.href = "/savingsproducts/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createSavingsAccount() {
        this.actionName = "CREATE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = null;
        this.href = "/savingsaccounts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createGSIMAccount() {
        this.actionName = "CREATE";
        this.entityName = "GSIMACCOUNT";
        this.entityId = null;
        this.href = "/gsimaccounts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateSavingsAccount(final Long accountId) {
        this.actionName = "UPDATE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateGSIMAccount(final Long accountId) {
        this.actionName = "UPDATE";
        this.entityName = "GSIMACCOUNT";
        this.entityId = accountId;
        this.href = "/gsimaccounts/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteSavingsAccount(final Long accountId) {
        this.actionName = "DELETE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectSavingsAccountApplication(final Long accountId) {
        this.actionName = "REJECT";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=reject";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectGSIMAccountApplication(final Long accountId) {
        this.actionName = "REJECT";
        this.entityName = "GSIMACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=reject";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withdrawSavingsAccountApplication(final Long accountId) {
        this.actionName = "WITHDRAW";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=withdrawnByApplicant";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveSavingsAccountApplication(final Long accountId) {
        this.actionName = "APPROVE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=approve";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveGSIMAccountApplication(final Long accountId) {
        this.actionName = "APPROVE";
        this.entityName = "GSIMACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/gsimsaccounts/" + accountId + "?command=approve";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoSavingsAccountApplication(final Long accountId) {
        this.actionName = "APPROVALUNDO";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoGSIMApplicationApproval(final Long accountId) {
        this.actionName = "APPROVALUNDO";
        this.entityName = "GSIMACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder savingsAccountActivation(final Long accountId) {
        this.actionName = "ACTIVATE";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder gsimAccountActivation(final Long accountId) {
        this.actionName = "ACTIVATE";
        this.entityName = "GSIMACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeSavingsAccountApplication(final Long accountId) {
        this.actionName = "CLOSE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeGSIMApplication(final Long accountId) {
        this.actionName = "CLOSE";
        this.entityName = "GSIMACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createAccountTransfer() {
        this.actionName = "CREATE";
        this.entityName = "ACCOUNTTRANSFER";
        this.entityId = null;
        this.href = "/accounttransfers";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createStandingInstruction() {
        this.actionName = "CREATE";
        this.entityName = "STANDINGINSTRUCTION";
        this.entityId = null;
        this.href = "/standinginstructions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateStandingInstruction(final Long standingInstructionId) {
        this.actionName = "UPDATE";
        this.entityName = "STANDINGINSTRUCTION";
        this.entityId = standingInstructionId;
        this.href = "/standinginstructions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteStandingInstruction(final Long standingInstructionId) {
        this.actionName = "DELETE";
        this.entityName = "STANDINGINSTRUCTION";
        this.entityId = standingInstructionId;
        this.href = "/standinginstructions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder savingsAccountDeposit(final Long accountId) {
        this.actionName = "DEPOSIT";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder gsimSavingsAccountDeposit(final Long accountId) {
        this.actionName = "DEPOSIT";
        this.entityName = "GSIMACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder savingsAccountWithdrawal(final Long accountId) {
        this.actionName = "WITHDRAWAL";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoSavingsAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = "UNDOTRANSACTION";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=undo";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder reverseSavingsAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = "REVERSETRANSACTION";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=reverse";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder adjustSavingsAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = "ADJUSTTRANSACTION";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=modify";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder savingsAccountInterestCalculation(final Long accountId) {
        this.actionName = "CALCULATEINTEREST";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=calculateInterest";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder savingsAccountInterestPosting(final Long accountId) {
        this.actionName = "POSTINTEREST";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=postInterest";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder savingsAccountApplyAnnualFees(final Long accountId) {
        this.actionName = "APPLYANNUALFEE";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=applyAnnualFees";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createSavingsAccountCharge(final Long savingsAccountId) {
        this.actionName = "CREATE";
        this.entityName = "SAVINGSACCOUNTCHARGE";
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = "UPDATE";
        this.entityName = "SAVINGSACCOUNTCHARGE";
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder waiveSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = "WAIVE";
        this.entityName = "SAVINGSACCOUNTCHARGE";
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;

    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder paySavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = "PAY";
        this.entityName = "SAVINGSACCOUNTCHARGE";
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;

    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder inactivateSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = "INACTIVATE";
        this.entityName = "SAVINGSACCOUNTCHARGE";
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;

    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = "DELETE";
        this.entityName = "SAVINGSACCOUNTCHARGE";
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createFixedDepositProduct() {
        this.actionName = "CREATE";
        this.entityName = "FIXEDDEPOSITPRODUCT";
        this.entityId = null;
        this.href = "/fixeddepositproducts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateFixedDepositProduct(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "FIXEDDEPOSITPRODUCT";
        this.entityId = productId;
        this.href = "/fixeddepositproducts/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteFixedDepositProduct(final Long productId) {
        this.actionName = "DELETE";
        this.entityName = "FIXEDDEPOSITPRODUCT";
        this.entityId = productId;
        this.href = "/fixeddepositproducts/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createRecurringDepositProduct() {
        this.actionName = "CREATE";
        this.entityName = "RECURRINGDEPOSITPRODUCT";
        this.entityId = null;
        this.href = "/recurringdepositproducts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateRecurringDepositProduct(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "RECURRINGDEPOSITPRODUCT";
        this.entityId = productId;
        this.href = "/recurringdepositproducts/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteRecurringDepositProduct(final Long productId) {
        this.actionName = "DELETE";
        this.entityName = "RECURRINGDEPOSITPRODUCT";
        this.entityId = productId;
        this.href = "/recurringdepositproducts/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createInterestRateChart() {
        this.actionName = "CREATE";
        this.entityName = "INTERESTRATECHART";
        this.entityId = null;
        this.href = "/interestratechart/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateInterestRateChart(final Long interestRateChartId) {
        this.actionName = "UPDATE";
        this.entityName = "INTERESTRATECHART";
        this.entityId = interestRateChartId;
        this.href = "/interestratechart/" + interestRateChartId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteInterestRateChart(final Long interestRateChartId) {
        this.actionName = "DELETE";
        this.entityName = "INTERESTRATECHART";
        this.entityId = interestRateChartId;
        this.href = "/interestratechart/" + interestRateChartId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createInterestRateChartSlab(final Long chartId) {
        this.actionName = "CREATE";
        this.entityName = "CHARTSLAB";
        this.entityId = null;
        this.subentityId = chartId; // refer to chart id
        this.href = "/interestratechart/" + chartId + "/chartdetails/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateInterestRateChartSlab(final Long chartId, final Long chartSlabId) {
        this.actionName = "UPDATE";
        this.entityName = "CHARTSLAB";
        this.entityId = chartSlabId;
        this.subentityId = chartId;// refers parent chart
        this.href = "/interestratechart/" + chartId + "/chartdetails/" + chartSlabId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteInterestRateChartSlab(final Long chartId, final Long chartSlabId) {
        this.actionName = "DELETE";
        this.entityName = "CHARTSLAB";
        this.entityId = chartSlabId;
        this.subentityId = chartId;// refers parent chart
        this.href = "/interestratechart/" + chartId + "/chartdetails/" + chartSlabId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCalendar(final CommandWrapper resourceDetails, final String supportedEntityType,
            final Long supportedEntityId) {
        this.actionName = "CREATE";
        this.entityName = "CALENDAR";
        this.clientId = resourceDetails.getClientId();
        this.loanId = resourceDetails.getLoanId();
        this.groupId = resourceDetails.getGroupId();
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCalendar(final String supportedEntityType, final Long supportedEntityId, final Long calendarId) {
        this.actionName = "UPDATE";
        this.entityName = "CALENDAR";
        this.entityId = calendarId;
        this.groupId = supportedEntityId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/" + calendarId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCalendar(final String supportedEntityType, final Long supportedEntityId, final Long calendarId) {
        this.actionName = "DELETE";
        this.entityName = "CALENDAR";
        this.entityId = calendarId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/" + calendarId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createNote(final CommandWrapper resourceDetails, final String resourceType, final Long resourceId) {
        this.actionName = "CREATE";
        this.entityName = resourceDetails.entityName();// Note supports multiple
                                                       // resources. Note
                                                       // Permissions are set
                                                       // for each resource.
        this.clientId = resourceDetails.getClientId();
        this.loanId = resourceDetails.getLoanId();
        this.savingsId = resourceDetails.getSavingsId();
        this.groupId = resourceDetails.getGroupId();
        this.subentityId = resourceDetails.subresourceId();
        this.href = "/" + resourceType + "/" + resourceId + "/notes/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateNote(final CommandWrapper resourceDetails, final String resourceType, final Long resourceId,
            final Long noteId) {
        this.actionName = "UPDATE";
        this.entityName = resourceDetails.entityName();// Note supports multiple
                                                       // resources. Note
                                                       // Permissions are set
                                                       // for each resource.
        this.entityId = noteId;
        this.clientId = resourceDetails.getClientId();
        this.loanId = resourceDetails.getLoanId();
        this.savingsId = resourceDetails.getSavingsId();
        this.groupId = resourceDetails.getGroupId();
        this.subentityId = resourceDetails.subresourceId();
        this.href = "/" + resourceType + "/" + resourceId + "/notes";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteNote(final CommandWrapper resourceDetails, final String resourceType, final Long resourceId,
            final Long noteId) {
        this.actionName = "DELETE";
        this.entityName = resourceDetails.entityName();// Note supports multiple
                                                       // resources. Note
                                                       // Permissions are set
                                                       // for each resource.
        this.entityId = noteId;
        this.clientId = resourceDetails.getClientId();
        this.loanId = resourceDetails.getLoanId();
        this.savingsId = resourceDetails.getSavingsId();
        this.groupId = resourceDetails.getGroupId();
        this.subentityId = resourceDetails.subresourceId();
        this.href = "/" + resourceType + "/" + resourceId + "/calendars/" + noteId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createGroup() {
        this.actionName = "CREATE";
        this.entityName = "GROUP";
        this.href = "/groups/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateGroup(final Long groupId) {
        this.actionName = "UPDATE";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder activateGroup(final Long groupId) {
        this.actionName = "ACTIVATE";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder saveGroupCollectionSheet(final Long groupId) {
        this.actionName = "SAVECOLLECTIONSHEET";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=saveCollectionSheet";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder saveIndividualCollectionSheet() {
        this.actionName = "SAVE";
        this.entityName = "COLLECTIONSHEET";
        this.href = "/collectionsheet?command=saveCollectionSheet";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteGroup(final Long groupId) {
        this.actionName = "DELETE";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder associateClientsToGroup(final Long groupId) {
        this.actionName = "ASSOCIATECLIENTS";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=associateClients";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder disassociateClientsFromGroup(final Long groupId) {
        this.actionName = "DISASSOCIATECLIENTS";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=disassociateClients";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder transferClientsBetweenGroups(final Long sourceGroupId) {
        this.actionName = "TRANSFERCLIENTS";
        this.entityName = "GROUP";
        this.entityId = sourceGroupId;
        this.groupId = sourceGroupId;
        this.href = "/groups/" + sourceGroupId + "?command=transferClients";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unassignGroupStaff(final Long groupId) {
        this.actionName = "UNASSIGNSTAFF";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder assignGroupStaff(final Long groupId) {
        this.actionName = "ASSIGNSTAFF";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=assignStaff";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeGroup(final Long groupId) {
        this.actionName = "CLOSE";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCollateral(final Long loanId) {
        this.actionName = "CREATE";
        this.entityName = "COLLATERAL";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCollateral(final Long loanId, final Long collateralId) {
        this.actionName = "UPDATE";
        this.entityName = "COLLATERAL";
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/" + collateralId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCollateralProduct(final Long collateralId) {
        this.actionName = "UPDATE";
        this.entityName = "COLLATERAL_PRODUCT";
        this.entityId = collateralId;
        this.href = "/collateral-management/" + collateralId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateClientCollateralProduct(final Long clientId, final Long collateralId) {
        this.actionName = "UPDATE";
        this.entityName = "CLIENT_COLLATERAL_PRODUCT";
        this.entityId = collateralId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/collateral/" + collateralId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteLoanCollateral(final Long loanId, final Long collateralId) {
        this.actionName = "DELETE";
        this.entityName = "LOAN_COLLATERAL_PRODUCT";
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collateral/" + collateralId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCollateral(final Long loanId, final Long collateralId) {
        this.actionName = "DELETE";
        this.entityName = "COLLATERAL";
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/" + collateralId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCollateralProduct(final Long collateralId) {
        this.actionName = "DELETE";
        this.entityName = "COLLATERAL_PRODUCT";
        this.entityId = collateralId;
        this.href = "/collateral-management/" + collateralId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteClientCollateralProduct(final Long collateralId, final Long clientId) {
        this.actionName = "DELETE";
        this.entityName = "CLIENT_COLLATERAL_PRODUCT";
        this.entityId = collateralId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/collateral-management/" + collateralId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder addClientCollateralProduct(final Long clientId) {
        this.actionName = "CREATE";
        this.entityName = "CLIENT_COLLATERAL_PRODUCT";
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/collateral-management";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCollectionSheet(final Long groupId) {
        this.actionName = "UPDATE";
        this.entityName = "COLLECTIONSHEET";
        this.entityId = groupId;
        this.href = "/groups/" + groupId + "/collectionsheet";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createCenter() {
        this.actionName = "CREATE";
        this.entityName = "CENTER";
        this.href = "/centers/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCenter(final Long centerId) {
        this.actionName = "UPDATE";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.href = "/centers/" + centerId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteCenter(final Long centerId) {
        this.actionName = "DELETE";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.href = "/centers/" + centerId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder activateCenter(final Long centerId) {
        this.actionName = "ACTIVATE";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/centers/" + centerId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder saveCenterCollectionSheet(final Long centerId) {
        this.actionName = "SAVECOLLECTIONSHEET";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/centers/" + centerId + "?command=saveCollectionSheet";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeCenter(final Long centerId) {
        this.actionName = "CLOSE";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/centers/" + centerId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder associateGroupsToCenter(final Long centerId) {
        this.actionName = "ASSOCIATEGROUPS";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/groups/" + centerId + "?command=associateGroups";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder disassociateGroupsFromCenter(final Long centerId) {
        this.actionName = "DISASSOCIATEGROUPS";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/groups/" + centerId + "?command=disassociateGroups";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createAccountingRule() {
        this.actionName = "CREATE";
        this.entityName = "ACCOUNTINGRULE";
        this.entityId = null;
        this.href = "/accountingrules/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateAccountingRule(final Long accountingRuleId) {
        this.actionName = "UPDATE";
        this.entityName = "ACCOUNTINGRULE";
        this.entityId = accountingRuleId;
        this.href = "/accountingrules/" + accountingRuleId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteAccountingRule(final Long accountingRuleId) {
        this.actionName = "DELETE";
        this.entityName = "ACCOUNTINGRULE";
        this.entityId = accountingRuleId;
        this.href = "/accountingrules/" + accountingRuleId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateTaxonomyMapping(final Long mappingId) {
        this.actionName = "UPDATE";
        this.entityName = "XBRLMAPPING";
        this.entityId = mappingId;
        this.href = "/xbrlmapping";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createHoliday() {
        this.actionName = "CREATE";
        this.entityName = "HOLIDAY";
        this.entityId = null;
        this.href = "/holidays/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder activateHoliday(final Long holidayId) {
        this.actionName = "ACTIVATE";
        this.entityName = "HOLIDAY";
        this.entityId = holidayId;
        this.href = "/holidays/" + holidayId + "command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateHoliday(final Long holidayId) {
        this.actionName = "UPDATE";
        this.entityName = "HOLIDAY";
        this.entityId = holidayId;
        this.href = "/holidays/" + holidayId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteHoliday(final Long holidayId) {
        this.actionName = "DELETE";
        this.entityName = "HOLIDAY";
        this.entityId = holidayId;
        this.href = "/holidays/" + holidayId + "command=delete";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder assignRole(final Long groupId) {
        this.actionName = "ASSIGNROLE";
        this.entityName = "GROUP";
        this.groupId = groupId;
        this.entityId = null;
        this.href = "/groups/" + groupId + "?command=assignRole";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unassignRole(final Long groupId, final Long roleId) {
        this.actionName = "UNASSIGNROLE";
        this.entityName = "GROUP";
        this.groupId = groupId;
        this.entityId = roleId;
        this.href = "/groups/" + groupId + "?command=unassignRole";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateRole(final Long groupId, final Long roleId) {
        this.actionName = "UPDATEROLE";
        this.entityName = "GROUP";
        this.groupId = groupId;
        this.entityId = roleId;
        this.href = "/groups/" + groupId + "?command=updateRole";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unassignClientStaff(final Long clientId) {
        this.actionName = "UNASSIGNSTAFF";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=unassignStaff";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createTemplate() {
        this.actionName = "CREATE";
        this.entityName = "TEMPLATE";
        this.entityId = null;
        this.href = "/templates";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateTemplate(final Long templateId) {
        this.actionName = "UPDATE";
        this.entityName = "TEMPLATE";
        this.entityId = templateId;
        this.href = "/templates/" + templateId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteTemplate(final Long templateId) {
        this.actionName = "DELETE";
        this.entityName = "TEMPLATE";
        this.entityId = templateId;
        this.href = "/templates/" + templateId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder assignClientStaff(final Long clientId) {
        this.actionName = "ASSIGNSTAFF";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=assignStaff";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateClientSavingsAccount(final Long clientId) {
        this.actionName = "UPDATESAVINGSACCOUNT";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=updateSavingsAccount";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createProductMix(final Long productId) {
        this.actionName = "CREATE";
        this.entityName = "PRODUCTMIX";
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateProductMix(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "PRODUCTMIX";
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteProductMix(final Long productId) {
        this.actionName = "DELETE";
        this.entityName = "PRODUCTMIX";
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withProduct(final Long productId) {
        this.productId = productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateJobDetail(final Long jobId) {
        this.actionName = "UPDATE";
        this.entityName = "SCHEDULER";
        this.entityId = jobId;
        this.href = "/updateJobDetail/" + jobId + "/updateJobDetail";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createMeeting(final CommandWrapper resourceDetails, final String supportedEntityType,
            final Long supportedEntityId) {
        this.actionName = "CREATE";
        this.entityName = "MEETING";
        this.clientId = resourceDetails.getClientId();
        this.loanId = resourceDetails.getLoanId();
        this.groupId = resourceDetails.getGroupId();
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/meetings";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateMeeting(final String supportedEntityType, final Long supportedEntityId, final Long meetingId) {
        this.actionName = "UPDATE";
        this.entityName = "MEETING";
        this.entityId = meetingId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/meetings/" + meetingId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteMeeting(final String supportedEntityType, final Long supportedEntityId, final Long meetingId) {
        this.actionName = "DELETE";
        this.entityName = "MEETING";
        this.entityId = meetingId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/meetings/" + meetingId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder saveOrUpdateAttendance(final Long entityId, final String supportedEntityType,
            final Long supportedEntityId) {
        this.actionName = "SAVEORUPDATEATTENDANCE";
        this.entityName = "MEETING";
        this.entityId = entityId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/meetings/" + entityId + "?command=saveOrUpdateAttendance";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateCache() {
        this.actionName = "UPDATE";
        this.entityName = "CACHE";
        this.href = "/cache";
        return this;
    }

    /**
     * Deposit account mappings
     */

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createFixedDepositAccount() {
        this.actionName = "CREATE";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = null;
        this.href = "/fixeddepositaccounts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateFixedDepositAccount(final Long accountId) {
        this.actionName = "UPDATE";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteFixedDepositAccount(final Long accountId) {
        this.actionName = "DELETE";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectFixedDepositAccountApplication(final Long accountId) {
        this.actionName = "REJECT";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=reject";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withdrawFixedDepositAccountApplication(final Long accountId) {
        this.actionName = "WITHDRAW";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=withdrawnByApplicant";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveFixedDepositAccountApplication(final Long accountId) {
        this.actionName = "APPROVE";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=approve";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoFixedDepositAccountApplication(final Long accountId) {
        this.actionName = "APPROVALUNDO";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder fixedDepositAccountActivation(final Long accountId) {
        this.actionName = "ACTIVATE";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeFixedDepositAccount(final Long accountId) {
        this.actionName = "CLOSE";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder prematureCloseFixedDepositAccount(final Long accountId) {
        this.actionName = "PREMATURECLOSE";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=prematureClose";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder fixedDepositAccountInterestCalculation(final Long accountId) {
        this.actionName = "CALCULATEINTEREST";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=calculateInterest";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder fixedDepositAccountInterestPosting(final Long accountId) {
        this.actionName = "POSTINTEREST";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=postInterest";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder fixedDepositAccountDeposit(final Long accountId) {
        this.actionName = "DEPOSIT";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "/transactions?command=deposit";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder fixedDepositAccountWithdrawal(final Long accountId) {
        this.actionName = "WITHDRAWAL";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "/transactions?command=withdrawal";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createRecurringDepositAccount() {
        this.actionName = "CREATE";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = null;
        this.href = "/recurringdepositaccounts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateRecurringDepositAccount(final Long accountId) {
        this.actionName = "UPDATE";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder recurringAccountDeposit(final Long accountId) {
        this.actionName = "DEPOSIT";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions?command=deposit";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder recurringAccountWithdrawal(final Long accountId) {
        this.actionName = "WITHDRAWAL";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions?command=withdrawal";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder adjustRecurringAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = "ADJUSTTRANSACTION";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions/" + transactionId + "?command=modify";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoRecurringAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = "UNDOTRANSACTION";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions/" + transactionId + "?command=undo";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteRecurringDepositAccount(final Long accountId) {
        this.actionName = "DELETE";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = "REJECT";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=reject";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder withdrawRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = "WITHDRAW";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=withdrawnByApplicant";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = "APPROVE";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=approve";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = "APPROVALUNDO";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder recurringDepositAccountActivation(final Long accountId) {
        this.actionName = "ACTIVATE";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeRecurringDepositAccount(final Long accountId) {
        this.actionName = "CLOSE";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateDepositAmountForRecurringDepositAccount(final Long accountId) {
        this.actionName = DepositsApiConstants.UPDATE_DEPOSIT_AMOUNT.toUpperCase();
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=" + DepositsApiConstants.UPDATE_DEPOSIT_AMOUNT;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder prematureCloseRecurringDepositAccount(final Long accountId) {
        this.actionName = "PREMATURECLOSE";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=prematureClose";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder recurringDepositAccountInterestCalculation(final Long accountId) {
        this.actionName = "CALCULATEINTEREST";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=calculateInterest";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder recurringDepositAccountInterestPosting(final Long accountId) {
        this.actionName = "POSTINTEREST";
        this.entityName = "RECURRINGDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=postInterest";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createOfficeToGLAccountMapping() {
        this.actionName = "CREATE";
        this.entityName = "FINANCIALACTIVITYACCOUNT";
        this.entityId = null;
        this.href = "/organizationglaccounts/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateOfficeToGLAccountMapping(final Long mappingId) {
        this.actionName = "UPDATE";
        this.entityName = "FINANCIALACTIVITYACCOUNT";
        this.entityId = mappingId;
        this.href = "/organizationglaccounts/" + mappingId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteOfficeToGLAccountMapping(final Long mappingId) {
        this.actionName = "DELETE";
        this.entityName = "FINANCIALACTIVITYACCOUNT";
        this.entityId = mappingId;
        this.href = "/organizationglaccounts/" + mappingId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder registerDBDatatable(final String datatable, final String apptable) {
        this.actionName = "REGISTER";
        this.entityName = "DATATABLE";
        this.entityId = null;
        this.href = "/datatables/register/" + datatable + "/" + apptable;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder registerSurvey(final String datatable, final String apptable) {
        this.actionName = "REGISTER";
        this.entityName = "SURVEY";
        this.entityId = null;
        this.href = "/survey/register/" + datatable + "/" + apptable;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder fullFilSurvey(final String datatable, final Long apptableId) {
        this.entityName = datatable;
        this.entityId = apptableId;
        this.actionName = "CREATE";
        this.href = "/survey/" + datatable + "/" + apptableId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateLikelihood(final Long entityId) {
        this.actionName = "UPDATE";
        this.entityName = "LIKELIHOOD";
        this.href = "/likelihood/" + entityId;
        this.entityId = entityId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder assignSavingsOfficer(final Long accountId) {
        this.actionName = "UPDATESAVINGSOFFICER";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=assignSavingsOfficer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unassignSavingsOfficer(final Long accountId) {
        this.actionName = "REMOVESAVINGSOFFICER";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?commad=unassignSavingsOfficer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder savingsInterestPostingAsOnDate(final Long accountId) {
        this.actionName = "POSTINTERESTASONDATE";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=postInterestAsOn";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createLoanRescheduleRequest(final String entityName) {
        this.actionName = "CREATE";
        this.entityName = entityName;
        this.entityId = null;
        this.href = "/rescheduleloans";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveLoanRescheduleRequest(final String entityName, final Long requestId) {
        this.actionName = "APPROVE";
        this.entityName = entityName;
        this.entityId = requestId;
        this.href = "/rescheduleloans/" + requestId + "?command=approve";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder rejectLoanRescheduleRequest(final String entityName, final Long requestId) {
        this.actionName = "REJECT";
        this.entityName = entityName;
        this.entityId = requestId;
        this.href = "/rescheduleloans/" + requestId + "?command=reject";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createAccountNumberFormat() {
        this.actionName = "CREATE";
        this.entityName = AccountNumberFormatConstants.ENTITY_NAME.toUpperCase();
        this.href = AccountNumberFormatConstants.resourceRelativeURL;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateAccountNumberFormat(final Long accountNumberFormatId) {
        this.actionName = "UPDATE";
        this.entityName = AccountNumberFormatConstants.ENTITY_NAME.toUpperCase();
        this.entityId = accountNumberFormatId;
        this.href = AccountNumberFormatConstants.resourceRelativeURL + "/" + accountNumberFormatId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteAccountNumberFormat(final Long accountNumberFormatId) {
        this.actionName = "DELETE";
        this.entityName = AccountNumberFormatConstants.ENTITY_NAME.toUpperCase();
        this.entityId = accountNumberFormatId;
        this.href = "AccountNumberFormatConstants.resourceRelativeURL" + "/" + accountNumberFormatId;
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder refundByTransfer() {
        this.actionName = "REFUNDBYTRANSFER";
        this.entityName = "ACCOUNTTRANSFER";
        this.entityId = null;
        this.href = "/refundByTransfer";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createTeller() {
        this.actionName = "CREATE";
        this.entityName = "TELLER";
        this.entityId = null;
        this.href = "/tellers/templates";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateTeller(final Long tellerId) {
        this.actionName = "UPDATE";
        this.entityName = "TELLER";
        this.entityId = tellerId;
        this.href = "/tellers/" + tellerId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteTeller(final Long tellerId) {
        this.actionName = "DELETE";
        this.entityName = "TELLER";
        this.entityId = tellerId;
        this.href = "/tellers/" + tellerId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder allocateTeller(final long tellerId) {
        this.actionName = "ALLOCATECASHIER";
        this.entityName = "TELLER";
        this.entityId = tellerId;
        this.href = "/tellers/" + tellerId + "/cashiers/templates";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateAllocationTeller(final Long tellerId, final Long cashierId) {
        this.actionName = "UPDATECASHIERALLOCATION";
        this.entityName = "TELLER";
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteAllocationTeller(final Long tellerId, final Long cashierId) {
        this.actionName = "DELETECASHIERALLOCATION";
        this.entityName = "TELLER";
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder allocateCashToCashier(final Long tellerId, final Long cashierId) {
        this.actionName = "ALLOCATECASHTOCASHIER";
        this.entityName = "TELLER";
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId + "/allocate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder settleCashFromCashier(final Long tellerId, final Long cashierId) {
        this.actionName = "SETTLECASHFROMCASHIER";
        this.entityName = "TELLER";
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId + "/settle";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteRole(Long roleId) {
        this.actionName = "DELETE";
        this.entityName = "ROLE";
        this.entityId = roleId;
        this.href = "/roles/" + roleId;
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder disableRole(Long roleId) {
        this.actionName = "DISABLE";
        this.entityName = "ROLE";
        this.entityId = roleId;
        this.href = "/roles/" + roleId + "/disbales";
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder enableRole(Long roleId) {
        this.actionName = "ENABLE";
        this.entityName = "ROLE";
        this.entityId = roleId;
        this.href = "/roles/" + roleId + "/enable";
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createMap(Long relId) {
        this.actionName = "CREATE";
        this.entityName = "ENTITYMAPPING";
        this.entityId = relId;
        this.href = "/entitytoentitymapping/" + relId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateMap(Long mapId) {
        this.actionName = "UPDATE";
        this.entityName = "ENTITYMAPPING";
        this.entityId = mapId;
        this.href = "/entitytoentitymapping" + mapId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteMap(final Long mapId) {
        this.actionName = "DELETE";
        this.entityName = "ENTITYMAPPING";
        this.entityId = mapId;
        this.href = "/entitytoentitymapping/" + mapId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateWorkingDays() {
        this.actionName = "UPDATE";
        this.entityName = "WORKINGDAYS";
        this.href = "/workingdays/";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updatePasswordPreferences() {
        this.actionName = "UPDATE";
        this.entityName = PasswordPreferencesApiConstants.ENTITY_NAME;
        this.href = "/" + PasswordPreferencesApiConstants.RESOURCE_NAME;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createPaymentType() {
        this.actionName = "CREATE";
        this.entityName = PaymentTypeApiResourceConstants.ENTITY_NAME;
        this.entityId = null;
        this.href = "/" + PaymentTypeApiResourceConstants.RESOURCE_NAME;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updatePaymentType(final Long paymentTypeId) {
        this.actionName = "UPDATE";
        this.entityName = PaymentTypeApiResourceConstants.ENTITY_NAME;
        this.entityId = paymentTypeId;
        this.href = "/" + PaymentTypeApiResourceConstants.RESOURCE_NAME + paymentTypeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deletePaymentType(final Long paymentTypeId) {
        this.actionName = "DELETE";
        this.entityName = "PAYMENTTYPE";
        this.entityId = paymentTypeId;
        this.href = "/" + PaymentTypeApiResourceConstants.RESOURCE_NAME + paymentTypeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateExternalServiceProperties(final String externalServiceName) {
        this.actionName = "UPDATE";
        this.entityName = "EXTERNALSERVICES";
        this.transactionId = externalServiceName;
        this.href = "/externalservices/" + externalServiceName;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createClientCharge(final Long clientId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_CREATE;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/charges";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteClientCharge(final Long clientId, final Long chargeId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_DELETE;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.clientId = clientId;
        this.entityId = chargeId;
        this.href = "/clients/" + clientId + "/charges/" + chargeId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder waiveClientCharge(final Long clientId, final Long chargeId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_WAIVE;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.entityId = chargeId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/charges/" + chargeId + "?command=waive";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder payClientCharge(final Long clientId, final Long chargeId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_PAY;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.entityId = chargeId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/charges/" + chargeId + "?command=paycharge";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder inactivateClientCharge(final Long clientId, final Long chargeId) {
        this.actionName = "INACTIVATE";
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.entityId = chargeId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/charges/" + chargeId + "?command=inactivate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder undoClientTransaction(final Long clientId, final Long transactionId) {
        this.actionName = ClientApiConstants.CLIENT_TRANSACTION_ACTION_UNDO;
        this.entityName = ClientApiConstants.CLIENT_RESOURCE_NAME;
        this.entityId = transactionId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/transactions/" + transactionId + "?command=undo";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createProvisioningCategory() {
        this.actionName = "CREATE";
        this.entityName = "PROVISIONCATEGORY";
        this.entityId = null;
        this.href = "/provisioningcategory";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateProvisioningCategory(final Long cateoryId) {
        this.actionName = "UPDATE";
        this.entityName = "PROVISIONCATEGORY";
        this.entityId = cateoryId;
        this.href = "/provisioningcategory/" + cateoryId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteProvisioningCategory(final Long categoryId) {
        this.actionName = "DELETE";
        this.entityName = "PROVISIONCATEGORY";
        this.entityId = categoryId;
        this.href = "/provisioningcategory/" + categoryId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createProvisioningCriteria() {
        this.actionName = "CREATE";
        this.entityName = "PROVISIONCRITERIA";
        this.entityId = null;
        this.href = "/provisioningcriteria";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateProvisioningCriteria(final Long criteriaId) {
        this.actionName = "UPDATE";
        this.entityName = "PROVISIONCRITERIA";
        this.entityId = criteriaId;
        this.href = "/provisioningcriteria/" + criteriaId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteProvisioningCriteria(final Long criteriaId) {
        this.actionName = "DELETE";
        this.entityName = "PROVISIONCRITERIA";
        this.entityId = criteriaId;
        this.href = "/provisioningcriteria/" + criteriaId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createProvisioningEntries() {
        this.actionName = "CREATE";
        this.entityName = "PROVISIONENTRIES";
        this.entityId = null;
        this.href = "/provisioningentries";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createProvisioningJournalEntries(final Long entryId) {
        this.actionName = "CREATE";
        this.entityName = "PROVISIONJOURNALENTRIES";
        this.entityId = entryId;
        this.href = "/provisioningentries/" + entryId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder reCreateProvisioningEntries(final Long entryId) {
        this.actionName = "RECREATE";
        this.entityName = "PROVISIONENTRIES";
        this.entityId = entryId;
        this.href = "/provisioningentries/" + entryId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createFloatingRate() {
        this.actionName = "CREATE";
        this.entityName = "FLOATINGRATE";
        this.entityId = null;
        this.href = "/floatingrates";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateFloatingRate(final Long floatingRateId) {
        this.actionName = "UPDATE";
        this.entityName = "FLOATINGRATE";
        this.entityId = floatingRateId;
        this.href = "/floatingrates/" + floatingRateId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createScheduleExceptions(final Long loanId) {
        this.actionName = "CREATESCHEDULEEXCEPTIONS";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/schedule";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteScheduleExceptions(final Long loanId) {
        this.actionName = "DELETESCHEDULEEXCEPTIONS";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/schedule";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createProduct(String productType) {
        this.entityName = productType.toUpperCase() + "PRODUCT"; // To Support
                                                                 // different
                                                                 // type of
                                                                 // products
        this.actionName = "CREATE";
        this.entityId = null;
        this.href = "/products/" + productType;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateProduct(String productType, final Long productId) {
        this.entityName = productType.toUpperCase() + "PRODUCT";
        this.actionName = "UPDATE";
        this.entityId = productId;
        this.href = "/products/" + productType + "/" + productId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createAccount(String accountType) {
        this.entityName = accountType.toUpperCase() + "ACCOUNT"; // To Support
                                                                 // different
                                                                 // type of
                                                                 // Accounts
        this.actionName = "CREATE";
        this.entityId = null;
        this.href = "/accounts/" + accountType;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateAccount(String accountType, final Long accountId) {
        this.entityName = accountType.toUpperCase() + "ACCOUNT";
        this.actionName = "UPDATE";
        this.entityId = accountId;
        this.href = "/accounts/" + accountType + "/" + accountId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createProductCommand(String productType, String command, final Long productId) {
        this.entityName = productType.toUpperCase() + "PRODUCT";
        this.actionName = "CREATE" + "_" + command.toUpperCase();
        this.entityId = productId;
        this.href = "/products/" + productType + "/" + productId + "?command=" + command;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createShareProductDividendPayoutCommand(final Long productId) {
        this.entityName = "SHAREPRODUCT";
        this.actionName = "CREATE_DIVIDEND";
        this.entityId = productId;
        this.href = "/shareproduct/" + productId + "/dividend";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder approveShareProductDividendPayoutCommand(final Long productId, final Long dividendId) {
        this.entityName = "SHAREPRODUCT";
        this.actionName = "APPROVE_DIVIDEND";
        this.entityId = dividendId;
        this.href = "/shareproduct/" + productId + "/dividend/" + dividendId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteShareProductDividendPayoutCommand(final Long productId, final Long dividendId) {
        this.entityName = "SHAREPRODUCT";
        this.actionName = "DELETE_DIVIDEND";
        this.entityId = dividendId;
        this.href = "/shareproduct/" + productId + "/dividend/" + dividendId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createAccountCommand(String accountType, final Long accountId, String command) {
        this.entityName = accountType.toUpperCase() + "ACCOUNT";
        this.actionName = command.toUpperCase();
        this.entityId = accountId;
        this.href = "/accounts/" + accountType + "/" + accountId + "?command=" + command;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createTaxComponent() {
        this.actionName = "CREATE";
        this.entityName = "TAXCOMPONENT";
        this.entityId = null;
        this.href = "/taxes/component";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateTaxComponent(final Long taxComponentId) {
        this.actionName = "UPDATE";
        this.entityName = "TAXCOMPONENT";
        this.entityId = taxComponentId;
        this.href = "/taxes/component/" + taxComponentId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createTaxGroup() {
        this.actionName = "CREATE";
        this.entityName = "TAXGROUP";
        this.entityId = null;
        this.href = "/taxes/group";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateTaxGroup(final Long taxGroupId) {
        this.actionName = "UPDATE";
        this.entityName = "TAXGROUP";
        this.entityId = taxGroupId;
        this.href = "/taxes/group/" + taxGroupId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateWithHoldTax(final Long accountId) {
        this.actionName = "UPDATEWITHHOLDTAX";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?commad=updateTaxWithHoldTax";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createEntityDatatableChecks(final String json) {
        this.actionName = "CREATE";
        this.entityName = "ENTITY_DATATABLE_CHECK";
        this.entityId = null;
        this.href = "/entityDatatableChecks/";
        this.json = json;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteEntityDatatableChecks(final long entityDatatableCheckId, final String json) {
        this.actionName = "DELETE";
        this.entityName = "ENTITY_DATATABLE_CHECK";
        this.entityId = entityDatatableCheckId;
        this.href = "/entityDatatableChecks/" + entityDatatableCheckId;
        this.json = json;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder addSelfServiceBeneficiaryTPT() {
        this.actionName = "CREATE";
        this.entityName = "SSBENEFICIARYTPT";
        this.entityId = null;
        this.href = "/self/beneficiaries/tpt";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateSelfServiceBeneficiaryTPT(final Long beneficiaryId) {
        this.actionName = "UPDATE";
        this.entityName = "SSBENEFICIARYTPT";
        this.entityId = beneficiaryId;
        this.href = "/self/beneficiaries/tpt/" + beneficiaryId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteSelfServiceBeneficiaryTPT(final Long beneficiaryId) {
        this.actionName = "DELETE";
        this.entityName = "SSBENEFICIARYTPT";
        this.entityId = beneficiaryId;
        this.href = "/self/beneficiaries/tpt/" + beneficiaryId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createReportMailingJob(final String entityName) {
        this.actionName = "CREATE";
        this.entityName = entityName;
        this.entityId = null;
        this.href = "/reportmailingjobs";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateReportMailingJob(final String entityName, final Long entityId) {
        this.actionName = "UPDATE";
        this.entityName = entityName;
        this.entityId = entityId;
        this.href = "/reportmailingjobs/" + entityId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteReportMailingJob(final String entityName, final Long entityId) {
        this.actionName = "DELETE";
        this.entityName = entityName;
        this.entityId = entityId;
        this.href = "/reportmailingjobs/" + entityId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createSmsCampaign() {
        this.actionName = "CREATE";
        this.entityName = "SMSCAMPAIGN";
        this.entityId = null;
        this.href = "/smscampaigns";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateSmsCampaign(final Long resourceId) {
        this.actionName = "UPDATE";
        this.entityName = "SMSCAMPAIGN";
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder activateSmsCampaign(final Long resourceId) {
        this.actionName = "ACTIVATE";
        this.entityName = "SMSCAMPAIGN";
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeSmsCampaign(final Long resourceId) {
        this.actionName = "CLOSE";
        this.entityName = "SMSCAMPAIGN";
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder reactivateSmsCampaign(final Long resourceId) {
        this.actionName = "REACTIVATE";
        this.entityName = "SMSCAMPAIGN";
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId + "?command=reactivate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteSmsCampaign(final Long resourceId) {
        this.actionName = "DELETE";
        this.entityName = "SMSCAMPAIGN";
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder holdAmount(final Long accountId) {
        this.actionName = "HOLDAMOUNT";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions?command=holdAmount";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder releaseAmount(final Long accountId, final Long transactionId) {
        this.actionName = "RELEASEAMOUNT";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = null;
        this.savingsId = accountId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=releaseAmount";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder blockDebitsFromSavingsAccount(final Long accountId) {
        this.actionName = "BLOCKDEBIT";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=blockDebit";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unblockDebitsFromSavingsAccount(final Long accountId) {
        this.actionName = "UNBLOCKDEBIT";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=unblockDebit";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder blockCreditsToSavingsAccount(final Long accountId) {
        this.actionName = "BLOCKCREDIT";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=blockCredit";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unblockCreditsToSavingsAccount(final Long accountId) {
        this.actionName = "UNBLOCKCREDIT";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=unblockCredit";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder blockSavingsAccount(final Long accountId) {
        this.actionName = "BLOCK";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=block";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder unblockSavingsAccount(final Long accountId) {
        this.actionName = "UNBLOCK";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=unblock";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder disableAdHoc(Long adHocId) {
        this.actionName = "DISABLE";
        this.entityName = "ADHOC";
        this.entityId = adHocId;
        this.href = "/adhoc/" + adHocId + "/disbale";
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder enableAdHoc(Long adHocId) {
        this.actionName = "ENABLE";
        this.entityName = "ADHOC";
        this.entityId = adHocId;
        this.href = "/adhoc/" + adHocId + "/enable";
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createAdHoc() {
        this.actionName = "CREATE";
        this.entityName = "ADHOC";
        this.href = "/adhocquery/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateAdHoc(final Long adHocId) {
        this.actionName = "UPDATE";
        this.entityName = "ADHOC";
        this.entityId = adHocId;
        this.href = "/adhocquery/" + adHocId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteAdHoc(Long adHocId) {
        this.actionName = "DELETE";
        this.entityName = "ADHOC";
        this.entityId = adHocId;
        this.href = "/adhocquery/" + adHocId;
        this.json = "{}";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createEmail() {
        this.actionName = "CREATE";
        this.entityName = "EMAIL";
        this.entityId = null;
        this.href = "/emailcampaigns/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateEmail(final Long resourceId) {
        this.actionName = "UPDATE";
        this.entityName = "EMAIL";
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteEmail(final Long resourceId) {
        this.actionName = "DELETE";
        this.entityName = "EMAIL";
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createEmailCampaign() {
        this.actionName = "CREATE";
        this.entityName = "EMAIL_CAMPAIGN";
        this.entityId = null;
        this.href = "/emailcampaigns/campaign";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateEmailCampaign(final Long resourceId) {
        this.actionName = "UPDATE";
        this.entityName = "EMAIL_CAMPAIGN";
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteEmailCampaign(final Long resourceId) {
        this.actionName = "DELETE";
        this.entityName = "EMAIL_CAMPAIGN";
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder activateEmailCampaign(final Long resourceId) {
        this.actionName = "ACTIVATE";
        this.entityName = "EMAIL_CAMPAIGN";
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId + "?command=activate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder closeEmailCampaign(final Long resourceId) {
        this.actionName = "CLOSE";
        this.entityName = "EMAIL_CAMPAIGN";
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId + "?command=close";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder reactivateEmailCampaign(final Long resourceId) {
        this.actionName = "REACTIVATE";
        this.entityName = "EMAIL_CAMPAIGN";
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId + "?command=reactivate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateEmailConfiguration() {
        this.actionName = "UPDATE";
        this.entityName = "EMAIL_CONFIGURATION";
        this.href = "/emailcampaigns/configuration/";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder invalidateTwoFactorAccessToken() {
        this.actionName = "INVALIDATE";
        this.entityName = "TWOFACTOR_ACCESSTOKEN";
        this.href = "/twofactor/invalidate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateTwoFactorConfiguration() {
        this.actionName = "UPDATE";
        this.entityName = "TWOFACTOR_CONFIGURATION";
        this.href = "/twofactor/configure";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder linkAccountsToPocket() {
        this.actionName = PocketApiConstants.linkAccountsActionName;
        this.entityName = PocketApiConstants.pocketEntityName;
        this.href = "/self/pocket?command=" + PocketApiConstants.linkAccountsToPocketCommandParam;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder delinkAccountsFromPocket() {
        this.actionName = PocketApiConstants.delinkAccountsActionName;
        this.entityName = PocketApiConstants.pocketEntityName;
        this.href = "/self/pocket?command=" + PocketApiConstants.delinkAccountsFromPocketCommandParam;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createRate() {
        this.actionName = "CREATE";
        this.entityName = "RATE";
        this.entityId = null;
        this.href = "/rates/template";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateRate(final Long rateId) {
        this.actionName = "UPDATE";
        this.entityName = "RATE";
        this.entityId = rateId;
        this.href = "/rates/" + rateId;
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateBusinessDate() {
        this.actionName = "UPDATE";
        this.entityName = "BUSINESS_DATE";
        this.href = "/businessdate";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createDelinquencyRange() {
        this.actionName = "CREATE";
        this.entityName = "DELINQUENCY_RANGE";
        this.href = "/delinquency/range";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateDelinquencyRange(final Long delinquencyRangeId) {
        this.actionName = "UPDATE";
        this.entityName = "DELINQUENCY_RANGE";
        this.entityId = delinquencyRangeId;
        this.href = "/delinquency/range";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteDelinquencyRange(final Long delinquencyRangeId) {
        this.actionName = "DELETE";
        this.entityName = "DELINQUENCY_RANGE";
        this.entityId = delinquencyRangeId;
        this.href = "/delinquency/range";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder createDelinquencyBucket() {
        this.actionName = "CREATE";
        this.entityName = "DELINQUENCY_BUCKET";
        this.href = "/delinquency/bucket";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateDelinquencyBucket(final Long delinquencyBucketId) {
        this.actionName = "UPDATE";
        this.entityName = "DELINQUENCY_BUCKET";
        this.entityId = delinquencyBucketId;
        this.href = "/delinquency/bucket";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder deleteDelinquencyBucket(final Long delinquencyBucketId) {
        this.actionName = "DELETE";
        this.entityName = "DELINQUENCY_BUCKET";
        this.entityId = delinquencyBucketId;
        this.href = "/delinquency/bucket";
        return this;
    }

    @CanIgnoreReturnValue
    public CommandWrapperBuilder updateBusinessStepConfig(String jobName) {
        this.actionName = "UPDATE";
        this.entityName = "BATCH_BUSINESS_STEP";
        this.href = "/jobs/" + jobName + "/steps";
        return this;
    }

}
