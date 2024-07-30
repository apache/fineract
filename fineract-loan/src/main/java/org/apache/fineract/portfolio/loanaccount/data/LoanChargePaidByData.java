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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import lombok.Data;
import lombok.Getter;
import org.springframework.integration.annotation.Default;

@Data
@Getter
public class LoanChargePaidByData {

    private Long id;
    private BigDecimal amount;
    private Integer installmentNumber;
    private Long chargeId;
    private Long transactionId;
    private String name;

    @Default
    public LoanChargePaidByData(Long id, BigDecimal amount, Integer installmentNumber, Long chargeId, Long transactionId, String name) {
        this.id = id;
        this.amount = amount;
        this.installmentNumber = installmentNumber;
        this.chargeId = chargeId;
        this.transactionId = transactionId;
        this.name = name;
    }

    public LoanChargePaidByData(final Long id, final BigDecimal amount, final Integer installmentNumber, final Long chargeId,
            final Long transactionId) {
        this.id = id;
        this.amount = amount;
        this.installmentNumber = installmentNumber;
        this.chargeId = chargeId;
        this.transactionId = transactionId;
        this.name = null;
    }

}
