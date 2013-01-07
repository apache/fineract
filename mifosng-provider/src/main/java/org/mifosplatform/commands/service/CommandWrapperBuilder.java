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

    public CommandWrapper build() {
        return new CommandWrapper(this.officeId, this.groupId, this.clientId, this.loanId, this.actionName, this.entityName, this.entityId, this.apptableId, this.datatableId,
                this.href, this.json);
    }

    public CommandWrapperBuilder withEntityId(final Long withId) {
        this.entityId = withId;
        return this;
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

    public CommandWrapperBuilder withUrl(final String withUrl) {
        this.href = withUrl;
        return this;
    }

    public CommandWrapperBuilder updatePermissions() {
        this.actionName = "UPDATE";
        this.entityName = "PERMISSION";
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
        return this;
    }

    public CommandWrapperBuilder updateUser() {
        this.actionName = "UPDATE";
        this.entityName = "USER";
        return this;
    }

    public CommandWrapperBuilder deleteUser() {
        this.actionName = "DELETE";
        this.entityName = "USER";
        return this;
    }

    public CommandWrapperBuilder createOffice() {
        this.actionName = "CREATE";
        this.entityName = "OFFICE";
        return this;
    }

    public CommandWrapperBuilder updateOffice() {
        this.actionName = "UPDATE";
        this.entityName = "OFFICE";
        return this;
    }

    public CommandWrapperBuilder createOfficeTransaction() {
        this.actionName = "CREATE";
        this.entityName = "OFFICETRANSACTION";
        return this;
    }

    public CommandWrapperBuilder createStaff() {
        this.actionName = "CREATE";
        this.entityName = "STAFF";
        return this;
    }

    public CommandWrapperBuilder updateStaff() {
        this.actionName = "UPDATE";
        this.entityName = "STAFF";
        return this;
    }

    public CommandWrapperBuilder createFund() {
        this.actionName = "CREATE";
        this.entityName = "FUND";
        return this;
    }

    public CommandWrapperBuilder updateFund() {
        this.actionName = "UPDATE";
        this.entityName = "FUND";
        return this;
    }

    public CommandWrapperBuilder updateCurrencies() {
        this.actionName = "UPDATE";
        this.entityName = "CURRENCY";
        return this;
    }

    public CommandWrapperBuilder createCode() {
        this.actionName = "CREATE";
        this.entityName = "CODE";
        return this;
    }

    public CommandWrapperBuilder updateCode() {
        this.actionName = "UPDATE";
        this.entityName = "CODE";
        return this;
    }

    public CommandWrapperBuilder deleteCode() {
        this.actionName = "DELETE";
        this.entityName = "CODE";
        return this;
    }

    public CommandWrapperBuilder createCharge() {
        this.actionName = "CREATE";
        this.entityName = "CHARGE";
        return this;
    }

    public CommandWrapperBuilder updateCharge() {
        this.actionName = "UPDATE";
        this.entityName = "CHARGE";
        return this;
    }

    public CommandWrapperBuilder deleteCharge() {
        this.actionName = "DELETE";
        this.entityName = "CHARGE";
        return this;
    }

    public CommandWrapperBuilder createLoanProduct() {
        this.actionName = "CREATE";
        this.entityName = "LOANPRODUCT";
        return this;
    }

    public CommandWrapperBuilder updateLoanProduct() {
        this.actionName = "UPDATE";
        this.entityName = "LOANPRODUCT";
        return this;
    }

    public CommandWrapperBuilder createClientIdentifier() {
        this.actionName = "CREATE";
        this.entityName = "CLIENTIDENTIFIER";
        return this;
    }

    public CommandWrapperBuilder updateClientIdentifier() {
        this.actionName = "UPDATE";
        this.entityName = "CLIENTIDENTIFIER";
        return this;
    }

    public CommandWrapperBuilder deleteClientIdentifier() {
        this.actionName = "DELETE";
        this.entityName = "CLIENTIDENTIFIER";
        return this;
    }

    public CommandWrapperBuilder createClientNote() {
        this.actionName = "CREATE";
        this.entityName = "CLIENTNOTE";
        return this;
    }

    public CommandWrapperBuilder updateClientNote() {
        this.actionName = "UPDATE";
        this.entityName = "CLIENTNOTE";
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
        return this;
    }

    public CommandWrapperBuilder updateDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.apptableId = apptableId;
        this.datatableId = datatableId;
        this.actionName = "UPDATE";
        this.entityName = datatable;
        return this;
    }

    public CommandWrapperBuilder deleteDatatable(final String datatable, final Long apptableId, final Long datatableId) {
        this.apptableId = apptableId;
        this.datatableId = datatableId;
        this.actionName = "DELETE";
        this.entityName = datatable;
        return this;
    }

    public CommandWrapperBuilder createLoanCharge() {
        this.actionName = "CREATE";
        this.entityName = "LOANCHARGE";
        return this;
    }

    public CommandWrapperBuilder updateLoanCharge() {
        this.actionName = "UPDATE";
        this.entityName = "LOANCHARGE";
        return this;
    }

    public CommandWrapperBuilder waiveLoanCharge() {
        this.actionName = "WAIVE";
        this.entityName = "LOANCHARGE";
        return this;

    }

    public CommandWrapperBuilder deleteLoanCharge() {
        this.actionName = "DELETE";
        this.entityName = "LOANCHARGE";
        return this;
    }

    public CommandWrapperBuilder loanRepaymentTransaction() {
        this.actionName = "REPAYMENT";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder waiveInterestPortionTransaction() {
        this.actionName = "WAIVEINTERESTPORTION";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder writeOffLoanTransaction() {
        this.actionName = "WRITEOFF";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder closeLoanAsRescheduledTransaction() {
        this.actionName = "CLOSEASRESCHEDULED";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder closeLoanTransaction() {
        this.actionName = "CLOSE";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder adjustTransaction() {
        this.actionName = "ADJUST";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder createLoanApplication() {
        this.actionName = "CREATE";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder updateLoanApplication() {
        this.actionName = "UPDATE";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder deleteLoanApplication() {
        this.actionName = "DELETE";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder rejectLoanApplication() {
        this.actionName = "REJECT";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder withdrawLoanApplication() {
        this.actionName = "WITHDRAW";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder approveLoanApplication() {
        this.actionName = "APPROVE";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder disburseLoanApplication() {
        this.actionName = "DISBURSE";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder undoLoanApplicationApproval() {
        this.actionName = "APPROVALUNDO";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder undoLoanApplicationDisbursal() {
        this.actionName = "DISBURSALUNDO";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder assignLoanOfficer() {
        this.actionName = "UPDATELOANOFFICER";
        this.entityName = "LOAN";
        return this;
    }

    public CommandWrapperBuilder assignLoanOfficersInBulk() {
        this.actionName = "BULKREASSIGN";
        this.entityName = "LOAN";
        return this;
    }
}