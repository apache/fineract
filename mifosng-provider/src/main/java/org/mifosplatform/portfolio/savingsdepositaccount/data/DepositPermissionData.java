package org.mifosplatform.portfolio.savingsdepositaccount.data;

public class DepositPermissionData {

    private final boolean rejectAllowed;
    private final boolean withdrawnByApplicantAllowed;
    private final boolean undoApprovalAllowed;
    private final boolean pendingApproval;
    private final boolean isInterestWithdrawalAllowed;
    private final boolean renewelAllowed;
    private final boolean isMaturedDepositAccount;

    private final boolean anyActionOnAccountAllowed;

    public DepositPermissionData(final boolean rejectAllowed, final boolean withdrawnByApplicantAllowed, final boolean undoApprovalAllowed,
            final boolean pendingApproval, final boolean interestWithdrawalAllowed, final boolean renewelAllowed,
            final boolean isMaturedDepositAccount) {
        this.rejectAllowed = rejectAllowed;
        this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
        this.undoApprovalAllowed = undoApprovalAllowed;
        this.pendingApproval = pendingApproval;
        this.isInterestWithdrawalAllowed = interestWithdrawalAllowed;
        this.renewelAllowed = renewelAllowed;
        this.isMaturedDepositAccount = isMaturedDepositAccount;
        this.anyActionOnAccountAllowed = isRejectAllowed() || isPendingApproval() || isUndoApprovalAllowed()
                || isWithdrawnByApplicantAllowed() || isInterestWithdrawalAllowed() || isRenewelAllowed() || isMaturedDepositAccount();
    }

    public boolean isRejectAllowed() {
        return rejectAllowed;
    }

    public boolean isWithdrawnByApplicantAllowed() {
        return withdrawnByApplicantAllowed;
    }

    public boolean isUndoApprovalAllowed() {
        return undoApprovalAllowed;
    }

    public boolean isPendingApproval() {
        return pendingApproval;
    }

    public boolean isAnyActionOnAccountAllowed() {
        return anyActionOnAccountAllowed;
    }

    public boolean isInterestWithdrawalAllowed() {
        return isInterestWithdrawalAllowed;
    }

    public boolean isRenewelAllowed() {
        return renewelAllowed;
    }

    public boolean isMaturedDepositAccount() {
        return isMaturedDepositAccount;
    }
}
