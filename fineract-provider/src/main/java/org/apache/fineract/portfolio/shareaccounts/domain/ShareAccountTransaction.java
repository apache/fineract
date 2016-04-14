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

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_share_account_transactions")
public class ShareAccountTransaction extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private ShareAccount shareAccount;

    @Column(name = "transaction_date")
    @Temporal(TemporalType.DATE)
    private Date transactionDate;

    @Column(name = "total_shares")
    private Long totalShares;

    @Column(name = "unit_price")
    private BigDecimal shareValue;

    @Column(name = "amount") 
    private BigDecimal amount ;
    
    @Column(name = "charge_amount") 
    private BigDecimal totalChargeAmount ;
    
    @Column(name = "status_enum", nullable = true)
    private Integer status;

    @Column(name = "type_enum", nullable = true) 
    private Integer type ;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareAccountTransaction", orphanRemoval = true)
    private final Set<ShareAccountChargePaidBy> shareAccountChargesPaid = new HashSet<>();
    
    protected ShareAccountTransaction() {

    }

    public void setShareAccount(final ShareAccount shareAccount) {
        this.shareAccount = shareAccount;
    }

    public ShareAccountTransaction(final Date transactionDate, final Long totalShares, final BigDecimal shareValue) {
        this.transactionDate = transactionDate;
        this.totalShares = totalShares;
        this.shareValue = shareValue;
        this.status = PurchasedSharesStatusType.APPLIED.getValue();
        this.type = PurchasedSharesStatusType.PURCHASED.getValue() ;
        this.amount = shareValue.multiply(BigDecimal.valueOf(totalShares)) ;
    }

    private ShareAccountTransaction(final Date transactionDate, final Long totalShares, final BigDecimal shareValue,
            final Integer status, final Integer type, final BigDecimal amount, final BigDecimal chargeAmount) {
        this.transactionDate = transactionDate;
        this.totalShares = totalShares;
        this.shareValue = shareValue;
        this.status = status ;
        this.type = type ;
        this.amount = amount ;
        this.totalChargeAmount = chargeAmount ;
    }
    
    public static ShareAccountTransaction createRedeemTransaction(final Date transactionDate, final Long totalShares, final BigDecimal shareValue) {
        final Integer status = PurchasedSharesStatusType.APPROVED.getValue() ;
        final Integer type = PurchasedSharesStatusType.REDEEMED.getValue() ;
        final BigDecimal amount = shareValue.multiply(BigDecimal.valueOf(totalShares)) ;
        return new ShareAccountTransaction(transactionDate, totalShares, shareValue, status, type, amount, null) ; 
    }
    
    public static ShareAccountTransaction createChargeTransaction(final Date transactionDate, final ShareAccountCharge charge) {
       final Long totalShares = null ;
       final BigDecimal unitPrice = null ;
       final Integer status = PurchasedSharesStatusType.APPROVED.getValue() ;
       final Integer type = PurchasedSharesStatusType.CHARGE_PAYMENT.getValue() ;
       BigDecimal amount = charge.percentageOrAmount() ;
       BigDecimal chargeAmount = null ;
       return new ShareAccountTransaction(transactionDate, totalShares, unitPrice, status, type, amount, chargeAmount) ;
    }
    
    public Date getPurchasedDate() {
        return this.transactionDate;
    }

    public Long getTotalShares() {
        return this.totalShares;
    }

    public BigDecimal getPurchasePrice() {
        return this.shareValue;
    }

    public void update(final Date purchasedDate, final Long totalShares, final BigDecimal shareValue) {
        this.transactionDate = purchasedDate;
        this.totalShares = totalShares;
        this.shareValue = shareValue;
        this.amount = shareValue.multiply(BigDecimal.valueOf(totalShares)) ;
        this.status = PurchasedSharesStatusType.APPLIED.getValue();
    }

    public void approve() {
        this.status = PurchasedSharesStatusType.APPROVED.getValue() ;
    }
    
    public void undoApprove() {
        this.status = PurchasedSharesStatusType.APPLIED.getValue() ;
    }

    public void reject() {
       this.status = PurchasedSharesStatusType.REJECTED.getValue() ;
       if(this.totalChargeAmount != null) {
           this.amount = this.amount.subtract(totalChargeAmount) ;
       }
    }
    
    public boolean isPendingForApprovalTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPLIED.getValue()) && this.type.equals(PurchasedSharesStatusType.PURCHASED.getValue()) ;
    }
    
    public boolean isPurchasTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPROVED.getValue()) && this.type.equals(PurchasedSharesStatusType.PURCHASED.getValue()) ;
    }
    
    public boolean isRedeemTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPROVED.getValue()) && this.type.equals(PurchasedSharesStatusType.REDEEMED.getValue()) ;
    }
    
    public boolean isChargeTransaction() {
        return this.status.equals(PurchasedSharesStatusType.APPROVED.getValue()) && this.type.equals(PurchasedSharesStatusType.CHARGE_PAYMENT.getValue()) ;
    }
    
    public void addShareAccountChargePaidBy(final ShareAccountChargePaidBy chargePaidBy) {
        this.shareAccountChargesPaid.add(chargePaidBy) ;
    }
    
    public BigDecimal amount() {
        return this.amount ;
    }

    public BigDecimal chargeAmount() {
        return this.totalChargeAmount ;
    }
    
    public void updateChargeAmount(BigDecimal totalChargeAmount) {
        this.amount = this.amount.add(totalChargeAmount);
        this.totalChargeAmount = totalChargeAmount ;
    }
    
    public void deductChargesFromTotalAmount(BigDecimal totalChargeAmount) {
        this.amount = this.amount.subtract(totalChargeAmount);
        this.totalChargeAmount = totalChargeAmount ;
    }
    
    public Set<ShareAccountChargePaidBy> getChargesPaidBy() {
        return this.shareAccountChargesPaid ;
    }
    
    public Integer getTransactionStatus() {
        return this.status ;
    }
    
    public Integer getTransactionType() {
        return this.type ;
    }
    
    public void adjustRedeemAmount() {
        if(this.totalChargeAmount != null) {
            this.amount = this.amount.subtract(totalChargeAmount) ;
        }
    }
}
