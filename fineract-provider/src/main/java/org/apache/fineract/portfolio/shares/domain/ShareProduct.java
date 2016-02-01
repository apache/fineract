/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.shares.data.ShareMarketPriceData;
import org.mifosplatform.portfolio.shares.data.ShareProductData;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_shareproducts")
public class ShareProduct extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", nullable = false, unique = true)
    private String shortName;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "total_shares", nullable = false)
    private Long totalShares;

    @Column(name = "total_shares_issued", nullable = false)
    private Long totalSharesIssued;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "share_capital", nullable = false)
    private BigDecimal shareCapital;

    @ManyToOne
    @JoinColumn(name = "suspence_account", nullable = false)
    private GLAccount suspenseAccount;

    @ManyToOne
    @JoinColumn(name = "equity_account", nullable = false)
    private GLAccount equityAccount;

    @Column(name = "minimum_client_shares")
    private Long minimumShares;

    @Column(name = "default_client_shares", nullable = false)
    private Long nominalShares;

    @Column(name = "maximum_client_shares")
    private Long maximumShares;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    Set<ShareMarketPrice> marketPrice = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_product_charges", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private Set<Charge> charges;

    @Column(name = "allow_dividends_inactive_clients")
    private Boolean allowDividendCalculationForInactiveClients;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "lock_period", nullable = true)
    private PeriodFrequencyType lockPeriod;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "dividend_active_period", nullable = true)
    private PeriodFrequencyType minimumActivePeriodForDividends;

    protected ShareProduct() {
        
    }
    // FIXME Remove this method
    public void setTempId(Long id) {
        super.setId(id);
    }

    public ShareProduct(final String name, final String shortName, final String description, final String externalId,
            final MonetaryCurrency currency, final Long totalShares, final Long totalSharesIssued, final BigDecimal unitPrice,
            final BigDecimal shareCapital, final GLAccount suspenseAccount, final GLAccount equityAccount, final Long minimumShares,
            final Long nominalShares, final Long maximumShares, Set<ShareMarketPrice> marketPrice, Set<Charge> charges,
            final Boolean allowDividendCalculationForInactiveClients, final PeriodFrequencyType lockPeriod,
            final PeriodFrequencyType minimumActivePeriodForDividends,
            AppUser createdBy, DateTime createdDate, AppUser lastModifiedBy, DateTime lastModifiedDate) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.externalId = externalId;
        this.currency = currency;
        this.totalShares = totalShares;
        this.totalSharesIssued = totalSharesIssued;
        this.unitPrice = unitPrice;
        this.shareCapital = shareCapital;
        this.suspenseAccount = suspenseAccount;
        this.equityAccount = equityAccount;
        this.minimumShares = minimumShares;
        this.nominalShares = nominalShares;
        this.maximumShares = maximumShares;
        this.marketPrice = marketPrice;
        this.charges = charges;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
        this.lockPeriod = lockPeriod;
        this.minimumActivePeriodForDividends = minimumActivePeriodForDividends;
        setCreatedBy(createdBy) ;
        setCreatedDate(createdDate) ;
        setLastModifiedBy(lastModifiedBy) ;
        setLastModifiedDate(lastModifiedDate) ;
    }

    public ShareProductData toData() {
        GLAccountData suspenseAccount1 = new GLAccountData(suspenseAccount.getId(), suspenseAccount.getName(), suspenseAccount.getGlCode());
        GLAccountData equityAccount1 = new GLAccountData(equityAccount.getId(), equityAccount.getName(), equityAccount.getGlCode());
        List<ChargeData> chargeData = new ArrayList<>();
        for(Charge charge: this.charges) {
            chargeData.add(ChargeData.lookup(charge.getId(), charge.getName(), charge.isPenalty())) ;
        }
        List<ShareMarketPriceData> marketData = new ArrayList<>() ;
        for(ShareMarketPrice pri: marketPrice) {
            marketData.add(new ShareMarketPriceData(pri.getStartDate(), pri.getPrice())) ;
        }
        EnumOptionData lock = new EnumOptionData(this.lockPeriod.getValue().longValue(), this.lockPeriod.getCode(),
                this.lockPeriod.toString());
        EnumOptionData mini = new EnumOptionData(this.minimumActivePeriodForDividends.getValue().longValue(), this.minimumActivePeriodForDividends.getCode(),
                this.minimumActivePeriodForDividends.toString());;
        CurrencyData curr = new CurrencyData(currency.getCode(), "", currency.getDigitsAfterDecimal(), currency.getCurrencyInMultiplesOf(),
                "", "");
        return new ShareProductData(getId(), name, shortName, description, externalId, curr, totalShares, totalSharesIssued, unitPrice,
                shareCapital, suspenseAccount1, equityAccount1, minimumShares, nominalShares, maximumShares, marketData, chargeData,
                allowDividendCalculationForInactiveClients, lock, mini);
    }

    public boolean setProductName(String productName) {
        boolean returnValue = false;
        if (!this.name.equals(productName)) {
            this.name = productName;
            returnValue = true;
        }
        return returnValue;
    }

    public String getProductName() {
        return this.name ;
    }
    
    public boolean setShortName(String shortName) {
        boolean returnValue = false;
        if (!this.shortName.equals(shortName)) {
            this.shortName = shortName;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setDescription(String description) {
        boolean returnValue = false;
        if (!this.description.equals(description)) {
            this.description = description;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setExternalId(String externalId) {
        boolean returnValue = false;
        if (!this.externalId.equals(externalId)) {
            this.externalId = externalId;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setTotalShares(Long totalShares) {
        boolean returnValue = false;
        if (!this.totalShares.equals(totalShares)) {
            this.totalShares = totalShares;
            returnValue = true;
        }
        return returnValue;

    }

    public Long getTotalShares() {
        return this.totalShares ;
    }
    
    public boolean setTotalIssuedShares(Long totalSharesIssued) {
        boolean returnValue = false;
        if (!this.totalSharesIssued.equals(totalSharesIssued)) {
            this.totalSharesIssued = totalSharesIssued;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setMonetaryCurrency(MonetaryCurrency currency) {
        boolean returnValue = false;
        if (!this.currency.equals(currency)) {
            this.currency = currency;
            returnValue = true;
        }
        return returnValue;
    }

    public MonetaryCurrency getCurrency() {
        return this.currency ;
    }
    
    public boolean setUnitPrice(BigDecimal unitPrice) {
        boolean returnValue = false;
        if (!this.unitPrice.equals(unitPrice)) {
            this.unitPrice = unitPrice;
            returnValue = true;
        }
        return returnValue;
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

    public boolean setMinimumShares(final Long minimumShares) {
        boolean returnValue = false;
        if (!this.minimumShares.equals(minimumShares)) {
            this.minimumShares = minimumShares;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setNominalShares(final Long nominalShares) {
        boolean returnValue = false;
        if (!this.nominalShares.equals(nominalShares)) {
            this.nominalShares = nominalShares;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setMaximumShares(final Long maximumShares) {
        boolean returnValue = false;
        if (!this.maximumShares.equals(maximumShares)) {
            this.maximumShares = maximumShares;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setMarketPrice(Set<ShareMarketPrice> marketPrice) {
        this.marketPrice = marketPrice;
        return true;
    }

    public boolean setCharges(Set<Charge> charges) {
        this.charges = charges;
        return true;
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
}
