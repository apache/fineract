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
package org.apache.fineract.portfolio.loanaccount.service;

import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.jspecify.annotations.NonNull;

/**
 * Resolves whether a newly posted buy-down fee (or fee adjustment) should be recognized immediately. When the investor
 * module is disabled, the no-op implementation always reports deferred behaviour.
 */
public interface BuyDownFeeAmortizationStrategyService {

    /**
     * Immediate recognition applies only when the loan product is configured IMMEDIATE and the loan is already sold to
     * an external owner.
     */
    boolean shouldRecognizeImmediately(@NonNull Loan loan);
}
