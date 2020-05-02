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
package org.apache.fineract.accounting.closure.data;

import java.math.BigDecimal;

public class SingleDebitOrCreditEntryData {

    private final Long glAccountId;
    private final String glAccountName;
    private final BigDecimal amount;
    private final String comments;

    public SingleDebitOrCreditEntryData(Long glAccountId, String glAccountName, BigDecimal amount, String comments) {
        this.glAccountId = glAccountId;
        this.glAccountName = glAccountName;
        this.amount = amount;
        this.comments = comments;
    }

    public Long getGlAccountId() {return this.glAccountId;}

    public String getGlAccountName() {return this.glAccountName;}

    public BigDecimal getAmount() {return this.amount;}

    public String getComments() {return this.comments;}

}
