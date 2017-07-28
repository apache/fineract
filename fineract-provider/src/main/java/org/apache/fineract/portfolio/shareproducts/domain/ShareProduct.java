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
package org.apache.fineract.portfolio.shareproducts.domain;

import java.math.BigDecimal;
import java.util.Date;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductMarketPriceData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.DateTime;

@Entity
@Table(name = "m_share_product")
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

    @Column(name = "issued_shares", nullable = false)
    private Long totalSharesIssued;

    @Column(name = "totalsubscribed_shares", nullable = true)
    private Long totalSubscribedShares;
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "capital_amount", nullable = false)
    private BigDecimal shareCapital;

    @Column(name = "minimum_client_shares")
    private Long minimumShares;

    @Column(name = "nominal_client_shares", nullable = false)
    private Long nominalShares;

    @Column(name = "maximum_client_shares")
    private Long maximumShares;

    @OrderBy(value = "fromDate,id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true, fetch=FetchType.EAGER)
    Set<ShareProductMarketPrice> marketPrice;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_share_product_charge", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "charge_id"))
    private Set<Charge> charges;

    @Column(name = "allow_dividends_inactive_clients")
    private Boolean allowDividendCalculationForInactiveClients;

    @Column(name = "lockin_period_frequency")
    private Integer lockinPeriod;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "lockin_period_frequency_enum", nullable = true)
    private PeriodFrequencyType lockPeriodType;

    @Column(name = "minimum_active_period_frequency")
    private Integer minimumActivePeriod;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "minimum_active_period_frequency_enum", nullable = true)
    private PeriodFrequencyType minimumActivePeriodType;

    @Column(name = "accounting_type", nullable = false)
    protected Integer accountingRule;

    protected ShareProduct() {

    }

    public ShareProduct(final String name, final String shortName, final String description, final String externalId,
            final MonetaryCurrency currency, final Long totalShares, final Long totalSharesIssued, final BigDecimal unitPrice,
            final BigDecimal shareCapital, final Long minimumShares, final Long nominalShares, final Long maximumShares,
            Set<ShareProductMarketPrice> marketPrice, Set<Charge> charges, final Boolean allowDividendCalculationForInactiveClients,
            final Integer lockinPeriod, final PeriodFrequencyType lockPeriodType, final Integer minimumActivePeriod,
            final PeriodFrequencyType minimumActivePeriodForDividendsType, AppUser createdBy, DateTime createdDate, AppUser lastModifiedBy,
            DateTime lastModifiedDate, final AccountingRuleType accountingRuleType) {

        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.externalId = externalId;
        this.currency = currency;
        this.totalShares = totalShares;
        this.totalSharesIssued = totalSharesIssued;
        this.unitPrice = unitPrice;
        this.shareCapital = shareCapital;
        this.minimumShares = minimumShares;
        this.nominalShares = nominalShares;
        this.maximumShares = maximumShares;
        this.marketPrice = marketPrice;
        this.charges = charges;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
        this.lockinPeriod = lockinPeriod;
        this.lockPeriodType = lockPeriodType;
        this.minimumActivePeriod = minimumActivePeriod;
        this.minimumActivePeriodType = minimumActivePeriodForDividendsType;
        setCreatedBy(createdBy);
        setCreatedDate(createdDate);
        setLastModifiedBy(lastModifiedBy);
        setLastModifiedDate(lastModifiedDate);
        startDate = DateUtils.getDateOfTenant();
        endDate = DateUtils.getDateOfTenant();
        if (accountingRuleType != null) {
            this.accountingRule = accountingRuleType.getValue();
        }
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
        return this.name;
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
        if (this.description == null || !this.description.equals(description)) {
            this.description = description;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setExternalId(String externalId) {
        boolean returnValue = false;
        if (this.externalId == null || !this.externalId.equals(externalId)) {
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
        return this.totalShares;
    }

    public boolean setTotalIssuedShares(Long totalSharesIssued) {
        boolean returnValue = false;
        if (this.totalSharesIssued == null || !this.totalSharesIssued.equals(totalSharesIssued)) {
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
        return this.currency;
    }

    public boolean setUnitPrice(BigDecimal unitPrice) {
        boolean returnValue = false;
        if (!this.unitPrice.equals(unitPrice)) {
            this.unitPrice = unitPrice;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setMinimumShares(final Long minimumShares) {
        boolean returnValue = false;
        if (this.minimumShares == null || !this.minimumShares.equals(minimumShares)) {
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
        if (this.maximumShares == null || !this.maximumShares.equals(maximumShares)) {
            this.maximumShares = maximumShares;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setMarketPrice(Set<ShareProductMarketPriceData> marketPrice) {
        boolean update = false;
        for (ShareProductMarketPriceData data : marketPrice) {
            if (data.getId() == null) {
                ShareProductMarketPrice entity = new ShareProductMarketPrice(data.getStartDate(), data.getShareValue());
                entity.setShareProduct(this);
                this.marketPrice.add(entity);
                update = true;
            } else {
                for (ShareProductMarketPrice priceData : this.marketPrice) {
                    if (priceData.getId() == data.getId()) {
                        priceData.setStartDate(data.getStartDate());
                        priceData.setShareValue(data.getShareValue());
                        update = true;
                    }
                }
            }
        }
        return update;
    }

    public boolean setCharges(Set<Charge> charges) {
        this.charges.clear();
        this.charges.addAll(charges);
        return true;
    }

    public boolean setAllowDividendCalculationForInactiveClients(Boolean allowDividendCalculationForInactiveClients) {
        boolean returnValue = false;
        if (this.allowDividendCalculationForInactiveClients == null || !this.allowDividendCalculationForInactiveClients.equals(allowDividendCalculationForInactiveClients)) {
            this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setLockinPeriod(final Integer lockinPeriod) {
        boolean returnValue = false;
        if (this.lockinPeriod == null || !this.lockinPeriod.equals(lockinPeriod)) {
            this.lockinPeriod = lockinPeriod;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setLockPeriodFrequencyType(final PeriodFrequencyType lockPeriod) {
        boolean returnValue = false;
        if (this.lockPeriodType == null || !this.lockPeriodType.equals(lockPeriod)) {
            this.lockPeriodType = lockPeriod;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setminimumActivePeriod(final Integer minimumActivePeriod) {
        boolean returnValue = false;
        if (this.minimumActivePeriod == null || !this.minimumActivePeriod.equals(minimumActivePeriod)) {
            this.minimumActivePeriod = minimumActivePeriod;
            returnValue = true;
        }
        return returnValue;
    }

    public boolean setminimumActivePeriodFrequencyType(final PeriodFrequencyType minimumActivePeriodForDividends) {
        boolean returnValue = false;
        if (this.minimumActivePeriodType == null || !this.minimumActivePeriodType.equals(minimumActivePeriodForDividends)) {
            this.minimumActivePeriodType = minimumActivePeriodForDividends;
            returnValue = true;
        }
        return returnValue;
    }

    public String getShortName() {
        return this.shortName;
    }

    public boolean setshareCapitalValue(BigDecimal shareCapitalValue) {
        boolean updated = false;
        if (this.shareCapital == null || !this.shareCapital.equals(shareCapitalValue)) {
            this.shareCapital = shareCapitalValue;
            updated = true;
        }
        return updated;
    }

    public boolean setAccountingRule(final Integer accountingRule) {
        boolean returnValue = false;
        if (!this.accountingRule.equals(accountingRule)) {
            this.accountingRule = accountingRule;
            returnValue = true;
        }
        return returnValue;
    }

    public Long getSharesIssued() {
        return this.totalSharesIssued;
    }

    public BigDecimal getUnitPrice() {
        return this.unitPrice;
    }

    public Integer getAccountingType() {
        return this.accountingRule;
    }

    public boolean isSharesAllowed(Long requestedShares) {
        boolean allowed = true;
        if (minimumShares != null && maximumShares != null) {
            if (requestedShares < minimumShares || requestedShares > maximumShares) {
                allowed = false;
            }
        }
        return allowed;
    }

    public BigDecimal deriveMarketPrice(final Date currentDate) {
        BigDecimal marketValue = this.unitPrice;
        if (this.marketPrice != null && !this.marketPrice.isEmpty()) {
            for (ShareProductMarketPrice data : this.marketPrice) {
                Date futureDate = data.getStartDate();
                if (currentDate.equals(futureDate) || currentDate.after(futureDate)) {
                    marketValue = data.getPrice();
                }
            }
        }
        return marketValue;
    }
    
    public void addSubscribedShares(final Long subscribedShares) {
        if(this.totalSubscribedShares == null) {
            this.totalSubscribedShares = new Long(0) ;
        }
        this.totalSubscribedShares += subscribedShares ;
    }
    
    public void removeSubscribedShares(final Long subscribedShares) {
        this.totalSubscribedShares -= subscribedShares ;
    }
    
    public Long getSubscribedShares() {
        return this.totalSubscribedShares ;
    }
 
    public Long getMinimumClientShares() {
        return this.minimumShares ;
    }
    
    public Long getMaximumClientShares() {
        return this.maximumShares ;
    }
    
    public Long getDefaultClientShares() {
        return this.nominalShares ;
    }
}