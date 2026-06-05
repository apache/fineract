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

import java.time.LocalDate;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;

public interface SavingsDormancyService {

    /**
     * Decides, for the given business date, whether the dormancy sub-status of the account should escalate one level
     * (NONE -> INACTIVE -> DORMANT -> ESCHEAT) based on the owning product's dormancy configuration and the number of
     * days elapsed since the last deposit/withdrawal. Mirrors the criteria of the legacy
     * {@code UPDATE_SAVINGS_DORMANT_ACCOUNTS} job so it can be applied per-account during Savings COB.
     *
     * @return the sub-status the account should transition to, or {@code null} if no transition applies.
     */
    SavingsAccountSubStatusEnum deriveDormancyTransition(SavingsAccount savingsAccount, LocalDate businessDate);
}
