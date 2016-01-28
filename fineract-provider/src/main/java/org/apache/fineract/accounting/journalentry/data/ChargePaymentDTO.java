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

public class ChargePaymentDTO {

    private final Long chargeId;
    private final BigDecimal amount;
    private final Long loanChargeId;

    public ChargePaymentDTO(final Long chargeId, final Long loanChargeId, final BigDecimal amount) {
        this.chargeId = chargeId;
        this.amount = amount;
        this.loanChargeId = loanChargeId;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Long getLoanChargeId() {
        return this.loanChargeId;
    }

}
