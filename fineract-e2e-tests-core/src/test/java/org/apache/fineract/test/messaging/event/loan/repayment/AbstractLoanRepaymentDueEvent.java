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
package org.apache.fineract.test.messaging.event.loan.repayment;

import java.util.function.Function;
import org.apache.fineract.avro.loan.v1.LoanRepaymentDueDataV1;
import org.apache.fineract.test.messaging.event.Event;

public abstract class AbstractLoanRepaymentDueEvent implements Event<LoanRepaymentDueDataV1> {

    @Override
    public Class<LoanRepaymentDueDataV1> getDataClass() {
        return LoanRepaymentDueDataV1.class;
    }

    @Override
    public Function<LoanRepaymentDueDataV1, Long> getIdExtractor() {
        return loanRepaymentDueDataV1 -> (Long) loanRepaymentDueDataV1.getLoanId();
    }
}
