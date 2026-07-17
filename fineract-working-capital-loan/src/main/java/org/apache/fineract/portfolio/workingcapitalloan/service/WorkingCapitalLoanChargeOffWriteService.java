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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

/**
 * Write operations for the Working Capital Loan charge-off feature.
 * <p>
 * Charge-off is a pure accounting tag: it marks the account as charged-off and records a non-monetary charge-off
 * transaction, but it does not affect the portfolio (balance, schedule) and the loan stays {@code ACTIVE}. The tag is
 * only removed by an explicit undo (used when the charge-off was applied in error).
 */
public interface WorkingCapitalLoanChargeOffWriteService {

    CommandProcessingResult chargeOff(Long loanId, JsonCommand command);

    CommandProcessingResult undoChargeOff(Long loanId, JsonCommand command);
}
