/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymentdetail.service;

import java.util.Map;

import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentDetailWritePlatformServiceJpaRepositoryImpl implements PaymentDetailWritePlatformService {

    private final PaymentDetailRepository paymentDetailRepository;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Autowired
    public PaymentDetailWritePlatformServiceJpaRepositoryImpl(final PaymentDetailRepository paymentDetailRepository,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
        this.paymentDetailRepository = paymentDetailRepository;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
    }

    @Override
    @Transactional
    public PaymentDetail createPaymentDetail(final JsonCommand command, final Map<String, Object> changes) {
        final Long paymentTypeId = command.longValueOfParameterNamed(PaymentDetailConstants.paymentTypeParamName);
        if (paymentTypeId == null) { return null; }

        final CodeValue paymentType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                PaymentDetailConstants.paymentTypeCodeName, paymentTypeId);
        final PaymentDetail paymentDetail = PaymentDetail.generatePaymentDetail(paymentType, command, changes);
        return paymentDetail;

    }

    @Override
    @Transactional
    public PaymentDetail persistPaymentDetail(final PaymentDetail paymentDetail) {
        return this.paymentDetailRepository.save(paymentDetail);
    }

    @Override
    @Transactional
    public PaymentDetail createAndPersistPaymentDetail(final JsonCommand command, final Map<String, Object> changes) {
        final PaymentDetail paymentDetail = createPaymentDetail(command, changes);
        if (paymentDetail != null) { return persistPaymentDetail(paymentDetail); }
        return paymentDetail;
    }
}