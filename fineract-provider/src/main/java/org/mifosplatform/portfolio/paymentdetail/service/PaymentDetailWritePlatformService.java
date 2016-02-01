/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymentdetail.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;

public interface PaymentDetailWritePlatformService {

    PaymentDetail createAndPersistPaymentDetail(final JsonCommand command, Map<String, Object> changes);

    PaymentDetail createPaymentDetail(final JsonCommand command, Map<String, Object> changes);

    PaymentDetail persistPaymentDetail(PaymentDetail paymentDetail);
}