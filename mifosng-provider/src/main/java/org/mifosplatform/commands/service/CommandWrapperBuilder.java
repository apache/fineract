package org.mifosplatform.commands.service;

import org.mifosplatform.commands.domain.CommandWrapper;

public class CommandWrapperBuilder {

    private Long officeId;
    private Long groupId;
    private Long clientId;
    private Long loanId;
    private String actionName;
    private String entityName;
    private Long entityId;
    private String href;
    private String json = "{}";
    private Long apptableId;
    private Long datatableId;
    private Long codeId;

    public CommandWrapper build() {
        return new CommandWrapper(this.officeId, this.groupId, this.clientId, this.loanId, this.actionName, this.entityName, this.entityId,
                this.apptableId, this.datatableId, this.codeId, this.href, this.json);
    }

    // public CommandWrapperBuilder withEntityId(final Long withId) {
    // this.entityId = withId;
    // return this;
    // }

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

    public CommandWrapperBuilder createClientNote(final Long clientId) {
        this.actionName = "CREATE";
        this.entityName = "CLIENTNOTE";
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/notes/template";
        return this;
    }

    public CommandWrapperBuilder updateClientNote(final Long clientId, final Long noteId) {
        this.actionName = "UPDATE";
        this.entityName = "CLIENTNOTE";
        this.entityId = noteId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/notes";
        return this;
    }

    public CommandWrapperBuilder createClient() {
        this.actionName = "CREATE";
        this.entityName = "CLIENT";
        this.href = "/clients/template";
        return this;
    }

    public CommandWrapperBuilder updateClient(final Long clientId) {
        this.actionName = "UPDATE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.href = "/clients/" + clientId;
        return this;
    }

    public CommandWrapperBuilder deleteClient(final Long clientId) {
        this.actionName = "DELETE";
        this.entityName = "CLIENT";
        this.entityId = clientId;
        this.href = "/clients/" + clientId;
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder createDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.apptableId = apptableId;
        this.datatableId = datatableId;
        this.actionName = "CREATE";
        this.entityName = datatable;
        if (datatableId == null) {
            this.href = "/datatables/" + datatable + "/" + apptableId;
        } else {
            this.href = "/datatables/" + datatable + "/" + apptableId + "/" + datatableId;
        }
        return this;
    }

    public CommandWrapperBuilder updateDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.apptableId = apptableId;
        this.datatableId = datatableId;
        this.actionName = "UPDATE";
        this.entityName = datatable;
        if (datatableId == null) {
            this.href = "/datatables/" + datatable + "/" + apptableId;
        } else {
            this.href = "/datatables/" + datatable + "/" + apptableId + "/" + datatableId;
        }
        return this;
    }

    public CommandWrapperBuilder deleteDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.apptableId = apptableId;
        this.datatableId = datatableId;
        this.actionName = "DELETE";
        this.entityName = datatable;
        if (datatableId == null) {
            this.href = "/datatables/" + datatable + "/" + apptableId;
        } else {
            this.href = "/datatables/" + datatable + "/" + apptableId + "/" + datatableId;
        }
        return this;
    }

    public CommandWrapperBuilder createLoanCharge(final Long loanId) {
        this.actionName = "CREATE";
        this.entityName = "LOANCHARGE";
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges";
        return this;
    }

    public CommandWrapperBuilder updateLoanCharge(final Long loanId, Long loanChargeId) {
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

    public CommandWrapperBuilder updateCodeValue(Long codeId, final Long codeValueId) {
        this.actionName = "UPDATE";
        this.entityName = "CODEVALUE";
        this.entityId = codeValueId;
        this.codeId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }

    public CommandWrapperBuilder deleteCodeValue(Long codeId, final Long codeValueId) {
        this.actionName = "DELETE";
        this.entityName = "CODEVALUE";
        this.entityId = codeValueId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }
}