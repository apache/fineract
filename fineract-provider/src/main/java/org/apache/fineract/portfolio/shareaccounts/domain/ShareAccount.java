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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProduct;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_share_account")
public class ShareAccount extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private ShareProduct shareProduct;

    @Column(name = "status_enum", nullable = false)
    protected Integer status;

    @Column(name = "submitted_date")
    @Temporal(TemporalType.DATE)
    private Date submittedDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "submitted_userid", nullable = true)
    protected AppUser submittedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "approved_date")
    protected Date approvedDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "approved_userid", nullable = true)
    protected AppUser approvedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "rejected_date")
    protected Date rejectedDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "rejected_userid", nullable = true)
    protected AppUser rejectedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "activated_date", nullable = true)
    protected Date activatedDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "activated_userid", nullable = true)
    protected AppUser activatedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "closed_date")
    protected Date closedDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "closed_userid", nullable = true)
    protected AppUser closedBy;

    @Temporal(TemporalType.DATE)
    @Column(name = "lastmodified_date")
    protected Date modifiedDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "lastmodifiedby_id", nullable = true)
    protected AppUser modifiedBy;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "total_approved_shares")
    private Long totalSharesApproved;

    @Column(name = "total_pending_shares")
    private Long totalSharesPending;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "allow_dividends_inactive_clients")
    private Boolean allowDividendCalculationForInactiveClients;

    @ManyToOne
    @JoinColumn(name = "savings_account_id", nullable = true)
    private SavingsAccount savingsAccount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareAccount", orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<ShareAccountTransaction> shareAccountTransactions;

    @Column(name = "lockin_period_frequency")
    private Integer lockinPeriodFrequency;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "lockin_period_frequency_enum", nullable = true)
    private PeriodFrequencyType lockinPeriodFrequencyType;

    @Column(name = "minimum_active_period_frequency")
    private Integer minimumActivePeriodFrequency;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "minimum_active_period_frequency_enum", nullable = true)
    private PeriodFrequencyType minimumActivePeriodFrequencyType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareAccount", orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<ShareAccountCharge> charges;

    @Transient
    protected boolean accountNumberRequiresAutoGeneration = false;

    protected ShareAccount() {

    }

    public ShareAccount(final Client client, final ShareProduct shareProduct, final String externalId, final MonetaryCurrency currency,
            final SavingsAccount savingsAccount, final String accountNo, final Long totalSharesApproved, final Long totalSharesPending,
            final Set<ShareAccountTransaction> purchasedShares, final Boolean allowDividendCalculationForInactiveClients,
            final Integer lockinPeriodFrequency, final PeriodFrequencyType lockPeriodType, final Integer minimumActivePeriodFrequency,
            final PeriodFrequencyType minimumActivePeriodType, Set<ShareAccountCharge> charges, AppUser submittedBy,
            final Date submittedDate, AppUser approvedBy, Date approvedDate, AppUser rejectedBy, Date rejectedDate, AppUser activatedBy,
            Date activatedDate, AppUser closedBy, Date closedDate, AppUser modifiedBy, Date modifiedDate) {

        this.client = client;
        this.shareProduct = shareProduct;
        this.externalId = externalId;
        this.currency = currency;
        this.savingsAccount = savingsAccount;
        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }
        this.totalSharesApproved = totalSharesApproved;
        this.totalSharesPending = totalSharesPending;
        this.shareAccountTransactions = purchasedShares;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockPeriodType;
        this.minimumActivePeriodFrequency = minimumActivePeriodFrequency;
        this.minimumActivePeriodFrequencyType = minimumActivePeriodType;
        this.charges = charges;
        this.submittedDate = submittedDate;
        this.submittedBy = submittedBy;
        this.approvedDate = approvedDate;
        this.approvedBy = approvedBy;
        this.rejectedDate = rejectedDate;
        this.rejectedBy = rejectedBy;
        this.activatedDate = activatedDate;
        this.activatedBy = activatedBy;
        this.closedDate = closedDate;
        this.closedBy = closedBy;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
        this.status = ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue();
    }

    public boolean setShareProduct(final ShareProduct shareProduct) {
        boolean toReturn = false;
        if (!this.shareProduct.getId().equals(shareProduct.getId())) {
            this.shareProduct = shareProduct;
            toReturn = true;
        }
        return toReturn;
    }

    public ShareProduct getShareProduct() {
        return this.shareProduct;
    }

    public boolean setSubmittedDate(final Date submittedDate) {
        boolean toReturn = false;
        if (!this.submittedDate.equals(submittedDate)) {
            this.submittedDate = submittedDate;
            toReturn = true;
        }
        return toReturn;
    }

    public boolean setApprovedDate(final Date approvedDate) {
        boolean toReturn = false;
        if (!this.approvedDate.equals(approvedDate)) {
            this.approvedDate = approvedDate;
            toReturn = true;
        }
        return toReturn;
    }

    public boolean setExternalId(final String externalId) {
        boolean toReturn = false;
        if (this.externalId == null || !this.externalId.equals(externalId)) {
            this.externalId = externalId;
            toReturn = true;
        }
        return toReturn;
    }

    public boolean setSavingsAccount(final SavingsAccount savingsAccount) {
        boolean returnValue = false;
        if (!this.savingsAccount.getId().equals(savingsAccount.getId())) {
            this.savingsAccount = savingsAccount;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setPurchasedShares(Set<ShareAccountTransaction> purchasedShares) {
        this.shareAccountTransactions = purchasedShares;
        return true;
    }

    public void addTransaction(final ShareAccountTransaction transaction) {
        transaction.setShareAccount(this);
        if(transaction.isPendingForApprovalTransaction()) {
            if(this.totalSharesPending == null) {
                this.totalSharesPending = transaction.getTotalShares() ;
            }else {
                this.totalSharesPending += transaction.getTotalShares() ;    
            }
            
        }else if(transaction.isPurchasTransaction()) {
            if(this.totalSharesApproved == null) {
                this.totalSharesApproved = transaction.getTotalShares() ; 
            }else {
                this.totalSharesApproved += transaction.getTotalShares() ;
            }
        }
        
        this.shareAccountTransactions.add(transaction);
    }
    
    public boolean setAllowDividendCalculationForInactiveClients(Boolean allowDividendCalculationForInactiveClients) {
        boolean returnValue = false;
        if (this.allowDividendCalculationForInactiveClients == null || !this.allowDividendCalculationForInactiveClients.equals(allowDividendCalculationForInactiveClients)) {
            this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setLockPeriod(final Integer lockinPeriod) {
        boolean returnValue = false;
        if (this.lockinPeriodFrequency == null || !this.lockinPeriodFrequency.equals(lockinPeriod)) {
            this.lockinPeriodFrequency = lockinPeriod;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setLockPeriodFrequencyEnum(final PeriodFrequencyType lockinPeriodFrequencyType) {
        boolean returnValue = false;
        if (this.lockinPeriodFrequencyType == null || !this.lockinPeriodFrequencyType.getValue().equals(lockinPeriodFrequencyType.getValue())) {
            this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setminimumActivePeriod(final Integer minimumActivePeriodFrequency) {
        boolean returnValue = false;
        if (this.minimumActivePeriodFrequency == null || !this.minimumActivePeriodFrequency.equals(minimumActivePeriodFrequency)) {
            this.minimumActivePeriodFrequency = minimumActivePeriodFrequency;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setminimumActivePeriodTypeEnum(final PeriodFrequencyType minimumActivePeriodForDividends) {
        boolean returnValue = false;
        if (this.minimumActivePeriodFrequencyType == null || !this.minimumActivePeriodFrequencyType.getValue().equals(minimumActivePeriodForDividends.getValue())) {
            this.minimumActivePeriodFrequencyType = minimumActivePeriodForDividends;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setCharges(final Set<ShareAccountCharge> charges) {
        this.charges = charges;
        return true;
    }

    public void updateAccountNumber(String accountNo) {
        this.accountNumber = accountNo;
        this.accountNumberRequiresAutoGeneration = false;
    }

    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }

    public Long getClientId() {
        return this.client.getId();
    }

    public String getClientName() {
        return this.client.getDisplayName();
    }

    public Client getClient() {
        return this.client ;
    }
    
    public String getSavingsAccountNo() {
        return this.savingsAccount.getAccountNumber();
    }

    public void addAddtionalShares(Set<ShareAccountTransaction> additionalShares) {
        this.shareAccountTransactions.addAll(additionalShares);
    }

    public Set<ShareAccountTransaction> getShareAccountTransactions() {
        return this.shareAccountTransactions;
    }

    public Set<ShareAccountTransaction> getChargeTransactions() {
        Set<ShareAccountTransaction> chargeTransactions = new HashSet<>();
        for (ShareAccountTransaction transaction : this.shareAccountTransactions) {
            if (transaction.isActive() && transaction.isChargeTransaction()) {
                chargeTransactions.add(transaction);
            }
        }
        return chargeTransactions;
    }

    public void updateRequestedShares(ShareAccountTransaction purchased) {
    	for(ShareAccountTransaction transaction: this.shareAccountTransactions) {
            if(!transaction.isChargeTransaction() && transaction.getId().equals(purchased.getId())) {
                transaction.update(purchased.getPurchasedDate(), purchased.getTotalShares(), purchased.getPurchasePrice());    
            }
        }
    }

    public void addAdditionalPurchasedShares(ShareAccountTransaction purchased) {
        purchased.setShareAccount(this);
        if (purchased.isRedeemTransaction()) {
            this.totalSharesApproved -= purchased.getTotalShares();
        } else {
            if (this.totalSharesPending == null) {
                this.totalSharesPending = purchased.getTotalShares();
            } else {
                this.totalSharesPending += purchased.getTotalShares();
            }
        }
        this.shareAccountTransactions.add(purchased);
    }

    public void addShareAccountCharge(ShareAccountCharge charge) {
        charge.update(this);
        this.charges.add(charge);
    }

    public void approve(final Date approvedDate, final AppUser approvedUser) {
        this.approvedDate = approvedDate;
        this.approvedBy = approvedUser;
        for (ShareAccountTransaction transaction : this.shareAccountTransactions) {
            transaction.approve();
        }
        this.status = ShareAccountStatusType.APPROVED.getValue();
        this.totalSharesApproved = this.totalSharesPending;
        this.totalSharesPending = null;
    }

    public void activate(final Date approvedDate, final AppUser approvedUser) {
        this.activatedDate = approvedDate;
        this.activatedBy = approvedUser;
        this.status = ShareAccountStatusType.ACTIVE.getValue();
    }

    public void undoApprove() {
        this.status = ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue();
        this.approvedDate = null;
        this.approvedBy = null;
        this.rejectedDate = null;
        this.rejectedBy = null;
        this.closedDate = null;
        this.closedBy = null;
        this.totalSharesApproved = null;
        Long tempTotalShares = new Long(0);
        for (ShareAccountTransaction transaction : this.shareAccountTransactions) {
            if(transaction.isPurchasTransaction()) {
                transaction.undoApprove();
                tempTotalShares += transaction.getTotalShares() ;
            }
        }
        this.totalSharesPending = tempTotalShares;
    }

    public void reject(final Date rejectedDate, final AppUser rejectedUser) {
        this.rejectedDate = rejectedDate;
        this.rejectedBy = rejectedUser;
        this.status = ShareAccountStatusType.REJECTED.getValue();
        this.totalSharesPending = null;
        this.totalSharesApproved = null;
        for (ShareAccountTransaction transaction : this.shareAccountTransactions) {
            if(transaction.isPurchasTransaction()) {
                transaction.reject();
            }
        }
    }

    public void close(final Date closedDate, final AppUser closedBy) {
        this.closedDate = closedDate ;
        this.closedBy = closedBy ;
        this.status = ShareAccountStatusType.CLOSED.getValue() ;
        this.totalSharesPending = null;
        this.totalSharesApproved = null;
    }
    public String getAccountNumber() {
        return this.accountNumber;
    }

    public ShareAccountTransaction retrievePurchasedShares(final Long id) {
        ShareAccountTransaction toReturn = null;
        for (ShareAccountTransaction share : this.shareAccountTransactions) {
            if (share.getId().equals(id)) {
                toReturn = share;
                break;
            }
        }
        return toReturn;
    }

    public Set<ShareAccountCharge> getCharges() {
        return this.charges;
    }

    public void addChargeTransaction(ShareAccountTransaction chargeTransaction) {
        chargeTransaction.setShareAccount(this);
        this.shareAccountTransactions.add(chargeTransaction);
    }

    public Set<ShareAccountTransaction> getPendingForApprovalSharePurchaseTransactions() {
        Set<ShareAccountTransaction> purchaseTransactions = new HashSet<>();
        for (ShareAccountTransaction transaction : this.shareAccountTransactions) {
            if (transaction.isActive() && transaction.isPendingForApprovalTransaction()) {
                purchaseTransactions.add(transaction);
            }
        }
        return purchaseTransactions;
    }

    public MonetaryCurrency getCurrency() {
        return this.currency;
    }

    public void updateApprovedShares(Long shares) {
        if (this.totalSharesApproved == null) {
            this.totalSharesApproved = shares;
        } else {
            this.totalSharesApproved += shares;
            this.totalSharesPending -= shares;
        }
    }

    public Long getTotalApprovedShares() {
        return this.totalSharesApproved ;
    }
    
    public void removePendingShares(Long totalShares) {
        this.totalSharesPending -= totalShares;
    }

    public Long getOfficeId() {
        return this.client.getOffice().getId();
    }

    public void setTotalPendingShares(final Long shares) {
        this.totalSharesPending = shares ;
    }
    
    public ShareAccountTransaction getShareAccountTransaction(final ShareAccountTransaction transaction) {
        ShareAccountTransaction returnTrans = null;
        for (ShareAccountTransaction tran : this.shareAccountTransactions) {
            if (tran.getPurchasedDate().equals(transaction.getPurchasedDate())
                    && tran.getTotalShares().equals(transaction.getTotalShares())
                    && tran.getPurchasePrice().equals(transaction.getPurchasePrice())
                    && tran.getTransactionStatus().equals(transaction.getTransactionStatus())
                    && tran.getTransactionType().equals(transaction.getTransactionType())) {
                returnTrans = tran;
                break;
            }
        }
        return returnTrans;
    }
    
    public Date getSubmittedDate(){
        return this.submittedDate ;
    }
    
    public Date getApprovedDate() {
        return this.approvedDate ;
    }

    public void removeTransactions() {
        for(ShareAccountTransaction transaction: this.shareAccountTransactions) {
            transaction.setActive(false);
        }
        this.totalSharesApproved = new Long(0) ;
        this.totalSharesPending = new Long(0) ;
    }

    public void removeCharges() {
        for(ShareAccountCharge charge: this.charges) {
            charge.setActive(false);
        }
    }

    public void addCharges(Set<ShareAccountCharge> charges) {
        this.charges.addAll(charges) ;
    }
    
    public Integer getLockinPeriodFrequency() {
        return this.lockinPeriodFrequency ;
    }
    
    public  PeriodFrequencyType getLockinPeriodFrequencyType() {
        return this.lockinPeriodFrequencyType ;
    }
    
    public Date getActivatedDate() {
        return this.activatedDate ;
    }
    
    public Integer status() {
        return this.status ;
    }
}
