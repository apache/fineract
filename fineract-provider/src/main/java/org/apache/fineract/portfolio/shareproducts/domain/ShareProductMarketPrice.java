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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_share_product_market_price")
public class ShareProductMarketPrice extends AbstractPersistableCustom<Long> {
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private ShareProduct product;
    
    @Column(name = "from_date")
    @Temporal(TemporalType.DATE)
    private Date fromDate;
    
    @Column(name = "share_value", nullable = false)
    private BigDecimal shareValue ;
    
    public ShareProductMarketPrice() {
	
    }
    
    public ShareProductMarketPrice(final Date fromDate, final BigDecimal shareValue) {
        this.fromDate = fromDate ;
        this.shareValue = shareValue ;
    }
    
    public void setShareProduct(final ShareProduct product) {
        this.product = product ;
    }
    
    public Date getStartDate() {
        return this.fromDate ;
    }
    
    public BigDecimal getPrice() {
        return this.shareValue ;
    }
    
    public void setStartDate(Date date) {
    	this.fromDate = date ;
    }
    
    public void setShareValue(BigDecimal value) {
    	this.shareValue = value ;
    }
}
