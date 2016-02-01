/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.mifosplatform.portfolio.accounts.data.PurchasedSharesData;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_purchasedshares")
public class PurchasedShares extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private ShareAccount shareAccount ;
    
    @Column(name = "purchased_date")
    @Temporal(TemporalType.DATE)
    private Date purchasedDate ;
    
    @Column(name = "share_count")
    private Long totalShares ;
    
    @Column(name = "share_price")
    private BigDecimal shareValue ; 
    
    @Column(name ="status")
    private String status = "Submitted";
    
    protected PurchasedShares() {
        
    }
    
    public void setShareAccount(final ShareAccount shareAccount) {
        this.shareAccount = shareAccount ;
    }
    
    public PurchasedShares(final Date purchasedDate, final Long totalShares, final BigDecimal shareValue) {
        this.purchasedDate = purchasedDate ;
        this.totalShares = totalShares ;
        this.shareValue = shareValue ;
    }
    
    public PurchasedSharesData toData() {
        return new PurchasedSharesData(this.purchasedDate, this.totalShares, this.shareValue, this.status) ;
    }
    
    public Date getPurchasedDate() {
        return this.purchasedDate ;
    }
    
    public Long getTotalShares() {
        return this.totalShares ;
    }
    
    public BigDecimal getPurchasePrice() {
        return this.shareValue ;
    }
    
    public String getStatus() {
        return this.status ;
    }
    
    public void setStatus(String status) {
        this.status = status ;
    }
}
