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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;

@RequiredArgsConstructor
@Getter
public class TaxPaymentDTO {

    private final Long debitAccountId;
    private final Long creditAccountId;
    private final BigDecimal amount;

    public static List<TaxPaymentDTO> share(final List<TaxPaymentDTO> taxPayments, final BigDecimal part, final BigDecimal whole) {
        return taxPayments.stream().map(tax -> new TaxPaymentDTO(tax.debitAccountId, tax.creditAccountId,
                tax.amount.multiply(part).divide(whole, tax.amount.scale(), MoneyHelper.getRoundingMode()))).toList();
    }

    public static List<TaxPaymentDTO> remainder(final List<TaxPaymentDTO> taxPayments, final List<TaxPaymentDTO> share) {
        final List<TaxPaymentDTO> remainder = new ArrayList<>();
        for (int i = 0; i < taxPayments.size(); i++) {
            final TaxPaymentDTO tax = taxPayments.get(i);
            remainder.add(new TaxPaymentDTO(tax.debitAccountId, tax.creditAccountId, tax.amount.subtract(share.get(i).amount)));
        }
        return remainder;
    }
}
