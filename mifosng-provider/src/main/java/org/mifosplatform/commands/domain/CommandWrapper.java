package org.mifosplatform.commands.domain;

public class CommandWrapper {

    private final Long commandId;
    private final String actionName;
    private final String entityName;
    private final String apiOperation;
    private final String resource;
    private final Long resourceId;
    private final String subResource;
    private final Long subResourceId;

    public static CommandWrapper wrap(final String actionName, final String enityName, final String apiOperation, final String resource,
            final Long resourceId) {
        return new CommandWrapper(null, actionName, enityName, apiOperation, resource, resourceId, null, null);
    }

    public static CommandWrapper wrap(final String actionName, final String enityName, final String apiOperation, final String resource,
            final Long resourceId, final String subResource, final Long subRescourceId) {
        return new CommandWrapper(null, actionName, enityName, apiOperation, resource, resourceId, subResource, subRescourceId);
    }

    public static CommandWrapper fromExistingCommand(final Long commandId, final String actionName, final String enityName,
            final String apiOperation, final String resource, final Long resourceId, final String subResource, final Long subRescourceId) {
        return new CommandWrapper(commandId, actionName, enityName, apiOperation, resource, resourceId, subResource, subRescourceId);
    }

    private CommandWrapper(final Long commandId, final String actionName, final String enityName, final String apiOperation,
            final String resource, final Long resourceId, final String subResource, final Long subResourceId) {
        this.commandId = commandId;
        this.actionName = actionName;
        this.entityName = enityName;
        this.apiOperation = apiOperation;
        this.resource = resource;
        this.resourceId = resourceId;
        this.subResource = subResource;
        this.subResourceId = subResourceId;
    }

    public String commandName() {
        return this.apiOperation + '-' + this.resource;
    }

    public Long commandId() {
        return this.commandId;
    }

    public String actionName() {
        return this.actionName;
    }

    public String entityName() {
        return this.entityName;
    }

    public Long resourceId() {
        return this.resourceId;
    }

    public String resourceName() {
        return this.resource;
    }

    public Long subResourceId() {
        return this.subResourceId;
    }

    public String subResourceName() {
        return this.subResource;
    }

    public String taskPermissionName() {
        return this.actionName + "_" + this.entityName;
    }

    public String operation() {
        return this.apiOperation;
    }

    public boolean isCreate() {
        return this.apiOperation.equalsIgnoreCase("CREATE");
    }

    public boolean isUpdate() {
        // permissions resource has special update which involves no resource.
        return (isPermissionResource() && isUpdateOperation()) || (isCurrencyResource() && isUpdateOperation())
                || (isUpdateOperation() && this.resourceId != null);
    }

    private boolean isUpdateOperation() {
        return this.apiOperation.equalsIgnoreCase("UPDATE");
    }

    public boolean isDelete() {
        return this.apiOperation.equalsIgnoreCase("DELETE") && this.resourceId != null;
    }

    public boolean isUpdateRolePermissions() {
        return this.apiOperation.equalsIgnoreCase("UPDATEPERMISSIONS") && this.resourceId != null;
    }

    public boolean isPermissionResource() {
        return this.resource.equalsIgnoreCase("PERMISSIONS");
    }

    public boolean isRoleResource() {
        return this.resource.equalsIgnoreCase("ROLES");
    }

    public boolean isUserResource() {
        return this.resource.equalsIgnoreCase("USERS");
    }

    public boolean isCurrencyResource() {
        return this.resource.equalsIgnoreCase("CURRENCIES");
    }

    public boolean isCodeResource() {
        return this.resource.equalsIgnoreCase("CODES");
    }

    public boolean isStaffResource() {
        return this.resource.equalsIgnoreCase("STAFF");
    }

    public boolean isFundResource() {
        return this.resource.equalsIgnoreCase("FUNDS");
    }

    public boolean isOfficeResource() {
        return this.resource.equalsIgnoreCase("OFFICES");
    }

    public boolean isOfficeTransactionResource() {
        return this.resource.equalsIgnoreCase("OFFICETRANSACTIONS");
    }

    public boolean isChargeDefinitionResource() {
        return this.resource.equalsIgnoreCase("CHARGES");
    }

    public boolean isLoanProductResource() {
        return this.resource.equalsIgnoreCase("LOANPRODUCTS");
    }

    public boolean isClientResource() {
        return this.resource.equalsIgnoreCase("CLIENTS");
    }

    public boolean isClientIdentifierResource() {
        return isClientResource() && isIdentifiersSubResource();
    }

    public boolean isClientNoteResource() {
        return isClientResource() && isNotesSubResource();
    }

    public boolean isNotesSubResource() {
        return "NOTES".equalsIgnoreCase(this.subResource);
    }

    public boolean isIdentifiersSubResource() {
        return "IDENTIFIERS".equalsIgnoreCase(this.subResource);
    }

    public boolean isLoanResource() {
        return this.resource.equalsIgnoreCase("LOANS");
    }

    public boolean isApproveLoanApplication() {
        return this.actionName.equalsIgnoreCase("APPROVE") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isUndoApprovalOfLoanApplication() {
        return this.actionName.equalsIgnoreCase("APPROVALUNDO") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isApplicantWithdrawalFromLoanApplication() {
        return this.actionName.equalsIgnoreCase("WITHDRAW") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isRejectionOfLoanApplication() {
        return this.actionName.equalsIgnoreCase("REJECT") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isDisbursementOfLoan() {
        return this.actionName.equalsIgnoreCase("DISBURSE") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isUndoDisbursementOfLoan() {
        return this.actionName.equalsIgnoreCase("DISBURSALUNDO") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isLoanRepayment() {
        return this.actionName.equalsIgnoreCase("REPAYMENT") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isLoanRepaymentAdjustment() {
        return this.actionName.equalsIgnoreCase("ADJUST") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isWaiveInterestPortionOnLoan() {
        return this.actionName.equalsIgnoreCase("WAIVEINTERESTPORTION") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isLoanWriteOff() {
        return this.actionName.equalsIgnoreCase("WRITEOFF") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isCloseLoanAsObligationsMet() {
        return this.actionName.equalsIgnoreCase("CLOSE") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isCloseLoanAsRescheduled() {
        return this.actionName.equalsIgnoreCase("CLOSEASRESCHEDULED") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isAddLoanCharge() {
        return this.actionName.equalsIgnoreCase("CREATE") && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }

    public boolean isDeleteLoanCharge() {
        return this.actionName.equalsIgnoreCase("DELETE") && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }
    
    public boolean isUpdateLoanCharge() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }

    public boolean isWaiveLoanCharge() {
        return this.actionName.equalsIgnoreCase("WAIVE") && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }
}