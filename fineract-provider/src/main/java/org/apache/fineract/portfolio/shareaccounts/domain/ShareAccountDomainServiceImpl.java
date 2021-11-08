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
package org.apache.fineract.portfolio.shareaccounts.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareAccountDomainServiceImpl implements ShareAccountDomainService {

    @Autowired
    public ShareAccountDomainServiceImpl() {}

    @Transactional
    @Override
    public ShareAccountTransaction purchaseShares(final ShareAccount shareAccount, final LocalDate transactionDate, final Long shares,
            final BigDecimal unitPrice, final BigDecimal transactionAmount, final ShareAccountStatusType shareAccountStatusType,
            final AccountTransferType accountTransferType) {
        return new ShareAccountTransaction(shareAccount, transactionDate, shares, unitPrice, transactionAmount, shareAccountStatusType,
                accountTransferType);
    }
}
