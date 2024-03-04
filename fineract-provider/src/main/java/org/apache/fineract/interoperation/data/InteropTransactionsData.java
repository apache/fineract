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
package org.apache.fineract.interoperation.data;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

@Getter
@Setter
public class InteropTransactionsData extends CommandProcessingResult {

    List<InteropTransactionData> transactions;

    public InteropTransactionsData(Long entityId, List<InteropTransactionData> transactions) {
        super(entityId);
        this.transactions = transactions;
    }

    public static InteropTransactionsData build(SavingsAccount account, @NotNull Predicate<SavingsAccountTransaction> filter) {
        if (account == null) {
            return null;
        }

        List<InteropTransactionData> trans = account.getTransactions().stream().filter(filter).sorted((t1, t2) -> {
            int i = DateUtils.compare(t2.getDateOf(), t1.getDateOf());
            return i != 0 ? i : Long.signum(t2.getId() - t1.getId());
        }).map(InteropTransactionData::build).collect(Collectors.toList());
        return new InteropTransactionsData(account.getId(), trans);
    }
}
