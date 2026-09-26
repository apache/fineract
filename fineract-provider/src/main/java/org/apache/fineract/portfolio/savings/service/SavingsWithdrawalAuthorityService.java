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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavingsWithdrawalAuthorityService {

    private final NsimbiMonetaryAuthorityPolicyService monetaryAuthority;

    public void require(SavingsTransactionExecutionContext context, SavingsTransactionKind kind, SavingsAccount account,
            BigDecimal amount) {
        if (context == null) {
            throw SavingsTransactionCommandEnvelope.untrustedOrigin(kind);
        }
        context.requireKind(kind);
        if (!monetaryAuthority.allows(context.maker().getId(), MonetaryAuthorityType.WITHDRAWALS, account.getCurrency().getCode(),
                amount)) {
            throw new GeneralPlatformDomainRuleException("error.msg.savings.withdrawal.monetary.authority.denied",
                    "The original submitter does not have WITHDRAWALS monetary authority for this amount and account currency.");
        }
    }
}
