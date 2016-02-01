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
package org.apache.fineract.portfolio.shares.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.apache.fineract.organisation.monetary.data.CurrencyData;


public class ProductDividendsData {

    private Long id ;
    
    private Long productId ;
    
    private String productName ;
    
    private Date dividendsIssuedDate ;
    
    private BigDecimal dividendAmount ;
    
    private CurrencyData currency ;
    
    Collection<DividendsData> dividendsData ;
    
    public ProductDividendsData(final Long productId, final String productName, final Date dividendsIssuedDate, 
            final BigDecimal dividendAmount, final CurrencyData currency, final Collection<DividendsData> dividendsData) {
        this.productId = productId ;
        this.productName = productName ;
        this.dividendsIssuedDate = dividendsIssuedDate ;
        this.dividendAmount = dividendAmount ;
        this.dividendsData = dividendsData ;
        this.currency = currency ;
    }
    
    public void setId(Long id) {
        this.id = id ;
    }
    
    public Long getId() {
        return this.id ;
    }
    
    public Long getProductId() {
        return productId ;
    }
}
