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
package org.apache.fineract.portfolio.savings.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;

public class SavingsAccountChargesPaidByData implements Serializable {

    private final Long chargeId;
    private final BigDecimal amount;
    private SavingsAccountChargeData savingsAccountChargeData;

    public SavingsAccountChargesPaidByData(final Long chargeId, final BigDecimal amount) {
        this.chargeId = chargeId;
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    public void setSavingsAccountChargeData(final SavingsAccountChargeData savingsAccountChargeData) {
        this.savingsAccountChargeData = savingsAccountChargeData;
    }

    public static SavingsAccountChargesPaidByData instance(final Long savingsAccountChargeId, final BigDecimal amount) {
        return new SavingsAccountChargesPaidByData(savingsAccountChargeId, amount);
    }

    public boolean isFeeCharge() {
        return (this.savingsAccountChargeData == null) ? false : this.savingsAccountChargeData.isFeeCharge();
    }

    public boolean isPenaltyCharge() {
        return (this.savingsAccountChargeData == null) ? false : this.savingsAccountChargeData.isPenaltyCharge();
    }

    public SavingsAccountChargeData getSavingsAccountCharge() {
        return this.savingsAccountChargeData;
    }

}
