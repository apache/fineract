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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectedAmortizationScheduleCalculatorTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, null);

    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final BigDecimal RATE = new BigDecimal("18");
    private static final int DAY_COUNT = 360;
    private static final LocalDate EXPECTED_DISBURSEMENT_DATE = LocalDate.of(2019, 1, 1);
    private static final int TERM = 200;

    private final ProjectedAmortizationScheduleCalculator calculator = new DefaultProjectedAmortizationScheduleCalculator();

    @BeforeEach
    void setBusinessDate() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, EXPECTED_DISBURSEMENT_DATE)));
    }

    @AfterEach
    void resetContext() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testAddDisbursement_term10_discountFee50_netDisbursement450_then430() {
        final BigDecimal discountFee = new BigDecimal("50");
        final BigDecimal initialNetDisbursement = new BigDecimal("450");
        final LocalDate initialDisbursementDate = LocalDate.of(2019, 1, 1);

        final ProjectedAmortizationScheduleModel initial = ProjectedAmortizationScheduleModel.generate(discountFee, initialNetDisbursement,
                TPV, RATE, DAY_COUNT, initialDisbursementDate, MC, CURRENCY, EXPECTED_DISBURSEMENT_DATE);
        final ProjectedAmortizationScheduleModel model1 = initial.regenerate(discountFee, initialNetDisbursement, initialDisbursementDate,
                initialDisbursementDate);

        assertEquals(10, model1.originalPaymentNumber());
        assertEquals(11, model1.projectedPayments().size());

        checkInst(model1, 0, 0, LocalDate.of(2019, 1, 1), 0, -450.00, null, 1.00000000, -450.00, 450.00, 450.00, null, null, null, 50.00,
                50.00);
        checkInst(model1, 1, 1, LocalDate.of(2019, 1, 2), 1, 50.00, null, 0.98074794, 49.04, 408.83, null, 8.83, null, null, 41.17, null);
        checkInst(model1, 2, 2, LocalDate.of(2019, 1, 3), 2, 50.00, null, 0.96186652, 48.09, 366.86, null, 8.03, null, null, 33.14, null);
        checkInst(model1, 3, 3, LocalDate.of(2019, 1, 4), 3, 50.00, null, 0.94334860, 47.17, 324.06, null, 7.20, null, null, 25.94, null);
        checkInst(model1, 4, 4, LocalDate.of(2019, 1, 5), 4, 50.00, null, 0.92518720, 46.26, 280.42, null, 6.36, null, null, 19.58, null);
        checkInst(model1, 5, 5, LocalDate.of(2019, 1, 6), 5, 50.00, null, 0.90737544, 45.37, 235.93, null, 5.50, null, null, 14.08, null);
        checkInst(model1, 6, 6, LocalDate.of(2019, 1, 7), 6, 50.00, null, 0.88990659, 44.50, 190.56, null, 4.63, null, null, 9.45, null);
        checkInst(model1, 7, 7, LocalDate.of(2019, 1, 8), 7, 50.00, null, 0.87277405, 43.64, 144.30, null, 3.74, null, null, 5.71, null);
        checkInst(model1, 8, 8, LocalDate.of(2019, 1, 9), 8, 50.00, null, 0.85597135, 42.80, 97.13, null, 2.83, null, null, 2.88, null);
        checkInst(model1, 9, 9, LocalDate.of(2019, 1, 10), 9, 50.00, null, 0.83949214, 41.97, 49.04, null, 1.91, null, null, 0.97, null);
        checkInst(model1, 10, 10, LocalDate.of(2019, 1, 11), 10, 50.00, null, 0.82333018, 41.17, 0.00, null, 0.97, null, null, 0.00, null);

        final BigDecimal newNetDisbursement = new BigDecimal("430");
        final LocalDate newDisbursementDate = LocalDate.of(2019, 1, 5);

        final ProjectedAmortizationScheduleModel model2 = model1.regenerate(discountFee, newNetDisbursement, newDisbursementDate,
                newDisbursementDate);

        assertEquals(10, model2.originalPaymentNumber());
        assertEquals(11, model2.projectedPayments().size());

        checkInst(model2, 0, 0, LocalDate.of(2019, 1, 5), 0, -430.00, null, 1.00000000, -430.00, 430.00, 430.00, null, null, null, 50.00,
                50.00);
        checkInst(model2, 1, 1, LocalDate.of(2019, 1, 6), 1, 50.00, null, 0.97919271, 48.96, 389.14, null, 9.14, null, null, 40.86, null);
        checkInst(model2, 2, 2, LocalDate.of(2019, 1, 7), 2, 50.00, null, 0.95881836, 47.94, 347.41, null, 8.27, null, null, 32.59, null);
        checkInst(model2, 3, 3, LocalDate.of(2019, 1, 8), 3, 50.00, null, 0.93886795, 46.94, 304.79, null, 7.38, null, null, 25.21, null);
        checkInst(model2, 4, 4, LocalDate.of(2019, 1, 9), 4, 50.00, null, 0.91933266, 45.97, 261.26, null, 6.48, null, null, 18.73, null);
        checkInst(model2, 5, 5, LocalDate.of(2019, 1, 10), 5, 50.00, null, 0.90020383, 45.01, 216.82, null, 5.55, null, null, 13.18, null);
        checkInst(model2, 6, 6, LocalDate.of(2019, 1, 11), 6, 50.00, null, 0.88147303, 44.07, 171.42, null, 4.61, null, null, 8.57, null);
        checkInst(model2, 7, 7, LocalDate.of(2019, 1, 12), 7, 50.00, null, 0.86313197, 43.16, 125.07, null, 3.64, null, null, 4.93, null);
        checkInst(model2, 8, 8, LocalDate.of(2019, 1, 13), 8, 50.00, null, 0.84517253, 42.26, 77.72, null, 2.66, null, null, 2.27, null);
        checkInst(model2, 9, 9, LocalDate.of(2019, 1, 14), 9, 50.00, null, 0.82758678, 41.38, 29.38, null, 1.65, null, null, 0.62, null);
        checkInst(model2, 10, 10, LocalDate.of(2019, 1, 15), 10, 30.00, null, 0.81036694, 24.31, 0.00, null, 0.62, null, null, 0.00, null);
    }

    @Test
    void testProjectedSchedule_term200_discountFee1000_netDisbursement9000() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        assertEquals(TERM, model.originalPaymentNumber());

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 0, -9000.00, null, 1.00000000, -9000.00, 9000.00, 9000.00, null, null, null,
                1000.00, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 1, 50.00, null, 0.99893332, 49.95, 8959.61, null, 9.61, null, null, 990.39, null);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 2, 50.00, null, 0.99786779, 49.89, 8919.18, null, 9.57, null, null, 980.82, null);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 3, 50.00, null, 0.99680339, 49.84, 8878.70, null, 9.52, null, null, 971.30, null);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 4, 50.00, null, 0.99574012, 49.79, 8838.18, null, 9.48, null, null, 961.82, null);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 5, 50.00, null, 0.99467799, 49.73, 8797.62, null, 9.44, null, null, 952.38, null);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 6, 50.00, null, 0.99361699, 49.68, 8757.01, null, 9.39, null, null, 942.99, null);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 7, 50.00, null, 0.99255712, 49.63, 8716.36, null, 9.35, null, null, 933.64, null);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 8, 50.00, null, 0.99149839, 49.57, 8675.67, null, 9.31, null, null, 924.33, null);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 9, 50.00, null, 0.99044078, 49.52, 8634.94, null, 9.26, null, null, 915.07, null);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 10, 50.00, null, 0.98938430, 49.47, 8594.16, null, 9.22, null, null, 905.85,
                null);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 11, 50.00, null, 0.98832895, 49.42, 8553.33, null, 9.18, null, null, 896.67,
                null);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 12, 50.00, null, 0.98727472, 49.36, 8512.47, null, 9.13, null, null, 887.54,
                null);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 13, 50.00, null, 0.98622162, 49.31, 8471.56, null, 9.09, null, null, 878.45,
                null);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 14, 50.00, null, 0.98516964, 49.26, 8430.60, null, 9.05, null, null, 869.40,
                null);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 15, 50.00, null, 0.98411879, 49.21, 8389.61, null, 9.00, null, null, 860.40,
                null);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 16, 50.00, null, 0.98306905, 49.15, 8348.56, null, 8.96, null, null, 851.44,
                null);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 17, 50.00, null, 0.98202044, 49.10, 8307.48, null, 8.91, null, null, 842.53,
                null);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 18, 50.00, null, 0.98097294, 49.05, 8266.35, null, 8.87, null, null, 833.66,
                null);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 19, 50.00, null, 0.97992656, 49.00, 8225.18, null, 8.83, null, null, 824.83,
                null);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 20, 50.00, null, 0.97888129, 48.94, 8183.96, null, 8.78, null, null, 816.05,
                null);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 21, 50.00, null, 0.97783715, 48.89, 8142.70, null, 8.74, null, null, 807.31,
                null);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 22, 50.00, null, 0.97679411, 48.84, 8101.39, null, 8.69, null, null, 798.62,
                null);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 23, 50.00, null, 0.97575219, 48.79, 8060.04, null, 8.65, null, null, 789.97,
                null);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 24, 50.00, null, 0.97471138, 48.74, 8018.65, null, 8.61, null, null, 781.36,
                null);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 25, 50.00, null, 0.97367168, 48.68, 7977.21, null, 8.56, null, null, 772.80,
                null);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 26, 50.00, null, 0.97263309, 48.63, 7935.73, null, 8.52, null, null, 764.28,
                null);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 27, 50.00, null, 0.97159560, 48.58, 7894.21, null, 8.47, null, null, 755.81,
                null);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 28, 50.00, null, 0.97055922, 48.53, 7852.63, null, 8.43, null, null, 747.38,
                null);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 29, 50.00, null, 0.96952395, 48.48, 7811.02, null, 8.39, null, null, 738.99,
                null);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 30, 50.00, null, 0.96848979, 48.42, 7769.36, null, 8.34, null, null, 730.65,
                null);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 31, 50.00, null, 0.96745672, 48.37, 7727.66, null, 8.30, null, null, 722.35,
                null);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 32, 50.00, null, 0.96642476, 48.32, 7685.91, null, 8.25, null, null, 714.10,
                null);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 33, 50.00, null, 0.96539390, 48.27, 7644.12, null, 8.21, null, null, 705.89,
                null);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 34, 50.00, null, 0.96436413, 48.22, 7602.28, null, 8.16, null, null, 697.73,
                null);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 35, 50.00, null, 0.96333547, 48.17, 7560.40, null, 8.12, null, null, 689.61,
                null);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 36, 50.00, null, 0.96230790, 48.12, 7518.47, null, 8.07, null, null, 681.54,
                null);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 37, 50.00, null, 0.96128143, 48.06, 7476.50, null, 8.03, null, null, 673.51,
                null);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 38, 50.00, null, 0.96025606, 48.01, 7434.48, null, 7.98, null, null, 665.53,
                null);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 39, 50.00, null, 0.95923178, 47.96, 7392.42, null, 7.94, null, null, 657.59,
                null);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 40, 50.00, null, 0.95820859, 47.91, 7350.31, null, 7.89, null, null, 649.70,
                null);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 41, 50.00, null, 0.95718649, 47.86, 7308.16, null, 7.85, null, null, 641.85,
                null);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 42, 50.00, null, 0.95616548, 47.81, 7265.97, null, 7.80, null, null, 634.05,
                null);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 43, 50.00, null, 0.95514557, 47.76, 7223.72, null, 7.76, null, null, 626.29,
                null);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 44, 50.00, null, 0.95412674, 47.71, 7181.44, null, 7.71, null, null, 618.58,
                null);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 45, 50.00, null, 0.95310899, 47.66, 7139.11, null, 7.67, null, null, 610.91,
                null);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 46, 50.00, null, 0.95209233, 47.60, 7096.73, null, 7.62, null, null, 603.29,
                null);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 47, 50.00, null, 0.95107676, 47.55, 7054.31, null, 7.58, null, null, 595.71,
                null);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 48, 50.00, null, 0.95006227, 47.50, 7011.84, null, 7.53, null, null, 588.18,
                null);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 49, 50.00, null, 0.94904886, 47.45, 6969.33, null, 7.49, null, null, 580.69,
                null);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 50, 50.00, null, 0.94803653, 47.40, 6926.77, null, 7.44, null, null, 573.25,
                null);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 51, 50.00, null, 0.94702529, 47.35, 6884.17, null, 7.40, null, null, 565.85,
                null);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 52, 50.00, null, 0.94601512, 47.30, 6841.52, null, 7.35, null, null, 558.50,
                null);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 53, 50.00, null, 0.94500603, 47.25, 6798.82, null, 7.31, null, null, 551.19,
                null);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 54, 50.00, null, 0.94399801, 47.20, 6756.08, null, 7.26, null, null, 543.93,
                null);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 55, 50.00, null, 0.94299107, 47.15, 6713.30, null, 7.21, null, null, 536.72,
                null);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 56, 50.00, null, 0.94198521, 47.10, 6670.47, null, 7.17, null, null, 529.55,
                null);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 57, 50.00, null, 0.94098042, 47.05, 6627.59, null, 7.12, null, null, 522.43,
                null);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 58, 50.00, null, 0.93997669, 47.00, 6584.67, null, 7.08, null, null, 515.35,
                null);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 59, 50.00, null, 0.93897404, 46.95, 6541.70, null, 7.03, null, null, 508.32,
                null);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 60, 50.00, null, 0.93797246, 46.90, 6498.68, null, 6.99, null, null, 501.33,
                null);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 61, 50.00, null, 0.93697195, 46.85, 6455.62, null, 6.94, null, null, 494.39,
                null);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 62, 50.00, null, 0.93597251, 46.80, 6412.51, null, 6.89, null, null, 487.50,
                null);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 63, 50.00, null, 0.93497413, 46.75, 6369.36, null, 6.85, null, null, 480.65,
                null);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 64, 50.00, null, 0.93397681, 46.70, 6326.16, null, 6.80, null, null, 473.85,
                null);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 65, 50.00, null, 0.93298056, 46.65, 6282.92, null, 6.76, null, null, 467.09,
                null);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 66, 50.00, null, 0.93198538, 46.60, 6239.63, null, 6.71, null, null, 460.38,
                null);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 67, 50.00, null, 0.93099125, 46.55, 6196.29, null, 6.66, null, null, 453.72,
                null);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 68, 50.00, null, 0.92999818, 46.50, 6152.91, null, 6.62, null, null, 447.10,
                null);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 69, 50.00, null, 0.92900618, 46.45, 6109.48, null, 6.57, null, null, 440.53,
                null);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 70, 50.00, null, 0.92801523, 46.40, 6066.00, null, 6.52, null, null, 434.01,
                null);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 71, 50.00, null, 0.92702534, 46.35, 6022.48, null, 6.48, null, null, 427.53,
                null);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 72, 50.00, null, 0.92603650, 46.30, 5978.91, null, 6.43, null, null, 421.10,
                null);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 73, 50.00, null, 0.92504872, 46.25, 5935.29, null, 6.38, null, null, 414.72,
                null);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 74, 50.00, null, 0.92406200, 46.20, 5891.63, null, 6.34, null, null, 408.38,
                null);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 75, 50.00, null, 0.92307632, 46.15, 5847.92, null, 6.29, null, null, 402.09,
                null);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 76, 50.00, null, 0.92209170, 46.10, 5804.17, null, 6.24, null, null, 395.85,
                null);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 77, 50.00, null, 0.92110813, 46.06, 5760.36, null, 6.20, null, null, 389.65,
                null);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 78, 50.00, null, 0.92012560, 46.01, 5716.52, null, 6.15, null, null, 383.50,
                null);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 79, 50.00, null, 0.91914413, 45.96, 5672.62, null, 6.10, null, null, 377.40,
                null);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 80, 50.00, null, 0.91816370, 45.91, 5628.68, null, 6.06, null, null, 371.34,
                null);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 81, 50.00, null, 0.91718432, 45.86, 5584.69, null, 6.01, null, null, 365.33,
                null);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 82, 50.00, null, 0.91620598, 45.81, 5540.65, null, 5.96, null, null, 359.37,
                null);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 83, 50.00, null, 0.91522868, 45.76, 5496.57, null, 5.92, null, null, 353.45,
                null);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 84, 50.00, null, 0.91425243, 45.71, 5452.44, null, 5.87, null, null, 347.58,
                null);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 85, 50.00, null, 0.91327722, 45.66, 5408.26, null, 5.82, null, null, 341.76,
                null);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 86, 50.00, null, 0.91230305, 45.62, 5364.03, null, 5.78, null, null, 335.98,
                null);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 87, 50.00, null, 0.91132992, 45.57, 5319.76, null, 5.73, null, null, 330.25,
                null);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 88, 50.00, null, 0.91035783, 45.52, 5275.44, null, 5.68, null, null, 324.57,
                null);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 89, 50.00, null, 0.90938677, 45.47, 5231.08, null, 5.63, null, null, 318.94,
                null);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 90, 50.00, null, 0.90841675, 45.42, 5186.66, null, 5.59, null, null, 313.35,
                null);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 91, 50.00, null, 0.90744776, 45.37, 5142.20, null, 5.54, null, null, 307.81,
                null);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 92, 50.00, null, 0.90647981, 45.32, 5097.69, null, 5.49, null, null, 302.32,
                null);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 93, 50.00, null, 0.90551289, 45.28, 5053.13, null, 5.44, null, null, 296.88,
                null);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 94, 50.00, null, 0.90454700, 45.23, 5008.53, null, 5.40, null, null, 291.48,
                null);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 95, 50.00, null, 0.90358215, 45.18, 4963.88, null, 5.35, null, null, 286.13,
                null);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 96, 50.00, null, 0.90261832, 45.13, 4919.18, null, 5.30, null, null, 280.83,
                null);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 97, 50.00, null, 0.90165552, 45.08, 4874.43, null, 5.25, null, null, 275.58,
                null);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 98, 50.00, null, 0.90069374, 45.03, 4829.64, null, 5.20, null, null, 270.38,
                null);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 99, 50.00, null, 0.89973299, 44.99, 4784.79, null, 5.16, null, null, 265.22,
                null);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 100, 50.00, null, 0.89877327, 44.94, 4739.90, null, 5.11, null, null, 260.11,
                null);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 101, 50.00, null, 0.89781457, 44.89, 4694.96, null, 5.06, null, null, 255.05,
                null);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 102, 50.00, null, 0.89685689, 44.84, 4649.98, null, 5.01, null, null, 250.04,
                null);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 103, 50.00, null, 0.89590024, 44.80, 4604.94, null, 4.97, null, null, 245.07,
                null);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 104, 50.00, null, 0.89494460, 44.75, 4559.86, null, 4.92, null, null, 240.15,
                null);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 105, 50.00, null, 0.89398999, 44.70, 4514.73, null, 4.87, null, null, 235.28,
                null);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 106, 50.00, null, 0.89303639, 44.65, 4469.55, null, 4.82, null, null, 230.46,
                null);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 107, 50.00, null, 0.89208381, 44.60, 4424.32, null, 4.77, null, null, 225.69,
                null);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 108, 50.00, null, 0.89113225, 44.56, 4379.05, null, 4.72, null, null, 220.97,
                null);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 109, 50.00, null, 0.89018170, 44.51, 4333.72, null, 4.68, null, null, 216.29,
                null);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 110, 50.00, null, 0.88923216, 44.46, 4288.35, null, 4.63, null, null, 211.66,
                null);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 111, 50.00, null, 0.88828364, 44.41, 4242.93, null, 4.58, null, null, 207.08,
                null);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 112, 50.00, null, 0.88733613, 44.37, 4197.46, null, 4.53, null, null, 202.55,
                null);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 113, 50.00, null, 0.88638963, 44.32, 4151.94, null, 4.48, null, null, 198.07,
                null);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 114, 50.00, null, 0.88544414, 44.27, 4106.38, null, 4.43, null, null, 193.64,
                null);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 115, 50.00, null, 0.88449966, 44.22, 4060.76, null, 4.38, null, null, 189.26,
                null);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 116, 50.00, null, 0.88355619, 44.18, 4015.10, null, 4.34, null, null, 184.92,
                null);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 117, 50.00, null, 0.88261372, 44.13, 3969.38, null, 4.29, null, null, 180.63,
                null);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 118, 50.00, null, 0.88167226, 44.08, 3923.62, null, 4.24, null, null, 176.39,
                null);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 119, 50.00, null, 0.88073180, 44.04, 3877.81, null, 4.19, null, null, 172.20,
                null);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 120, 50.00, null, 0.87979234, 43.99, 3831.95, null, 4.14, null, null, 168.06,
                null);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 121, 50.00, null, 0.87885389, 43.94, 3786.04, null, 4.09, null, null, 163.97,
                null);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 122, 50.00, null, 0.87791644, 43.90, 3740.09, null, 4.04, null, null, 159.93,
                null);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 123, 50.00, null, 0.87697999, 43.85, 3694.08, null, 3.99, null, null, 155.94,
                null);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 124, 50.00, null, 0.87604453, 43.80, 3648.03, null, 3.94, null, null, 152.00,
                null);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 125, 50.00, null, 0.87511008, 43.76, 3601.92, null, 3.90, null, null, 148.10,
                null);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 126, 50.00, null, 0.87417662, 43.71, 3555.77, null, 3.85, null, null, 144.25,
                null);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 127, 50.00, null, 0.87324416, 43.66, 3509.56, null, 3.80, null, null, 140.45,
                null);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 128, 50.00, null, 0.87231269, 43.62, 3463.31, null, 3.75, null, null, 136.70,
                null);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 129, 50.00, null, 0.87138221, 43.57, 3417.01, null, 3.70, null, null, 133.00,
                null);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 130, 50.00, null, 0.87045273, 43.52, 3370.66, null, 3.65, null, null, 129.35,
                null);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 131, 50.00, null, 0.86952424, 43.48, 3324.26, null, 3.60, null, null, 125.75,
                null);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 132, 50.00, null, 0.86859674, 43.43, 3277.81, null, 3.55, null, null, 122.20,
                null);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 133, 50.00, null, 0.86767023, 43.38, 3231.31, null, 3.50, null, null, 118.70,
                null);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 134, 50.00, null, 0.86674471, 43.34, 3184.76, null, 3.45, null, null, 115.25,
                null);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 135, 50.00, null, 0.86582017, 43.29, 3138.16, null, 3.40, null, null, 111.85,
                null);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 136, 50.00, null, 0.86489662, 43.24, 3091.51, null, 3.35, null, null, 108.50,
                null);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 137, 50.00, null, 0.86397406, 43.20, 3044.81, null, 3.30, null, null, 105.20,
                null);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 138, 50.00, null, 0.86305248, 43.15, 2998.06, null, 3.25, null, null, 101.95,
                null);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 139, 50.00, null, 0.86213188, 43.11, 2951.26, null, 3.20, null, null, 98.75,
                null);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 140, 50.00, null, 0.86121227, 43.06, 2904.42, null, 3.15, null, null, 95.60,
                null);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 141, 50.00, null, 0.86029363, 43.01, 2857.52, null, 3.10, null, null, 92.50,
                null);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 142, 50.00, null, 0.85937598, 42.97, 2810.57, null, 3.05, null, null, 89.45,
                null);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 143, 50.00, null, 0.85845930, 42.92, 2763.57, null, 3.00, null, null, 86.45,
                null);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 144, 50.00, null, 0.85754361, 42.88, 2716.52, null, 2.95, null, null, 83.50,
                null);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 145, 50.00, null, 0.85662889, 42.83, 2669.42, null, 2.90, null, null, 80.60,
                null);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 146, 50.00, null, 0.85571514, 42.79, 2622.27, null, 2.85, null, null, 77.75,
                null);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 147, 50.00, null, 0.85480237, 42.74, 2575.07, null, 2.80, null, null, 74.95,
                null);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 148, 50.00, null, 0.85389057, 42.69, 2527.82, null, 2.75, null, null, 72.20,
                null);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 149, 50.00, null, 0.85297975, 42.65, 2480.52, null, 2.70, null, null, 69.50,
                null);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 150, 50.00, null, 0.85206990, 42.60, 2433.17, null, 2.65, null, null, 66.85,
                null);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 151, 50.00, null, 0.85116101, 42.56, 2385.77, null, 2.60, null, null, 64.25,
                null);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 152, 50.00, null, 0.85025310, 42.51, 2338.31, null, 2.55, null, null, 61.70,
                null);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 153, 50.00, null, 0.84934616, 42.47, 2290.81, null, 2.50, null, null, 59.20,
                null);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 154, 50.00, null, 0.84844018, 42.42, 2243.26, null, 2.45, null, null, 56.75,
                null);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 155, 50.00, null, 0.84753517, 42.38, 2195.65, null, 2.40, null, null, 54.35,
                null);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 156, 50.00, null, 0.84663113, 42.33, 2148.00, null, 2.34, null, null, 52.01,
                null);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 157, 50.00, null, 0.84572805, 42.29, 2100.29, null, 2.29, null, null, 49.72,
                null);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 158, 50.00, null, 0.84482593, 42.24, 2052.53, null, 2.24, null, null, 47.48,
                null);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 159, 50.00, null, 0.84392477, 42.20, 2004.73, null, 2.19, null, null, 45.29,
                null);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 160, 50.00, null, 0.84302458, 42.15, 1956.87, null, 2.14, null, null, 43.15,
                null);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 161, 50.00, null, 0.84212535, 42.11, 1908.96, null, 2.09, null, null, 41.06,
                null);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 162, 50.00, null, 0.84122707, 42.06, 1860.99, null, 2.04, null, null, 39.02,
                null);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 163, 50.00, null, 0.84032975, 42.02, 1812.98, null, 1.99, null, null, 37.03,
                null);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 164, 50.00, null, 0.83943340, 41.97, 1764.92, null, 1.94, null, null, 35.09,
                null);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 165, 50.00, null, 0.83853799, 41.93, 1716.80, null, 1.88, null, null, 33.21,
                null);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 166, 50.00, null, 0.83764354, 41.88, 1668.64, null, 1.83, null, null, 31.38,
                null);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 167, 50.00, null, 0.83675005, 41.84, 1620.42, null, 1.78, null, null, 29.60,
                null);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 168, 50.00, null, 0.83585751, 41.79, 1572.15, null, 1.73, null, null, 27.87,
                null);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 169, 50.00, null, 0.83496592, 41.75, 1523.83, null, 1.68, null, null, 26.19,
                null);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 170, 50.00, null, 0.83407528, 41.70, 1475.45, null, 1.63, null, null, 24.56,
                null);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 171, 50.00, null, 0.83318560, 41.66, 1427.03, null, 1.58, null, null, 22.98,
                null);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 172, 50.00, null, 0.83229686, 41.61, 1378.55, null, 1.52, null, null, 21.46,
                null);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 173, 50.00, null, 0.83140907, 41.57, 1330.02, null, 1.47, null, null, 19.99,
                null);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 174, 50.00, null, 0.83052222, 41.53, 1281.45, null, 1.42, null, null, 18.57,
                null);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 175, 50.00, null, 0.82963633, 41.48, 1232.81, null, 1.37, null, null, 17.20,
                null);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 176, 50.00, null, 0.82875137, 41.44, 1184.13, null, 1.32, null, null, 15.88,
                null);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 177, 50.00, null, 0.82786736, 41.39, 1135.39, null, 1.26, null, null, 14.62,
                null);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 178, 50.00, null, 0.82698430, 41.35, 1086.61, null, 1.21, null, null, 13.41,
                null);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 179, 50.00, null, 0.82610217, 41.31, 1037.77, null, 1.16, null, null, 12.25,
                null);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 180, 50.00, null, 0.82522099, 41.26, 988.88, null, 1.11, null, null, 11.14,
                null);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 181, 50.00, null, 0.82434075, 41.22, 939.93, null, 1.06, null, null, 10.08,
                null);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 182, 50.00, null, 0.82346144, 41.17, 890.93, null, 1.00, null, null, 9.08,
                null);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 183, 50.00, null, 0.82258308, 41.13, 841.89, null, 0.95, null, null, 8.13,
                null);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 184, 50.00, null, 0.82170565, 41.09, 792.79, null, 0.90, null, null, 7.23,
                null);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 185, 50.00, null, 0.82082916, 41.04, 743.63, null, 0.85, null, null, 6.38,
                null);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 186, 50.00, null, 0.81995360, 41.00, 694.43, null, 0.79, null, null, 5.59,
                null);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 187, 50.00, null, 0.81907897, 40.95, 645.17, null, 0.74, null, null, 4.85,
                null);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 188, 50.00, null, 0.81820528, 40.91, 595.86, null, 0.69, null, null, 4.16,
                null);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 189, 50.00, null, 0.81733252, 40.87, 546.49, null, 0.64, null, null, 3.52,
                null);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 190, 50.00, null, 0.81646069, 40.82, 497.08, null, 0.58, null, null, 2.94,
                null);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 191, 50.00, null, 0.81558979, 40.78, 447.61, null, 0.53, null, null, 2.41,
                null);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 192, 50.00, null, 0.81471983, 40.74, 398.08, null, 0.48, null, null, 1.93,
                null);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 193, 50.00, null, 0.81385078, 40.69, 348.51, null, 0.43, null, null, 1.50,
                null);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 194, 50.00, null, 0.81298267, 40.65, 298.88, null, 0.37, null, null, 1.13,
                null);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 195, 50.00, null, 0.81211548, 40.61, 249.20, null, 0.32, null, null, 0.81,
                null);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 196, 50.00, null, 0.81124922, 40.56, 199.47, null, 0.27, null, null, 0.54,
                null);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 197, 50.00, null, 0.81038388, 40.52, 149.68, null, 0.21, null, null, 0.33,
                null);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 198, 50.00, null, 0.80951946, 40.48, 99.84, null, 0.16, null, null, 0.17,
                null);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 199, 50.00, null, 0.80865597, 40.43, 49.95, null, 0.11, null, null, 0.06,
                null);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 200, 50.00, null, 0.80779339, 40.39, 0.00, null, 0.06, null, null, 0.00,
                null);
    }

    @Test
    void testNoDiscountLoan_term180_discountFee0_netDisbursement9000() {
        final BigDecimal zeroDiscount = BigDecimal.ZERO;
        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(zeroDiscount, NET_DISBURSEMENT, TPV,
                RATE, DAY_COUNT, EXPECTED_DISBURSEMENT_DATE, MC, CURRENCY, EXPECTED_DISBURSEMENT_DATE);

        assertEquals(180, model.originalPaymentNumber(), "loanTerm = ceil(9000/50) = 180");
        assertEquals(BigDecimal.ZERO, model.effectiveInterestRate(), "EIR should be 0 when no discount fee");
        assertEquals(181, model.projectedPayments().size(), "disbursement + 180 periods");

        // All discount factors should be 1.0 (EIR = 0)
        for (int i = 1; i < model.projectedPayments().size(); i++) {
            final ProjectedPayment p = model.projectedPayments().get(i);
            assertEquals(0, BigDecimal.ONE.compareTo(p.discountFactor()), "period " + i + ": discount factor should be 1.0");
            assertMoneyValue(50.00, p.npvValue(), 2, "period " + i + ": NPV should equal payment (DF=1)");
        }

        // Deferred balance should be 0 throughout (no discount to defer)
        for (int i = 0; i < model.projectedPayments().size(); i++) {
            final ProjectedPayment p = model.projectedPayments().get(i);
            if (p.expectedDiscountFeeBalance() != null) {
                assertMoneyValue(0.00, p.expectedDiscountFeeBalance(), 2, "period " + i + ": deferred balance should be 0");
            }
        }

        // Last period balance should be 0
        final ProjectedPayment last = model.projectedPayments().getLast();
        assertMoneyValue(0.00, last.expectedBalance(), 2, "last period balance should be 0");
    }

    @Test
    void testOnTimePayment_term200_discountFee1000_netDisbursement9000_pay50_50() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("50"));
        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(2), new BigDecimal("50"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 0, -9000.00, null, 1.00000000, -9000.00, 9000.00, 9000.00, null, null, null,
                1000.00, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 0, 50.00, 50.00, 1.00000000, 50.00, 8959.61, 8959.61, 9.61, 9.61, 0.00, 990.39,
                990.39);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 0, 50.00, 50.00, 1.00000000, 50.00, 8919.18, 8919.18, 9.57, 9.57, 0.00, 980.82,
                980.82);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 1, 50.00, null, 0.99893332, 49.95, 8878.70, null, 9.52, null, null, 971.30, null);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 2, 50.00, null, 0.99786779, 49.89, 8838.18, null, 9.48, null, null, 961.82, null);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 3, 50.00, null, 0.99680339, 49.84, 8797.62, null, 9.44, null, null, 952.38, null);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 4, 50.00, null, 0.99574012, 49.79, 8757.01, null, 9.39, null, null, 942.99, null);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 5, 50.00, null, 0.99467799, 49.73, 8716.36, null, 9.35, null, null, 933.64, null);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 6, 50.00, null, 0.99361699, 49.68, 8675.67, null, 9.31, null, null, 924.33, null);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 7, 50.00, null, 0.99255712, 49.63, 8634.94, null, 9.26, null, null, 915.07, null);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 8, 50.00, null, 0.99149839, 49.57, 8594.16, null, 9.22, null, null, 905.85,
                null);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 9, 50.00, null, 0.99044078, 49.52, 8553.33, null, 9.18, null, null, 896.67,
                null);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 10, 50.00, null, 0.98938430, 49.47, 8512.47, null, 9.13, null, null, 887.54,
                null);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 11, 50.00, null, 0.98832895, 49.42, 8471.56, null, 9.09, null, null, 878.45,
                null);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 12, 50.00, null, 0.98727472, 49.36, 8430.60, null, 9.05, null, null, 869.40,
                null);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 13, 50.00, null, 0.98622162, 49.31, 8389.61, null, 9.00, null, null, 860.40,
                null);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 14, 50.00, null, 0.98516964, 49.26, 8348.56, null, 8.96, null, null, 851.44,
                null);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 15, 50.00, null, 0.98411879, 49.21, 8307.48, null, 8.91, null, null, 842.53,
                null);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 16, 50.00, null, 0.98306905, 49.15, 8266.35, null, 8.87, null, null, 833.66,
                null);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 17, 50.00, null, 0.98202044, 49.10, 8225.18, null, 8.83, null, null, 824.83,
                null);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 18, 50.00, null, 0.98097294, 49.05, 8183.96, null, 8.78, null, null, 816.05,
                null);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 19, 50.00, null, 0.97992656, 49.00, 8142.70, null, 8.74, null, null, 807.31,
                null);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 20, 50.00, null, 0.97888129, 48.94, 8101.39, null, 8.69, null, null, 798.62,
                null);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 21, 50.00, null, 0.97783715, 48.89, 8060.04, null, 8.65, null, null, 789.97,
                null);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 22, 50.00, null, 0.97679411, 48.84, 8018.65, null, 8.61, null, null, 781.36,
                null);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 23, 50.00, null, 0.97575219, 48.79, 7977.21, null, 8.56, null, null, 772.80,
                null);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 24, 50.00, null, 0.97471138, 48.74, 7935.73, null, 8.52, null, null, 764.28,
                null);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 25, 50.00, null, 0.97367168, 48.68, 7894.21, null, 8.47, null, null, 755.81,
                null);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 26, 50.00, null, 0.97263309, 48.63, 7852.63, null, 8.43, null, null, 747.38,
                null);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 27, 50.00, null, 0.97159560, 48.58, 7811.02, null, 8.39, null, null, 738.99,
                null);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 28, 50.00, null, 0.97055922, 48.53, 7769.36, null, 8.34, null, null, 730.65,
                null);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 29, 50.00, null, 0.96952395, 48.48, 7727.66, null, 8.30, null, null, 722.35,
                null);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 30, 50.00, null, 0.96848979, 48.42, 7685.91, null, 8.25, null, null, 714.10,
                null);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 31, 50.00, null, 0.96745672, 48.37, 7644.12, null, 8.21, null, null, 705.89,
                null);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 32, 50.00, null, 0.96642476, 48.32, 7602.28, null, 8.16, null, null, 697.73,
                null);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 33, 50.00, null, 0.96539390, 48.27, 7560.40, null, 8.12, null, null, 689.61,
                null);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 34, 50.00, null, 0.96436413, 48.22, 7518.47, null, 8.07, null, null, 681.54,
                null);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 35, 50.00, null, 0.96333547, 48.17, 7476.50, null, 8.03, null, null, 673.51,
                null);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 36, 50.00, null, 0.96230790, 48.12, 7434.48, null, 7.98, null, null, 665.53,
                null);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 37, 50.00, null, 0.96128143, 48.06, 7392.42, null, 7.94, null, null, 657.59,
                null);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 38, 50.00, null, 0.96025606, 48.01, 7350.31, null, 7.89, null, null, 649.70,
                null);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 39, 50.00, null, 0.95923178, 47.96, 7308.16, null, 7.85, null, null, 641.85,
                null);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 40, 50.00, null, 0.95820859, 47.91, 7265.97, null, 7.80, null, null, 634.05,
                null);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 41, 50.00, null, 0.95718649, 47.86, 7223.72, null, 7.76, null, null, 626.29,
                null);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 42, 50.00, null, 0.95616548, 47.81, 7181.44, null, 7.71, null, null, 618.58,
                null);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 43, 50.00, null, 0.95514557, 47.76, 7139.11, null, 7.67, null, null, 610.91,
                null);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 44, 50.00, null, 0.95412674, 47.71, 7096.73, null, 7.62, null, null, 603.29,
                null);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 45, 50.00, null, 0.95310899, 47.66, 7054.31, null, 7.58, null, null, 595.71,
                null);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 46, 50.00, null, 0.95209233, 47.60, 7011.84, null, 7.53, null, null, 588.18,
                null);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 47, 50.00, null, 0.95107676, 47.55, 6969.33, null, 7.49, null, null, 580.69,
                null);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 48, 50.00, null, 0.95006227, 47.50, 6926.77, null, 7.44, null, null, 573.25,
                null);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 49, 50.00, null, 0.94904886, 47.45, 6884.17, null, 7.40, null, null, 565.85,
                null);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 50, 50.00, null, 0.94803653, 47.40, 6841.52, null, 7.35, null, null, 558.50,
                null);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 51, 50.00, null, 0.94702529, 47.35, 6798.82, null, 7.31, null, null, 551.19,
                null);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 52, 50.00, null, 0.94601512, 47.30, 6756.08, null, 7.26, null, null, 543.93,
                null);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 53, 50.00, null, 0.94500603, 47.25, 6713.30, null, 7.21, null, null, 536.72,
                null);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 54, 50.00, null, 0.94399801, 47.20, 6670.47, null, 7.17, null, null, 529.55,
                null);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 55, 50.00, null, 0.94299107, 47.15, 6627.59, null, 7.12, null, null, 522.43,
                null);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 56, 50.00, null, 0.94198521, 47.10, 6584.67, null, 7.08, null, null, 515.35,
                null);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 57, 50.00, null, 0.94098042, 47.05, 6541.70, null, 7.03, null, null, 508.32,
                null);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 58, 50.00, null, 0.93997669, 47.00, 6498.68, null, 6.99, null, null, 501.33,
                null);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 59, 50.00, null, 0.93897404, 46.95, 6455.62, null, 6.94, null, null, 494.39,
                null);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 60, 50.00, null, 0.93797246, 46.90, 6412.51, null, 6.89, null, null, 487.50,
                null);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 61, 50.00, null, 0.93697195, 46.85, 6369.36, null, 6.85, null, null, 480.65,
                null);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 62, 50.00, null, 0.93597251, 46.80, 6326.16, null, 6.80, null, null, 473.85,
                null);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 63, 50.00, null, 0.93497413, 46.75, 6282.92, null, 6.76, null, null, 467.09,
                null);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 64, 50.00, null, 0.93397681, 46.70, 6239.63, null, 6.71, null, null, 460.38,
                null);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 65, 50.00, null, 0.93298056, 46.65, 6196.29, null, 6.66, null, null, 453.72,
                null);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 66, 50.00, null, 0.93198538, 46.60, 6152.91, null, 6.62, null, null, 447.10,
                null);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 67, 50.00, null, 0.93099125, 46.55, 6109.48, null, 6.57, null, null, 440.53,
                null);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 68, 50.00, null, 0.92999818, 46.50, 6066.00, null, 6.52, null, null, 434.01,
                null);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 69, 50.00, null, 0.92900618, 46.45, 6022.48, null, 6.48, null, null, 427.53,
                null);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 70, 50.00, null, 0.92801523, 46.40, 5978.91, null, 6.43, null, null, 421.10,
                null);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 71, 50.00, null, 0.92702534, 46.35, 5935.29, null, 6.38, null, null, 414.72,
                null);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 72, 50.00, null, 0.92603650, 46.30, 5891.63, null, 6.34, null, null, 408.38,
                null);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 73, 50.00, null, 0.92504872, 46.25, 5847.92, null, 6.29, null, null, 402.09,
                null);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 74, 50.00, null, 0.92406200, 46.20, 5804.17, null, 6.24, null, null, 395.85,
                null);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 75, 50.00, null, 0.92307632, 46.15, 5760.36, null, 6.20, null, null, 389.65,
                null);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 76, 50.00, null, 0.92209170, 46.10, 5716.52, null, 6.15, null, null, 383.50,
                null);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 77, 50.00, null, 0.92110813, 46.06, 5672.62, null, 6.10, null, null, 377.40,
                null);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 78, 50.00, null, 0.92012560, 46.01, 5628.68, null, 6.06, null, null, 371.34,
                null);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 79, 50.00, null, 0.91914413, 45.96, 5584.69, null, 6.01, null, null, 365.33,
                null);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 80, 50.00, null, 0.91816370, 45.91, 5540.65, null, 5.96, null, null, 359.37,
                null);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 81, 50.00, null, 0.91718432, 45.86, 5496.57, null, 5.92, null, null, 353.45,
                null);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 82, 50.00, null, 0.91620598, 45.81, 5452.44, null, 5.87, null, null, 347.58,
                null);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 83, 50.00, null, 0.91522868, 45.76, 5408.26, null, 5.82, null, null, 341.76,
                null);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 84, 50.00, null, 0.91425243, 45.71, 5364.03, null, 5.78, null, null, 335.98,
                null);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 85, 50.00, null, 0.91327722, 45.66, 5319.76, null, 5.73, null, null, 330.25,
                null);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 86, 50.00, null, 0.91230305, 45.62, 5275.44, null, 5.68, null, null, 324.57,
                null);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 87, 50.00, null, 0.91132992, 45.57, 5231.08, null, 5.63, null, null, 318.94,
                null);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 88, 50.00, null, 0.91035783, 45.52, 5186.66, null, 5.59, null, null, 313.35,
                null);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 89, 50.00, null, 0.90938677, 45.47, 5142.20, null, 5.54, null, null, 307.81,
                null);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 90, 50.00, null, 0.90841675, 45.42, 5097.69, null, 5.49, null, null, 302.32,
                null);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 91, 50.00, null, 0.90744776, 45.37, 5053.13, null, 5.44, null, null, 296.88,
                null);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 92, 50.00, null, 0.90647981, 45.32, 5008.53, null, 5.40, null, null, 291.48,
                null);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 93, 50.00, null, 0.90551289, 45.28, 4963.88, null, 5.35, null, null, 286.13,
                null);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 94, 50.00, null, 0.90454700, 45.23, 4919.18, null, 5.30, null, null, 280.83,
                null);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 95, 50.00, null, 0.90358215, 45.18, 4874.43, null, 5.25, null, null, 275.58,
                null);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 96, 50.00, null, 0.90261832, 45.13, 4829.64, null, 5.20, null, null, 270.38,
                null);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 97, 50.00, null, 0.90165552, 45.08, 4784.79, null, 5.16, null, null, 265.22,
                null);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 98, 50.00, null, 0.90069374, 45.03, 4739.90, null, 5.11, null, null, 260.11,
                null);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 99, 50.00, null, 0.89973299, 44.99, 4694.96, null, 5.06, null, null, 255.05,
                null);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 100, 50.00, null, 0.89877327, 44.94, 4649.98, null, 5.01, null, null, 250.04,
                null);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 101, 50.00, null, 0.89781457, 44.89, 4604.94, null, 4.97, null, null, 245.07,
                null);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 102, 50.00, null, 0.89685689, 44.84, 4559.86, null, 4.92, null, null, 240.15,
                null);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 103, 50.00, null, 0.89590024, 44.80, 4514.73, null, 4.87, null, null, 235.28,
                null);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 104, 50.00, null, 0.89494460, 44.75, 4469.55, null, 4.82, null, null, 230.46,
                null);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 105, 50.00, null, 0.89398999, 44.70, 4424.32, null, 4.77, null, null, 225.69,
                null);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 106, 50.00, null, 0.89303639, 44.65, 4379.05, null, 4.72, null, null, 220.97,
                null);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 107, 50.00, null, 0.89208381, 44.60, 4333.72, null, 4.68, null, null, 216.29,
                null);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 108, 50.00, null, 0.89113225, 44.56, 4288.35, null, 4.63, null, null, 211.66,
                null);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 109, 50.00, null, 0.89018170, 44.51, 4242.93, null, 4.58, null, null, 207.08,
                null);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 110, 50.00, null, 0.88923216, 44.46, 4197.46, null, 4.53, null, null, 202.55,
                null);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 111, 50.00, null, 0.88828364, 44.41, 4151.94, null, 4.48, null, null, 198.07,
                null);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 112, 50.00, null, 0.88733613, 44.37, 4106.38, null, 4.43, null, null, 193.64,
                null);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 113, 50.00, null, 0.88638963, 44.32, 4060.76, null, 4.38, null, null, 189.26,
                null);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 114, 50.00, null, 0.88544414, 44.27, 4015.10, null, 4.34, null, null, 184.92,
                null);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 115, 50.00, null, 0.88449966, 44.22, 3969.38, null, 4.29, null, null, 180.63,
                null);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 116, 50.00, null, 0.88355619, 44.18, 3923.62, null, 4.24, null, null, 176.39,
                null);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 117, 50.00, null, 0.88261372, 44.13, 3877.81, null, 4.19, null, null, 172.20,
                null);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 118, 50.00, null, 0.88167226, 44.08, 3831.95, null, 4.14, null, null, 168.06,
                null);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 119, 50.00, null, 0.88073180, 44.04, 3786.04, null, 4.09, null, null, 163.97,
                null);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 120, 50.00, null, 0.87979234, 43.99, 3740.09, null, 4.04, null, null, 159.93,
                null);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 121, 50.00, null, 0.87885389, 43.94, 3694.08, null, 3.99, null, null, 155.94,
                null);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 122, 50.00, null, 0.87791644, 43.90, 3648.03, null, 3.94, null, null, 152.00,
                null);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 123, 50.00, null, 0.87697999, 43.85, 3601.92, null, 3.90, null, null, 148.10,
                null);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 124, 50.00, null, 0.87604453, 43.80, 3555.77, null, 3.85, null, null, 144.25,
                null);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 125, 50.00, null, 0.87511008, 43.76, 3509.56, null, 3.80, null, null, 140.45,
                null);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 126, 50.00, null, 0.87417662, 43.71, 3463.31, null, 3.75, null, null, 136.70,
                null);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 127, 50.00, null, 0.87324416, 43.66, 3417.01, null, 3.70, null, null, 133.00,
                null);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 128, 50.00, null, 0.87231269, 43.62, 3370.66, null, 3.65, null, null, 129.35,
                null);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 129, 50.00, null, 0.87138221, 43.57, 3324.26, null, 3.60, null, null, 125.75,
                null);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 130, 50.00, null, 0.87045273, 43.52, 3277.81, null, 3.55, null, null, 122.20,
                null);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 131, 50.00, null, 0.86952424, 43.48, 3231.31, null, 3.50, null, null, 118.70,
                null);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 132, 50.00, null, 0.86859674, 43.43, 3184.76, null, 3.45, null, null, 115.25,
                null);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 133, 50.00, null, 0.86767023, 43.38, 3138.16, null, 3.40, null, null, 111.85,
                null);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 134, 50.00, null, 0.86674471, 43.34, 3091.51, null, 3.35, null, null, 108.50,
                null);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 135, 50.00, null, 0.86582017, 43.29, 3044.81, null, 3.30, null, null, 105.20,
                null);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 136, 50.00, null, 0.86489662, 43.24, 2998.06, null, 3.25, null, null, 101.95,
                null);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 137, 50.00, null, 0.86397406, 43.20, 2951.26, null, 3.20, null, null, 98.75,
                null);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 138, 50.00, null, 0.86305248, 43.15, 2904.42, null, 3.15, null, null, 95.60,
                null);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 139, 50.00, null, 0.86213188, 43.11, 2857.52, null, 3.10, null, null, 92.50,
                null);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 140, 50.00, null, 0.86121227, 43.06, 2810.57, null, 3.05, null, null, 89.45,
                null);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 141, 50.00, null, 0.86029363, 43.01, 2763.57, null, 3.00, null, null, 86.45,
                null);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 142, 50.00, null, 0.85937598, 42.97, 2716.52, null, 2.95, null, null, 83.50,
                null);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 143, 50.00, null, 0.85845930, 42.92, 2669.42, null, 2.90, null, null, 80.60,
                null);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 144, 50.00, null, 0.85754361, 42.88, 2622.27, null, 2.85, null, null, 77.75,
                null);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 145, 50.00, null, 0.85662889, 42.83, 2575.07, null, 2.80, null, null, 74.95,
                null);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 146, 50.00, null, 0.85571514, 42.79, 2527.82, null, 2.75, null, null, 72.20,
                null);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 147, 50.00, null, 0.85480237, 42.74, 2480.52, null, 2.70, null, null, 69.50,
                null);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 148, 50.00, null, 0.85389057, 42.69, 2433.17, null, 2.65, null, null, 66.85,
                null);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 149, 50.00, null, 0.85297975, 42.65, 2385.77, null, 2.60, null, null, 64.25,
                null);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 150, 50.00, null, 0.85206990, 42.60, 2338.31, null, 2.55, null, null, 61.70,
                null);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 151, 50.00, null, 0.85116101, 42.56, 2290.81, null, 2.50, null, null, 59.20,
                null);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 152, 50.00, null, 0.85025310, 42.51, 2243.26, null, 2.45, null, null, 56.75,
                null);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 153, 50.00, null, 0.84934616, 42.47, 2195.65, null, 2.40, null, null, 54.35,
                null);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 154, 50.00, null, 0.84844018, 42.42, 2148.00, null, 2.34, null, null, 52.01,
                null);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 155, 50.00, null, 0.84753517, 42.38, 2100.29, null, 2.29, null, null, 49.72,
                null);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 156, 50.00, null, 0.84663113, 42.33, 2052.53, null, 2.24, null, null, 47.48,
                null);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 157, 50.00, null, 0.84572805, 42.29, 2004.73, null, 2.19, null, null, 45.29,
                null);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 158, 50.00, null, 0.84482593, 42.24, 1956.87, null, 2.14, null, null, 43.15,
                null);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 159, 50.00, null, 0.84392477, 42.20, 1908.96, null, 2.09, null, null, 41.06,
                null);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 160, 50.00, null, 0.84302458, 42.15, 1860.99, null, 2.04, null, null, 39.02,
                null);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 161, 50.00, null, 0.84212535, 42.11, 1812.98, null, 1.99, null, null, 37.03,
                null);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 162, 50.00, null, 0.84122707, 42.06, 1764.92, null, 1.94, null, null, 35.09,
                null);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 163, 50.00, null, 0.84032975, 42.02, 1716.80, null, 1.88, null, null, 33.21,
                null);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 164, 50.00, null, 0.83943340, 41.97, 1668.64, null, 1.83, null, null, 31.38,
                null);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 165, 50.00, null, 0.83853799, 41.93, 1620.42, null, 1.78, null, null, 29.60,
                null);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 166, 50.00, null, 0.83764354, 41.88, 1572.15, null, 1.73, null, null, 27.87,
                null);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 167, 50.00, null, 0.83675005, 41.84, 1523.83, null, 1.68, null, null, 26.19,
                null);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 168, 50.00, null, 0.83585751, 41.79, 1475.45, null, 1.63, null, null, 24.56,
                null);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 169, 50.00, null, 0.83496592, 41.75, 1427.03, null, 1.58, null, null, 22.98,
                null);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 170, 50.00, null, 0.83407528, 41.70, 1378.55, null, 1.52, null, null, 21.46,
                null);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 171, 50.00, null, 0.83318560, 41.66, 1330.02, null, 1.47, null, null, 19.99,
                null);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 172, 50.00, null, 0.83229686, 41.61, 1281.45, null, 1.42, null, null, 18.57,
                null);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 173, 50.00, null, 0.83140907, 41.57, 1232.81, null, 1.37, null, null, 17.20,
                null);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 174, 50.00, null, 0.83052222, 41.53, 1184.13, null, 1.32, null, null, 15.88,
                null);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 175, 50.00, null, 0.82963633, 41.48, 1135.39, null, 1.26, null, null, 14.62,
                null);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 176, 50.00, null, 0.82875137, 41.44, 1086.61, null, 1.21, null, null, 13.41,
                null);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 177, 50.00, null, 0.82786736, 41.39, 1037.77, null, 1.16, null, null, 12.25,
                null);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 178, 50.00, null, 0.82698430, 41.35, 988.88, null, 1.11, null, null, 11.14,
                null);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 179, 50.00, null, 0.82610217, 41.31, 939.93, null, 1.06, null, null, 10.08,
                null);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 180, 50.00, null, 0.82522099, 41.26, 890.93, null, 1.00, null, null, 9.08,
                null);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 181, 50.00, null, 0.82434075, 41.22, 841.89, null, 0.95, null, null, 8.13,
                null);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 182, 50.00, null, 0.82346144, 41.17, 792.79, null, 0.90, null, null, 7.23,
                null);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 183, 50.00, null, 0.82258308, 41.13, 743.63, null, 0.85, null, null, 6.38,
                null);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 184, 50.00, null, 0.82170565, 41.09, 694.43, null, 0.79, null, null, 5.59,
                null);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 185, 50.00, null, 0.82082916, 41.04, 645.17, null, 0.74, null, null, 4.85,
                null);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 186, 50.00, null, 0.81995360, 41.00, 595.86, null, 0.69, null, null, 4.16,
                null);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 187, 50.00, null, 0.81907897, 40.95, 546.49, null, 0.64, null, null, 3.52,
                null);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 188, 50.00, null, 0.81820528, 40.91, 497.08, null, 0.58, null, null, 2.94,
                null);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 189, 50.00, null, 0.81733252, 40.87, 447.61, null, 0.53, null, null, 2.41,
                null);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 190, 50.00, null, 0.81646069, 40.82, 398.08, null, 0.48, null, null, 1.93,
                null);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 191, 50.00, null, 0.81558979, 40.78, 348.51, null, 0.43, null, null, 1.50,
                null);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 192, 50.00, null, 0.81471983, 40.74, 298.88, null, 0.37, null, null, 1.13,
                null);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 193, 50.00, null, 0.81385078, 40.69, 249.20, null, 0.32, null, null, 0.81,
                null);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 194, 50.00, null, 0.81298267, 40.65, 199.47, null, 0.27, null, null, 0.54,
                null);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 195, 50.00, null, 0.81211548, 40.61, 149.68, null, 0.21, null, null, 0.33,
                null);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 196, 50.00, null, 0.81124922, 40.56, 99.84, null, 0.16, null, null, 0.17,
                null);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 197, 50.00, null, 0.81038388, 40.52, 49.95, null, 0.11, null, null, 0.06,
                null);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 198, 50.00, null, 0.80951946, 40.48, 0.00, null, 0.06, null, null, 0.00,
                null);

    }

    @Test
    void testExcessPayment_term200_discountFee1000_netDisbursement9000_pay70_80() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("70"));
        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(2), new BigDecimal("80"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 0, -9000.00, null, 1.00000000, -9000.00, 9000.00, 9000.00, null, null, null,
                1000.00, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 0, 50.00, 70.00, 1.00000000, 70.00, 8959.61, 8943.44, 9.61, 13.44, 3.83, 990.39,
                986.56);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 0, 50.00, 80.00, 1.00000000, 80.00, 8919.18, 8878.70, 9.57, 15.26, 5.69, 980.82,
                971.30);

        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 1, 50.00, null, 0.99893332, 49.95, 8878.70, null, 9.52, null, null, 971.30, null);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 2, 50.00, null, 0.99786779, 49.89, 8838.18, null, 9.48, null, null, 961.82, null);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 3, 50.00, null, 0.99680339, 49.84, 8797.62, null, 9.44, null, null, 952.38, null);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 4, 50.00, null, 0.99574012, 49.79, 8757.01, null, 9.39, null, null, 942.99, null);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 5, 50.00, null, 0.99467799, 49.73, 8716.36, null, 9.35, null, null, 933.64, null);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 6, 50.00, null, 0.99361699, 49.68, 8675.67, null, 9.31, null, null, 924.33, null);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 7, 50.00, null, 0.99255712, 49.63, 8634.94, null, 9.26, null, null, 915.07, null);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 8, 50.00, null, 0.99149839, 49.57, 8594.16, null, 9.22, null, null, 905.85,
                null);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 9, 50.00, null, 0.99044078, 49.52, 8553.33, null, 9.18, null, null, 896.67,
                null);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 10, 50.00, null, 0.98938430, 49.47, 8512.47, null, 9.13, null, null, 887.54,
                null);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 11, 50.00, null, 0.98832895, 49.42, 8471.56, null, 9.09, null, null, 878.45,
                null);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 12, 50.00, null, 0.98727472, 49.36, 8430.60, null, 9.05, null, null, 869.40,
                null);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 13, 50.00, null, 0.98622162, 49.31, 8389.61, null, 9.00, null, null, 860.40,
                null);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 14, 50.00, null, 0.98516964, 49.26, 8348.56, null, 8.96, null, null, 851.44,
                null);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 15, 50.00, null, 0.98411879, 49.21, 8307.48, null, 8.91, null, null, 842.53,
                null);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 16, 50.00, null, 0.98306905, 49.15, 8266.35, null, 8.87, null, null, 833.66,
                null);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 17, 50.00, null, 0.98202044, 49.10, 8225.18, null, 8.83, null, null, 824.83,
                null);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 18, 50.00, null, 0.98097294, 49.05, 8183.96, null, 8.78, null, null, 816.05,
                null);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 19, 50.00, null, 0.97992656, 49.00, 8142.70, null, 8.74, null, null, 807.31,
                null);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 20, 50.00, null, 0.97888129, 48.94, 8101.39, null, 8.69, null, null, 798.62,
                null);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 21, 50.00, null, 0.97783715, 48.89, 8060.04, null, 8.65, null, null, 789.97,
                null);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 22, 50.00, null, 0.97679411, 48.84, 8018.65, null, 8.61, null, null, 781.36,
                null);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 23, 50.00, null, 0.97575219, 48.79, 7977.21, null, 8.56, null, null, 772.80,
                null);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 24, 50.00, null, 0.97471138, 48.74, 7935.73, null, 8.52, null, null, 764.28,
                null);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 25, 50.00, null, 0.97367168, 48.68, 7894.21, null, 8.47, null, null, 755.81,
                null);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 26, 50.00, null, 0.97263309, 48.63, 7852.63, null, 8.43, null, null, 747.38,
                null);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 27, 50.00, null, 0.97159560, 48.58, 7811.02, null, 8.39, null, null, 738.99,
                null);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 28, 50.00, null, 0.97055922, 48.53, 7769.36, null, 8.34, null, null, 730.65,
                null);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 29, 50.00, null, 0.96952395, 48.48, 7727.66, null, 8.30, null, null, 722.35,
                null);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 30, 50.00, null, 0.96848979, 48.42, 7685.91, null, 8.25, null, null, 714.10,
                null);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 31, 50.00, null, 0.96745672, 48.37, 7644.12, null, 8.21, null, null, 705.89,
                null);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 32, 50.00, null, 0.96642476, 48.32, 7602.28, null, 8.16, null, null, 697.73,
                null);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 33, 50.00, null, 0.96539390, 48.27, 7560.40, null, 8.12, null, null, 689.61,
                null);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 34, 50.00, null, 0.96436413, 48.22, 7518.47, null, 8.07, null, null, 681.54,
                null);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 35, 50.00, null, 0.96333547, 48.17, 7476.50, null, 8.03, null, null, 673.51,
                null);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 36, 50.00, null, 0.96230790, 48.12, 7434.48, null, 7.98, null, null, 665.53,
                null);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 37, 50.00, null, 0.96128143, 48.06, 7392.42, null, 7.94, null, null, 657.59,
                null);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 38, 50.00, null, 0.96025606, 48.01, 7350.31, null, 7.89, null, null, 649.70,
                null);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 39, 50.00, null, 0.95923178, 47.96, 7308.16, null, 7.85, null, null, 641.85,
                null);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 40, 50.00, null, 0.95820859, 47.91, 7265.97, null, 7.80, null, null, 634.05,
                null);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 41, 50.00, null, 0.95718649, 47.86, 7223.72, null, 7.76, null, null, 626.29,
                null);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 42, 50.00, null, 0.95616548, 47.81, 7181.44, null, 7.71, null, null, 618.58,
                null);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 43, 50.00, null, 0.95514557, 47.76, 7139.11, null, 7.67, null, null, 610.91,
                null);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 44, 50.00, null, 0.95412674, 47.71, 7096.73, null, 7.62, null, null, 603.29,
                null);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 45, 50.00, null, 0.95310899, 47.66, 7054.31, null, 7.58, null, null, 595.71,
                null);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 46, 50.00, null, 0.95209233, 47.60, 7011.84, null, 7.53, null, null, 588.18,
                null);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 47, 50.00, null, 0.95107676, 47.55, 6969.33, null, 7.49, null, null, 580.69,
                null);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 48, 50.00, null, 0.95006227, 47.50, 6926.77, null, 7.44, null, null, 573.25,
                null);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 49, 50.00, null, 0.94904886, 47.45, 6884.17, null, 7.40, null, null, 565.85,
                null);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 50, 50.00, null, 0.94803653, 47.40, 6841.52, null, 7.35, null, null, 558.50,
                null);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 51, 50.00, null, 0.94702529, 47.35, 6798.82, null, 7.31, null, null, 551.19,
                null);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 52, 50.00, null, 0.94601512, 47.30, 6756.08, null, 7.26, null, null, 543.93,
                null);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 53, 50.00, null, 0.94500603, 47.25, 6713.30, null, 7.21, null, null, 536.72,
                null);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 54, 50.00, null, 0.94399801, 47.20, 6670.47, null, 7.17, null, null, 529.55,
                null);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 55, 50.00, null, 0.94299107, 47.15, 6627.59, null, 7.12, null, null, 522.43,
                null);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 56, 50.00, null, 0.94198521, 47.10, 6584.67, null, 7.08, null, null, 515.35,
                null);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 57, 50.00, null, 0.94098042, 47.05, 6541.70, null, 7.03, null, null, 508.32,
                null);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 58, 50.00, null, 0.93997669, 47.00, 6498.68, null, 6.99, null, null, 501.33,
                null);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 59, 50.00, null, 0.93897404, 46.95, 6455.62, null, 6.94, null, null, 494.39,
                null);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 60, 50.00, null, 0.93797246, 46.90, 6412.51, null, 6.89, null, null, 487.50,
                null);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 61, 50.00, null, 0.93697195, 46.85, 6369.36, null, 6.85, null, null, 480.65,
                null);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 62, 50.00, null, 0.93597251, 46.80, 6326.16, null, 6.80, null, null, 473.85,
                null);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 63, 50.00, null, 0.93497413, 46.75, 6282.92, null, 6.76, null, null, 467.09,
                null);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 64, 50.00, null, 0.93397681, 46.70, 6239.63, null, 6.71, null, null, 460.38,
                null);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 65, 50.00, null, 0.93298056, 46.65, 6196.29, null, 6.66, null, null, 453.72,
                null);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 66, 50.00, null, 0.93198538, 46.60, 6152.91, null, 6.62, null, null, 447.10,
                null);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 67, 50.00, null, 0.93099125, 46.55, 6109.48, null, 6.57, null, null, 440.53,
                null);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 68, 50.00, null, 0.92999818, 46.50, 6066.00, null, 6.52, null, null, 434.01,
                null);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 69, 50.00, null, 0.92900618, 46.45, 6022.48, null, 6.48, null, null, 427.53,
                null);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 70, 50.00, null, 0.92801523, 46.40, 5978.91, null, 6.43, null, null, 421.10,
                null);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 71, 50.00, null, 0.92702534, 46.35, 5935.29, null, 6.38, null, null, 414.72,
                null);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 72, 50.00, null, 0.92603650, 46.30, 5891.63, null, 6.34, null, null, 408.38,
                null);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 73, 50.00, null, 0.92504872, 46.25, 5847.92, null, 6.29, null, null, 402.09,
                null);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 74, 50.00, null, 0.92406200, 46.20, 5804.17, null, 6.24, null, null, 395.85,
                null);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 75, 50.00, null, 0.92307632, 46.15, 5760.36, null, 6.20, null, null, 389.65,
                null);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 76, 50.00, null, 0.92209170, 46.10, 5716.52, null, 6.15, null, null, 383.50,
                null);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 77, 50.00, null, 0.92110813, 46.06, 5672.62, null, 6.10, null, null, 377.40,
                null);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 78, 50.00, null, 0.92012560, 46.01, 5628.68, null, 6.06, null, null, 371.34,
                null);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 79, 50.00, null, 0.91914413, 45.96, 5584.69, null, 6.01, null, null, 365.33,
                null);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 80, 50.00, null, 0.91816370, 45.91, 5540.65, null, 5.96, null, null, 359.37,
                null);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 81, 50.00, null, 0.91718432, 45.86, 5496.57, null, 5.92, null, null, 353.45,
                null);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 82, 50.00, null, 0.91620598, 45.81, 5452.44, null, 5.87, null, null, 347.58,
                null);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 83, 50.00, null, 0.91522868, 45.76, 5408.26, null, 5.82, null, null, 341.76,
                null);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 84, 50.00, null, 0.91425243, 45.71, 5364.03, null, 5.78, null, null, 335.98,
                null);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 85, 50.00, null, 0.91327722, 45.66, 5319.76, null, 5.73, null, null, 330.25,
                null);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 86, 50.00, null, 0.91230305, 45.62, 5275.44, null, 5.68, null, null, 324.57,
                null);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 87, 50.00, null, 0.91132992, 45.57, 5231.08, null, 5.63, null, null, 318.94,
                null);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 88, 50.00, null, 0.91035783, 45.52, 5186.66, null, 5.59, null, null, 313.35,
                null);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 89, 50.00, null, 0.90938677, 45.47, 5142.20, null, 5.54, null, null, 307.81,
                null);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 90, 50.00, null, 0.90841675, 45.42, 5097.69, null, 5.49, null, null, 302.32,
                null);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 91, 50.00, null, 0.90744776, 45.37, 5053.13, null, 5.44, null, null, 296.88,
                null);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 92, 50.00, null, 0.90647981, 45.32, 5008.53, null, 5.40, null, null, 291.48,
                null);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 93, 50.00, null, 0.90551289, 45.28, 4963.88, null, 5.35, null, null, 286.13,
                null);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 94, 50.00, null, 0.90454700, 45.23, 4919.18, null, 5.30, null, null, 280.83,
                null);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 95, 50.00, null, 0.90358215, 45.18, 4874.43, null, 5.25, null, null, 275.58,
                null);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 96, 50.00, null, 0.90261832, 45.13, 4829.64, null, 5.20, null, null, 270.38,
                null);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 97, 50.00, null, 0.90165552, 45.08, 4784.79, null, 5.16, null, null, 265.22,
                null);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 98, 50.00, null, 0.90069374, 45.03, 4739.90, null, 5.11, null, null, 260.11,
                null);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 99, 50.00, null, 0.89973299, 44.99, 4694.96, null, 5.06, null, null, 255.05,
                null);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 100, 50.00, null, 0.89877327, 44.94, 4649.98, null, 5.01, null, null, 250.04,
                null);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 101, 50.00, null, 0.89781457, 44.89, 4604.94, null, 4.97, null, null, 245.07,
                null);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 102, 50.00, null, 0.89685689, 44.84, 4559.86, null, 4.92, null, null, 240.15,
                null);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 103, 50.00, null, 0.89590024, 44.80, 4514.73, null, 4.87, null, null, 235.28,
                null);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 104, 50.00, null, 0.89494460, 44.75, 4469.55, null, 4.82, null, null, 230.46,
                null);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 105, 50.00, null, 0.89398999, 44.70, 4424.32, null, 4.77, null, null, 225.69,
                null);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 106, 50.00, null, 0.89303639, 44.65, 4379.05, null, 4.72, null, null, 220.97,
                null);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 107, 50.00, null, 0.89208381, 44.60, 4333.72, null, 4.68, null, null, 216.29,
                null);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 108, 50.00, null, 0.89113225, 44.56, 4288.35, null, 4.63, null, null, 211.66,
                null);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 109, 50.00, null, 0.89018170, 44.51, 4242.93, null, 4.58, null, null, 207.08,
                null);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 110, 50.00, null, 0.88923216, 44.46, 4197.46, null, 4.53, null, null, 202.55,
                null);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 111, 50.00, null, 0.88828364, 44.41, 4151.94, null, 4.48, null, null, 198.07,
                null);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 112, 50.00, null, 0.88733613, 44.37, 4106.38, null, 4.43, null, null, 193.64,
                null);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 113, 50.00, null, 0.88638963, 44.32, 4060.76, null, 4.38, null, null, 189.26,
                null);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 114, 50.00, null, 0.88544414, 44.27, 4015.10, null, 4.34, null, null, 184.92,
                null);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 115, 50.00, null, 0.88449966, 44.22, 3969.38, null, 4.29, null, null, 180.63,
                null);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 116, 50.00, null, 0.88355619, 44.18, 3923.62, null, 4.24, null, null, 176.39,
                null);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 117, 50.00, null, 0.88261372, 44.13, 3877.81, null, 4.19, null, null, 172.20,
                null);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 118, 50.00, null, 0.88167226, 44.08, 3831.95, null, 4.14, null, null, 168.06,
                null);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 119, 50.00, null, 0.88073180, 44.04, 3786.04, null, 4.09, null, null, 163.97,
                null);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 120, 50.00, null, 0.87979234, 43.99, 3740.09, null, 4.04, null, null, 159.93,
                null);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 121, 50.00, null, 0.87885389, 43.94, 3694.08, null, 3.99, null, null, 155.94,
                null);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 122, 50.00, null, 0.87791644, 43.90, 3648.03, null, 3.94, null, null, 152.00,
                null);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 123, 50.00, null, 0.87697999, 43.85, 3601.92, null, 3.90, null, null, 148.10,
                null);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 124, 50.00, null, 0.87604453, 43.80, 3555.77, null, 3.85, null, null, 144.25,
                null);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 125, 50.00, null, 0.87511008, 43.76, 3509.56, null, 3.80, null, null, 140.45,
                null);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 126, 50.00, null, 0.87417662, 43.71, 3463.31, null, 3.75, null, null, 136.70,
                null);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 127, 50.00, null, 0.87324416, 43.66, 3417.01, null, 3.70, null, null, 133.00,
                null);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 128, 50.00, null, 0.87231269, 43.62, 3370.66, null, 3.65, null, null, 129.35,
                null);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 129, 50.00, null, 0.87138221, 43.57, 3324.26, null, 3.60, null, null, 125.75,
                null);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 130, 50.00, null, 0.87045273, 43.52, 3277.81, null, 3.55, null, null, 122.20,
                null);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 131, 50.00, null, 0.86952424, 43.48, 3231.31, null, 3.50, null, null, 118.70,
                null);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 132, 50.00, null, 0.86859674, 43.43, 3184.76, null, 3.45, null, null, 115.25,
                null);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 133, 50.00, null, 0.86767023, 43.38, 3138.16, null, 3.40, null, null, 111.85,
                null);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 134, 50.00, null, 0.86674471, 43.34, 3091.51, null, 3.35, null, null, 108.50,
                null);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 135, 50.00, null, 0.86582017, 43.29, 3044.81, null, 3.30, null, null, 105.20,
                null);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 136, 50.00, null, 0.86489662, 43.24, 2998.06, null, 3.25, null, null, 101.95,
                null);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 137, 50.00, null, 0.86397406, 43.20, 2951.26, null, 3.20, null, null, 98.75,
                null);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 138, 50.00, null, 0.86305248, 43.15, 2904.42, null, 3.15, null, null, 95.60,
                null);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 139, 50.00, null, 0.86213188, 43.11, 2857.52, null, 3.10, null, null, 92.50,
                null);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 140, 50.00, null, 0.86121227, 43.06, 2810.57, null, 3.05, null, null, 89.45,
                null);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 141, 50.00, null, 0.86029363, 43.01, 2763.57, null, 3.00, null, null, 86.45,
                null);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 142, 50.00, null, 0.85937598, 42.97, 2716.52, null, 2.95, null, null, 83.50,
                null);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 143, 50.00, null, 0.85845930, 42.92, 2669.42, null, 2.90, null, null, 80.60,
                null);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 144, 50.00, null, 0.85754361, 42.88, 2622.27, null, 2.85, null, null, 77.75,
                null);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 145, 50.00, null, 0.85662889, 42.83, 2575.07, null, 2.80, null, null, 74.95,
                null);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 146, 50.00, null, 0.85571514, 42.79, 2527.82, null, 2.75, null, null, 72.20,
                null);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 147, 50.00, null, 0.85480237, 42.74, 2480.52, null, 2.70, null, null, 69.50,
                null);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 148, 50.00, null, 0.85389057, 42.69, 2433.17, null, 2.65, null, null, 66.85,
                null);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 149, 50.00, null, 0.85297975, 42.65, 2385.77, null, 2.60, null, null, 64.25,
                null);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 150, 50.00, null, 0.85206990, 42.60, 2338.31, null, 2.55, null, null, 61.70,
                null);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 151, 50.00, null, 0.85116101, 42.56, 2290.81, null, 2.50, null, null, 59.20,
                null);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 152, 50.00, null, 0.85025310, 42.51, 2243.26, null, 2.45, null, null, 56.75,
                null);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 153, 50.00, null, 0.84934616, 42.47, 2195.65, null, 2.40, null, null, 54.35,
                null);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 154, 50.00, null, 0.84844018, 42.42, 2148.00, null, 2.34, null, null, 52.01,
                null);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 155, 50.00, null, 0.84753517, 42.38, 2100.29, null, 2.29, null, null, 49.72,
                null);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 156, 50.00, null, 0.84663113, 42.33, 2052.53, null, 2.24, null, null, 47.48,
                null);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 157, 50.00, null, 0.84572805, 42.29, 2004.73, null, 2.19, null, null, 45.29,
                null);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 158, 50.00, null, 0.84482593, 42.24, 1956.87, null, 2.14, null, null, 43.15,
                null);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 159, 50.00, null, 0.84392477, 42.20, 1908.96, null, 2.09, null, null, 41.06,
                null);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 160, 50.00, null, 0.84302458, 42.15, 1860.99, null, 2.04, null, null, 39.02,
                null);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 161, 50.00, null, 0.84212535, 42.11, 1812.98, null, 1.99, null, null, 37.03,
                null);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 162, 50.00, null, 0.84122707, 42.06, 1764.92, null, 1.94, null, null, 35.09,
                null);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 163, 50.00, null, 0.84032975, 42.02, 1716.80, null, 1.88, null, null, 33.21,
                null);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 164, 50.00, null, 0.83943340, 41.97, 1668.64, null, 1.83, null, null, 31.38,
                null);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 165, 50.00, null, 0.83853799, 41.93, 1620.42, null, 1.78, null, null, 29.60,
                null);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 166, 50.00, null, 0.83764354, 41.88, 1572.15, null, 1.73, null, null, 27.87,
                null);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 167, 50.00, null, 0.83675005, 41.84, 1523.83, null, 1.68, null, null, 26.19,
                null);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 168, 50.00, null, 0.83585751, 41.79, 1475.45, null, 1.63, null, null, 24.56,
                null);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 169, 50.00, null, 0.83496592, 41.75, 1427.03, null, 1.58, null, null, 22.98,
                null);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 170, 50.00, null, 0.83407528, 41.70, 1378.55, null, 1.52, null, null, 21.46,
                null);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 171, 50.00, null, 0.83318560, 41.66, 1330.02, null, 1.47, null, null, 19.99,
                null);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 172, 50.00, null, 0.83229686, 41.61, 1281.45, null, 1.42, null, null, 18.57,
                null);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 173, 50.00, null, 0.83140907, 41.57, 1232.81, null, 1.37, null, null, 17.20,
                null);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 174, 50.00, null, 0.83052222, 41.53, 1184.13, null, 1.32, null, null, 15.88,
                null);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 175, 50.00, null, 0.82963633, 41.48, 1135.39, null, 1.26, null, null, 14.62,
                null);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 176, 50.00, null, 0.82875137, 41.44, 1086.61, null, 1.21, null, null, 13.41,
                null);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 177, 50.00, null, 0.82786736, 41.39, 1037.77, null, 1.16, null, null, 12.25,
                null);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 178, 50.00, null, 0.82698430, 41.35, 988.88, null, 1.11, null, null, 11.14,
                null);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 179, 50.00, null, 0.82610217, 41.31, 939.93, null, 1.06, null, null, 10.08,
                null);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 180, 50.00, null, 0.82522099, 41.26, 890.93, null, 1.00, null, null, 9.08,
                null);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 181, 50.00, null, 0.82434075, 41.22, 841.89, null, 0.95, null, null, 8.13,
                null);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 182, 50.00, null, 0.82346144, 41.17, 792.79, null, 0.90, null, null, 7.23,
                null);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 183, 50.00, null, 0.82258308, 41.13, 743.63, null, 0.85, null, null, 6.38,
                null);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 184, 50.00, null, 0.82170565, 41.09, 694.43, null, 0.79, null, null, 5.59,
                null);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 185, 50.00, null, 0.82082916, 41.04, 645.17, null, 0.74, null, null, 4.85,
                null);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 186, 50.00, null, 0.81995360, 41.00, 595.86, null, 0.69, null, null, 4.16,
                null);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 187, 50.00, null, 0.81907897, 40.95, 546.49, null, 0.64, null, null, 3.52,
                null);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 188, 50.00, null, 0.81820528, 40.91, 497.08, null, 0.58, null, null, 2.94,
                null);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 189, 50.00, null, 0.81733252, 40.87, 447.61, null, 0.53, null, null, 2.41,
                null);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 190, 50.00, null, 0.81646069, 40.82, 398.08, null, 0.48, null, null, 1.93,
                null);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 191, 50.00, null, 0.81558979, 40.78, 348.51, null, 0.43, null, null, 1.50,
                null);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 192, 50.00, null, 0.81471983, 40.74, 298.88, null, 0.37, null, null, 1.13,
                null);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 193, 50.00, null, 0.81385078, 40.69, 249.20, null, 0.32, null, null, 0.81,
                null);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 194, 50.00, null, 0.81298267, 40.65, 199.47, null, 0.27, null, null, 0.54,
                null);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 195, 50.00, null, 0.81211548, 40.61, 149.68, null, 0.21, null, null, 0.33,
                null);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 196, 50.00, null, 0.81124922, 40.56, 99.84, null, 0.16, null, null, 0.17,
                null);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 197, 50.00, null, 0.81038388, 40.52, 49.95, null, 0.11, null, null, 0.06,
                null);

        assertEquals(200, model.projectedPayments().size(), "disbursement + 199 regular (period 200 removed, forecast was 0)");
    }

    @Test
    void testLessPayment_term200_discountFee1000_netDisbursement9000_pay40() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("40"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 0, -9000.00, null, 1.00000000, -9000.00, 9000.00, 9000.00, null, null, null,
                1000.00, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 0, 50.00, 40.00, 1.00000000, 40.00, 8959.61, 8967.69, 9.61, 7.69, -1.92, 990.39,
                992.31);

        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 1, 50.00, null, 0.99893332, 49.95, 8919.18, null, 9.57, null, null, 980.82, null);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 2, 50.00, null, 0.99786779, 49.89, 8878.70, null, 9.52, null, null, 971.30, null);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 3, 50.00, null, 0.99680339, 49.84, 8838.18, null, 9.48, null, null, 961.82, null);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 4, 50.00, null, 0.99574012, 49.79, 8797.62, null, 9.44, null, null, 952.38, null);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 5, 50.00, null, 0.99467799, 49.73, 8757.01, null, 9.39, null, null, 942.99, null);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 6, 50.00, null, 0.99361699, 49.68, 8716.36, null, 9.35, null, null, 933.64, null);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 7, 50.00, null, 0.99255712, 49.63, 8675.67, null, 9.31, null, null, 924.33, null);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 8, 50.00, null, 0.99149839, 49.57, 8634.94, null, 9.26, null, null, 915.07, null);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 9, 50.00, null, 0.99044078, 49.52, 8594.16, null, 9.22, null, null, 905.85,
                null);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 10, 50.00, null, 0.98938430, 49.47, 8553.33, null, 9.18, null, null, 896.67,
                null);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 11, 50.00, null, 0.98832895, 49.42, 8512.47, null, 9.13, null, null, 887.54,
                null);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 12, 50.00, null, 0.98727472, 49.36, 8471.56, null, 9.09, null, null, 878.45,
                null);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 13, 50.00, null, 0.98622162, 49.31, 8430.60, null, 9.05, null, null, 869.40,
                null);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 14, 50.00, null, 0.98516964, 49.26, 8389.61, null, 9.00, null, null, 860.40,
                null);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 15, 50.00, null, 0.98411879, 49.21, 8348.56, null, 8.96, null, null, 851.44,
                null);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 16, 50.00, null, 0.98306905, 49.15, 8307.48, null, 8.91, null, null, 842.53,
                null);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 17, 50.00, null, 0.98202044, 49.10, 8266.35, null, 8.87, null, null, 833.66,
                null);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 18, 50.00, null, 0.98097294, 49.05, 8225.18, null, 8.83, null, null, 824.83,
                null);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 19, 50.00, null, 0.97992656, 49.00, 8183.96, null, 8.78, null, null, 816.05,
                null);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 20, 50.00, null, 0.97888129, 48.94, 8142.70, null, 8.74, null, null, 807.31,
                null);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 21, 50.00, null, 0.97783715, 48.89, 8101.39, null, 8.69, null, null, 798.62,
                null);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 22, 50.00, null, 0.97679411, 48.84, 8060.04, null, 8.65, null, null, 789.97,
                null);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 23, 50.00, null, 0.97575219, 48.79, 8018.65, null, 8.61, null, null, 781.36,
                null);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 24, 50.00, null, 0.97471138, 48.74, 7977.21, null, 8.56, null, null, 772.80,
                null);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 25, 50.00, null, 0.97367168, 48.68, 7935.73, null, 8.52, null, null, 764.28,
                null);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 26, 50.00, null, 0.97263309, 48.63, 7894.21, null, 8.47, null, null, 755.81,
                null);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 27, 50.00, null, 0.97159560, 48.58, 7852.63, null, 8.43, null, null, 747.38,
                null);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 28, 50.00, null, 0.97055922, 48.53, 7811.02, null, 8.39, null, null, 738.99,
                null);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 29, 50.00, null, 0.96952395, 48.48, 7769.36, null, 8.34, null, null, 730.65,
                null);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 30, 50.00, null, 0.96848979, 48.42, 7727.66, null, 8.30, null, null, 722.35,
                null);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 31, 50.00, null, 0.96745672, 48.37, 7685.91, null, 8.25, null, null, 714.10,
                null);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 32, 50.00, null, 0.96642476, 48.32, 7644.12, null, 8.21, null, null, 705.89,
                null);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 33, 50.00, null, 0.96539390, 48.27, 7602.28, null, 8.16, null, null, 697.73,
                null);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 34, 50.00, null, 0.96436413, 48.22, 7560.40, null, 8.12, null, null, 689.61,
                null);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 35, 50.00, null, 0.96333547, 48.17, 7518.47, null, 8.07, null, null, 681.54,
                null);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 36, 50.00, null, 0.96230790, 48.12, 7476.50, null, 8.03, null, null, 673.51,
                null);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 37, 50.00, null, 0.96128143, 48.06, 7434.48, null, 7.98, null, null, 665.53,
                null);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 38, 50.00, null, 0.96025606, 48.01, 7392.42, null, 7.94, null, null, 657.59,
                null);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 39, 50.00, null, 0.95923178, 47.96, 7350.31, null, 7.89, null, null, 649.70,
                null);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 40, 50.00, null, 0.95820859, 47.91, 7308.16, null, 7.85, null, null, 641.85,
                null);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 41, 50.00, null, 0.95718649, 47.86, 7265.97, null, 7.80, null, null, 634.05,
                null);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 42, 50.00, null, 0.95616548, 47.81, 7223.72, null, 7.76, null, null, 626.29,
                null);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 43, 50.00, null, 0.95514557, 47.76, 7181.44, null, 7.71, null, null, 618.58,
                null);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 44, 50.00, null, 0.95412674, 47.71, 7139.11, null, 7.67, null, null, 610.91,
                null);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 45, 50.00, null, 0.95310899, 47.66, 7096.73, null, 7.62, null, null, 603.29,
                null);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 46, 50.00, null, 0.95209233, 47.60, 7054.31, null, 7.58, null, null, 595.71,
                null);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 47, 50.00, null, 0.95107676, 47.55, 7011.84, null, 7.53, null, null, 588.18,
                null);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 48, 50.00, null, 0.95006227, 47.50, 6969.33, null, 7.49, null, null, 580.69,
                null);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 49, 50.00, null, 0.94904886, 47.45, 6926.77, null, 7.44, null, null, 573.25,
                null);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 50, 50.00, null, 0.94803653, 47.40, 6884.17, null, 7.40, null, null, 565.85,
                null);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 51, 50.00, null, 0.94702529, 47.35, 6841.52, null, 7.35, null, null, 558.50,
                null);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 52, 50.00, null, 0.94601512, 47.30, 6798.82, null, 7.31, null, null, 551.19,
                null);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 53, 50.00, null, 0.94500603, 47.25, 6756.08, null, 7.26, null, null, 543.93,
                null);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 54, 50.00, null, 0.94399801, 47.20, 6713.30, null, 7.21, null, null, 536.72,
                null);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 55, 50.00, null, 0.94299107, 47.15, 6670.47, null, 7.17, null, null, 529.55,
                null);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 56, 50.00, null, 0.94198521, 47.10, 6627.59, null, 7.12, null, null, 522.43,
                null);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 57, 50.00, null, 0.94098042, 47.05, 6584.67, null, 7.08, null, null, 515.35,
                null);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 58, 50.00, null, 0.93997669, 47.00, 6541.70, null, 7.03, null, null, 508.32,
                null);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 59, 50.00, null, 0.93897404, 46.95, 6498.68, null, 6.99, null, null, 501.33,
                null);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 60, 50.00, null, 0.93797246, 46.90, 6455.62, null, 6.94, null, null, 494.39,
                null);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 61, 50.00, null, 0.93697195, 46.85, 6412.51, null, 6.89, null, null, 487.50,
                null);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 62, 50.00, null, 0.93597251, 46.80, 6369.36, null, 6.85, null, null, 480.65,
                null);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 63, 50.00, null, 0.93497413, 46.75, 6326.16, null, 6.80, null, null, 473.85,
                null);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 64, 50.00, null, 0.93397681, 46.70, 6282.92, null, 6.76, null, null, 467.09,
                null);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 65, 50.00, null, 0.93298056, 46.65, 6239.63, null, 6.71, null, null, 460.38,
                null);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 66, 50.00, null, 0.93198538, 46.60, 6196.29, null, 6.66, null, null, 453.72,
                null);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 67, 50.00, null, 0.93099125, 46.55, 6152.91, null, 6.62, null, null, 447.10,
                null);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 68, 50.00, null, 0.92999818, 46.50, 6109.48, null, 6.57, null, null, 440.53,
                null);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 69, 50.00, null, 0.92900618, 46.45, 6066.00, null, 6.52, null, null, 434.01,
                null);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 70, 50.00, null, 0.92801523, 46.40, 6022.48, null, 6.48, null, null, 427.53,
                null);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 71, 50.00, null, 0.92702534, 46.35, 5978.91, null, 6.43, null, null, 421.10,
                null);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 72, 50.00, null, 0.92603650, 46.30, 5935.29, null, 6.38, null, null, 414.72,
                null);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 73, 50.00, null, 0.92504872, 46.25, 5891.63, null, 6.34, null, null, 408.38,
                null);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 74, 50.00, null, 0.92406200, 46.20, 5847.92, null, 6.29, null, null, 402.09,
                null);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 75, 50.00, null, 0.92307632, 46.15, 5804.17, null, 6.24, null, null, 395.85,
                null);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 76, 50.00, null, 0.92209170, 46.10, 5760.36, null, 6.20, null, null, 389.65,
                null);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 77, 50.00, null, 0.92110813, 46.06, 5716.52, null, 6.15, null, null, 383.50,
                null);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 78, 50.00, null, 0.92012560, 46.01, 5672.62, null, 6.10, null, null, 377.40,
                null);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 79, 50.00, null, 0.91914413, 45.96, 5628.68, null, 6.06, null, null, 371.34,
                null);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 80, 50.00, null, 0.91816370, 45.91, 5584.69, null, 6.01, null, null, 365.33,
                null);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 81, 50.00, null, 0.91718432, 45.86, 5540.65, null, 5.96, null, null, 359.37,
                null);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 82, 50.00, null, 0.91620598, 45.81, 5496.57, null, 5.92, null, null, 353.45,
                null);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 83, 50.00, null, 0.91522868, 45.76, 5452.44, null, 5.87, null, null, 347.58,
                null);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 84, 50.00, null, 0.91425243, 45.71, 5408.26, null, 5.82, null, null, 341.76,
                null);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 85, 50.00, null, 0.91327722, 45.66, 5364.03, null, 5.78, null, null, 335.98,
                null);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 86, 50.00, null, 0.91230305, 45.62, 5319.76, null, 5.73, null, null, 330.25,
                null);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 87, 50.00, null, 0.91132992, 45.57, 5275.44, null, 5.68, null, null, 324.57,
                null);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 88, 50.00, null, 0.91035783, 45.52, 5231.08, null, 5.63, null, null, 318.94,
                null);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 89, 50.00, null, 0.90938677, 45.47, 5186.66, null, 5.59, null, null, 313.35,
                null);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 90, 50.00, null, 0.90841675, 45.42, 5142.20, null, 5.54, null, null, 307.81,
                null);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 91, 50.00, null, 0.90744776, 45.37, 5097.69, null, 5.49, null, null, 302.32,
                null);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 92, 50.00, null, 0.90647981, 45.32, 5053.13, null, 5.44, null, null, 296.88,
                null);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 93, 50.00, null, 0.90551289, 45.28, 5008.53, null, 5.40, null, null, 291.48,
                null);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 94, 50.00, null, 0.90454700, 45.23, 4963.88, null, 5.35, null, null, 286.13,
                null);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 95, 50.00, null, 0.90358215, 45.18, 4919.18, null, 5.30, null, null, 280.83,
                null);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 96, 50.00, null, 0.90261832, 45.13, 4874.43, null, 5.25, null, null, 275.58,
                null);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 97, 50.00, null, 0.90165552, 45.08, 4829.64, null, 5.20, null, null, 270.38,
                null);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 98, 50.00, null, 0.90069374, 45.03, 4784.79, null, 5.16, null, null, 265.22,
                null);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 99, 50.00, null, 0.89973299, 44.99, 4739.90, null, 5.11, null, null, 260.11,
                null);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 100, 50.00, null, 0.89877327, 44.94, 4694.96, null, 5.06, null, null, 255.05,
                null);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 101, 50.00, null, 0.89781457, 44.89, 4649.98, null, 5.01, null, null, 250.04,
                null);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 102, 50.00, null, 0.89685689, 44.84, 4604.94, null, 4.97, null, null, 245.07,
                null);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 103, 50.00, null, 0.89590024, 44.80, 4559.86, null, 4.92, null, null, 240.15,
                null);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 104, 50.00, null, 0.89494460, 44.75, 4514.73, null, 4.87, null, null, 235.28,
                null);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 105, 50.00, null, 0.89398999, 44.70, 4469.55, null, 4.82, null, null, 230.46,
                null);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 106, 50.00, null, 0.89303639, 44.65, 4424.32, null, 4.77, null, null, 225.69,
                null);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 107, 50.00, null, 0.89208381, 44.60, 4379.05, null, 4.72, null, null, 220.97,
                null);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 108, 50.00, null, 0.89113225, 44.56, 4333.72, null, 4.68, null, null, 216.29,
                null);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 109, 50.00, null, 0.89018170, 44.51, 4288.35, null, 4.63, null, null, 211.66,
                null);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 110, 50.00, null, 0.88923216, 44.46, 4242.93, null, 4.58, null, null, 207.08,
                null);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 111, 50.00, null, 0.88828364, 44.41, 4197.46, null, 4.53, null, null, 202.55,
                null);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 112, 50.00, null, 0.88733613, 44.37, 4151.94, null, 4.48, null, null, 198.07,
                null);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 113, 50.00, null, 0.88638963, 44.32, 4106.38, null, 4.43, null, null, 193.64,
                null);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 114, 50.00, null, 0.88544414, 44.27, 4060.76, null, 4.38, null, null, 189.26,
                null);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 115, 50.00, null, 0.88449966, 44.22, 4015.10, null, 4.34, null, null, 184.92,
                null);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 116, 50.00, null, 0.88355619, 44.18, 3969.38, null, 4.29, null, null, 180.63,
                null);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 117, 50.00, null, 0.88261372, 44.13, 3923.62, null, 4.24, null, null, 176.39,
                null);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 118, 50.00, null, 0.88167226, 44.08, 3877.81, null, 4.19, null, null, 172.20,
                null);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 119, 50.00, null, 0.88073180, 44.04, 3831.95, null, 4.14, null, null, 168.06,
                null);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 120, 50.00, null, 0.87979234, 43.99, 3786.04, null, 4.09, null, null, 163.97,
                null);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 121, 50.00, null, 0.87885389, 43.94, 3740.09, null, 4.04, null, null, 159.93,
                null);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 122, 50.00, null, 0.87791644, 43.90, 3694.08, null, 3.99, null, null, 155.94,
                null);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 123, 50.00, null, 0.87697999, 43.85, 3648.03, null, 3.94, null, null, 152.00,
                null);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 124, 50.00, null, 0.87604453, 43.80, 3601.92, null, 3.90, null, null, 148.10,
                null);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 125, 50.00, null, 0.87511008, 43.76, 3555.77, null, 3.85, null, null, 144.25,
                null);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 126, 50.00, null, 0.87417662, 43.71, 3509.56, null, 3.80, null, null, 140.45,
                null);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 127, 50.00, null, 0.87324416, 43.66, 3463.31, null, 3.75, null, null, 136.70,
                null);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 128, 50.00, null, 0.87231269, 43.62, 3417.01, null, 3.70, null, null, 133.00,
                null);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 129, 50.00, null, 0.87138221, 43.57, 3370.66, null, 3.65, null, null, 129.35,
                null);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 130, 50.00, null, 0.87045273, 43.52, 3324.26, null, 3.60, null, null, 125.75,
                null);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 131, 50.00, null, 0.86952424, 43.48, 3277.81, null, 3.55, null, null, 122.20,
                null);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 132, 50.00, null, 0.86859674, 43.43, 3231.31, null, 3.50, null, null, 118.70,
                null);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 133, 50.00, null, 0.86767023, 43.38, 3184.76, null, 3.45, null, null, 115.25,
                null);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 134, 50.00, null, 0.86674471, 43.34, 3138.16, null, 3.40, null, null, 111.85,
                null);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 135, 50.00, null, 0.86582017, 43.29, 3091.51, null, 3.35, null, null, 108.50,
                null);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 136, 50.00, null, 0.86489662, 43.24, 3044.81, null, 3.30, null, null, 105.20,
                null);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 137, 50.00, null, 0.86397406, 43.20, 2998.06, null, 3.25, null, null, 101.95,
                null);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 138, 50.00, null, 0.86305248, 43.15, 2951.26, null, 3.20, null, null, 98.75,
                null);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 139, 50.00, null, 0.86213188, 43.11, 2904.42, null, 3.15, null, null, 95.60,
                null);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 140, 50.00, null, 0.86121227, 43.06, 2857.52, null, 3.10, null, null, 92.50,
                null);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 141, 50.00, null, 0.86029363, 43.01, 2810.57, null, 3.05, null, null, 89.45,
                null);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 142, 50.00, null, 0.85937598, 42.97, 2763.57, null, 3.00, null, null, 86.45,
                null);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 143, 50.00, null, 0.85845930, 42.92, 2716.52, null, 2.95, null, null, 83.50,
                null);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 144, 50.00, null, 0.85754361, 42.88, 2669.42, null, 2.90, null, null, 80.60,
                null);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 145, 50.00, null, 0.85662889, 42.83, 2622.27, null, 2.85, null, null, 77.75,
                null);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 146, 50.00, null, 0.85571514, 42.79, 2575.07, null, 2.80, null, null, 74.95,
                null);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 147, 50.00, null, 0.85480237, 42.74, 2527.82, null, 2.75, null, null, 72.20,
                null);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 148, 50.00, null, 0.85389057, 42.69, 2480.52, null, 2.70, null, null, 69.50,
                null);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 149, 50.00, null, 0.85297975, 42.65, 2433.17, null, 2.65, null, null, 66.85,
                null);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 150, 50.00, null, 0.85206990, 42.60, 2385.77, null, 2.60, null, null, 64.25,
                null);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 151, 50.00, null, 0.85116101, 42.56, 2338.31, null, 2.55, null, null, 61.70,
                null);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 152, 50.00, null, 0.85025310, 42.51, 2290.81, null, 2.50, null, null, 59.20,
                null);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 153, 50.00, null, 0.84934616, 42.47, 2243.26, null, 2.45, null, null, 56.75,
                null);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 154, 50.00, null, 0.84844018, 42.42, 2195.65, null, 2.40, null, null, 54.35,
                null);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 155, 50.00, null, 0.84753517, 42.38, 2148.00, null, 2.34, null, null, 52.01,
                null);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 156, 50.00, null, 0.84663113, 42.33, 2100.29, null, 2.29, null, null, 49.72,
                null);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 157, 50.00, null, 0.84572805, 42.29, 2052.53, null, 2.24, null, null, 47.48,
                null);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 158, 50.00, null, 0.84482593, 42.24, 2004.73, null, 2.19, null, null, 45.29,
                null);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 159, 50.00, null, 0.84392477, 42.20, 1956.87, null, 2.14, null, null, 43.15,
                null);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 160, 50.00, null, 0.84302458, 42.15, 1908.96, null, 2.09, null, null, 41.06,
                null);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 161, 50.00, null, 0.84212535, 42.11, 1860.99, null, 2.04, null, null, 39.02,
                null);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 162, 50.00, null, 0.84122707, 42.06, 1812.98, null, 1.99, null, null, 37.03,
                null);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 163, 50.00, null, 0.84032975, 42.02, 1764.92, null, 1.94, null, null, 35.09,
                null);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 164, 50.00, null, 0.83943340, 41.97, 1716.80, null, 1.88, null, null, 33.21,
                null);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 165, 50.00, null, 0.83853799, 41.93, 1668.64, null, 1.83, null, null, 31.38,
                null);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 166, 50.00, null, 0.83764354, 41.88, 1620.42, null, 1.78, null, null, 29.60,
                null);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 167, 50.00, null, 0.83675005, 41.84, 1572.15, null, 1.73, null, null, 27.87,
                null);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 168, 50.00, null, 0.83585751, 41.79, 1523.83, null, 1.68, null, null, 26.19,
                null);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 169, 50.00, null, 0.83496592, 41.75, 1475.45, null, 1.63, null, null, 24.56,
                null);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 170, 50.00, null, 0.83407528, 41.70, 1427.03, null, 1.58, null, null, 22.98,
                null);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 171, 50.00, null, 0.83318560, 41.66, 1378.55, null, 1.52, null, null, 21.46,
                null);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 172, 50.00, null, 0.83229686, 41.61, 1330.02, null, 1.47, null, null, 19.99,
                null);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 173, 50.00, null, 0.83140907, 41.57, 1281.45, null, 1.42, null, null, 18.57,
                null);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 174, 50.00, null, 0.83052222, 41.53, 1232.81, null, 1.37, null, null, 17.20,
                null);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 175, 50.00, null, 0.82963633, 41.48, 1184.13, null, 1.32, null, null, 15.88,
                null);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 176, 50.00, null, 0.82875137, 41.44, 1135.39, null, 1.26, null, null, 14.62,
                null);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 177, 50.00, null, 0.82786736, 41.39, 1086.61, null, 1.21, null, null, 13.41,
                null);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 178, 50.00, null, 0.82698430, 41.35, 1037.77, null, 1.16, null, null, 12.25,
                null);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 179, 50.00, null, 0.82610217, 41.31, 988.88, null, 1.11, null, null, 11.14,
                null);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 180, 50.00, null, 0.82522099, 41.26, 939.93, null, 1.06, null, null, 10.08,
                null);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 181, 50.00, null, 0.82434075, 41.22, 890.93, null, 1.00, null, null, 9.08,
                null);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 182, 50.00, null, 0.82346144, 41.17, 841.89, null, 0.95, null, null, 8.13,
                null);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 183, 50.00, null, 0.82258308, 41.13, 792.79, null, 0.90, null, null, 7.23,
                null);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 184, 50.00, null, 0.82170565, 41.09, 743.63, null, 0.85, null, null, 6.38,
                null);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 185, 50.00, null, 0.82082916, 41.04, 694.43, null, 0.79, null, null, 5.59,
                null);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 186, 50.00, null, 0.81995360, 41.00, 645.17, null, 0.74, null, null, 4.85,
                null);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 187, 50.00, null, 0.81907897, 40.95, 595.86, null, 0.69, null, null, 4.16,
                null);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 188, 50.00, null, 0.81820528, 40.91, 546.49, null, 0.64, null, null, 3.52,
                null);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 189, 50.00, null, 0.81733252, 40.87, 497.08, null, 0.58, null, null, 2.94,
                null);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 190, 50.00, null, 0.81646069, 40.82, 447.61, null, 0.53, null, null, 2.41,
                null);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 191, 50.00, null, 0.81558979, 40.78, 398.08, null, 0.48, null, null, 1.93,
                null);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 192, 50.00, null, 0.81471983, 40.74, 348.51, null, 0.43, null, null, 1.50,
                null);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 193, 50.00, null, 0.81385078, 40.69, 298.88, null, 0.37, null, null, 1.13,
                null);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 194, 50.00, null, 0.81298267, 40.65, 249.20, null, 0.32, null, null, 0.81,
                null);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 195, 50.00, null, 0.81211548, 40.61, 199.47, null, 0.27, null, null, 0.54,
                null);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 196, 50.00, null, 0.81124922, 40.56, 149.68, null, 0.21, null, null, 0.33,
                null);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 197, 50.00, null, 0.81038388, 40.52, 99.84, null, 0.16, null, null, 0.17,
                null);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 198, 50.00, null, 0.80951946, 40.48, 49.95, null, 0.11, null, null, 0.06,
                null);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 199, 50.00, null, 0.80865597, 40.43, 0.00, null, 0.06, null, null, 0.00,
                null);

        assertEquals(202, model.projectedPayments().size(), "disbursement + 200 regular + 1 additional");
        checkInst(model, 201, 201, LocalDate.of(2019, 7, 21), 200, 10.00, null, 0.80779339, 8.08, 0.00, null, 0.00, null, null, 0.00, null);
    }

    @Test
    void testNoPayment_term200_discountFee1000_netDisbursement9000_pay0_0_50() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(1), BigDecimal.ZERO);
        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(2), BigDecimal.ZERO);
        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(3), new BigDecimal("50"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 0, -9000.00, null, 1.00000000, -9000.00, 9000.00, 9000.00, null, null, null,
                1000.00, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 0, 50.00, 0.00, 1.00000000, 0.00, 8959.61, 9000.00, 9.61, 0.00, null, 990.39,
                1000.00);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 0, 50.00, 0.00, 1.00000000, 0.00, 8919.18, 9000.00, 9.57, 0.00, null, 980.82,
                1000.00);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 0, 50.00, 50.00, 1.00000000, 50.00, 8878.70, 8959.61, 9.52, 9.61, 0.09, 971.30,
                990.39);

        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 1, 50.00, null, 0.99893332, 49.95, 8838.18, null, 9.48, null, null, 961.82, null);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 2, 50.00, null, 0.99786779, 49.89, 8797.62, null, 9.44, null, null, 952.38, null);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 3, 50.00, null, 0.99680339, 49.84, 8757.01, null, 9.39, null, null, 942.99, null);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 4, 50.00, null, 0.99574012, 49.79, 8716.36, null, 9.35, null, null, 933.64, null);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 5, 50.00, null, 0.99467799, 49.73, 8675.67, null, 9.31, null, null, 924.33, null);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 6, 50.00, null, 0.99361699, 49.68, 8634.94, null, 9.26, null, null, 915.07, null);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 7, 50.00, null, 0.99255712, 49.63, 8594.16, null, 9.22, null, null, 905.85,
                null);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 8, 50.00, null, 0.99149839, 49.57, 8553.33, null, 9.18, null, null, 896.67,
                null);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 9, 50.00, null, 0.99044078, 49.52, 8512.47, null, 9.13, null, null, 887.54,
                null);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 10, 50.00, null, 0.98938430, 49.47, 8471.56, null, 9.09, null, null, 878.45,
                null);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 11, 50.00, null, 0.98832895, 49.42, 8430.60, null, 9.05, null, null, 869.40,
                null);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 12, 50.00, null, 0.98727472, 49.36, 8389.61, null, 9.00, null, null, 860.40,
                null);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 13, 50.00, null, 0.98622162, 49.31, 8348.56, null, 8.96, null, null, 851.44,
                null);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 14, 50.00, null, 0.98516964, 49.26, 8307.48, null, 8.91, null, null, 842.53,
                null);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 15, 50.00, null, 0.98411879, 49.21, 8266.35, null, 8.87, null, null, 833.66,
                null);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 16, 50.00, null, 0.98306905, 49.15, 8225.18, null, 8.83, null, null, 824.83,
                null);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 17, 50.00, null, 0.98202044, 49.10, 8183.96, null, 8.78, null, null, 816.05,
                null);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 18, 50.00, null, 0.98097294, 49.05, 8142.70, null, 8.74, null, null, 807.31,
                null);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 19, 50.00, null, 0.97992656, 49.00, 8101.39, null, 8.69, null, null, 798.62,
                null);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 20, 50.00, null, 0.97888129, 48.94, 8060.04, null, 8.65, null, null, 789.97,
                null);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 21, 50.00, null, 0.97783715, 48.89, 8018.65, null, 8.61, null, null, 781.36,
                null);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 22, 50.00, null, 0.97679411, 48.84, 7977.21, null, 8.56, null, null, 772.80,
                null);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 23, 50.00, null, 0.97575219, 48.79, 7935.73, null, 8.52, null, null, 764.28,
                null);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 24, 50.00, null, 0.97471138, 48.74, 7894.21, null, 8.47, null, null, 755.81,
                null);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 25, 50.00, null, 0.97367168, 48.68, 7852.63, null, 8.43, null, null, 747.38,
                null);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 26, 50.00, null, 0.97263309, 48.63, 7811.02, null, 8.39, null, null, 738.99,
                null);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 27, 50.00, null, 0.97159560, 48.58, 7769.36, null, 8.34, null, null, 730.65,
                null);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 28, 50.00, null, 0.97055922, 48.53, 7727.66, null, 8.30, null, null, 722.35,
                null);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 29, 50.00, null, 0.96952395, 48.48, 7685.91, null, 8.25, null, null, 714.10,
                null);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 30, 50.00, null, 0.96848979, 48.42, 7644.12, null, 8.21, null, null, 705.89,
                null);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 31, 50.00, null, 0.96745672, 48.37, 7602.28, null, 8.16, null, null, 697.73,
                null);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 32, 50.00, null, 0.96642476, 48.32, 7560.40, null, 8.12, null, null, 689.61,
                null);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 33, 50.00, null, 0.96539390, 48.27, 7518.47, null, 8.07, null, null, 681.54,
                null);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 34, 50.00, null, 0.96436413, 48.22, 7476.50, null, 8.03, null, null, 673.51,
                null);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 35, 50.00, null, 0.96333547, 48.17, 7434.48, null, 7.98, null, null, 665.53,
                null);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 36, 50.00, null, 0.96230790, 48.12, 7392.42, null, 7.94, null, null, 657.59,
                null);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 37, 50.00, null, 0.96128143, 48.06, 7350.31, null, 7.89, null, null, 649.70,
                null);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 38, 50.00, null, 0.96025606, 48.01, 7308.16, null, 7.85, null, null, 641.85,
                null);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 39, 50.00, null, 0.95923178, 47.96, 7265.97, null, 7.80, null, null, 634.05,
                null);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 40, 50.00, null, 0.95820859, 47.91, 7223.72, null, 7.76, null, null, 626.29,
                null);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 41, 50.00, null, 0.95718649, 47.86, 7181.44, null, 7.71, null, null, 618.58,
                null);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 42, 50.00, null, 0.95616548, 47.81, 7139.11, null, 7.67, null, null, 610.91,
                null);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 43, 50.00, null, 0.95514557, 47.76, 7096.73, null, 7.62, null, null, 603.29,
                null);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 44, 50.00, null, 0.95412674, 47.71, 7054.31, null, 7.58, null, null, 595.71,
                null);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 45, 50.00, null, 0.95310899, 47.66, 7011.84, null, 7.53, null, null, 588.18,
                null);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 46, 50.00, null, 0.95209233, 47.60, 6969.33, null, 7.49, null, null, 580.69,
                null);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 47, 50.00, null, 0.95107676, 47.55, 6926.77, null, 7.44, null, null, 573.25,
                null);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 48, 50.00, null, 0.95006227, 47.50, 6884.17, null, 7.40, null, null, 565.85,
                null);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 49, 50.00, null, 0.94904886, 47.45, 6841.52, null, 7.35, null, null, 558.50,
                null);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 50, 50.00, null, 0.94803653, 47.40, 6798.82, null, 7.31, null, null, 551.19,
                null);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 51, 50.00, null, 0.94702529, 47.35, 6756.08, null, 7.26, null, null, 543.93,
                null);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 52, 50.00, null, 0.94601512, 47.30, 6713.30, null, 7.21, null, null, 536.72,
                null);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 53, 50.00, null, 0.94500603, 47.25, 6670.47, null, 7.17, null, null, 529.55,
                null);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 54, 50.00, null, 0.94399801, 47.20, 6627.59, null, 7.12, null, null, 522.43,
                null);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 55, 50.00, null, 0.94299107, 47.15, 6584.67, null, 7.08, null, null, 515.35,
                null);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 56, 50.00, null, 0.94198521, 47.10, 6541.70, null, 7.03, null, null, 508.32,
                null);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 57, 50.00, null, 0.94098042, 47.05, 6498.68, null, 6.99, null, null, 501.33,
                null);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 58, 50.00, null, 0.93997669, 47.00, 6455.62, null, 6.94, null, null, 494.39,
                null);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 59, 50.00, null, 0.93897404, 46.95, 6412.51, null, 6.89, null, null, 487.50,
                null);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 60, 50.00, null, 0.93797246, 46.90, 6369.36, null, 6.85, null, null, 480.65,
                null);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 61, 50.00, null, 0.93697195, 46.85, 6326.16, null, 6.80, null, null, 473.85,
                null);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 62, 50.00, null, 0.93597251, 46.80, 6282.92, null, 6.76, null, null, 467.09,
                null);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 63, 50.00, null, 0.93497413, 46.75, 6239.63, null, 6.71, null, null, 460.38,
                null);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 64, 50.00, null, 0.93397681, 46.70, 6196.29, null, 6.66, null, null, 453.72,
                null);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 65, 50.00, null, 0.93298056, 46.65, 6152.91, null, 6.62, null, null, 447.10,
                null);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 66, 50.00, null, 0.93198538, 46.60, 6109.48, null, 6.57, null, null, 440.53,
                null);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 67, 50.00, null, 0.93099125, 46.55, 6066.00, null, 6.52, null, null, 434.01,
                null);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 68, 50.00, null, 0.92999818, 46.50, 6022.48, null, 6.48, null, null, 427.53,
                null);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 69, 50.00, null, 0.92900618, 46.45, 5978.91, null, 6.43, null, null, 421.10,
                null);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 70, 50.00, null, 0.92801523, 46.40, 5935.29, null, 6.38, null, null, 414.72,
                null);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 71, 50.00, null, 0.92702534, 46.35, 5891.63, null, 6.34, null, null, 408.38,
                null);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 72, 50.00, null, 0.92603650, 46.30, 5847.92, null, 6.29, null, null, 402.09,
                null);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 73, 50.00, null, 0.92504872, 46.25, 5804.17, null, 6.24, null, null, 395.85,
                null);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 74, 50.00, null, 0.92406200, 46.20, 5760.36, null, 6.20, null, null, 389.65,
                null);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 75, 50.00, null, 0.92307632, 46.15, 5716.52, null, 6.15, null, null, 383.50,
                null);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 76, 50.00, null, 0.92209170, 46.10, 5672.62, null, 6.10, null, null, 377.40,
                null);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 77, 50.00, null, 0.92110813, 46.06, 5628.68, null, 6.06, null, null, 371.34,
                null);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 78, 50.00, null, 0.92012560, 46.01, 5584.69, null, 6.01, null, null, 365.33,
                null);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 79, 50.00, null, 0.91914413, 45.96, 5540.65, null, 5.96, null, null, 359.37,
                null);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 80, 50.00, null, 0.91816370, 45.91, 5496.57, null, 5.92, null, null, 353.45,
                null);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 81, 50.00, null, 0.91718432, 45.86, 5452.44, null, 5.87, null, null, 347.58,
                null);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 82, 50.00, null, 0.91620598, 45.81, 5408.26, null, 5.82, null, null, 341.76,
                null);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 83, 50.00, null, 0.91522868, 45.76, 5364.03, null, 5.78, null, null, 335.98,
                null);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 84, 50.00, null, 0.91425243, 45.71, 5319.76, null, 5.73, null, null, 330.25,
                null);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 85, 50.00, null, 0.91327722, 45.66, 5275.44, null, 5.68, null, null, 324.57,
                null);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 86, 50.00, null, 0.91230305, 45.62, 5231.08, null, 5.63, null, null, 318.94,
                null);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 87, 50.00, null, 0.91132992, 45.57, 5186.66, null, 5.59, null, null, 313.35,
                null);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 88, 50.00, null, 0.91035783, 45.52, 5142.20, null, 5.54, null, null, 307.81,
                null);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 89, 50.00, null, 0.90938677, 45.47, 5097.69, null, 5.49, null, null, 302.32,
                null);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 90, 50.00, null, 0.90841675, 45.42, 5053.13, null, 5.44, null, null, 296.88,
                null);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 91, 50.00, null, 0.90744776, 45.37, 5008.53, null, 5.40, null, null, 291.48,
                null);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 92, 50.00, null, 0.90647981, 45.32, 4963.88, null, 5.35, null, null, 286.13,
                null);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 93, 50.00, null, 0.90551289, 45.28, 4919.18, null, 5.30, null, null, 280.83,
                null);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 94, 50.00, null, 0.90454700, 45.23, 4874.43, null, 5.25, null, null, 275.58,
                null);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 95, 50.00, null, 0.90358215, 45.18, 4829.64, null, 5.20, null, null, 270.38,
                null);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 96, 50.00, null, 0.90261832, 45.13, 4784.79, null, 5.16, null, null, 265.22,
                null);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 97, 50.00, null, 0.90165552, 45.08, 4739.90, null, 5.11, null, null, 260.11,
                null);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 98, 50.00, null, 0.90069374, 45.03, 4694.96, null, 5.06, null, null, 255.05,
                null);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 99, 50.00, null, 0.89973299, 44.99, 4649.98, null, 5.01, null, null, 250.04,
                null);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 100, 50.00, null, 0.89877327, 44.94, 4604.94, null, 4.97, null, null, 245.07,
                null);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 101, 50.00, null, 0.89781457, 44.89, 4559.86, null, 4.92, null, null, 240.15,
                null);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 102, 50.00, null, 0.89685689, 44.84, 4514.73, null, 4.87, null, null, 235.28,
                null);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 103, 50.00, null, 0.89590024, 44.80, 4469.55, null, 4.82, null, null, 230.46,
                null);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 104, 50.00, null, 0.89494460, 44.75, 4424.32, null, 4.77, null, null, 225.69,
                null);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 105, 50.00, null, 0.89398999, 44.70, 4379.05, null, 4.72, null, null, 220.97,
                null);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 106, 50.00, null, 0.89303639, 44.65, 4333.72, null, 4.68, null, null, 216.29,
                null);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 107, 50.00, null, 0.89208381, 44.60, 4288.35, null, 4.63, null, null, 211.66,
                null);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 108, 50.00, null, 0.89113225, 44.56, 4242.93, null, 4.58, null, null, 207.08,
                null);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 109, 50.00, null, 0.89018170, 44.51, 4197.46, null, 4.53, null, null, 202.55,
                null);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 110, 50.00, null, 0.88923216, 44.46, 4151.94, null, 4.48, null, null, 198.07,
                null);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 111, 50.00, null, 0.88828364, 44.41, 4106.38, null, 4.43, null, null, 193.64,
                null);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 112, 50.00, null, 0.88733613, 44.37, 4060.76, null, 4.38, null, null, 189.26,
                null);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 113, 50.00, null, 0.88638963, 44.32, 4015.10, null, 4.34, null, null, 184.92,
                null);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 114, 50.00, null, 0.88544414, 44.27, 3969.38, null, 4.29, null, null, 180.63,
                null);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 115, 50.00, null, 0.88449966, 44.22, 3923.62, null, 4.24, null, null, 176.39,
                null);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 116, 50.00, null, 0.88355619, 44.18, 3877.81, null, 4.19, null, null, 172.20,
                null);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 117, 50.00, null, 0.88261372, 44.13, 3831.95, null, 4.14, null, null, 168.06,
                null);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 118, 50.00, null, 0.88167226, 44.08, 3786.04, null, 4.09, null, null, 163.97,
                null);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 119, 50.00, null, 0.88073180, 44.04, 3740.09, null, 4.04, null, null, 159.93,
                null);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 120, 50.00, null, 0.87979234, 43.99, 3694.08, null, 3.99, null, null, 155.94,
                null);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 121, 50.00, null, 0.87885389, 43.94, 3648.03, null, 3.94, null, null, 152.00,
                null);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 122, 50.00, null, 0.87791644, 43.90, 3601.92, null, 3.90, null, null, 148.10,
                null);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 123, 50.00, null, 0.87697999, 43.85, 3555.77, null, 3.85, null, null, 144.25,
                null);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 124, 50.00, null, 0.87604453, 43.80, 3509.56, null, 3.80, null, null, 140.45,
                null);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 125, 50.00, null, 0.87511008, 43.76, 3463.31, null, 3.75, null, null, 136.70,
                null);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 126, 50.00, null, 0.87417662, 43.71, 3417.01, null, 3.70, null, null, 133.00,
                null);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 127, 50.00, null, 0.87324416, 43.66, 3370.66, null, 3.65, null, null, 129.35,
                null);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 128, 50.00, null, 0.87231269, 43.62, 3324.26, null, 3.60, null, null, 125.75,
                null);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 129, 50.00, null, 0.87138221, 43.57, 3277.81, null, 3.55, null, null, 122.20,
                null);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 130, 50.00, null, 0.87045273, 43.52, 3231.31, null, 3.50, null, null, 118.70,
                null);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 131, 50.00, null, 0.86952424, 43.48, 3184.76, null, 3.45, null, null, 115.25,
                null);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 132, 50.00, null, 0.86859674, 43.43, 3138.16, null, 3.40, null, null, 111.85,
                null);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 133, 50.00, null, 0.86767023, 43.38, 3091.51, null, 3.35, null, null, 108.50,
                null);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 134, 50.00, null, 0.86674471, 43.34, 3044.81, null, 3.30, null, null, 105.20,
                null);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 135, 50.00, null, 0.86582017, 43.29, 2998.06, null, 3.25, null, null, 101.95,
                null);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 136, 50.00, null, 0.86489662, 43.24, 2951.26, null, 3.20, null, null, 98.75,
                null);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 137, 50.00, null, 0.86397406, 43.20, 2904.42, null, 3.15, null, null, 95.60,
                null);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 138, 50.00, null, 0.86305248, 43.15, 2857.52, null, 3.10, null, null, 92.50,
                null);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 139, 50.00, null, 0.86213188, 43.11, 2810.57, null, 3.05, null, null, 89.45,
                null);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 140, 50.00, null, 0.86121227, 43.06, 2763.57, null, 3.00, null, null, 86.45,
                null);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 141, 50.00, null, 0.86029363, 43.01, 2716.52, null, 2.95, null, null, 83.50,
                null);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 142, 50.00, null, 0.85937598, 42.97, 2669.42, null, 2.90, null, null, 80.60,
                null);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 143, 50.00, null, 0.85845930, 42.92, 2622.27, null, 2.85, null, null, 77.75,
                null);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 144, 50.00, null, 0.85754361, 42.88, 2575.07, null, 2.80, null, null, 74.95,
                null);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 145, 50.00, null, 0.85662889, 42.83, 2527.82, null, 2.75, null, null, 72.20,
                null);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 146, 50.00, null, 0.85571514, 42.79, 2480.52, null, 2.70, null, null, 69.50,
                null);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 147, 50.00, null, 0.85480237, 42.74, 2433.17, null, 2.65, null, null, 66.85,
                null);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 148, 50.00, null, 0.85389057, 42.69, 2385.77, null, 2.60, null, null, 64.25,
                null);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 149, 50.00, null, 0.85297975, 42.65, 2338.31, null, 2.55, null, null, 61.70,
                null);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 150, 50.00, null, 0.85206990, 42.60, 2290.81, null, 2.50, null, null, 59.20,
                null);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 151, 50.00, null, 0.85116101, 42.56, 2243.26, null, 2.45, null, null, 56.75,
                null);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 152, 50.00, null, 0.85025310, 42.51, 2195.65, null, 2.40, null, null, 54.35,
                null);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 153, 50.00, null, 0.84934616, 42.47, 2148.00, null, 2.34, null, null, 52.01,
                null);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 154, 50.00, null, 0.84844018, 42.42, 2100.29, null, 2.29, null, null, 49.72,
                null);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 155, 50.00, null, 0.84753517, 42.38, 2052.53, null, 2.24, null, null, 47.48,
                null);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 156, 50.00, null, 0.84663113, 42.33, 2004.73, null, 2.19, null, null, 45.29,
                null);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 157, 50.00, null, 0.84572805, 42.29, 1956.87, null, 2.14, null, null, 43.15,
                null);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 158, 50.00, null, 0.84482593, 42.24, 1908.96, null, 2.09, null, null, 41.06,
                null);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 159, 50.00, null, 0.84392477, 42.20, 1860.99, null, 2.04, null, null, 39.02,
                null);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 160, 50.00, null, 0.84302458, 42.15, 1812.98, null, 1.99, null, null, 37.03,
                null);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 161, 50.00, null, 0.84212535, 42.11, 1764.92, null, 1.94, null, null, 35.09,
                null);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 162, 50.00, null, 0.84122707, 42.06, 1716.80, null, 1.88, null, null, 33.21,
                null);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 163, 50.00, null, 0.84032975, 42.02, 1668.64, null, 1.83, null, null, 31.38,
                null);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 164, 50.00, null, 0.83943340, 41.97, 1620.42, null, 1.78, null, null, 29.60,
                null);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 165, 50.00, null, 0.83853799, 41.93, 1572.15, null, 1.73, null, null, 27.87,
                null);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 166, 50.00, null, 0.83764354, 41.88, 1523.83, null, 1.68, null, null, 26.19,
                null);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 167, 50.00, null, 0.83675005, 41.84, 1475.45, null, 1.63, null, null, 24.56,
                null);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 168, 50.00, null, 0.83585751, 41.79, 1427.03, null, 1.58, null, null, 22.98,
                null);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 169, 50.00, null, 0.83496592, 41.75, 1378.55, null, 1.52, null, null, 21.46,
                null);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 170, 50.00, null, 0.83407528, 41.70, 1330.02, null, 1.47, null, null, 19.99,
                null);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 171, 50.00, null, 0.83318560, 41.66, 1281.45, null, 1.42, null, null, 18.57,
                null);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 172, 50.00, null, 0.83229686, 41.61, 1232.81, null, 1.37, null, null, 17.20,
                null);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 173, 50.00, null, 0.83140907, 41.57, 1184.13, null, 1.32, null, null, 15.88,
                null);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 174, 50.00, null, 0.83052222, 41.53, 1135.39, null, 1.26, null, null, 14.62,
                null);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 175, 50.00, null, 0.82963633, 41.48, 1086.61, null, 1.21, null, null, 13.41,
                null);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 176, 50.00, null, 0.82875137, 41.44, 1037.77, null, 1.16, null, null, 12.25,
                null);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 177, 50.00, null, 0.82786736, 41.39, 988.88, null, 1.11, null, null, 11.14,
                null);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 178, 50.00, null, 0.82698430, 41.35, 939.93, null, 1.06, null, null, 10.08,
                null);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 179, 50.00, null, 0.82610217, 41.31, 890.93, null, 1.00, null, null, 9.08,
                null);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 180, 50.00, null, 0.82522099, 41.26, 841.89, null, 0.95, null, null, 8.13,
                null);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 181, 50.00, null, 0.82434075, 41.22, 792.79, null, 0.90, null, null, 7.23,
                null);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 182, 50.00, null, 0.82346144, 41.17, 743.63, null, 0.85, null, null, 6.38,
                null);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 183, 50.00, null, 0.82258308, 41.13, 694.43, null, 0.79, null, null, 5.59,
                null);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 184, 50.00, null, 0.82170565, 41.09, 645.17, null, 0.74, null, null, 4.85,
                null);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 185, 50.00, null, 0.82082916, 41.04, 595.86, null, 0.69, null, null, 4.16,
                null);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 186, 50.00, null, 0.81995360, 41.00, 546.49, null, 0.64, null, null, 3.52,
                null);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 187, 50.00, null, 0.81907897, 40.95, 497.08, null, 0.58, null, null, 2.94,
                null);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 188, 50.00, null, 0.81820528, 40.91, 447.61, null, 0.53, null, null, 2.41,
                null);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 189, 50.00, null, 0.81733252, 40.87, 398.08, null, 0.48, null, null, 1.93,
                null);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 190, 50.00, null, 0.81646069, 40.82, 348.51, null, 0.43, null, null, 1.50,
                null);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 191, 50.00, null, 0.81558979, 40.78, 298.88, null, 0.37, null, null, 1.13,
                null);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 192, 50.00, null, 0.81471983, 40.74, 249.20, null, 0.32, null, null, 0.81,
                null);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 193, 50.00, null, 0.81385078, 40.69, 199.47, null, 0.27, null, null, 0.54,
                null);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 194, 50.00, null, 0.81298267, 40.65, 149.68, null, 0.21, null, null, 0.33,
                null);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 195, 50.00, null, 0.81211548, 40.61, 99.84, null, 0.16, null, null, 0.17,
                null);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 196, 50.00, null, 0.81124922, 40.56, 49.95, null, 0.11, null, null, 0.06,
                null);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 197, 50.00, null, 0.81038388, 40.52, 0.00, null, 0.06, null, null, 0.00,
                null);

        assertEquals(203, model.projectedPayments().size(), "disbursement + 200 regular + 2 additional");
        checkInst(model, 201, 201, LocalDate.of(2019, 7, 21), 198, 50.00, null, 0.80951946, 40.48, 0.00, null, 0.00, null, null, 0.00,
                null);
        checkInst(model, 202, 202, LocalDate.of(2019, 7, 22), 199, 50.00, null, 0.80865597, 40.43, 0.00, null, 0.00, null, null, 0.00,
                null);
    }

    @Test
    void testLessPayment_term10_discountFee50_netDisbursement450_pay40() {
        final BigDecimal smallDiscountFee = new BigDecimal("50");
        final BigDecimal smallNetDisbursement = new BigDecimal("450");
        final ProjectedAmortizationScheduleModel initial = ProjectedAmortizationScheduleModel.generate(smallDiscountFee,
                smallNetDisbursement, TPV, RATE, DAY_COUNT, EXPECTED_DISBURSEMENT_DATE, MC, CURRENCY, EXPECTED_DISBURSEMENT_DATE);
        final ProjectedAmortizationScheduleModel model = initial.regenerate(smallDiscountFee, smallNetDisbursement,
                EXPECTED_DISBURSEMENT_DATE, EXPECTED_DISBURSEMENT_DATE);

        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("40"));

        assertEquals(12, model.projectedPayments().size(), "disbursement + 10 regular + 1 tail");

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 0, -450.00, null, 1.00000000, -450.00, 450.00, 450.00, null, null, null, 50.00,
                50.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 0, 50.00, 40.00, 1.00000000, 40.00, 408.83, 417.06, 8.83, 7.06, -1.77, 41.17,
                42.94);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 1, 50.00, null, 0.98074794, 49.04, 366.86, null, 8.03, null, null, 33.14, null);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 2, 50.00, null, 0.96186652, 48.09, 324.06, null, 7.20, null, null, 25.94, null);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 3, 50.00, null, 0.94334860, 47.17, 280.42, null, 6.36, null, null, 19.58, null);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 4, 50.00, null, 0.92518720, 46.26, 235.93, null, 5.50, null, null, 14.08, null);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 5, 50.00, null, 0.90737544, 45.37, 190.56, null, 4.63, null, null, 9.45, null);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 6, 50.00, null, 0.88990659, 44.50, 144.30, null, 3.74, null, null, 5.71, null);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 7, 50.00, null, 0.87277405, 43.64, 97.13, null, 2.83, null, null, 2.88, null);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 8, 50.00, null, 0.85597135, 42.80, 49.04, null, 1.91, null, null, 0.97, null);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 9, 50.00, null, 0.83949214, 41.97, 0.00, null, 0.97, null, null, 0.00, null);

        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 10, 10.00, null, 0.82333018, 8.23, 0.00, null, 0.00, null, null, 0.00, null);
    }

    @Test
    void testExcessPayment_term10_discountFee50_netDisbursement450_pay110() {
        final BigDecimal smallDiscountFee = new BigDecimal("50");
        final BigDecimal smallNetDisbursement = new BigDecimal("450");
        final ProjectedAmortizationScheduleModel initial = ProjectedAmortizationScheduleModel.generate(smallDiscountFee,
                smallNetDisbursement, TPV, RATE, DAY_COUNT, EXPECTED_DISBURSEMENT_DATE, MC, CURRENCY, EXPECTED_DISBURSEMENT_DATE);
        final ProjectedAmortizationScheduleModel model = initial.regenerate(smallDiscountFee, smallNetDisbursement,
                EXPECTED_DISBURSEMENT_DATE, EXPECTED_DISBURSEMENT_DATE);

        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("110"));

        assertEquals(10, model.projectedPayments().size(), "disbursement + 9 regular (period 10 removed, forecast was 0)");

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 0, -450.00, null, 1.00000000, -450.00, 450.00, 450.00, null, null, null, 50.00,
                50.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 0, 50.00, 110.00, 1.00000000, 110.00, 408.83, 358.30, 8.83, 18.30, 9.47, 41.17,
                31.70);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 1, 50.00, null, 0.98074794, 49.04, 366.86, null, 8.03, null, null, 33.14, null);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 2, 50.00, null, 0.96186652, 48.09, 324.06, null, 7.20, null, null, 25.94, null);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 3, 50.00, null, 0.94334860, 47.17, 280.42, null, 6.36, null, null, 19.58, null);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 4, 50.00, null, 0.92518720, 46.26, 235.93, null, 5.50, null, null, 14.08, null);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 5, 50.00, null, 0.90737544, 45.37, 190.56, null, 4.63, null, null, 9.45, null);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 6, 50.00, null, 0.88990659, 44.50, 144.30, null, 3.74, null, null, 5.71, null);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 7, 50.00, null, 0.87277405, 43.64, 97.13, null, 2.83, null, null, 2.88, null);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 8, 50.00, null, 0.85597135, 34.24, 49.04, null, 1.91, null, null, 0.97, null);
    }

    // ==============================================================================================
    // applyRateChange tests — rate change mid-lifecycle with date gaps
    // ==============================================================================================

    @Test
    void testApplyRateChange_sameDayAsDisburse() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        model.applyRateChange(new BigDecimal("15"), EXPECTED_DISBURSEMENT_DATE);

        assertFalse(model.rateSegments().isEmpty());
        assertTrue(model.effectiveTotalTerm() > 0, "effective total term should be positive");
    }

    @Test
    void testApplyRateChange_8daysAfterDisburse() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        final LocalDate rateChangeDate = EXPECTED_DISBURSEMENT_DATE.plusDays(8);
        model.applyRateChange(new BigDecimal("15"), rateChangeDate);

        assertFalse(model.rateSegments().isEmpty());
        assertTrue(model.effectiveTotalTerm() > 0, "effective total term should be positive");
    }

    @Test
    void testApplyRateChange_twiceWithDateGap() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        model.applyRateChange(new BigDecimal("15"), EXPECTED_DISBURSEMENT_DATE);
        assertNotNull(model.rateSegments());
        assertFalse(model.rateSegments().isEmpty());

        final LocalDate secondChangeDate = EXPECTED_DISBURSEMENT_DATE.plusDays(8);
        model.applyRateChange(new BigDecimal("11"), secondChangeDate);

        assertTrue(model.effectiveTotalTerm() > 0, "effective total term should be positive");
    }

    @Test
    void testApplyRateChange_twiceWithDateGapAndPayment() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        model.applyPayment(EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("500"));

        final LocalDate rateChangeDate = EXPECTED_DISBURSEMENT_DATE.plusDays(8);
        model.applyRateChange(new BigDecimal("15"), rateChangeDate);

        assertTrue(model.effectiveTotalTerm() > 0, "effective total term should be positive");
    }

    @Test
    void testApplyRateChange_nearEndOfTerm() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        final LocalDate rateChangeDate = EXPECTED_DISBURSEMENT_DATE.plusDays(195);
        model.applyRateChange(new BigDecimal("15"), rateChangeDate);

        assertTrue(model.effectiveTotalTerm() > 0, "effective total term should be positive");
    }

    @Test
    void testApplyRateChange_pastEndOfTerm() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        final int originalTerm = model.originalPaymentNumber();
        final LocalDate rateChangeDate = EXPECTED_DISBURSEMENT_DATE.plusDays(250);

        model.applyRateChange(new BigDecimal("15"), rateChangeDate);

        // Past-term rate change should succeed — segment starts clamped at originalPaymentNumber
        assertFalse(model.rateSegments().isEmpty(), "should have a rate segment");
        assertEquals(originalTerm, model.rateSegments().getFirst().startDayIndex(),
                "segment should start at base originalPaymentNumber when past-term");
        assertTrue(model.effectiveTotalTerm() > originalTerm, "effective term should extend beyond base term");
    }

    @Test
    void testApplyRateChange_beforeDisburseDate() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        assertThrows(IllegalArgumentException.class, () -> {
            model.applyRateChange(new BigDecimal("15"), EXPECTED_DISBURSEMENT_DATE.minusDays(1));
        });
    }

    private ProjectedAmortizationScheduleModel generateModel() {
        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(DISCOUNT_FEE, NET_DISBURSEMENT, TPV,
                RATE, DAY_COUNT, EXPECTED_DISBURSEMENT_DATE, MC, CURRENCY, EXPECTED_DISBURSEMENT_DATE);
        return model.regenerate(DISCOUNT_FEE, NET_DISBURSEMENT, EXPECTED_DISBURSEMENT_DATE, EXPECTED_DISBURSEMENT_DATE);
    }

    @SuppressWarnings("ParameterNumber")
    private void checkInst(final ProjectedAmortizationScheduleModel model, final int index, final int expectedNo,
            final LocalDate expectedDate, final long expectedPaymentsLeft, final Double expectedPayment, final Double expectedActualPayment,
            final Double expectedDiscountFactor, final Double expectedNpvValue, final Double expectedExpectedBalance,
            final Double expectedActualBalance, final Double expectedAmortization, final Double expectedActualAmortization,
            final Double expectedIncomeModification, final Double expectedExpectedDiscountFeeBalance,
            final Double expectedActualDiscountFeeBalance) {
        final ProjectedPayment inst = model.projectedPayments().get(index);
        final String p = "inst " + expectedNo + ": ";

        assertEquals(expectedNo, inst.paymentNo(), p + "paymentNo");
        assertEquals(expectedDate, inst.date(), p + "date");
        assertEquals(expectedPaymentsLeft, inst.paymentsLeft(), p + "paymentsLeft");
        assertMoneyValue(expectedPayment, inst.expectedPaymentAmount(), 2, p + "expectedPayment");
        assertMoneyValue(expectedActualPayment, inst.actualPaymentAmount(), 2, p + "actualPayment");
        assertValue(expectedDiscountFactor, inst.discountFactor(), 8, p + "discountFactor");
        assertMoneyValue(expectedNpvValue, inst.npvValue(), 2, p + "npvValue");
        assertMoneyValue(expectedExpectedBalance, inst.expectedBalance(), 2, p + "expectedBalance");
        assertMoneyValue(expectedActualBalance, inst.actualBalance(), 2, p + "actualBalance");
        assertMoneyValue(expectedAmortization, inst.expectedAmortizationAmount(), 2, p + "expectedAmortization");
        assertMoneyValue(expectedActualAmortization, inst.actualAmortizationAmount(), 2, p + "actualAmort");
        assertMoneyValue(expectedIncomeModification, inst.incomeModification(), 2, p + "incomeModification");
        assertMoneyValue(expectedExpectedDiscountFeeBalance, inst.expectedDiscountFeeBalance(), 2, p + "expectedDiscountFeeBalance");
        assertMoneyValue(expectedActualDiscountFeeBalance, inst.actualDiscountFeeBalance(), 2, p + "actualDiscountFeeBalance");
    }

    private void assertMoneyValue(final Double expected, final Money actual, final int scale, final String msg) {
        if (expected == null) {
            assertNull(actual, msg);
            return;
        }
        final BigDecimal exp = BigDecimal.valueOf(expected).setScale(scale, RoundingMode.HALF_UP);
        final BigDecimal act = actual.getAmount().setScale(scale, RoundingMode.HALF_UP);
        assertEquals(0, exp.compareTo(act), msg + " — expected: " + exp + ", actual: " + act);
    }

    private void assertValue(final Double expected, final BigDecimal actual, final int scale, final String msg) {
        if (expected == null) {
            assertNull(actual, msg);
            return;
        }
        final BigDecimal exp = BigDecimal.valueOf(expected).setScale(scale, RoundingMode.HALF_UP);
        final BigDecimal act = actual.setScale(scale, RoundingMode.HALF_UP);
        assertEquals(0, exp.compareTo(act), msg + " — expected: " + exp + ", actual: " + act);
    }

}
