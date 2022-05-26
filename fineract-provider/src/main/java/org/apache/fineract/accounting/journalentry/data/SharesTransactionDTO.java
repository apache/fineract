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
package org.apache.fineract.accounting.journalentry.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountTransactionEnumData;

@RequiredArgsConstructor
@Getter
public class SharesTransactionDTO {

    private final Long officeId;
    private final Long paymentTypeId;
    private final String transactionId;
    private final Date transactionDate;
    private final ShareAccountTransactionEnumData transactionType;
    private final ShareAccountTransactionEnumData transactionStatus;

    private final BigDecimal amount;

    /*** Breakup of amounts **/
    private final BigDecimal chargeAmount;

    /** Breakdowns of fees and penalties this Transaction pays **/
    private final List<ChargePaymentDTO> feePayments;
}
