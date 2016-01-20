/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.data;

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
