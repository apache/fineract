/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.data;

import java.math.BigDecimal;
import java.util.Date;

public class DividendsData {

    private Long clientId;

    private String clientName;

    private String shareAccountNo;

    private String savingsAccountNo;

    private Long numberOfShares;

    private BigDecimal dividendAmount;

    private Date dividendIssuedDate;

    public DividendsData(final Long clientId, final String clientName, final String savingsAccountNo, final Long numberOfShares,
            final BigDecimal dividendAmount, final Date dividendIssuedDate) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.savingsAccountNo = savingsAccountNo;
        this.numberOfShares = numberOfShares;
        this.dividendAmount = dividendAmount;
        this.dividendIssuedDate = dividendIssuedDate;
    }
}
