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
package org.apache.fineract.portfolio.accounts.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.accounts.data.PurchasedSharesData;
import org.apache.fineract.portfolio.accounts.data.ShareAccountData;
import org.apache.fineract.portfolio.accounts.data.ShareChargeData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.shares.domain.ShareProduct;
import org.apache.fineract.useradministration.domain.AppUser;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;

@Entity
@Table(name = "m_shareaccounts")
public class ShareAccount extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private Client client ;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private ShareProduct shareProduct ;
    
    @Column(name = "submitted_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedDate ;
    
    @Column(name = "approved_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedDate ;
    
    @Column(name = "field_officer")
    private Long fieldOfficerId ;
    
    @Column(name = "external_id")
    private String externalId ;
    
    @Embedded
    private MonetaryCurrency currency;
    
    @ManyToOne
    @JoinColumn(name = "suspense_account", nullable = true)
    private GLAccount suspenseAccount ;
    
    @ManyToOne
    @JoinColumn(name = "equity_account", nullable = true)
    private GLAccount equityAccount ;
    
    @ManyToOne
    @JoinColumn(name = "savings_id", nullable = true)
    private SavingsAccount savingsAccount ;
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareAccount", orphanRemoval = true)
    private Set<PurchasedShares> purchasedShares ;
    
    @Column(name = "allow_dividends_inactive_clients")
    private Boolean allowDividendCalculationForInactiveClients;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "lock_period", nullable = true)
    private PeriodFrequencyType lockPeriod;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "dividend_active_period", nullable = true)
    private PeriodFrequencyType minimumActivePeriodForDividends;
    
    @Column(name = "status")
    private String status = "Submitted"; //change it to enum
    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareAccount", orphanRemoval = true)
    private Set<ShareAccountCharge> charges ;
    
    protected ShareAccount() {
        
    }
    
    public ShareAccount(final Client client, final ShareProduct shareProduct, final Date submittedDate, final Date approvedDate,
            final Long fieldOfficerId , final String externalId, final MonetaryCurrency currency, GLAccount suspenseAccount, 
            final GLAccount equityAccount, final SavingsAccount savingsAccount, final Set<PurchasedShares> purchasedShares,
            final Boolean allowDividendCalculationForInactiveClients, final PeriodFrequencyType lockPeriod, 
            final PeriodFrequencyType minimumActivePeriodForDividends, Set<ShareAccountCharge> charges,
            AppUser createdBy, DateTime createdDate, AppUser lastModifiedBy, DateTime lastModifiedDate) {
        this.client = client ;
        this.shareProduct = shareProduct ;
        this.submittedDate = submittedDate ;
        this.approvedDate =  approvedDate ;
        this.fieldOfficerId = fieldOfficerId ;
        this.externalId = externalId ;
        this.currency = currency ;
        this.suspenseAccount = suspenseAccount ;
        this.equityAccount = equityAccount ;
        this.savingsAccount = savingsAccount ;
        this.purchasedShares = purchasedShares ;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients ;
        this.lockPeriod = lockPeriod ;
        this.minimumActivePeriodForDividends = minimumActivePeriodForDividends ; 
        this.charges = charges ;
        setCreatedBy(createdBy) ;
        setCreatedDate(createdDate) ;
        setLastModifiedBy(lastModifiedBy) ;
        setLastModifiedDate(lastModifiedDate) ;
    }
    
    public boolean setShareProduct(final ShareProduct shareProduct) {
        boolean toReturn = false ;
        if(!this.shareProduct.getId().equals(shareProduct.getId())) {
            this.shareProduct = shareProduct ;
            toReturn = true ;
        }
        return toReturn ;
    }
    
    public ShareProduct getShareProduct() {
        return this.shareProduct ;
    }
    
    public boolean setSubmittedDate(final Date submittedDate) {
        boolean toReturn = false ;
        if(!this.submittedDate.equals(submittedDate)) {
            this.submittedDate = submittedDate ;
            toReturn = true ;
        }
        return toReturn ;
    }
    
    public boolean setApprovedDate(final Date approvedDate) {
        boolean toReturn = false ;
        if(!this.approvedDate.equals(approvedDate)) {
            this.approvedDate = approvedDate ;
            toReturn = true ;
        }
        return toReturn ;
    }
    
    public boolean setFieldOfficer(final Long fieldOfficerId) {
        boolean toReturn = false ;
        if(!this.fieldOfficerId.equals(fieldOfficerId)) {
            this.fieldOfficerId = fieldOfficerId ;
            toReturn = true ;
        }
        return toReturn ;
    }
    
    public boolean setExternalId(final String externalId) {
        boolean toReturn = false ;
        if(!this.externalId.equals(externalId)) {
            this.externalId = externalId ;
            toReturn = true ;
        }
        return toReturn ;
    }
    
    public boolean setSuspenseAccount(GLAccount suspenseAccount) {
        boolean returnValue = false;
        if (!this.suspenseAccount.getId().equals(suspenseAccount.getId())) {
            this.suspenseAccount = suspenseAccount;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setEquityAccount(GLAccount equityAccount) {
        boolean returnValue = false;
        if (!this.equityAccount.getId().equals(equityAccount.getId())) {
            this.equityAccount = equityAccount;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setSavingsAccount(final SavingsAccount savingsAccount) {
        boolean returnValue = false;
        if (!this.savingsAccount.getId().equals(savingsAccount.getId())) {
            this.savingsAccount = savingsAccount;
            returnValue = true;
        }
        return returnValue;
    }
    
    public boolean setPurchasedShares(Set<PurchasedShares> purchasedShares) {
        this.purchasedShares = purchasedShares ;
        return true ;
    }
    
    public boolean setAllowDividendCalculationForInactiveClients(Boolean allowDividendCalculationForInactiveClients) {
        boolean returnValue = false;
        if (!this.allowDividendCalculationForInactiveClients.equals(allowDividendCalculationForInactiveClients)) {
            this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setLockPeriod(final PeriodFrequencyType lockPeriod) {
        boolean returnValue = false;
        if (!this.lockPeriod.equals(lockPeriod)) {
            this.lockPeriod = lockPeriod;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setminimumActivePeriodForDividends(final PeriodFrequencyType minimumActivePeriodForDividends) {
        boolean returnValue = false;
        if (!this.minimumActivePeriodForDividends.equals(minimumActivePeriodForDividends)) {
            this.minimumActivePeriodForDividends = minimumActivePeriodForDividends;
            returnValue = true;
        }
        return returnValue;
    }
    
    public boolean setCharges(final Set<ShareAccountCharge> charges) {
        this.charges = charges ;
        return true ;
    }
    //FIXME: Remove this
    public void setTempId(Long id) {
        setId(id) ;
    }
    
    public ShareAccountData toData() {
        String accountNo = "000025900"+getId() ;
        Collection<PurchasedSharesData> purchasedSharesdata = new ArrayList<>();
        for(PurchasedShares val: purchasedShares) {
            purchasedSharesdata.add(new PurchasedSharesData(val.getPurchasedDate(), val.getTotalShares(), val.getPurchasePrice(), val.getStatus())) ;
        }
        Collection<ShareChargeData> chargesdata =  new ArrayList<>();
        for(ShareAccountCharge charge: charges) {
            chargesdata.add(new ShareChargeData(charge.getAccountId(), charge.getChargeId())) ;
        }
        
        GLAccountData suspenseAccount1 = new GLAccountData(suspenseAccount.getId(), suspenseAccount.getName(), suspenseAccount.getGlCode());
        GLAccountData equityAccount1 = new GLAccountData(equityAccount.getId(), equityAccount.getName(), equityAccount.getGlCode());
        EnumOptionData lock = new EnumOptionData(this.lockPeriod.getValue().longValue(), this.lockPeriod.getCode(),
                this.lockPeriod.toString());
        EnumOptionData mini = new EnumOptionData(this.minimumActivePeriodForDividends.getValue().longValue(), this.minimumActivePeriodForDividends.getCode(),
                this.minimumActivePeriodForDividends.toString());;
                
        ShareAccountData data = new ShareAccountData(getId(), accountNo, this.client.getId(), this.client.getDisplayName(), this.shareProduct.getId(), 
                this.shareProduct.getProductName(), fieldOfficerId, externalId,
                submittedDate, purchasedSharesdata, suspenseAccount1, equityAccount1, lock, mini, 
                allowDividendCalculationForInactiveClients, chargesdata, status) ;
        return data ;
    }
    
    public Long getClientId() {
        return this.client.getId() ;
    }
    
    public String getClientName() {
        return this.client.getDisplayName() ;
    }
    
    public Long getTotalShares() {
        long value = 0 ;
        for(PurchasedShares val: purchasedShares) {
            if(val.getStatus().equals("Approved")) {
                value += val.getTotalShares().longValue() ;    
            }
        }
        return new Long(value) ;
    }
    
    public String getShareAccountNo() {
        return "000025900"+getId() ; 
    }
    
    public String getSavingsAccountNo() {
        return this.savingsAccount.getAccountNumber() ;
    }
    
    public void setStatus(String status) {
        this.status = status ;
    }
    
    public String getStatus() {
        return this.status ;
    }
    
    public void addAddtionalShares(Set<PurchasedShares> additionalShares) {
        this.purchasedShares.addAll(additionalShares) ;
    }
    
    public Set<PurchasedShares> getPurchasedShares() {
        return this.purchasedShares ;
    }
}
