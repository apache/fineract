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
package org.apache.fineract.infrastructure.bulkimport.importhandler.savings;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.poi.ss.usermodel.Row;

/**
 * Checks that the account of a savings, fixed deposit or recurring deposit transaction import row belongs to the client
 * named in the row's Client Name column. The template writes that column as {@code name(clientId)}.
 */
public final class SavingsTransactionClientCheck {

    private static final Pattern CLIENT_ID_SUFFIX = Pattern.compile("\\((\\d+)\\)$");

    private SavingsTransactionClientCheck() {}

    public static void requireAccountOfClient(final Row row, final Long savingsAccountId,
            final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper) {
        final String accountNo = ImportHandlerUtils.readAsString(TransactionConstants.SAVINGS_ACCOUNT_NO_COL, row);
        if (savingsAccountId == null) {
            throw new IllegalArgumentException("Account No. is mandatory");
        }
        final String clientName = ImportHandlerUtils.readAsString(TransactionConstants.CLIENT_NAME_COL, row);
        if (clientName == null) {
            throw new IllegalArgumentException("Client Name is mandatory");
        }
        final Matcher matcher = CLIENT_ID_SUFFIX.matcher(clientName.trim());
        if (!matcher.find()) {
            throw new IllegalArgumentException("Client Name '" + clientName + "' not found");
        }
        final Long clientId = Long.valueOf(matcher.group(1));
        final Long accountClientId = savingsAccountRepositoryWrapper.findClientIdById(savingsAccountId);
        if (accountClientId == null && !savingsAccountRepositoryWrapper.existsById(savingsAccountId)) {
            throw new SavingsAccountNotFoundException(savingsAccountId);
        }
        if (!Objects.equals(clientId, accountClientId)) {
            throw new IllegalArgumentException("Account '" + accountNo + "' does not belong to client '" + clientName + "'");
        }
    }
}
