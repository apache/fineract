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
package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Template behind {@code GET /working-capital-loans/{loanId}/template?templateType=approve}.
 * <p>
 * Approval is the only loan action that posts no transaction - it just moves the loan's status - which is why it is the
 * only one left here. Everything that writes a transaction row, disbursement included, lives on {@code GET
 * /working-capital-loans/{loanId}/transactions/template} and answers with
 * {@link WorkingCapitalLoanTransactionTemplateData} instead.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingCapitalLoanCommandTemplateData implements Serializable {

    private LocalDate approvalDate;
    private BigDecimal approvalAmount;
    private BigDecimal discountAmount;
    private Boolean overrideDiscountDisabled;

    private LocalDate expectedDisbursementDate;

    private CurrencyData currency;

}
