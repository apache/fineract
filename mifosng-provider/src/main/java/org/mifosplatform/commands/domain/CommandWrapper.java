/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.domain;

import org.mifosplatform.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.mifosplatform.portfolio.savings.DepositsApiConstants;
import org.mifosplatform.useradministration.api.PasswordPreferencesApiConstants;

public class CommandWrapper {

    private final Long commandId;
    @SuppressWarnings("unused")
    private final Long officeId;
    private final Long groupId;
    private final Long clientId;
    private final Long loanId;
    private final Long savingsId;
    private final String actionName;
    private final String entityName;
    private final String taskPermissionName;
    private final Long entityId;
    private final Long subentityId;
    private final String href;
    private final String json;
    private final String transactionId;
    private final Long productId;

    @SuppressWarnings("unused")
    private Long templateId;

    public static CommandWrapper wrap(final String actionName, final String entityName, final Long resourceId, final Long subresourceId) {
        return new CommandWrapper(null, actionName, entityName, resourceId, subresourceId, null, null);
    }

    public static CommandWrapper fromExistingCommand(final Long commandId, final String actionName, final String entityName,
            final Long resourceId, final Long subresourceId, final String resourceGetUrl, final Long productId) {
        return new CommandWrapper(commandId, actionName, entityName, resourceId, subresourceId, resourceGetUrl, productId);
    }

    public static CommandWrapper fromExistingCommand(final Long commandId, final String actionName, final String entityName,
            final Long resourceId, final Long subresourceId, final String resourceGetUrl, final Long productId, final Long officeId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId) {
        return new CommandWrapper(commandId, actionName, entityName, resourceId, subresourceId, resourceGetUrl, productId, officeId,
                groupId, clientId, loanId, savingsId, transactionId);
    }

    private CommandWrapper(final Long commandId, final String actionName, final String entityName, final Long resourceId,
            final Long subresourceId, final String resourceGetUrl, final Long productId) {
        this.commandId = commandId;
        this.officeId = null;
        this.groupId = null;
        this.clientId = null;
        this.loanId = null;
        this.savingsId = null;
        this.actionName = actionName;
        this.entityName = entityName;
        this.taskPermissionName = actionName + "_" + entityName;
        this.entityId = resourceId;
        this.subentityId = subresourceId;
        this.href = resourceGetUrl;
        this.json = null;
        this.transactionId = null;
        this.productId = productId;
    }

    public CommandWrapper(final Long officeId, final Long groupId, final Long clientId, final Long loanId, final Long savingsId,
            final String actionName, final String entityName, final Long entityId, final Long subentityId, final String href,
            final String json, final String transactionId, final Long productId, final Long templateId) {

        this.commandId = null;
        this.officeId = officeId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.actionName = actionName;
        this.entityName = entityName;
        this.taskPermissionName = actionName + "_" + entityName;
        this.entityId = entityId;
        this.subentityId = subentityId;
        this.href = href;
        this.json = json;
        this.transactionId = transactionId;
        this.productId = productId;
        this.templateId = templateId;
    }

    private CommandWrapper(final Long commandId, final String actionName, final String entityName, final Long resourceId,
            final Long subresourceId, final String resourceGetUrl, final Long productId, final Long officeId, final Long groupId,
            final Long clientId, final Long loanId, final Long savingsId, final String transactionId) {

        this.commandId = commandId;
        this.officeId = officeId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.actionName = actionName;
        this.entityName = entityName;
        this.taskPermissionName = actionName + "_" + entityName;
        this.entityId = resourceId;
        this.subentityId = subresourceId;
        this.href = resourceGetUrl;
        this.json = null;
        this.transactionId = transactionId;
        this.productId = productId;
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
        return this.entityId;
    }

    public Long subresourceId() {
        return this.subentityId;
    }

    public String taskPermissionName() {
        return this.actionName + "_" + this.entityName;
    }

