/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.data;

public class SavingPermissionData {
	


    private final boolean rejectAllowed;
    private final boolean withdrawnByApplicantAllowed;
    private final boolean undoApprovalAllowed;
    private final boolean pendingApproval;
    private final boolean renewelAllowed;
    private final boolean isMaturedDepositAccount;

    private final boolean anyActionOnAccountAllowed;

    public SavingPermissionData(final boolean rejectAllowed, final boolean withdrawnByApplicantAllowed, final boolean undoApprovalAllowed,
            final boolean pendingApproval, final boolean renewelAllowed, final boolean isMaturedDepositAccount) {
        this.rejectAllowed = rejectAllowed;
        this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
        this.undoApprovalAllowed = undoApprovalAllowed;
        this.pendingApproval = pendingApproval;
        this.renewelAllowed = renewelAllowed;
        this.isMaturedDepositAccount = isMaturedDepositAccount;
        this.anyActionOnAccountAllowed = isRejectAllowed() || isPendingApproval() || isUndoApprovalAllowed()
                || isWithdrawnByApplicantAllowed() || isRenewelAllowed() || isMaturedDepositAccount();
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

    public boolean isRenewelAllowed() {
        return renewelAllowed;
    }

    public boolean isMaturedDepositAccount() {
        return isMaturedDepositAccount;
    }


}
