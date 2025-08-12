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
package org.apache.fineract.portfolio.collectionsheet.data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@FieldNameConstants
@AllArgsConstructor
@Builder
public class SavingDueTransactionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @PositiveOrZero(message = "{collection.sheet.savings.due.transactions.savings.id.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{collection.sheet.saving.due.transactions.savings.id.digits}")
    private Long savingsId;

    @NotNull(message = "{collection.sheet.savings.due.transaction.amount.required}")
    @DecimalMin(value = "0.01", message = "{collection.sheet.savings.due.transactions.amount.min}")
    private BigDecimal transactionAmount;

    @PositiveOrZero(message = "{collection.sheet.savings.due.transactions.deposit.account.type.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{collection.sheet.savings.due.transactions.deposit.account.type.digits}")
    private Long depositAccountType;

    @PositiveOrZero(message = "{collection.sheet.savings.due.transactions.deposit.payment.type.id.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{collection.sheet.savings.due.transactions.deposit.payment.type.id.digits}")
    private Long paymentTypeId;
    private String accountNumber;
    private String checkNumber;
    private String routingCode;
    private String receiptNumber;
    private String bankNumber;
}
