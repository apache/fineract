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
package org.apache.fineract.portfolio.loanaccount.exception;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when actual disbursement date does not match with expected
 * disbursement date
 *
 */
public class DateMismatchException extends AbstractPlatformDomainRuleException {

    public DateMismatchException(final LocalDate actualDisbursementDate, final LocalDate expectedDisbursedOnLocalDate) {
        super("error.msg.actual.disbursement.date.does.not.match.with.expected.disbursal.date", "Actual disbursement date  ("
                + actualDisbursementDate + ") " + "should be equal to Expected disbursal date (" + expectedDisbursedOnLocalDate + ")",
                actualDisbursementDate, expectedDisbursedOnLocalDate, null);
    }
}