    public boolean isCreate() {
        return this.actionName.equalsIgnoreCase("CREATE");
    }

    public boolean isCreateDatatable() {
        return this.actionName.equalsIgnoreCase("CREATE") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public boolean isDeleteDatatable() {
        return this.actionName.equalsIgnoreCase("DELETE") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public boolean isUpdateDatatable() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public String getTaskPermissionName() {
        return this.taskPermissionName;
    }

    public String getHref() {
        return this.href;
    }

    public String getJson() {
        return this.json;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public Long getEntityId() {
        return this.entityId;
    }

    public Long getSubentityId() {
        return this.subentityId;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Long getSavingsId() {
        return this.savingsId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public boolean isUpdate() {
        // permissions resource has special update which involves no resource.
        return isPermissionResource() && isUpdateOperation() || isCurrencyResource() && isUpdateOperation() || isCacheResource()
                && isUpdateOperation() || isWorkingDaysResource() && isUpdateOperation() || isPasswordPreferencesResource()
                && isUpdateOperation() || isUpdateOperation() && this.entityId != null;
    }

    public boolean isUpdateOperation() {
        return this.actionName.equalsIgnoreCase("UPDATE");
    }

    public boolean isDelete() {
        return isDeleteOperation() && this.entityId != null;
    }

    public boolean isDeleteOperation() {
        return this.actionName.equalsIgnoreCase("DELETE");
    }

    public boolean isUpdateRolePermissions() {
        return this.actionName.equalsIgnoreCase("PERMISSIONS") && this.entityId != null;
    }

    public boolean isConfigurationResource() {
        return this.entityName.equalsIgnoreCase("CONFIGURATION");
    }

    public boolean isPermissionResource() {
        return this.entityName.equalsIgnoreCase("PERMISSION");
    }

    public boolean isRoleResource() {
        return this.entityName.equalsIgnoreCase("ROLE");
    }

    public boolean isUserResource() {
        return this.entityName.equalsIgnoreCase("USER");
    }

    public boolean isHookResource() {
        return this.entityName.equalsIgnoreCase("HOOK");
    }

    public boolean isCurrencyResource() {
        return this.entityName.equalsIgnoreCase("CURRENCY");
    }

    public boolean isSmsResource() {
        return this.entityName.equalsIgnoreCase("SMS");
    }

    public boolean isSmsCampaignResource() {
        return this.entityName.equals("SMS_CAMPAIGN");
    }

    public boolean isLoanRescheduleResource() {
        return this.entityName.equals(RescheduleLoansApiConstants.ENTITY_NAME);
    }

    public boolean isAccountNumberFormatResource() {
        return this.entityName.equals(AccountNumberFormatConstants.ENTITY_NAME.toUpperCase());
    }

    public boolean isApprove() {
        return this.actionName.equalsIgnoreCase("APPROVE");
    }

    public boolean isReject() {
        return this.actionName.equalsIgnoreCase("REJECT");
    }

    public boolean isSmsCampaignActivation() {
        return this.actionName.equalsIgnoreCase("ACTIVATE") && this.entityName.equalsIgnoreCase("SMS_CAMPAIGN");
    }

    public boolean isSmsCampaignClosure() {
        return this.actionName.equalsIgnoreCase("CLOSE") && this.entityName.equalsIgnoreCase("SMS_CAMPAIGN");
    }

    public boolean isCodeResource() {
        return this.entityName.equalsIgnoreCase("CODE");
    }

    public boolean isCodeValueResource() {
        return this.entityName.equalsIgnoreCase("CODEVALUE");
    }

    public boolean isStaffResource() {
        return this.entityName.equalsIgnoreCase("STAFF");
    }

    public boolean isGuarantorResource() {
        return this.entityName.equalsIgnoreCase("GUARANTOR");
    }

    public boolean isGuaranteeRecovery() {
        return this.actionName.equalsIgnoreCase("RECOVERGUARANTEES");
    }

    public boolean isGLAccountResource() {
        return this.entityName.equalsIgnoreCase("GLACCOUNT");
    }

    public boolean isGLClosureResource() {
        return this.entityName.equalsIgnoreCase("GLCLOSURE");
    }

    public boolean isJournalEntryResource() {
        return this.entityName.equalsIgnoreCase("JOURNALENTRY");
    }

    public boolean isPeriodicAccrualResource() {
        return this.entityName.equalsIgnoreCase("PERIODICACCRUALACCOUNTING");
    }

    public boolean isExecute() {
        return this.actionName.equalsIgnoreCase("EXECUTE");
    }

    public boolean isRevertJournalEntry() {
        return this.actionName.equalsIgnoreCase("REVERSE") && this.entityName.equalsIgnoreCase("JOURNALENTRY");
    }

    public boolean isUpdateRunningbalance() {
        return this.actionName.equalsIgnoreCase("UPDATERUNNINGBALANCE") && this.entityName.equalsIgnoreCase("JOURNALENTRY");
    }

    public boolean isDefineOpeningalance() {
        return this.actionName.equalsIgnoreCase("DEFINEOPENINGBALANCE") && this.entityName.equalsIgnoreCase("JOURNALENTRY");
    }

    public boolean isUpdateOpeningbalance() {
        return this.actionName.equalsIgnoreCase("UPDATEOPENINGBALANCE") && this.entityName.equalsIgnoreCase("JOURNALENTRY");
    }

    public boolean isFundResource() {
        return this.entityName.equalsIgnoreCase("FUND");
    }

    public boolean isOfficeResource() {
        return this.entityName.equalsIgnoreCase("OFFICE");
    }

    public boolean isOfficeTransactionResource() {
        return this.entityName.equalsIgnoreCase("OFFICETRANSACTION");
    }

    public boolean isChargeDefinitionResource() {
        return this.entityName.equalsIgnoreCase("CHARGE");
    }

    public boolean isLoanProductResource() {
        return this.entityName.equalsIgnoreCase("LOANPRODUCT");
    }

    public boolean isClientResource() {
        return this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isClientActivation() {
        return this.actionName.equalsIgnoreCase("ACTIVATE") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isGroupActivation() {
        return this.actionName.equalsIgnoreCase("ACTIVATE") && this.entityName.equalsIgnoreCase("GROUP");
    }

    public boolean isGroupClose() {
        return this.actionName.equalsIgnoreCase("CLOSE") && this.entityName.equalsIgnoreCase("GROUP");
    }

    public boolean isCenterActivation() {
        return this.actionName.equalsIgnoreCase("ACTIVATE") && this.entityName.equalsIgnoreCase("CENTER");
    }

    public boolean isCenterClose() {
        return this.actionName.equalsIgnoreCase("CLOSE") && this.entityName.equalsIgnoreCase("CENTER");
    }

    public boolean isCenterAssociateGroups() {
        return this.actionName.equalsIgnoreCase("ASSOCIATEGROUPS") && this.entityName.equalsIgnoreCase("CENTER");
    }

    public boolean isCenterDisassociateGroups() {
        return this.actionName.equalsIgnoreCase("DISASSOCIATEGROUPS") && this.entityName.equalsIgnoreCase("CENTER");
    }

    public boolean isClientIdentifierResource() {
        return this.entityName.equals("CLIENTIDENTIFIER");
    }

    public boolean isClientNoteResource() {
        return this.entityName.equals("CLIENTNOTE");
    }

    public boolean isLoanResource() {
        return this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isTellerResource() {
        return this.entityName.equalsIgnoreCase("TELLER");
    }

    public boolean isAllocateCashier() {
        return this.actionName.equalsIgnoreCase("ALLOCATECASHIER");
    }

    public boolean isUpdateCashierAllocation() {
        return this.actionName.equalsIgnoreCase("UPDATECASHIERALLOCATION");
    }

    public boolean isDeleteCashierAllocation() {
        return this.actionName.equalsIgnoreCase("DELETECASHIERALLOCATION");
    }

    public boolean isAllocateCashToCashier() {
        return this.actionName.equalsIgnoreCase("ALLOCATECASHTOCASHIER");
    }

    public boolean isSettleCashFromCashier() {
        return this.actionName.equalsIgnoreCase("SETTLECASHFROMCASHIER");
    }

    public boolean isLoanChargeResource() {
        return this.entityName.equalsIgnoreCase("LOANCHARGE");
    }

    public boolean isLoanDisburseDetailResource() {
        return this.entityName.equalsIgnoreCase("DISBURSEMENTDETAIL");
    }

    public boolean isCollateralResource() {
        return this.entityName.equalsIgnoreCase("COLLATERAL");
    }

    public boolean isTemplateRessource() {
        return this.entityName.equalsIgnoreCase("Template");
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

    public boolean isDisbursementOfLoanToSavings() {
        return this.actionName.equalsIgnoreCase("DISBURSETOSAVINGS") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isUndoDisbursementOfLoan() {
        return this.actionName.equalsIgnoreCase("DISBURSALUNDO") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isLoanRepayment() {
        return this.actionName.equalsIgnoreCase("REPAYMENT") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isLoanRecoveryPayment() {
        return this.actionName.equalsIgnoreCase("RECOVERYPAYMENT") && this.entityName.equalsIgnoreCase("LOAN");
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

    public boolean isUndoLoanWriteOff() {
        return this.actionName.equalsIgnoreCase("UNDOWRITEOFF") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isUpdateDisbursementDate() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.entityName.equalsIgnoreCase("DISBURSEMENTDETAIL")
                && this.entityId != null;
    }

    public boolean addAndDeleteDisbursementDetails() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.entityName.equalsIgnoreCase("DISBURSEMENTDETAIL")
                && this.entityId == null;
    }

    public boolean isStandingInstruction() {
        return this.entityName.equalsIgnoreCase("STANDINGINSTRUCTION");
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
        return isDeleteOperation() && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }

    public boolean isUpdateLoanCharge() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }

    public boolean isWaiveLoanCharge() {
        return this.actionName.equalsIgnoreCase("WAIVE") && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }

    public boolean isPayLoanCharge() {
        return this.actionName.equalsIgnoreCase("PAY") && this.entityName.equalsIgnoreCase("LOANCHARGE");
    }

    public boolean isUpdateLoanOfficer() {
        return this.actionName.equalsIgnoreCase("UPDATELOANOFFICER") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isRemoveLoanOfficer() {
        return this.actionName.equalsIgnoreCase("REMOVELOANOFFICER") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isBulkUpdateLoanOfficer() {
        return this.actionName.equalsIgnoreCase("BULKREASSIGN") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isDatatableResource() {
        return this.href.startsWith("/datatables/");
    }

    public boolean isDeleteOneToOne() {
        /* also covers case of deleting all of a one to many */
        return isDatatableResource() && isDeleteOperation() && this.subentityId == null;
    }

    public boolean isDeleteMultiple() {
        return isDatatableResource() && isDeleteOperation() && this.subentityId != null;
    }

    public boolean isUpdateOneToOne() {
        return isDatatableResource() && isUpdateOperation() && this.subentityId == null;
    }

    public boolean isUpdateMultiple() {
        return isDatatableResource() && isUpdateOperation() && this.subentityId != null;
    }

    public boolean isUnassignStaff() {
        return this.actionName.equalsIgnoreCase("UNASSIGNSTAFF") && this.entityName.equalsIgnoreCase("GROUP");
    }

    public boolean isAssignStaff() {
        return this.actionName.equalsIgnoreCase("ASSIGNSTAFF");
    }

    public String commandName() {
        return this.actionName + "_" + this.entityName;
    }

    public boolean isUpdateOfOwnUserDetails(final Long loggedInUserId) {
        return isUserResource() && isUpdate() && loggedInUserId.equals(this.entityId);
    }

    public boolean isSavingsProductResource() {
        return this.entityName.equalsIgnoreCase("SAVINGSPRODUCT");
    }

    public boolean isSavingsAccountResource() {
        return this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isRejectionOfSavingsAccountApplication() {
        return this.actionName.equalsIgnoreCase("REJECT") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isWithdrawFromSavingsAccountApplicationByApplicant() {
        return this.actionName.equalsIgnoreCase("WITHDRAW") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isApprovalOfSavingsAccountApplication() {
        return this.actionName.equalsIgnoreCase("APPROVE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isUndoApprovalOfSavingsAccountApplication() {
        return this.actionName.equalsIgnoreCase("APPROVALUNDO") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountActivation() {
        return this.actionName.equalsIgnoreCase("ACTIVATE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountDeposit() {
        return this.actionName.equalsIgnoreCase("DEPOSIT") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountClose() {
        return this.actionName.equalsIgnoreCase("CLOSE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountWithdrawal() {
        return this.actionName.equalsIgnoreCase("WITHDRAWAL") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountInterestCalculation() {
        return this.actionName.equalsIgnoreCase("CALCULATEINTEREST") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountInterestPosting() {
        return this.actionName.equalsIgnoreCase("POSTINTEREST") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountApplyAnnualFee() {
        return this.actionName.equalsIgnoreCase("APPLYANNUALFEE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountUndoTransaction() {
        return this.actionName.equalsIgnoreCase("UNDOTRANSACTION") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isAdjustSavingsAccountTransaction() {
        return this.actionName.equalsIgnoreCase("ADJUSTTRANSACTION") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isUpdateSavingsOfficer() {
        return this.actionName.equalsIgnoreCase("UPDATESAVINGSOFFICER") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isRemoveSavingsOfficer() {
        return this.actionName.equalsIgnoreCase("REMOVESAVINGSOFFICER") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNT");
    }

    public boolean isSavingsAccountChargeResource() {
        return this.entityName.equalsIgnoreCase("SAVINGSACCOUNTCHARGE");
    }

    public boolean isAddSavingsAccountCharge() {
        return this.actionName.equalsIgnoreCase("CREATE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNTCHARGE");
    }

    public boolean isDeleteSavingsAccountCharge() {
        return isDeleteOperation() && this.entityName.equalsIgnoreCase("SAVINGSACCOUNTCHARGE");
    }

    public boolean isUpdateSavingsAccountCharge() {
        return this.actionName.equalsIgnoreCase("UPDATE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNTCHARGE");
    }

    public boolean isWaiveSavingsAccountCharge() {
        return this.actionName.equalsIgnoreCase("WAIVE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNTCHARGE");
    }

    public boolean isPaySavingsAccountCharge() {
        return this.actionName.equalsIgnoreCase("PAY") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNTCHARGE");
    }

    public boolean isInterestRateChartResource() {
        return this.entityName.equalsIgnoreCase("INTERESTRATECHART");
    }

    public boolean isInterestRateChartSlabResource() {
        return this.entityName.equalsIgnoreCase("CHARTSLAB");
    }

    public boolean isCalendarResource() {
        return this.entityName.equalsIgnoreCase("CALENDAR");
    }

    public boolean isNoteResource() {
        boolean isnoteResource = false;
        if (this.entityName.equalsIgnoreCase("CLIENTNOTE") || this.entityName.equalsIgnoreCase("LOANNOTE")
                || this.entityName.equalsIgnoreCase("LOANTRANSACTIONNOTE") || this.entityName.equalsIgnoreCase("SAVINGNOTE")
                || this.entityName.equalsIgnoreCase("GROUPNOTE")) {
            isnoteResource = true;
        }
        return isnoteResource;
    }

    public boolean isGroupResource() {
        return this.entityName.equalsIgnoreCase("GROUP");
    }

    public boolean isSaveGroupCollectionSheet() {
        return this.entityName.equalsIgnoreCase("GROUP") && this.actionName.equalsIgnoreCase("SAVECOLLECTIONSHEET");
    }

    public boolean isCollectionSheetResource() {
        return this.entityName.equals("COLLECTIONSHEET");
    }

    public boolean isSaveIndividualCollectionSheet() {
        return isCollectionSheetResource() && this.actionName.equalsIgnoreCase("SAVE");
    }

    public boolean isCenterResource() {
        return this.entityName.equalsIgnoreCase("CENTER");
    }

    public boolean isSaveCenterCollectionSheet() {
        return this.entityName.equalsIgnoreCase("CENTER") && this.actionName.equalsIgnoreCase("SAVECOLLECTIONSHEET");
    }

    public boolean isReportResource() {
        return this.entityName.equalsIgnoreCase("REPORT");
    }

    public boolean isAssociateClients() {
        return this.actionName.equalsIgnoreCase("ASSOCIATECLIENTS");
    }

    public boolean isDisassociateClients() {
        return this.actionName.equalsIgnoreCase("DISASSOCIATECLIENTS");
    }

    public boolean isXBRLMappingResource() {
        return this.entityName.equalsIgnoreCase("XBRLMAPPING");
    }

    public boolean isHolidayResource() {
        return this.entityName.equalsIgnoreCase("HOLIDAY");
    }

    public boolean isHolidayActivation() {
        return this.entityName.equalsIgnoreCase("HOLIDAY") && this.actionName.equalsIgnoreCase("ACTIVATE") && this.entityId != null;
    }

    public boolean isAccountingRuleResource() {
        return this.entityName.equalsIgnoreCase("ACCOUNTINGRULE");
    }

    public boolean isClientUnassignStaff() {
        return this.actionName.equalsIgnoreCase("UNASSIGNSTAFF") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isAssignGroupRole() {
        return this.entityName.equalsIgnoreCase("GROUP") && this.actionName.equalsIgnoreCase("ASSIGNROLE");
    }

    public boolean isUnAssignGroupRole() {
        return this.entityName.equalsIgnoreCase("GROUP") && this.actionName.equalsIgnoreCase("UNASSIGNROLE");
    }

    public boolean isUpdateGroupRole() {
        return this.entityName.equalsIgnoreCase("GROUP") && this.actionName.equalsIgnoreCase("UPDATEROLE");
    }

    public boolean isTransferClientsBetweenGroups() {
        return this.entityName.equalsIgnoreCase("GROUP") && this.actionName.equalsIgnoreCase("TRANSFERCLIENTS");
    }

    public boolean isClientAssignStaff() {
        return this.actionName.equalsIgnoreCase("ASSIGNSTAFF") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isClientClose() {
        return this.actionName.equalsIgnoreCase("CLOSE") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isProposeClientTransfer() {
        return this.actionName.equalsIgnoreCase("PROPOSETRANSFER") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isProposeAndAcceptClientTransfer() {
        return this.actionName.equalsIgnoreCase("PROPOSEANDACCEPTTRANSFER") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isWithdrawClientTransfer() {
        return this.actionName.equalsIgnoreCase("WITHDRAWTRANSFER") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isAcceptClientTransfer() {
        return this.actionName.equalsIgnoreCase("ACCEPTTRANSFER") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isRejectClientTransfer() {
        return this.actionName.equalsIgnoreCase("REJECTTRANSFER") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isUpdateClientSavingsAccount() {
        return this.actionName.equalsIgnoreCase("UPDATESAVINGSACCOUNT") && this.entityName.equalsIgnoreCase("CLIENT");
    }

    public boolean isProductMixResource() {
        return this.entityName.equalsIgnoreCase("PRODUCTMIX");
    }

    public boolean isSchedulerResource() {
        return this.entityName.equalsIgnoreCase("SCHEDULER");
    }

    public boolean isAccountTransferResource() {
        return this.entityName.equalsIgnoreCase("ACCOUNTTRANSFER");
    }

    public boolean isMeetingResource() {
        return this.entityName.equalsIgnoreCase("MEETING");
    }

    public boolean isSaveOrUpdateAttendance() {
        return this.actionName.equalsIgnoreCase("SAVEORUPDATEATTENDANCE");
    }

    public boolean isCacheResource() {
        return this.entityName.equalsIgnoreCase("CACHE");
    }

    // Begin - Deposit product
    public boolean isFixedDepositProductResource() {
        return this.entityName.equalsIgnoreCase("FIXEDDEPOSITPRODUCT");
    }

    public boolean isRecurringDepositProductResource() {
        return this.entityName.equalsIgnoreCase("RECURRINGDEPOSITPRODUCT");
    }

    // End - Deposit product

    // Begin - Deposit accounts
    public boolean isDepositAccountResource() {
        return isFixedDepositAccountResource() || isRecurringDepositAccountResource();
    }

    public boolean isFixedDepositAccountResource() {
        return this.entityName.equalsIgnoreCase("FIXEDDEPOSITACCOUNT");
    }

    public boolean isRecurringDepositAccountResource() {
        return this.entityName.equalsIgnoreCase("RECURRINGDEPOSITACCOUNT");
    }

    public boolean isFixedDepositAccountCreate() {
        return isCreate() && isFixedDepositAccountResource();
    }

    public boolean isRecurringDepositAccountCreate() {
        return isCreate() && isRecurringDepositAccountResource();
    }

    public boolean isFixedDepositAccountUpdate() {
        return isUpdate() && isFixedDepositAccountResource();
    }

    public boolean isRecurringDepositAccountUpdate() {
        return isUpdate() && isRecurringDepositAccountResource();
    }

    public boolean isFixedDepositAccountDelete() {
        return isDelete() && isFixedDepositAccountResource();
    }

    public boolean isRecurringDepositAccountDelete() {
        return isDelete() && isRecurringDepositAccountResource();
    }

    public boolean isRejectionOfFixedDepositAccountApplication() {
        return this.actionName.equalsIgnoreCase("REJECT") && isFixedDepositAccountResource();
    }

    public boolean isRejectionOfRecurringDepositAccountApplication() {
        return this.actionName.equalsIgnoreCase("REJECT") && isRecurringDepositAccountResource();
    }

    public boolean isWithdrawFixedDepositAccountApplicationByApplicant() {
        return this.actionName.equalsIgnoreCase("WITHDRAW") && isFixedDepositAccountResource();
    }

    public boolean isWithdrawRecurringDepositAccountApplicationByApplicant() {
        return this.actionName.equalsIgnoreCase("WITHDRAW") && isRecurringDepositAccountResource();
    }

    public boolean isApprovalOfFixedDepositAccountApplication() {
        return this.actionName.equalsIgnoreCase("APPROVE") && isFixedDepositAccountResource();
    }

    public boolean isApprovalOfRecurringDepositAccountApplication() {
        return this.actionName.equalsIgnoreCase("APPROVE") && isRecurringDepositAccountResource();
    }

    public boolean isUndoApprovalOfFixedDepositAccountApplication() {
        return this.actionName.equalsIgnoreCase("APPROVALUNDO") && isFixedDepositAccountResource();
    }

    public boolean isUndoApprovalOfRecurringDepositAccountApplication() {
        return this.actionName.equalsIgnoreCase("APPROVALUNDO") && isRecurringDepositAccountResource();
    }

    // Account transaction actions

    public boolean isDeposit() {
        return this.actionName.equalsIgnoreCase("DEPOSIT");
    }

    public boolean isWithdrawal() {
        return this.actionName.equalsIgnoreCase("WITHDRAWAL");
    }

    public boolean isWithdrawn() {
        return this.actionName.equalsIgnoreCase("WITHDRAW");
    }

    public boolean isReactivated() {
        return this.actionName.equalsIgnoreCase("REACTIVATE");
    }

    public boolean isActivation() {
        return this.actionName.equalsIgnoreCase("ACTIVATE");
    }

    public boolean isInterestCalculation() {
        return this.actionName.equalsIgnoreCase("CALCULATEINTEREST");
    }

    public boolean isInterestPosting() {
        return this.actionName.equalsIgnoreCase("POSTINTEREST");
    }

    public boolean isUndoTransaction() {
        return this.actionName.equalsIgnoreCase("UNDOTRANSACTION");
    }

    public boolean isAdjustTransaction() {
        return this.actionName.equalsIgnoreCase("ADJUSTTRANSACTION");
    }

    public boolean isDepositAccountClose() {
        return this.actionName.equalsIgnoreCase("CLOSE") && isDepositAccountResource();
    }

    public boolean isDepositAccountPrematureClose() {
        return this.actionName.equalsIgnoreCase("PREMATURECLOSE") && isDepositAccountResource();
    }

    public boolean isDepositAmountUpdateForRecurringDepositAccount() {
        return this.actionName.equalsIgnoreCase(DepositsApiConstants.UPDATE_DEPOSIT_AMOUNT.toUpperCase()) && isDepositAccountResource();
    }

    // End - Deposit accounts

    public boolean isFinancialActivityAccountMapping() {
        return this.entityName.equalsIgnoreCase("FINANCIALACTIVITYACCOUNT");
    }

    public boolean isRegisterDatatable() {
        return this.actionName.equalsIgnoreCase("REGISTER") && this.href.startsWith("/datatables/") && this.entityId == null;
    }

    public boolean isLikelihoodResource() {
        return this.entityName.equalsIgnoreCase("Likelihood");
    }

    public boolean isSurveyResource() {

        return this.href.startsWith("/survey/");

    }

    public boolean isUpdateLikelihood() {
        return this.actionName.equalsIgnoreCase("UPDATE");
    }

    public boolean isRegisterSurvey() {
        return this.actionName.equalsIgnoreCase("REGISTER");
    }

    public boolean isFullFilSurvey() {
        return this.actionName.equalsIgnoreCase("CREATE");
    }

    public boolean isInactivateSavingsAccountCharge() {
        return this.actionName.equalsIgnoreCase("INACTIVATE") && this.entityName.equalsIgnoreCase("SAVINGSACCOUNTCHARGE");
    }

    public boolean isRefundByTransfer() {
        return this.actionName.equalsIgnoreCase("REFUNDBYTRANSFER");
    }

    public boolean isLoanRefundByCash() {
        return this.actionName.equalsIgnoreCase("REFUNDBYCASH") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isUndoLoanRefund() {
        return this.actionName.equalsIgnoreCase("UNDOREFUND") && this.entityName.equalsIgnoreCase("LOAN");
    }

    public boolean isDisable() {
        return isDisableOperation() && this.entityId != null;
    }

    public boolean isDisableOperation() {
        return this.actionName.equalsIgnoreCase("DISABLE");
    }

    public boolean isEnable() {
        return isEnableOperation() && this.entityId != null;
    }

    public boolean isEnableOperation() {
        return this.actionName.equalsIgnoreCase("ENABLE");
    }

    public boolean isEntityMappingResource() {
        return this.entityName.equalsIgnoreCase("ENTITYMAPPING");
    }

    public boolean isWorkingDaysResource() {
        return this.entityName.equalsIgnoreCase("WORKINGDAYS");
    }

    public boolean isPasswordPreferencesResource() {
        return this.entityName.equalsIgnoreCase(PasswordPreferencesApiConstants.ENTITY_NAME);
    }

    public boolean isPaymentTypeResource() {
        return this.entityName.equalsIgnoreCase("PAYMENTTYPE");
    }
}