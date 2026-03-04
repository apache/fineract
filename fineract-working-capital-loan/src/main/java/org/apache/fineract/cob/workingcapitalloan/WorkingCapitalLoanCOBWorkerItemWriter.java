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
package org.apache.fineract.cob.workingcapitalloan;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.domain.WorkingCapitalLoanAccountLock;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.springframework.data.repository.CrudRepository;

@Slf4j
public class WorkingCapitalLoanCOBWorkerItemWriter extends AbstractWorkingCapitalLoanCOBWorkerItemWriter {

    public WorkingCapitalLoanCOBWorkerItemWriter(LockingService<WorkingCapitalLoanAccountLock> loanLockingService,
            CrudRepository<WorkingCapitalLoan, Long> repository) {
        super(loanLockingService, repository);
    }

    @Override
    protected LockOwner getLockOwner() {
        return LockOwner.LOAN_COB_CHUNK_PROCESSING;
    }

}
