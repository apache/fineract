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
package org.apache.fineract.accounting.journalentry.data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;

@Data
public class GLAccountBalanceHolder {

    private final Map<Long, GLAccount> glAccountMap = new LinkedHashMap<>();
    private final Map<Long, BigDecimal> debitBalances = new LinkedHashMap<>();
    private final Map<Long, BigDecimal> creditBalances = new LinkedHashMap<>();

    public void addToCredit(@NotNull GLAccount creditAccount, @NotNull BigDecimal amount) {
        addToProperBalance(creditBalances, creditAccount, amount);
    }

    public void addToDebit(@NotNull GLAccount debitAccount, @NotNull BigDecimal amount) {
        addToProperBalance(debitBalances, debitAccount, amount);
    }

    private void addToProperBalance(@NotNull Map<Long, BigDecimal> balanceMap, @NotNull @NotNull GLAccount account,
            @NotNull BigDecimal amount) {
        glAccountMap.putIfAbsent(account.getId(), account);
        if (balanceMap.containsKey(account.getId())) {
            BigDecimal totalAmount = balanceMap.get(account.getId()).add(amount);
            balanceMap.put(account.getId(), totalAmount);
        } else {
            balanceMap.put(account.getId(), amount);
        }
    }
}
