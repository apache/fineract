/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.mifosplatform.organisation.monetary.data.CurrencyData;


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
