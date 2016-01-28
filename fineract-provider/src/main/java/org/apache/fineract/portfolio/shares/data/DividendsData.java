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
