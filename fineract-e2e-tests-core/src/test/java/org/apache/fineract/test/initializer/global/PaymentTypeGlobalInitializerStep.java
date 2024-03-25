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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostPaymentTypesRequest;
import org.apache.fineract.client.services.PaymentTypeApi;
import org.apache.fineract.test.factory.PaymentTypesRequestFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

    private final PaymentTypeApi paymentTypeApi;

    @Override
    public void initialize() throws Exception {
        List<String> paymentTypes = new ArrayList<>();
        paymentTypes.add(PAYMENT_TYPE_AUTOPAY);
        paymentTypes.add(PAYMENT_TYPE_DOWN_PAYMENT);
        paymentTypes.add(PAYMENT_TYPE_REAL_TIME);
        paymentTypes.add(PAYMENT_TYPE_SCHEDULED);
        paymentTypes.add(PAYMENT_TYPE_CHECK_PAYMENT);
        paymentTypes.add(PAYMENT_TYPE_OCA_PAYMENT);
        paymentTypes.add(PAYMENT_TYPE_REPAYMENT_ADJUSTMENT_CHARGEBACK);
        paymentTypes.add(PAYMENT_TYPE_REPAYMENT_ADJUSTMENT_REFUND);

        paymentTypes.forEach(paymentType -> {
            Integer position = paymentTypes.indexOf(paymentType) + 2;
            PostPaymentTypesRequest postPaymentTypesRequest = PaymentTypesRequestFactory.defaultPaymentTypeRequest(paymentType, paymentType,
                    false, position);

            try {
                paymentTypeApi.createPaymentType(postPaymentTypesRequest).execute();
            } catch (IOException e) {
                throw new RuntimeException("Error while creating payment type", e);
            }
        });
    }
}
