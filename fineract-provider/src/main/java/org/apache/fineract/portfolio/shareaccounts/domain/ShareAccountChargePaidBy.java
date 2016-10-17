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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;


@Entity
@Table(name = "m_share_account_charge_paid_by")
public class ShareAccountChargePaidBy extends AbstractPersistableCustom<Long>{

    @ManyToOne(optional = false)
    @JoinColumn(name = "share_transaction_id", referencedColumnName = "id", nullable = false)
    private ShareAccountTransaction shareAccountTransaction;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_transaction_id", referencedColumnName = "id", nullable = false)
    private ShareAccountCharge shareAccountCharge;
    
    @Column(name = "amount", scale = 6, precision = 19, nullable = false) 
    private BigDecimal amount ;
    
    protected ShareAccountChargePaidBy() {
        
    }
    
    public ShareAccountChargePaidBy(final ShareAccountTransaction shareAccountTransaction,
            final ShareAccountCharge shareAccountCharge, final BigDecimal amount) {
        this.shareAccountTransaction = shareAccountTransaction ;
        this.shareAccountCharge = shareAccountCharge ;
        this.amount = amount ;
    }
    
    public ShareAccountCharge getCharge() {
        return this.shareAccountCharge ;
    }
    
    public BigDecimal getAmount() {
        return this.amount ;
    }
    
    public ShareAccountTransaction getShareAccountTransaction() {
        return this.shareAccountTransaction ;
    }
    
    public Long getChargeId() {
        return this.shareAccountCharge.getChargeId() ;
    }
    
    public Long getShareChargeId() {
        return this.shareAccountCharge.getId() ;
    }
    
    public void reset() {
        this.shareAccountTransaction = null ;
        this.shareAccountCharge = null ;
    }
}
