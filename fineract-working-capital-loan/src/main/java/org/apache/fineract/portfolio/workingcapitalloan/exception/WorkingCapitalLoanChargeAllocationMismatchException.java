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
package org.apache.fineract.portfolio.workingcapitalloan.exception;

import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;

/**
 * Raised when a working capital allocation plan references a charge that is not among the charges fetched for the loan.
 * The allocation plan is derived from that same charge set, so a mismatch means the persisted data is inconsistent -
 * this is not a user input error. Fail loudly rather than settle a non-existent charge or silently skip it.
 */
public class WorkingCapitalLoanChargeAllocationMismatchException extends PlatformDataIntegrityException {

    public WorkingCapitalLoanChargeAllocationMismatchException(final Long chargeId) {
        super("error.msg.wc.loan.charge.allocation.mismatch",
                "Working capital allocation plan references charge with identifier " + chargeId + " that is not among the loan's charges",
                chargeId);
    }
}
