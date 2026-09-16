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

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;

/**
 * Thrown when the stored Working Capital Loan inputs yield a Total Days / EIR that cannot be validly calculated, so an
 * amortization schedule cannot be built. A FLAT schedule solves no EIR, so for it the same structural failure is
 * reported under a schedule-level code instead.
 */
public class WorkingCapitalLoanEirNotCalculableException extends AbstractPlatformDomainRuleException {

    private static final String GLOBALISATION_CODE = "error.msg.workingcapitalloan.eir.not.calculable";
    private static final String FLAT_GLOBALISATION_CODE = "error.msg.workingcapitalloan.schedule.not.calculable";

    public WorkingCapitalLoanEirNotCalculableException() {
        super(GLOBALISATION_CODE, WorkingCapitalLoanConstants.EIR_NOT_CALCULABLE_USER_MESSAGE);
    }

    public WorkingCapitalLoanEirNotCalculableException(final Throwable cause) {
        super(GLOBALISATION_CODE, WorkingCapitalLoanConstants.EIR_NOT_CALCULABLE_USER_MESSAGE, cause);
    }

    private WorkingCapitalLoanEirNotCalculableException(final String code, final String message, final Throwable cause) {
        super(code, message, cause);
    }

    public static WorkingCapitalLoanEirNotCalculableException forType(final WorkingCapitalAmortizationType amortizationType) {
        return forType(amortizationType, null);
    }

    public static WorkingCapitalLoanEirNotCalculableException forType(final WorkingCapitalAmortizationType amortizationType,
            final Throwable cause) {
        if (amortizationType != null && amortizationType.isFlat()) {
            return new WorkingCapitalLoanEirNotCalculableException(FLAT_GLOBALISATION_CODE,
                    WorkingCapitalLoanConstants.SCHEDULE_NOT_CALCULABLE_USER_MESSAGE, cause);
        }
        return cause != null ? new WorkingCapitalLoanEirNotCalculableException(cause) : new WorkingCapitalLoanEirNotCalculableException();
    }
}
