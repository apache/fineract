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
package org.apache.fineract.test.messaging.event.loan.charge;

import java.util.function.Function;
import org.apache.fineract.avro.loan.v1.LoanChargeDataV1;
import org.apache.fineract.test.messaging.event.Event;

public abstract class AbstractLoanChargeEvent implements Event<LoanChargeDataV1> {

    @Override
    public Class<LoanChargeDataV1> getDataClass() {
        return LoanChargeDataV1.class;
    }

    @Override
    public Function<LoanChargeDataV1, Long> getIdExtractor() {
        return LoanChargeDataV1::getId;
    }

}
