/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.data;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for extracting flat data for joint liability group's
 * collection sheet.
 */
public class JLGCollectionSheetFlatData {

    private final String groupName;
    private final Long groupId;
    private final Long staffId;
    private final String staffName;
    private final Long levelId;
    private final String levelName;
    private final String clientName;
    private final Long clientId;
    private final Long loanId;
    private final String accountId;
    private final Integer accountStatusId;
    private final String productShortName;
    private final Long productId;
    private final CurrencyData currency;
    private BigDecimal disbursementAmount = BigDecimal.ZERO;
    private BigDecimal principalDue = BigDecimal.ZERO;
    private BigDecimal principalPaid = BigDecimal.ZERO;
    private BigDecimal interestDue = BigDecimal.ZERO;
    private BigDecimal interestPaid = BigDecimal.ZERO;
    private BigDecimal chargesDue = BigDecimal.ZERO;
    private final EnumOptionData attendanceType;

    public JLGCollectionSheetFlatData(final String groupName, final Long groupId, final Long staffId, final String staffName,
            final Long levelId, final String levelName, final String clientName, final Long clientId, final Long loanId,
            final String accountId, final Integer accountStatusId, final String productShortName, final Long productId,
            final CurrencyData currency, final BigDecimal disbursementAmount, final BigDecimal principalDue,
            final BigDecimal principalPaid, final BigDecimal interestDue, final BigDecimal interestPaid, final BigDecimal chargesDue,
            final EnumOptionData attendanceType) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.staffId = staffId;
        this.staffName = staffName;
        this.levelId = levelId;
        this.levelName = levelName;
        this.clientName = clientName;
        this.clientId = clientId;
        this.loanId = loanId;
        this.accountId = accountId;
        this.accountStatusId = accountStatusId;
        this.productShortName = productShortName;
        this.productId = productId;
        this.currency = currency;
        this.disbursementAmount = disbursementAmount;
        this.principalDue = principalDue;
        this.principalPaid = principalPaid;
        this.interestDue = interestDue;
        this.interestPaid = interestPaid;
        this.chargesDue = chargesDue;
        this.attendanceType = attendanceType;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public String getStaffName() {
        return this.staffName;
    }

    public Long getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return this.levelName;
    }

    public String getClientName() {
        return this.clientName;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public Integer getAccountStatusId() {
        return this.accountStatusId;
    }

    public String getProductShortName() {
        return this.productShortName;
    }

    public Long getProductId() {
        return this.productId;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getDisbursementAmount() {
        return this.disbursementAmount;
    }

    public BigDecimal getPrincipalDue() {
        return this.principalDue;
    }

    public BigDecimal getPrincipalPaid() {
        return this.principalPaid;
    }

    public BigDecimal getInterestDue() {
        return this.interestDue;
    }

    public BigDecimal getInterestPaid() {
        return this.interestPaid;
    }

    public BigDecimal getChargesDue() {
        return this.chargesDue;
    }

    public LoanDueData getLoanDueData() {
        return new LoanDueData(this.loanId, this.accountId, this.accountStatusId, this.productShortName, this.productId, this.currency,
                this.disbursementAmount, this.principalDue, this.principalPaid, this.interestDue, this.interestPaid, this.chargesDue);
    }

    public JLGClientData getClientData() {
        return JLGClientData.withAttendance(this.clientId, this.clientName, this.attendanceType);
    }

    public JLGGroupData getJLGGroupData() {

        return JLGGroupData.instance(this.groupId, this.groupName, this.staffId, this.staffName, this.levelId, this.levelName);
    }

}