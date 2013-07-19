/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.service;

import org.mifosplatform.commands.domain.CommandWrapper;

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
    private Long codeId;
    private String transactionId;
    private String supportedEntityType;
    private Long supportedEntityId;
    private Long productId;

    public CommandWrapper build() {
        return new CommandWrapper(this.officeId, this.groupId, this.clientId, this.loanId, this.savingsId, this.actionName,
                this.entityName, this.entityId, this.subentityId, this.codeId, this.supportedEntityType, this.supportedEntityId, this.href,
                this.json, this.transactionId, this.productId);
    }

    public CommandWrapperBuilder withLoanId(final Long withLoanId) {
        this.loanId = withLoanId;
        return this;
    }

    public CommandWrapperBuilder withClientId(final Long withClientId) {
        this.clientId = withClientId;
        return this;
    }

    public CommandWrapperBuilder withJson(final String withJson) {
        this.json = withJson;
        return this;
    }

    public CommandWrapperBuilder updateGlobalConfiguration() {
        this.actionName = "UPDATE";
        this.entityName = "CONFIGURATION";
        this.entityId = null;
        this.href = "/configurations";
        return this;
    }

    public CommandWrapperBuilder updatePermissions() {
        this.actionName = "UPDATE";
        this.entityName = "PERMISSION";
        this.entityId = null;
        this.href = "/permissions";
        return this;
    }

    public CommandWrapperBuilder createRole() {
        this.actionName = "CREATE";
        this.entityName = "ROLE";
        this.href = "/roles/template";
        return this;
    }

    public CommandWrapperBuilder updateRole(final Long roleId) {
        this.actionName = "UPDATE";
        this.entityName = "ROLE";
        this.entityId = roleId;
        this.href = "/roles/" + roleId;
        return this;
    }

    public CommandWrapperBuilder updateRolePermissions(final Long roleId) {
        this.actionName = "PERMISSIONS";
        this.entityName = "ROLE";
        this.entityId = roleId;
        this.href = "/roles/" + roleId + "/permissions";
        return this;
    }

    public CommandWrapperBuilder createUser() {
        this.actionName = "CREATE";
        this.entityName = "USER";
        this.entityId = null;
        this.href = "/users/template";
        return this;
    }

    public CommandWrapperBuilder updateUser(final Long userId) {
        this.actionName = "UPDATE";
        this.entityName = "USER";
        this.entityId = userId;
        this.href = "/users/" + userId;
        return this;
    }

    public CommandWrapperBuilder deleteUser(final Long userId) {
        this.actionName = "DELETE";
        this.entityName = "USER";
        this.entityId = userId;
        this.href = "/users/" + userId;
        return this;
    }

    public CommandWrapperBuilder createOffice() {
        this.actionName = "CREATE";
        this.entityName = "OFFICE";
        this.entityId = null;
        this.href = "/offices/template";
        return this;
    }

    public CommandWrapperBuilder updateOffice(final Long officeId) {
        this.actionName = "UPDATE";
        this.entityName = "OFFICE";
        this.entityId = officeId;
        this.href = "/offices/" + officeId;
        return this;
    }

    public CommandWrapperBuilder createOfficeTransaction() {
        this.actionName = "CREATE";
        this.entityName = "OFFICETRANSACTION";
        this.href = "/officetransactions/template";
        return this;
    }

    public CommandWrapperBuilder deleteOfficeTransaction(final Long transactionId) {
        this.actionName = "DELETE";
        this.entityName = "OFFICETRANSACTION";
        this.entityId = transactionId;
        this.href = "/officetransactions/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder createStaff() {
        this.actionName = "CREATE";
        this.entityName = "STAFF";
        this.entityId = null;
        this.href = "/staff/template";
        return this;
    }

    public CommandWrapperBuilder updateStaff(final Long staffId) {
        this.actionName = "UPDATE";
        this.entityName = "STAFF";
        this.entityId = staffId;
        this.href = "/staff/" + staffId;
        return this;
    }

    public CommandWrapperBuilder createGuarantor(final Long loanId) {
        this.actionName = "CREATE";
        this.entityName = "GUARANTOR";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors/template";
        return this;
    }

    public CommandWrapperBuilder updateGuarantor(final Long loanId, final Long guarantorId) {
        this.actionName = "UPDATE";
        this.entityName = "GUARANTOR";
        this.entityId = guarantorId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors/" + guarantorId;
        return this;
    }

    public CommandWrapperBuilder deleteGuarantor(final Long loanId, final Long guarantorId) {
        this.actionName = "DELETE";
        this.entityName = "GUARANTOR";
        this.entityId = guarantorId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors/" + guarantorId;
        return this;
    }

    public CommandWrapperBuilder createFund() {
        this.actionName = "CREATE";
        this.entityName = "FUND";
        this.entityId = null;
        this.href = "/funds/template";
        return this;
    }

    public CommandWrapperBuilder updateFund(final Long fundId) {
        this.actionName = "UPDATE";
        this.entityName = "FUND";
        this.entityId = fundId;
        this.href = "/funds/" + fundId;
        return this;
    }

    public CommandWrapperBuilder createReport() {
        this.actionName = "CREATE";
        this.entityName = "REPORT";
        this.entityId = null;
        this.href = "/reports/template";
        return this;
    }

    public CommandWrapperBuilder updateReport(final Long id) {
        this.actionName = "UPDATE";
        this.entityName = "REPORT";
        this.entityId = id;
        this.href = "/reports/" + id;
        return this;
    }

    public CommandWrapperBuilder deleteReport(final Long id) {
        this.actionName = "DELETE";
        this.entityName = "REPORT";
        this.entityId = id;
        this.href = "/reports/" + id;
        return this;
    }

    public CommandWrapperBuilder updateCurrencies() {
        this.actionName = "UPDATE";
        this.entityName = "CURRENCY";
        this.href = "/currencies";
        return this;
    }

    public CommandWrapperBuilder createCode() {
        this.actionName = "CREATE";
        this.entityName = "CODE";
        this.entityId = null;
        this.href = "/codes/template";
        return this;
    }

    public CommandWrapperBuilder updateCode(final Long codeId) {
        this.actionName = "UPDATE";
        this.entityName = "CODE";
        this.entityId = codeId;
        this.href = "/codes/" + codeId;
        return this;
    }

    public CommandWrapperBuilder deleteCode(final Long codeId) {
        this.actionName = "DELETE";
        this.entityName = "CODE";
        this.entityId = codeId;
        this.href = "/codes/" + codeId;
        return this;
    }

    public CommandWrapperBuilder createCharge() {
        this.actionName = "CREATE";
        this.entityName = "CHARGE";
        this.entityId = null;
        this.href = "/charges/template";
        return this;
    }

    public CommandWrapperBuilder updateCharge(final Long chargeId) {
        this.actionName = "UPDATE";
        this.entityName = "CHARGE";
        this.entityId = chargeId;
        this.href = "/charges/" + chargeId;
        return this;
    }

    public CommandWrapperBuilder deleteCharge(final Long chargeId) {
        this.actionName = "DELETE";
        this.entityName = "CHARGE";
        this.entityId = chargeId;
        this.href = "/charges/" + chargeId;
        return this;
    }

    public CommandWrapperBuilder createLoanProduct() {
        this.actionName = "CREATE";
        this.entityName = "LOANPRODUCT";
        this.entityId = null;
        this.href = "/loanproducts/template";
        return this;
    }

    public CommandWrapperBuilder updateLoanProduct(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "LOANPRODUCT";
        this.entityId = productId;
        this.href = "/loanproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder createClientIdentifier(final Long clientId) {
        this.actionName = "CREATE";
        this.entityName = "CLIENTIDENTIFIER";
        this.entityId = null;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/template";
        return this;
    }

    public CommandWrapperBuilder updateClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        this.actionName = "UPDATE";
        this.entityName = "CLIENTIDENTIFIER";
        this.entityId = clientIdentifierId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/" + clientIdentifierId;
        return this;
    }

    public CommandWrapperBuilder deleteClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        this.actionName = "DELETE";
        this.entityName = "CLIENTIDENTIFIER";
        this.entityId = clientIdentifierId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/" + clientIdentifierId;
        return this;
    }

    public CommandWrapperBuilder createClient() {
        this.actionName = "CREATE";
        this.entityName = "CLIENT";
        this.href = "/clients/template";
        return this;
    }

    public CommandWrapperBuilder activateClient(final Long clientId) {
        this.actionName = "ACTIVATE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=activate&template=true";
        return this;
    }
    
    public CommandWrapperBuilder closeClient(final Long clientId) {
        this.actionName = "CLOSE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=close&template=true";
        return this;
    }

    public CommandWrapperBuilder updateClient(final Long clientId) {
        this.actionName = "UPDATE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId;
        return this;
    }

    public CommandWrapperBuilder deleteClient(final Long clientId) {
        this.actionName = "DELETE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId;
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder createDBDatatable(final String json) {
    	this.actionName = "CREATE";
    	this.entityName = "DATATABLE";
    	this.entityId = null;
    	this.href = "/datatables/";
    	this.json = json;
    	return this;
    }

    public CommandWrapperBuilder updateDBDatatable(final String datatable, final String json) {
    	this.actionName = "UPDATE";
    	this.entityName = "DATATABLE";
    	this.entityId = null;
    	this.href = "/datatables/" + datatable;
    	this.json = json;
    	return this;
    }

    public CommandWrapperBuilder deleteDBDatatable(final String datatable, final String json) {
    	this.actionName = "DELETE";
    	this.entityName = "DATATABLE";
    	this.entityId = null;
    	this.href = "/datatables/" + datatable;
    	this.json = json;
    	return this;
    }

    public CommandWrapperBuilder createDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = "CREATE";
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

    public CommandWrapperBuilder updateDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = "UPDATE";
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

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

    public CommandWrapperBuilder createLoanCharge(final Long loanId) {
        this.actionName = "CREATE";
        this.entityName = "LOANCHARGE";
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges";
        return this;
    }

    public CommandWrapperBuilder updateLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = "UPDATE";
        this.entityName = "LOANCHARGE";
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    public CommandWrapperBuilder waiveLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = "WAIVE";
        this.entityName = "LOANCHARGE";
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;

    }

    public CommandWrapperBuilder deleteLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = "DELETE";
        this.entityName = "LOANCHARGE";
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    public CommandWrapperBuilder loanRepaymentTransaction(final Long loanId) {
        this.actionName = "REPAYMENT";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=repayment";
        return this;
    }

    public CommandWrapperBuilder waiveInterestPortionTransaction(final Long loanId) {
        this.actionName = "WAIVEINTERESTPORTION";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=waiveinterest";
        return this;
    }

    public CommandWrapperBuilder writeOffLoanTransaction(final Long loanId) {
        this.actionName = "WRITEOFF";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=writeoff";
        return this;
    }

    public CommandWrapperBuilder closeLoanAsRescheduledTransaction(final Long loanId) {
        this.actionName = "CLOSEASRESCHEDULED";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=close-rescheduled";
        return this;
    }

    public CommandWrapperBuilder closeLoanTransaction(final Long loanId) {
        this.actionName = "CLOSE";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=close";
        return this;
    }

    public CommandWrapperBuilder adjustTransaction(final Long loanId, final Long transactionId) {
        this.actionName = "ADJUST";
        this.entityName = "LOAN";
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder createLoanApplication() {
        this.actionName = "CREATE";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = null;
        this.href = "/loans";
        return this;
    }

    public CommandWrapperBuilder updateLoanApplication(final Long loanId) {
        this.actionName = "UPDATE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder deleteLoanApplication(final Long loanId) {
        this.actionName = "DELETE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder rejectLoanApplication(final Long loanId) {
        this.actionName = "REJECT";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder withdrawLoanApplication(final Long loanId) {
        this.actionName = "WITHDRAW";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder approveLoanApplication(final Long loanId) {
        this.actionName = "APPROVE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder disburseLoanApplication(final Long loanId) {
        this.actionName = "DISBURSE";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoLoanApplicationApproval(final Long loanId) {
        this.actionName = "APPROVALUNDO";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoLoanApplicationDisbursal(final Long loanId) {
        this.actionName = "DISBURSALUNDO";
        this.entityName = "LOAN";
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder assignLoanOfficer(final Long loanId) {
        this.actionName = "UPDATELOANOFFICER";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder unassignLoanOfficer(final Long loanId) {
        this.actionName = "REMOVELOANOFFICER";
        this.entityName = "LOAN";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder assignLoanOfficersInBulk() {
        this.actionName = "BULKREASSIGN";
        this.entityName = "LOAN";
        this.href = "/loans/loanreassignment";
        return this;
    }

    public CommandWrapperBuilder createCodeValue(final Long codeId) {
        this.actionName = "CREATE";
        this.entityName = "CODEVALUE";
        this.codeId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/template";
        return this;
    }

    public CommandWrapperBuilder updateCodeValue(final Long codeId, final Long codeValueId) {
        this.actionName = "UPDATE";
        this.entityName = "CODEVALUE";
        this.entityId = codeValueId;
        this.codeId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }

    public CommandWrapperBuilder deleteCodeValue(final Long codeId, final Long codeValueId) {
        this.actionName = "DELETE";
        this.entityName = "CODEVALUE";
        this.entityId = codeValueId;
        this.codeId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }

    public CommandWrapperBuilder createGLClosure() {
        this.actionName = "CREATE";
        this.entityName = "GLCLOSURE";
        this.entityId = null;
        this.href = "/glclosures/template";
        return this;
    }

    public CommandWrapperBuilder updateGLClosure(final Long glClosureId) {
        this.actionName = "UPDATE";
        this.entityName = "GLCLOSURE";
        this.entityId = glClosureId;
        this.href = "/glclosures/" + glClosureId;
        return this;
    }

    public CommandWrapperBuilder deleteGLClosure(final Long glClosureId) {
        this.actionName = "DELETE";
        this.entityName = "GLCLOSURE";
        this.entityId = glClosureId;
        this.href = "/glclosures/" + glClosureId;
        return this;
    }

    public CommandWrapperBuilder createGLAccount() {
        this.actionName = "CREATE";
        this.entityName = "GLACCOUNT";
        this.entityId = null;
        this.href = "/glaccounts/template";
        return this;
    }

    public CommandWrapperBuilder updateGLAccount(final Long glAccountId) {
        this.actionName = "UPDATE";
        this.entityName = "GLACCOUNT";
        this.entityId = glAccountId;
        this.href = "/glaccounts/" + glAccountId;
        return this;
    }

    public CommandWrapperBuilder deleteGLAccount(final Long glAccountId) {
        this.actionName = "DELETE";
        this.entityName = "GLACCOUNT";
        this.entityId = glAccountId;
        this.href = "/glaccounts/" + glAccountId;
        return this;
    }

    public CommandWrapperBuilder createJournalEntry() {
        this.actionName = "CREATE";
        this.entityName = "JOURNALENTRY";
        this.entityId = null;
        this.href = "/journalentries/template";
        return this;
    }

    public CommandWrapperBuilder reverseJournalEntry(final String transactionId) {
        this.actionName = "REVERSE";
        this.entityName = "JOURNALENTRY";
        this.entityId = null;
        this.transactionId = transactionId;
        this.href = "/journalentries/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder createSavingProduct() {
        this.actionName = "CREATE";
        this.entityName = "SAVINGSPRODUCT";
        this.entityId = null;
        this.href = "/savingsproducts/template";
        return this;
    }

    public CommandWrapperBuilder updateSavingProduct(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "SAVINGSPRODUCT";
        this.entityId = productId;
        this.href = "/savingsproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder deleteSavingProduct(final Long productId) {
        this.actionName = "DELETE";
        this.entityName = "SAVINGSPRODUCT";
        this.entityId = productId;
        this.href = "/savingsproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder createSavingsAccount() {
        this.actionName = "CREATE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = null;
        this.href = "/savingsaccounts/template";
        return this;
    }

    public CommandWrapperBuilder updateSavingsAccount(final Long accountId) {
        this.actionName = "UPDATE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder deleteSavingsAccount(final Long accountId) {
        this.actionName = "DELETE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder rejectSavingsAccountApplication(final Long accountId) {
        this.actionName = "REJECT";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=reject";
        return this;
    }

    public CommandWrapperBuilder withdrawSavingsAccountApplication(final Long accountId) {
        this.actionName = "WITHDRAW";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=withdrawnByApplicant";
        return this;
    }

    public CommandWrapperBuilder approveSavingsAccountApplication(final Long accountId) {
        this.actionName = "APPROVE";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=approve";
        return this;
    }

    public CommandWrapperBuilder undoSavingsAccountApplication(final Long accountId) {
        this.actionName = "APPROVALUNDO";
        this.entityName = "SAVINGSACCOUNT";
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    public CommandWrapperBuilder savingsAccountActivation(final Long accountId) {
        this.actionName = "ACTIVATE";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder savingsAccountDeposit(final Long accountId) {
        this.actionName = "DEPOSIT";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

    public CommandWrapperBuilder savingsAccountWithdrawal(final Long accountId) {
        this.actionName = "WITHDRAWAL";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

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

    public CommandWrapperBuilder savingsAccountInterestCalculation(final Long accountId) {
        this.actionName = "CALCULATEINTEREST";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=calculateInterest";
        return this;
    }

    public CommandWrapperBuilder savingsAccountInterestPosting(final Long accountId) {
        this.actionName = "POSTINTEREST";
        this.entityName = "SAVINGSACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=postInterest";
        return this;
    }

    public CommandWrapperBuilder createCalendar(final String supportedEntityType, final Long supportedEntityId) {
        this.actionName = "CREATE";
        this.entityName = "CALENDAR";
        this.supportedEntityType = supportedEntityType;
        this.supportedEntityId = supportedEntityId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/template";
        return this;
    }

    public CommandWrapperBuilder updateCalendar(final String supportedEntityType, final Long supportedEntityId, final Long calendarId) {
        this.actionName = "UPDATE";
        this.entityName = "CALENDAR";
        this.entityId = calendarId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/" + calendarId;
        return this;
    }

    public CommandWrapperBuilder deleteCalendar(final String supportedEntityType, final Long supportedEntityId, final Long calendarId) {
        this.actionName = "DELETE";
        this.entityName = "CALENDAR";
        this.entityId = calendarId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/" + calendarId;
        return this;
    }

    public CommandWrapperBuilder createNote(final String entityName, final String resourceType, final Long resourceId) {
        this.actionName = "CREATE";
        this.entityName = entityName;// Note supports multiple resources. Note
                                     // Permissions are set for each resource.
        this.supportedEntityType = resourceType;
        this.supportedEntityId = resourceId;
        this.href = "/" + resourceType + "/" + resourceId + "/notes/template";
        return this;
    }

    public CommandWrapperBuilder updateNote(final String entityName, final String resourceType, final Long resourceId, final Long noteId) {
        this.actionName = "UPDATE";
        this.entityName = entityName;// Note supports multiple resources. Note
                                     // Permissions are set for each resource.
        this.entityId = noteId;
        this.supportedEntityType = resourceType;
        this.supportedEntityId = resourceId;
        this.href = "/" + resourceType + "/" + resourceId + "/notes";
        return this;
    }

    public CommandWrapperBuilder deleteNote(final String entityName, final String resourceType, final Long resourceId, final Long noteId) {
        this.actionName = "DELETE";
        this.entityName = entityName;// Note supports multiple resources. Note
                                     // Permissions are set for each resource.
        this.entityId = noteId;
        this.supportedEntityType = resourceType;
        this.supportedEntityId = resourceId;
        this.href = "/" + resourceType + "/" + resourceId + "/calendars/" + noteId;
        return this;
    }

    public CommandWrapperBuilder createGroup() {
        this.actionName = "CREATE";
        this.entityName = "GROUP";
        this.href = "/groups/template";
        return this;
    }

    public CommandWrapperBuilder updateGroup(final Long groupId) {
        this.actionName = "UPDATE";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    public CommandWrapperBuilder activateGroup(final Long groupId) {
        this.actionName = "ACTIVATE";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=activate";
        return this;
    }
    
    public CommandWrapperBuilder saveGroupCollectionSheet(final Long groupId) {
        this.actionName = "SAVECOLLECTIONSHEET";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=saveCollectionSheet";
        return this;
    }
    
    public CommandWrapperBuilder deleteGroup(final Long groupId) {
        this.actionName = "DELETE";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    public CommandWrapperBuilder associateClientsToGroup(final Long groupId) {
        this.actionName = "ASSOCIATECLIENTS";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=associateClients";
        return this;
    }

    public CommandWrapperBuilder disassociateClientsFromGroup(final Long groupId) {
        this.actionName = "DISASSOCIATECLIENTS";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=disassociateClients";
        return this;
    }

    public CommandWrapperBuilder unassignGroupStaff(final Long groupId) {
        this.actionName = "UNASSIGNSTAFF";
        this.entityName = "GROUP";
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    public CommandWrapperBuilder createCollateral(final Long loanId) {
        this.actionName = "CREATE";
        this.entityName = "COLLATERAL";
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/template";
        return this;
    }

    public CommandWrapperBuilder updateCollateral(final Long loanId, final Long collateralId) {
        this.actionName = "UPDATE";
        this.entityName = "COLLATERAL";
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder deleteCollateral(final Long loanId, final Long collateralId) {
        this.actionName = "DELETE";
        this.entityName = "COLLATERAL";
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder updateCollectionSheet(final Long groupId) {
        this.actionName = "UPDATE";
        this.entityName = "COLLECTIONSHEET";
        this.entityId = groupId;
        this.href = "/groups/" + groupId + "/collectionsheet";
        return this;
    }

    public CommandWrapperBuilder createCenter() {
        this.actionName = "CREATE";
        this.entityName = "CENTER";
        this.href = "/centers/template";
        return this;
    }

    public CommandWrapperBuilder updateCenter(final Long centerId) {
        this.actionName = "UPDATE";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.href = "/centers/" + centerId;
        return this;
    }

    public CommandWrapperBuilder deleteCenter(final Long centerId) {
        this.actionName = "DELETE";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.href = "/centers/" + centerId;
        return this;
    }

    public CommandWrapperBuilder activateCenter(final Long centerId) {
        this.actionName = "ACTIVATE";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/centers/" + centerId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder saveCenterCollectionSheet(final Long centerId) {
        this.actionName = "SAVECOLLECTIONSHEET";
        this.entityName = "CENTER";
        this.entityId = centerId;
        this.href = "/centers/" + centerId + "?command=saveCollectionSheet";
        return this;
    }

    public CommandWrapperBuilder createAccountingRule() {
        this.actionName = "CREATE";
        this.entityName = "ACCOUNTINGRULE";
        this.entityId = null;
        this.href = "/accountingrules/template";
        return this;
    }

    public CommandWrapperBuilder updateAccountingRule(final Long accountingRuleId) {
        this.actionName = "UPDATE";
        this.entityName = "ACCOUNTINGRULE";
        this.entityId = accountingRuleId;
        this.href = "/accountingrules/" + accountingRuleId;
        return this;
    }

    public CommandWrapperBuilder deleteAccountingRule(final Long accountingRuleId) {
        this.actionName = "DELETE";
        this.entityName = "ACCOUNTINGRULE";
        this.entityId = accountingRuleId;
        this.href = "/accountingrules/" + accountingRuleId;
        return this;
    }
    
    public CommandWrapperBuilder createHoliday() {
        this.actionName = "CREATE";
        this.entityName = "HOLIDAY";
        this.entityId = null;
        this.href = "/holidays/template";
        return this;
    }

    public CommandWrapperBuilder assignRole(final Long groupId) {
        this.actionName = "ASSIGNROLE";
        this.entityName = "GROUP";
        this.groupId = groupId;
        this.entityId = null;
        this.href = "/groups/"+groupId+"?command=assignRole";
        return this;
    }

    public CommandWrapperBuilder unassignRole(final Long groupId, final Long roleId) {
        this.actionName = "UNASSIGNROLE";
        this.entityName = "GROUP";
        this.groupId = groupId;
        this.entityId = roleId;
        this.href = "/groups/"+groupId+"?command=unassignRole";
        return this;
    }

    public CommandWrapperBuilder updateRole(final Long groupId, final Long roleId) {
        this.actionName = "UPDATEROLE";
        this.entityName = "GROUP";
        this.groupId = groupId;
        this.entityId = roleId;
        this.href = "/groups/"+groupId+"?command=updateRole";
        return this;
    }
    
    public CommandWrapperBuilder unassignClientStaff(final Long clientId) {
        this.actionName = "UNASSIGNSTAFF";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=unassignStaff";
        return this;
    }

    public CommandWrapperBuilder assignClientStaff(final Long clientId) {
        this.actionName = "ASSIGNSTAFF";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=assignStaff";
        return this;
    }

    public CommandWrapperBuilder createProductMix(final Long productId) {
        this.actionName = "CREATE";
        this.entityName = "PRODUCTMIX";
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }

    public CommandWrapperBuilder updateProductMix(final Long productId) {
        this.actionName = "UPDATE";
        this.entityName = "PRODUCTMIX";
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }
    
    public CommandWrapperBuilder deleteProductMix(final Long productId) {
        this.actionName = "DELETE";
        this.entityName = "PRODUCTMIX";
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }
    
    public CommandWrapperBuilder withProduct(final Long productId) {
        this.productId = productId;
        return this;
    }
}