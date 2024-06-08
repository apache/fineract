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
package org.apache.fineract.portfolio.shareaccounts.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_share_account_transactions")
public class ShareAccountTransaction extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private ShareAccount shareAccount;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "total_shares")
    private Long totalShares;

    @Column(name = "unit_price")
    private BigDecimal shareValue;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "charge_amount")
    private BigDecimal chargeAmount;

    @Column(name = "status_enum", nullable = true)
    private Integer status;

    @Column(name = "type_enum", nullable = true)
    private Integer type;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareAccountTransaction", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ShareAccountChargePaidBy> shareAccountChargesPaid = new HashSet<>();

    protected ShareAccountTransaction() {

    }

    public void setShareAccount(final ShareAccount shareAccount) {
        this.shareAccount = shareAccount;
    }

    public ShareAccountTransaction(final LocalDate transactionDate, final Long totalShares, final BigDecimal shareValue) {
        this.transactionDate = transactionDate;
        this.totalShares = totalShares;
        this.shareValue = shareValue;
        this.status = PurchasedSharesStatusType.APPLIED.getValue();
        this.type = PurchasedSharesStatusType.PURCHASED.getValue();
        this.amount = shareValue.multiply(BigDecimal.valueOf(totalShares));
        this.amountPaid = new BigDecimal(this.amount.doubleValue());
    }

    private ShareAccountTransaction(final LocalDate transactionDate, final Long totalShares, final BigDecimal shareValue,
            final Integer status, final Integer type, final BigDecimal amount, final BigDecimal chargeAmount, final BigDecimal amountPaid) {
        this.transactionDate = transactionDate;
        this.totalShares = totalShares;
        this.shareValue = shareValue;
        this.status = status;
        this.type = type;
        this.amount = amount;
        this.chargeAmount = chargeAmount;
        this.amountPaid = amountPaid;
    }

    public static ShareAccountTransaction createRedeemTransaction(final LocalDate transactionDate, final Long totalShares,
            final BigDecimal shareValue) {
        final Integer status = PurchasedSharesStatusType.APPROVED.getValue();
        final Integer type = PurchasedSharesStatusType.REDEEMED.getValue();
        final BigDecimal amount = shareValue.multiply(BigDecimal.valueOf(totalShares));
        BigDecimal amountPaid = new BigDecimal(amount.doubleValue());
        return new ShareAccountTransaction(transactionDate, totalShares, shareValue, status, type, amount, null, amountPaid);
    }

    public static ShareAccountTransaction createChargeTransaction(final LocalDate transactionDate, final ShareAccountCharge charge) {
        final Long totalShares = null;
        final BigDecimal unitPrice = null;
        final Integer status = PurchasedSharesStatusType.APPROVED.getValue();
        final Integer type = PurchasedSharesStatusType.CHARGE_PAYMENT.getValue();
        BigDecimal amount = charge.percentageOrAmount();
        BigDecimal chargeAmount = null;
        BigDecimal amountPaid = null;
        return new ShareAccountTransaction(transactionDate, totalShares, unitPrice, status, type, amount, chargeAmount, amountPaid);
    }

    public LocalDate getPurchasedDate() {
        return this.transactionDate;
    }

    public Long getTotalShares() {
        return this.totalShares;
    }

    public BigDecimal getPurchasePrice() {
        return this.shareValue;
    }

    public void update(final LocalDate purchasedDate, final Long totalShares, final BigDecimal shareValue) {
        this.transactionDate = purchasedDate;
        this.totalShares = totalShares;
        this.shareValue = shareValue;
        this.amount = shareValue.multiply(BigDecimal.valueOf(totalShares));
        this.chargeAmount = BigDecimal.ZERO;
        this.status = PurchasedSharesStatusType.APPLIED.getValue();
    }

    public void approve() {
        this.status = PurchasedSharesStatusType.APPROVED.getValue();
    }

    public void undoApprove() {
        this.status = PurchasedSharesStatusType.APPLIED.getValue();
    }

    public void reject() {
        this.status = PurchasedSharesStatusType.REJECTED.getValue();
        if (this.chargeAmount != null) {
            this.amount = this.amount.subtract(chargeAmount);
        }
    }

    public boolean isPendingForApprovalTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPLIED.getValue())
                && this.type.equals(PurchasedSharesStatusType.PURCHASED.getValue());
    }

    public boolean isPurchasTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPROVED.getValue())
                && this.type.equals(PurchasedSharesStatusType.PURCHASED.getValue());
    }

    public boolean isRedeemTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPROVED.getValue())
                && this.type.equals(PurchasedSharesStatusType.REDEEMED.getValue());
    }

    public boolean isChargeTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPROVED.getValue())
                && this.type.equals(PurchasedSharesStatusType.CHARGE_PAYMENT.getValue());
    }

    public boolean isPurchaseRejectedTransaction() {
        return this.status.equals(PurchasedSharesStatusType.REJECTED.getValue())
                && this.type.equals(PurchasedSharesStatusType.PURCHASED.getValue());
    }

    public void addShareAccountChargePaidBy(final ShareAccountChargePaidBy chargePaidBy) {
        this.shareAccountChargesPaid.add(chargePaidBy);
    }

    public BigDecimal amount() {
        return this.amount;
    }

    public BigDecimal chargeAmount() {
        return this.chargeAmount;
    }

    public void updateChargeAmount(BigDecimal totalChargeAmount) {
        this.amount = this.amount.add(totalChargeAmount);
        this.chargeAmount = totalChargeAmount;
    }

    public void deductChargesFromTotalAmount(BigDecimal totalChargeAmount) {
        this.amount = this.amount.subtract(totalChargeAmount);
        this.chargeAmount = totalChargeAmount;
    }

    public Set<ShareAccountChargePaidBy> getChargesPaidBy() {
        return this.shareAccountChargesPaid;
    }

    public Integer getTransactionStatus() {
        return this.status;
    }

    public Integer getTransactionType() {
        return this.type;
    }

    public void updateAmountPaid(final BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void addAmountPaid(final BigDecimal amountPaid) {
        if (isRedeemTransaction()) {
            this.amountPaid = this.amountPaid.subtract(amountPaid);
        } else if (isPurchasTransaction() /*
                                           * || isPurchaseRejectedTransaction()
                                           */) {
            this.amountPaid = this.amountPaid.add(amountPaid);
        }
    }

    public void resetAmountPaid() {
        this.amountPaid = BigDecimal.ZERO;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!this.active) {
            // this.shareAccountChargesPaid.clear();
        }
    }

    public void updateTransactionDate(final LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public boolean isActive() {
        return this.active;
    }

    public BigDecimal shareValue() {
        return this.shareValue;
    }
}
