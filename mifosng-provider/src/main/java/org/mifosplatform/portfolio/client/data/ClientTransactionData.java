/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;

@SuppressWarnings("unused")
public class ClientTransactionData {

    private final Long id;
    private final Long officeId;
    private final String officeName;
    private final EnumOptionData type;
    private final LocalDate date;
    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;
    private final BigDecimal amount;
    private final String externalId;
    private final LocalDate submittedOnDate;
    private final boolean reversed;

    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;

    public static ClientTransactionData create(Long id, Long officeId, String officeName, EnumOptionData type, LocalDate date,
            CurrencyData currency, PaymentDetailData paymentDetailData, BigDecimal amount, String externalId, LocalDate submittedOnDate,
            boolean reversed) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new ClientTransactionData(id, officeId, officeName, type, date, currency, paymentDetailData, amount, externalId,
                submittedOnDate, reversed, paymentTypeOptions);
    }

    private ClientTransactionData(Long id, Long officeId, String officeName, EnumOptionData type, LocalDate date, CurrencyData currency,
            PaymentDetailData paymentDetailData, BigDecimal amount, String externalId, LocalDate submittedOnDate, boolean reversed,
            Collection<PaymentTypeData> paymentTypeOptions) {
        super();
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.type = type;
        this.date = date;
        this.currency = currency;
        this.paymentDetailData = paymentDetailData;
        this.amount = amount;
        this.externalId = externalId;
        this.submittedOnDate = submittedOnDate;
        this.reversed = reversed;
        this.paymentTypeOptions = paymentTypeOptions;
    }

}
