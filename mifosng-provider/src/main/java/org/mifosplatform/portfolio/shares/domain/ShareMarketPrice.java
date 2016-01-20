/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_share_marketprice")
public class ShareMarketPrice extends AbstractPersistable<Long> {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private ShareProduct product;
    
    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    
    @Column(name = "share_value", nullable = false)
    private BigDecimal shareValue ;
    
    public ShareMarketPrice(final Date startDate, final BigDecimal shareValue) {
        this.startDate = startDate ;
        this.shareValue = shareValue ;
    }
    
    public void setShareProduct(final ShareProduct product) {
        this.product = product ;
    }
    
    public Date getStartDate() {
        return this.startDate ;
    }
    
    public BigDecimal getPrice() {
        return this.shareValue ;
    }
}
