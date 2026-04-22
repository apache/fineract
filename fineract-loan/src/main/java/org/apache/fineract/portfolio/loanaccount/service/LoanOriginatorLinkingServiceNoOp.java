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

import com.google.gson.JsonArray;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * No-op implementation of {@link LoanOriginatorLinkingService} that is used when the loan-origination module is
 * disabled. When originator data is provided during loan application, this implementation silently ignores it.
 */
@Service
@ConditionalOnMissingBean(name = "loanOriginatorLinkingServiceImpl")
public class LoanOriginatorLinkingServiceNoOp implements LoanOriginatorLinkingService {

    @Override
    public void processOriginatorsForLoanApplication(Long loanId, JsonArray originatorsArray) {
        // No-op when loan-origination module is not enabled
    }
}
