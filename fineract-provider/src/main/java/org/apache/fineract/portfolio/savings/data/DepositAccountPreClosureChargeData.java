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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

/**
 * Immutable data object for Savings Account charge data.
 */
public class DepositAccountPreClosureChargeData {

    private final String name;
    private final BigDecimal amount;

    public DepositAccountPreClosureChargeData(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public DepositAccountPreClosureChargeData(SavingsAccountCharge charge) {
        this.name = charge.name();
        this.amount = charge.amount();
    }

    public static Collection<DepositAccountPreClosureChargeData> toDepositAccountPreClosureChargeData(List<SavingsAccountCharge> charges,
            List<SavingsAccountTransaction> withHoldTransaction) {
        Collection<DepositAccountPreClosureChargeData> dataList = new ArrayList<>();
        for (SavingsAccountCharge charge : charges) {
            dataList.add(new DepositAccountPreClosureChargeData(charge));
        }
        for (SavingsAccountTransaction tran : withHoldTransaction) {
            dataList.add(new DepositAccountPreClosureChargeData("WithHold Tax", tran.getAmount()));
        }

        return dataList;
    }
}
