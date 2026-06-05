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
package org.apache.fineract.cob.savings;

import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.writer.AbstractCOBItemWriter;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;

public abstract class AbstractSavingsItemWriter extends AbstractCOBItemWriter<SavingsAccount> {

    private final SavingsAccountAssembler savingsAccountAssembler;

    protected AbstractSavingsItemWriter(COBBusinessStepService cobBusinessStepService, LockingService savingsLockingService,
            SavingsAccountRepository savingsAccountRepository, SavingsAccountAssembler savingsAccountAssembler) {
        super(cobBusinessStepService, savingsLockingService, savingsAccountRepository);
        this.savingsAccountAssembler = savingsAccountAssembler;
    }

    @Override
    protected SavingsAccount beforeBusinessSteps(SavingsAccount savingsAccount) {
        // Populate the transient helpers (e.g. SavingsHelper) which are not part of the persisted entity and are
        // required by interest posting and other business steps.
        savingsAccountAssembler.setHelpers(savingsAccount);
        return savingsAccount;
    }

    @Override
    protected void setLastRun(SavingsAccount processedSavingsAccount) {
        processedSavingsAccount.setLastClosedBusinessDate(getBusinessDate());
    }
}
