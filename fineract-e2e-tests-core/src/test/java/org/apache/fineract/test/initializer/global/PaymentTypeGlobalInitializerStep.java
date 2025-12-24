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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PaymentTypeData;
import org.apache.fineract.client.models.PaymentTypeRequest;
import org.apache.fineract.test.factory.PaymentTypesRequestFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentTypeGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String PAYMENT_TYPE_AUTOPAY = "AUTOPAY";
    public static final String PAYMENT_TYPE_DOWN_PAYMENT = "DOWN_PAYMENT";
    public static final String PAYMENT_TYPE_REAL_TIME = "REAL_TIME";
    public static final String PAYMENT_TYPE_SCHEDULED = "SCHEDULED";
    public static final String PAYMENT_TYPE_CHECK_PAYMENT = "CHECK_PAYMENT";
    public static final String PAYMENT_TYPE_OCA_PAYMENT = "OCA_PAYMENT";
    public static final String PAYMENT_TYPE_REPAYMENT_ADJUSTMENT_CHARGEBACK = "REPAYMENT_ADJUSTMENT_CHARGEBACK";
    public static final String PAYMENT_TYPE_REPAYMENT_ADJUSTMENT_REFUND = "REPAYMENT_ADJUSTMENT_REFUND";

    private final FineractFeignClient fineractClient;

    @Override
    public void initialize() {
        List<PaymentTypeData> existingPaymentTypes = new ArrayList<>();
        try {
            existingPaymentTypes = fineractClient.paymentType().getAllPaymentTypesUniversal(Map.of());
        } catch (Exception e) {
            log.debug("Could not retrieve existing payment types, will create them", e);
        }

        final List<PaymentTypeData> paymentTypes = existingPaymentTypes;

        List<String> paymentTypeNames = new ArrayList<>();
        paymentTypeNames.add(PAYMENT_TYPE_AUTOPAY);
        paymentTypeNames.add(PAYMENT_TYPE_DOWN_PAYMENT);
        paymentTypeNames.add(PAYMENT_TYPE_REAL_TIME);
        paymentTypeNames.add(PAYMENT_TYPE_SCHEDULED);
        paymentTypeNames.add(PAYMENT_TYPE_CHECK_PAYMENT);
        paymentTypeNames.add(PAYMENT_TYPE_OCA_PAYMENT);
        paymentTypeNames.add(PAYMENT_TYPE_REPAYMENT_ADJUSTMENT_CHARGEBACK);
        paymentTypeNames.add(PAYMENT_TYPE_REPAYMENT_ADJUSTMENT_REFUND);

        paymentTypeNames.forEach(paymentTypeName -> {
            boolean paymentTypeExists = paymentTypes.stream().anyMatch(pt -> paymentTypeName.equals(pt.getName()));
            if (paymentTypeExists) {
                return;
            }

            Integer position = paymentTypeNames.indexOf(paymentTypeName) + 2;
            PaymentTypeRequest postPaymentTypesRequest = PaymentTypesRequestFactory.defaultPaymentTypeRequest(paymentTypeName,
                    paymentTypeName, false, position);

            executeVoid(() -> fineractClient.paymentType().createPaymentType(postPaymentTypesRequest, Map.of()));
        });
    }
}
