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
package org.apache.fineract.cob.loan;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.data.RepositoryItemWriter;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLoanItemWriter extends RepositoryItemWriter<Loan> {

    private final LoanAccountLockRepository accountLockRepository;

    @Override
    public void write(@NotNull List<? extends Loan> items) throws Exception {
        super.write(items);
        List<Long> loanIds = items.stream().map(AbstractPersistableCustom::getId).toList();
        accountLockRepository.deleteByLoanIdInAndLockOwner(loanIds, getLockOwner());
    }

    protected abstract LockOwner getLockOwner();

}
