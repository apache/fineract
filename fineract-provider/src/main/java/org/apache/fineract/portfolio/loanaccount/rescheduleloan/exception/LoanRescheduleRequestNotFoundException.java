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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan reschedule request resources are not found.
 **/
public class LoanRescheduleRequestNotFoundException extends AbstractPlatformResourceNotFoundException {

    /**
     * LoanRescheduleRequestNotFoundException constructor
     *
     * @param requestId
     *            the loan reschedule request identifier
     *
     **/
    public LoanRescheduleRequestNotFoundException(final Long requestId) {
        super("error.msg.loan.reschedule.request.id.invalid", "Loan reschedule request with identifier " + requestId + " does not exist",
                requestId);
    }

}
