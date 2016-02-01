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
package org.apache.fineract.portfolio.accounts.data;

import java.math.BigDecimal;
import java.util.Date;


public class PurchasedSharesData {

    private final Date purchasedDate ;
    
    private final Long numberOfShares ;
    
    private final BigDecimal purchasedPrice ;
    
    private final String status ;
    
    public PurchasedSharesData(final Date purchasedDate, final Long numberOfShares, final BigDecimal purchasedPrice, final String status) {
        this.purchasedDate = purchasedDate ;
        this.numberOfShares = numberOfShares ;
        this.purchasedPrice = purchasedPrice ;
        this.status = status ;
    }

    
    public Date getPurchasedDate() {
        return this.purchasedDate;
    }

    
    public Long getNumberOfShares() {
        return this.numberOfShares;
    }

    
    public BigDecimal getPurchasedPrice() {
        return this.purchasedPrice;
    }
    
    public String getStatus() {
        return this.status ;
    }
}
