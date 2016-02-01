/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.data;

import java.math.BigDecimal;
import java.util.Date;

public class ShareMarketPriceData {

    private final Date startDate;
    
    private final BigDecimal shareValue;

    public ShareMarketPriceData(final Date startDate, final BigDecimal shareValue) {
        this.startDate = startDate ;
        this.shareValue = shareValue ;
    }
    
    public Date getStartDate() {
        return this.startDate;
    }

    public BigDecimal getShareValue() {
        return this.shareValue;
    }
}
