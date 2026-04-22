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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.junit.jupiter.api.Test;

class ProjectedAmortizationScheduleCalculatorTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, null);

    private static final BigDecimal ORIGINATION_FEE = new BigDecimal("1000");
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final BigDecimal RATE = new BigDecimal("0.18");
    private static final int DAY_COUNT = 360;
    private static final LocalDate EXPECTED_DISBURSEMENT_DATE = LocalDate.of(2019, 1, 1);
    private static final int TERM = 200;

    private final ProjectedAmortizationScheduleCalculator calculator = new DefaultProjectedAmortizationScheduleCalculator();

    @Test
    void testAddDisbursement_term10_originationFee50_netDisbursement450_then430() {
        final BigDecimal originationFee = new BigDecimal("50");
        final BigDecimal initialNetDisbursement = new BigDecimal("450");
        final LocalDate initialDisbursementDate = LocalDate.of(2019, 1, 1);

        final ProjectedAmortizationScheduleModel initial = calculator.generateModel(originationFee, initialNetDisbursement, TPV, RATE,
                DAY_COUNT, initialDisbursementDate, MC, CURRENCY);
        final ProjectedAmortizationScheduleModel model1 = calculator.addDisbursement(initial, originationFee, initialNetDisbursement,
                initialDisbursementDate);

        assertEquals(10, model1.loanTerm());
        assertEquals(11, model1.payments().size());

        checkInst(model1, 0, 0, LocalDate.of(2019, 1, 1), 10, 0, -450.00, null, null, 1.00000000, -450.00, 450.00, null, null, null, null,
                50.00);
        checkInst(model1, 1, 1, LocalDate.of(2019, 1, 2), 9, 1, 50.00, 50.00, null, 0.98074794, 49.04, 408.83, 8.83, 0.00, null, -8.83,
                50.00);
        checkInst(model1, 2, 2, LocalDate.of(2019, 1, 3), 8, 2, 50.00, 50.00, null, 0.96186652, 48.09, 366.86, 8.03, 0.00, null, -8.03,
                50.00);
        checkInst(model1, 3, 3, LocalDate.of(2019, 1, 4), 7, 3, 50.00, 50.00, null, 0.94334860, 47.17, 324.06, 7.20, 0.00, null, -7.20,
                50.00);
        checkInst(model1, 4, 4, LocalDate.of(2019, 1, 5), 6, 4, 50.00, 50.00, null, 0.92518720, 46.26, 280.42, 6.36, 0.00, null, -6.36,
                50.00);
        checkInst(model1, 5, 5, LocalDate.of(2019, 1, 6), 5, 5, 50.00, 50.00, null, 0.90737544, 45.37, 235.93, 5.50, 0.00, null, -5.50,
                50.00);
        checkInst(model1, 6, 6, LocalDate.of(2019, 1, 7), 4, 6, 50.00, 50.00, null, 0.88990659, 44.50, 190.56, 4.63, 0.00, null, -4.63,
                50.00);
        checkInst(model1, 7, 7, LocalDate.of(2019, 1, 8), 3, 7, 50.00, 50.00, null, 0.87277405, 43.64, 144.30, 3.74, 0.00, null, -3.74,
                50.00);
        checkInst(model1, 8, 8, LocalDate.of(2019, 1, 9), 2, 8, 50.00, 50.00, null, 0.85597135, 42.80, 97.13, 2.83, 0.00, null, -2.83,
                50.00);
        checkInst(model1, 9, 9, LocalDate.of(2019, 1, 10), 1, 9, 50.00, 50.00, null, 0.83949214, 41.97, 49.04, 1.91, 0.00, null, -1.91,
                50.00);
        checkInst(model1, 10, 10, LocalDate.of(2019, 1, 11), 0, 10, 50.00, 50.00, null, 0.82333018, 41.17, 0.00, 0.96, 0.00, null, -0.96,
                50.00);

        final BigDecimal newNetDisbursement = new BigDecimal("430");
        final LocalDate newDisbursementDate = LocalDate.of(2019, 1, 5);

        final ProjectedAmortizationScheduleModel model2 = calculator.addDisbursement(model1, originationFee, newNetDisbursement,
                newDisbursementDate);

        assertEquals(10, model2.loanTerm());
        assertEquals(11, model2.payments().size());

        checkInst(model2, 0, 0, LocalDate.of(2019, 1, 5), 10, 0, -430.00, null, null, 1.00000000, -430.00, 430.00, null, null, null, null,
                50.00);
        checkInst(model2, 1, 1, LocalDate.of(2019, 1, 6), 9, 1, 50.00, 50.00, null, 0.97237826, 48.62, 392.21, 12.21, 0.00, null, -12.21,
                50.00);
        checkInst(model2, 2, 2, LocalDate.of(2019, 1, 7), 8, 2, 50.00, 50.00, null, 0.94551948, 47.28, 353.36, 11.14, 0.00, null, -11.14,
                50.00);
        checkInst(model2, 3, 3, LocalDate.of(2019, 1, 8), 7, 3, 50.00, 50.00, null, 0.91940259, 45.97, 313.39, 10.04, 0.00, null, -10.04,
                50.00);
        checkInst(model2, 4, 4, LocalDate.of(2019, 1, 9), 6, 4, 50.00, 50.00, null, 0.89400709, 44.70, 272.30, 8.90, 0.00, null, -8.90,
                50.00);
        checkInst(model2, 5, 5, LocalDate.of(2019, 1, 10), 5, 5, 50.00, 50.00, null, 0.86931306, 43.47, 230.03, 7.73, 0.00, null, -7.73,
                50.00);
        checkInst(model2, 6, 6, LocalDate.of(2019, 1, 11), 4, 6, 50.00, 50.00, null, 0.84530113, 42.27, 186.57, 6.53, 0.00, null, -6.53,
                50.00);
        checkInst(model2, 7, 7, LocalDate.of(2019, 1, 12), 3, 7, 50.00, 50.00, null, 0.82195244, 41.10, 141.87, 5.30, 0.00, null, -5.30,
                50.00);
        checkInst(model2, 8, 8, LocalDate.of(2019, 1, 13), 2, 8, 50.00, 50.00, null, 0.79924869, 39.96, 95.89, 4.03, 0.00, null, -4.03,
                50.00);
        checkInst(model2, 9, 9, LocalDate.of(2019, 1, 14), 1, 9, 50.00, 50.00, null, 0.77717205, 38.86, 48.62, 2.72, 0.00, null, -2.72,
                50.00);
        checkInst(model2, 10, 10, LocalDate.of(2019, 1, 15), 0, 10, 50.00, 50.00, null, 0.75570520, 37.79, 0.00, 1.38, 0.00, null, -1.38,
                50.00);
    }

    @Test
    void testProjectedSchedule_term200_originationFee1000_netDisbursement9000() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        assertEquals(TERM, model.loanTerm());

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 200, 0, -9000.00, null, null, 1.00000000, -9000.00, 9000.00, null, null, null,
                null, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 199, 1, 50.00, 50.00, null, 0.99893332, 49.95, 8959.61, 9.61, 0.00, null, -9.61,
                1000.00);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 198, 2, 50.00, 50.00, null, 0.99786779, 49.89, 8919.18, 9.57, 0.00, null, -9.57,
                1000.00);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 197, 3, 50.00, 50.00, null, 0.99680339, 49.84, 8878.70, 9.52, 0.00, null, -9.52,
                1000.00);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 196, 4, 50.00, 50.00, null, 0.99574012, 49.79, 8838.18, 9.48, 0.00, null, -9.48,
                1000.00);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 195, 5, 50.00, 50.00, null, 0.99467799, 49.73, 8797.62, 9.44, 0.00, null, -9.44,
                1000.00);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 194, 6, 50.00, 50.00, null, 0.99361699, 49.68, 8757.01, 9.39, 0.00, null, -9.39,
                1000.00);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 193, 7, 50.00, 50.00, null, 0.99255712, 49.63, 8716.36, 9.35, 0.00, null, -9.35,
                1000.00);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 192, 8, 50.00, 50.00, null, 0.99149839, 49.57, 8675.67, 9.31, 0.00, null, -9.31,
                1000.00);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 191, 9, 50.00, 50.00, null, 0.99044078, 49.52, 8634.94, 9.26, 0.00, null, -9.26,
                1000.00);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 190, 10, 50.00, 50.00, null, 0.98938430, 49.47, 8594.16, 9.22, 0.00, null,
                -9.22, 1000.00);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 189, 11, 50.00, 50.00, null, 0.98832895, 49.42, 8553.33, 9.18, 0.00, null,
                -9.18, 1000.00);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 188, 12, 50.00, 50.00, null, 0.98727472, 49.36, 8512.47, 9.13, 0.00, null,
                -9.13, 1000.00);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 187, 13, 50.00, 50.00, null, 0.98622162, 49.31, 8471.56, 9.09, 0.00, null,
                -9.09, 1000.00);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 186, 14, 50.00, 50.00, null, 0.98516964, 49.26, 8430.60, 9.05, 0.00, null,
                -9.05, 1000.00);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 185, 15, 50.00, 50.00, null, 0.98411879, 49.21, 8389.61, 9.00, 0.00, null,
                -9.00, 1000.00);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 184, 16, 50.00, 50.00, null, 0.98306905, 49.15, 8348.56, 8.96, 0.00, null,
                -8.96, 1000.00);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 183, 17, 50.00, 50.00, null, 0.98202044, 49.10, 8307.48, 8.91, 0.00, null,
                -8.91, 1000.00);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 182, 18, 50.00, 50.00, null, 0.98097294, 49.05, 8266.35, 8.87, 0.00, null,
                -8.87, 1000.00);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 181, 19, 50.00, 50.00, null, 0.97992656, 49.00, 8225.18, 8.83, 0.00, null,
                -8.83, 1000.00);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 180, 20, 50.00, 50.00, null, 0.97888129, 48.94, 8183.96, 8.78, 0.00, null,
                -8.78, 1000.00);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 179, 21, 50.00, 50.00, null, 0.97783715, 48.89, 8142.70, 8.74, 0.00, null,
                -8.74, 1000.00);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 178, 22, 50.00, 50.00, null, 0.97679411, 48.84, 8101.39, 8.69, 0.00, null,
                -8.69, 1000.00);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 177, 23, 50.00, 50.00, null, 0.97575219, 48.79, 8060.04, 8.65, 0.00, null,
                -8.65, 1000.00);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 176, 24, 50.00, 50.00, null, 0.97471138, 48.74, 8018.65, 8.61, 0.00, null,
                -8.61, 1000.00);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 175, 25, 50.00, 50.00, null, 0.97367168, 48.68, 7977.21, 8.56, 0.00, null,
                -8.56, 1000.00);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 174, 26, 50.00, 50.00, null, 0.97263309, 48.63, 7935.73, 8.52, 0.00, null,
                -8.52, 1000.00);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 173, 27, 50.00, 50.00, null, 0.97159560, 48.58, 7894.21, 8.47, 0.00, null,
                -8.47, 1000.00);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 172, 28, 50.00, 50.00, null, 0.97055922, 48.53, 7852.63, 8.43, 0.00, null,
                -8.43, 1000.00);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 171, 29, 50.00, 50.00, null, 0.96952395, 48.48, 7811.02, 8.39, 0.00, null,
                -8.39, 1000.00);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 170, 30, 50.00, 50.00, null, 0.96848979, 48.42, 7769.36, 8.34, 0.00, null,
                -8.34, 1000.00);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 169, 31, 50.00, 50.00, null, 0.96745672, 48.37, 7727.66, 8.30, 0.00, null, -8.30,
                1000.00);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 168, 32, 50.00, 50.00, null, 0.96642476, 48.32, 7685.91, 8.25, 0.00, null, -8.25,
                1000.00);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 167, 33, 50.00, 50.00, null, 0.96539390, 48.27, 7644.12, 8.21, 0.00, null, -8.21,
                1000.00);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 166, 34, 50.00, 50.00, null, 0.96436413, 48.22, 7602.28, 8.16, 0.00, null, -8.16,
                1000.00);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 165, 35, 50.00, 50.00, null, 0.96333547, 48.17, 7560.40, 8.12, 0.00, null, -8.12,
                1000.00);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 164, 36, 50.00, 50.00, null, 0.96230790, 48.12, 7518.47, 8.07, 0.00, null, -8.07,
                1000.00);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 163, 37, 50.00, 50.00, null, 0.96128143, 48.06, 7476.50, 8.03, 0.00, null, -8.03,
                1000.00);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 162, 38, 50.00, 50.00, null, 0.96025606, 48.01, 7434.48, 7.98, 0.00, null, -7.98,
                1000.00);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 161, 39, 50.00, 50.00, null, 0.95923178, 47.96, 7392.42, 7.94, 0.00, null, -7.94,
                1000.00);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 160, 40, 50.00, 50.00, null, 0.95820859, 47.91, 7350.31, 7.89, 0.00, null,
                -7.89, 1000.00);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 159, 41, 50.00, 50.00, null, 0.95718649, 47.86, 7308.16, 7.85, 0.00, null,
                -7.85, 1000.00);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 158, 42, 50.00, 50.00, null, 0.95616548, 47.81, 7265.97, 7.80, 0.00, null,
                -7.80, 1000.00);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 157, 43, 50.00, 50.00, null, 0.95514557, 47.76, 7223.72, 7.76, 0.00, null,
                -7.76, 1000.00);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 156, 44, 50.00, 50.00, null, 0.95412674, 47.71, 7181.44, 7.71, 0.00, null,
                -7.71, 1000.00);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 155, 45, 50.00, 50.00, null, 0.95310899, 47.66, 7139.11, 7.67, 0.00, null,
                -7.67, 1000.00);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 154, 46, 50.00, 50.00, null, 0.95209233, 47.60, 7096.73, 7.62, 0.00, null,
                -7.62, 1000.00);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 153, 47, 50.00, 50.00, null, 0.95107676, 47.55, 7054.31, 7.58, 0.00, null,
                -7.58, 1000.00);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 152, 48, 50.00, 50.00, null, 0.95006227, 47.50, 7011.84, 7.53, 0.00, null,
                -7.53, 1000.00);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 151, 49, 50.00, 50.00, null, 0.94904886, 47.45, 6969.33, 7.49, 0.00, null,
                -7.49, 1000.00);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 150, 50, 50.00, 50.00, null, 0.94803653, 47.40, 6926.77, 7.44, 0.00, null,
                -7.44, 1000.00);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 149, 51, 50.00, 50.00, null, 0.94702529, 47.35, 6884.17, 7.40, 0.00, null,
                -7.40, 1000.00);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 148, 52, 50.00, 50.00, null, 0.94601512, 47.30, 6841.52, 7.35, 0.00, null,
                -7.35, 1000.00);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 147, 53, 50.00, 50.00, null, 0.94500603, 47.25, 6798.82, 7.31, 0.00, null,
                -7.31, 1000.00);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 146, 54, 50.00, 50.00, null, 0.94399801, 47.20, 6756.08, 7.26, 0.00, null,
                -7.26, 1000.00);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 145, 55, 50.00, 50.00, null, 0.94299107, 47.15, 6713.30, 7.21, 0.00, null,
                -7.21, 1000.00);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 144, 56, 50.00, 50.00, null, 0.94198521, 47.10, 6670.47, 7.17, 0.00, null,
                -7.17, 1000.00);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 143, 57, 50.00, 50.00, null, 0.94098042, 47.05, 6627.59, 7.12, 0.00, null,
                -7.12, 1000.00);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 142, 58, 50.00, 50.00, null, 0.93997669, 47.00, 6584.67, 7.08, 0.00, null,
                -7.08, 1000.00);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 141, 59, 50.00, 50.00, null, 0.93897404, 46.95, 6541.70, 7.03, 0.00, null, -7.03,
                1000.00);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 140, 60, 50.00, 50.00, null, 0.93797246, 46.90, 6498.68, 6.99, 0.00, null, -6.99,
                1000.00);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 139, 61, 50.00, 50.00, null, 0.93697195, 46.85, 6455.62, 6.94, 0.00, null, -6.94,
                1000.00);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 138, 62, 50.00, 50.00, null, 0.93597251, 46.80, 6412.51, 6.89, 0.00, null, -6.89,
                1000.00);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 137, 63, 50.00, 50.00, null, 0.93497413, 46.75, 6369.36, 6.85, 0.00, null, -6.85,
                1000.00);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 136, 64, 50.00, 50.00, null, 0.93397681, 46.70, 6326.16, 6.80, 0.00, null, -6.80,
                1000.00);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 135, 65, 50.00, 50.00, null, 0.93298056, 46.65, 6282.92, 6.76, 0.00, null, -6.76,
                1000.00);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 134, 66, 50.00, 50.00, null, 0.93198538, 46.60, 6239.63, 6.71, 0.00, null, -6.71,
                1000.00);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 133, 67, 50.00, 50.00, null, 0.93099125, 46.55, 6196.29, 6.66, 0.00, null, -6.66,
                1000.00);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 132, 68, 50.00, 50.00, null, 0.92999818, 46.50, 6152.91, 6.62, 0.00, null,
                -6.62, 1000.00);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 131, 69, 50.00, 50.00, null, 0.92900618, 46.45, 6109.48, 6.57, 0.00, null,
                -6.57, 1000.00);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 130, 70, 50.00, 50.00, null, 0.92801523, 46.40, 6066.00, 6.52, 0.00, null,
                -6.52, 1000.00);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 129, 71, 50.00, 50.00, null, 0.92702534, 46.35, 6022.48, 6.48, 0.00, null,
                -6.48, 1000.00);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 128, 72, 50.00, 50.00, null, 0.92603650, 46.30, 5978.91, 6.43, 0.00, null,
                -6.43, 1000.00);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 127, 73, 50.00, 50.00, null, 0.92504872, 46.25, 5935.29, 6.38, 0.00, null,
                -6.38, 1000.00);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 126, 74, 50.00, 50.00, null, 0.92406200, 46.20, 5891.63, 6.34, 0.00, null,
                -6.34, 1000.00);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 125, 75, 50.00, 50.00, null, 0.92307632, 46.15, 5847.92, 6.29, 0.00, null,
                -6.29, 1000.00);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 124, 76, 50.00, 50.00, null, 0.92209170, 46.10, 5804.17, 6.24, 0.00, null,
                -6.24, 1000.00);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 123, 77, 50.00, 50.00, null, 0.92110813, 46.06, 5760.36, 6.20, 0.00, null,
                -6.20, 1000.00);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 122, 78, 50.00, 50.00, null, 0.92012560, 46.01, 5716.52, 6.15, 0.00, null,
                -6.15, 1000.00);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 121, 79, 50.00, 50.00, null, 0.91914413, 45.96, 5672.62, 6.10, 0.00, null,
                -6.10, 1000.00);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 120, 80, 50.00, 50.00, null, 0.91816370, 45.91, 5628.68, 6.06, 0.00, null,
                -6.06, 1000.00);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 119, 81, 50.00, 50.00, null, 0.91718432, 45.86, 5584.69, 6.01, 0.00, null,
                -6.01, 1000.00);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 118, 82, 50.00, 50.00, null, 0.91620598, 45.81, 5540.65, 5.96, 0.00, null,
                -5.96, 1000.00);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 117, 83, 50.00, 50.00, null, 0.91522868, 45.76, 5496.57, 5.92, 0.00, null,
                -5.92, 1000.00);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 116, 84, 50.00, 50.00, null, 0.91425243, 45.71, 5452.44, 5.87, 0.00, null,
                -5.87, 1000.00);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 115, 85, 50.00, 50.00, null, 0.91327722, 45.66, 5408.26, 5.82, 0.00, null,
                -5.82, 1000.00);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 114, 86, 50.00, 50.00, null, 0.91230305, 45.62, 5364.03, 5.78, 0.00, null,
                -5.78, 1000.00);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 113, 87, 50.00, 50.00, null, 0.91132992, 45.57, 5319.76, 5.73, 0.00, null,
                -5.73, 1000.00);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 112, 88, 50.00, 50.00, null, 0.91035783, 45.52, 5275.44, 5.68, 0.00, null,
                -5.68, 1000.00);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 111, 89, 50.00, 50.00, null, 0.90938677, 45.47, 5231.08, 5.63, 0.00, null,
                -5.63, 1000.00);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 110, 90, 50.00, 50.00, null, 0.90841675, 45.42, 5186.66, 5.59, 0.00, null, -5.59,
                1000.00);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 109, 91, 50.00, 50.00, null, 0.90744776, 45.37, 5142.20, 5.54, 0.00, null, -5.54,
                1000.00);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 108, 92, 50.00, 50.00, null, 0.90647981, 45.32, 5097.69, 5.49, 0.00, null, -5.49,
                1000.00);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 107, 93, 50.00, 50.00, null, 0.90551289, 45.28, 5053.13, 5.44, 0.00, null, -5.44,
                1000.00);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 106, 94, 50.00, 50.00, null, 0.90454700, 45.23, 5008.53, 5.40, 0.00, null, -5.40,
                1000.00);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 105, 95, 50.00, 50.00, null, 0.90358215, 45.18, 4963.88, 5.35, 0.00, null, -5.35,
                1000.00);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 104, 96, 50.00, 50.00, null, 0.90261832, 45.13, 4919.18, 5.30, 0.00, null, -5.30,
                1000.00);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 103, 97, 50.00, 50.00, null, 0.90165552, 45.08, 4874.43, 5.25, 0.00, null, -5.25,
                1000.00);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 102, 98, 50.00, 50.00, null, 0.90069374, 45.03, 4829.64, 5.20, 0.00, null, -5.20,
                1000.00);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 101, 99, 50.00, 50.00, null, 0.89973299, 44.99, 4784.79, 5.16, 0.00, null,
                -5.16, 1000.00);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 100, 100, 50.00, 50.00, null, 0.89877327, 44.94, 4739.90, 5.11, 0.00, null,
                -5.11, 1000.00);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 99, 101, 50.00, 50.00, null, 0.89781457, 44.89, 4694.96, 5.06, 0.00, null,
                -5.06, 1000.00);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 98, 102, 50.00, 50.00, null, 0.89685689, 44.84, 4649.98, 5.01, 0.00, null,
                -5.01, 1000.00);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 97, 103, 50.00, 50.00, null, 0.89590024, 44.80, 4604.94, 4.97, 0.00, null,
                -4.97, 1000.00);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 96, 104, 50.00, 50.00, null, 0.89494460, 44.75, 4559.86, 4.92, 0.00, null,
                -4.92, 1000.00);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 95, 105, 50.00, 50.00, null, 0.89398999, 44.70, 4514.73, 4.87, 0.00, null,
                -4.87, 1000.00);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 94, 106, 50.00, 50.00, null, 0.89303639, 44.65, 4469.55, 4.82, 0.00, null,
                -4.82, 1000.00);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 93, 107, 50.00, 50.00, null, 0.89208381, 44.60, 4424.32, 4.77, 0.00, null,
                -4.77, 1000.00);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 92, 108, 50.00, 50.00, null, 0.89113225, 44.56, 4379.05, 4.72, 0.00, null,
                -4.72, 1000.00);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 91, 109, 50.00, 50.00, null, 0.89018170, 44.51, 4333.72, 4.68, 0.00, null,
                -4.68, 1000.00);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 90, 110, 50.00, 50.00, null, 0.88923216, 44.46, 4288.35, 4.63, 0.00, null,
                -4.63, 1000.00);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 89, 111, 50.00, 50.00, null, 0.88828364, 44.41, 4242.93, 4.58, 0.00, null,
                -4.58, 1000.00);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 88, 112, 50.00, 50.00, null, 0.88733613, 44.37, 4197.46, 4.53, 0.00, null,
                -4.53, 1000.00);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 87, 113, 50.00, 50.00, null, 0.88638963, 44.32, 4151.94, 4.48, 0.00, null,
                -4.48, 1000.00);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 86, 114, 50.00, 50.00, null, 0.88544414, 44.27, 4106.38, 4.43, 0.00, null,
                -4.43, 1000.00);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 85, 115, 50.00, 50.00, null, 0.88449966, 44.22, 4060.76, 4.38, 0.00, null,
                -4.38, 1000.00);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 84, 116, 50.00, 50.00, null, 0.88355619, 44.18, 4015.10, 4.34, 0.00, null,
                -4.34, 1000.00);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 83, 117, 50.00, 50.00, null, 0.88261372, 44.13, 3969.38, 4.29, 0.00, null,
                -4.29, 1000.00);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 82, 118, 50.00, 50.00, null, 0.88167226, 44.08, 3923.62, 4.24, 0.00, null,
                -4.24, 1000.00);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 81, 119, 50.00, 50.00, null, 0.88073180, 44.04, 3877.81, 4.19, 0.00, null,
                -4.19, 1000.00);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 80, 120, 50.00, 50.00, null, 0.87979234, 43.99, 3831.95, 4.14, 0.00, null,
                -4.14, 1000.00);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 79, 121, 50.00, 50.00, null, 0.87885389, 43.94, 3786.04, 4.09, 0.00, null,
                -4.09, 1000.00);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 78, 122, 50.00, 50.00, null, 0.87791644, 43.90, 3740.09, 4.04, 0.00, null,
                -4.04, 1000.00);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 77, 123, 50.00, 50.00, null, 0.87697999, 43.85, 3694.08, 3.99, 0.00, null,
                -3.99, 1000.00);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 76, 124, 50.00, 50.00, null, 0.87604453, 43.80, 3648.03, 3.94, 0.00, null,
                -3.94, 1000.00);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 75, 125, 50.00, 50.00, null, 0.87511008, 43.76, 3601.92, 3.90, 0.00, null,
                -3.90, 1000.00);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 74, 126, 50.00, 50.00, null, 0.87417662, 43.71, 3555.77, 3.85, 0.00, null,
                -3.85, 1000.00);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 73, 127, 50.00, 50.00, null, 0.87324416, 43.66, 3509.56, 3.80, 0.00, null,
                -3.80, 1000.00);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 72, 128, 50.00, 50.00, null, 0.87231269, 43.62, 3463.31, 3.75, 0.00, null,
                -3.75, 1000.00);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 71, 129, 50.00, 50.00, null, 0.87138221, 43.57, 3417.01, 3.70, 0.00, null,
                -3.70, 1000.00);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 70, 130, 50.00, 50.00, null, 0.87045273, 43.52, 3370.66, 3.65, 0.00, null,
                -3.65, 1000.00);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 69, 131, 50.00, 50.00, null, 0.86952424, 43.48, 3324.26, 3.60, 0.00, null,
                -3.60, 1000.00);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 68, 132, 50.00, 50.00, null, 0.86859674, 43.43, 3277.81, 3.55, 0.00, null,
                -3.55, 1000.00);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 67, 133, 50.00, 50.00, null, 0.86767023, 43.38, 3231.31, 3.50, 0.00, null,
                -3.50, 1000.00);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 66, 134, 50.00, 50.00, null, 0.86674471, 43.34, 3184.76, 3.45, 0.00, null,
                -3.45, 1000.00);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 65, 135, 50.00, 50.00, null, 0.86582017, 43.29, 3138.16, 3.40, 0.00, null,
                -3.40, 1000.00);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 64, 136, 50.00, 50.00, null, 0.86489662, 43.24, 3091.51, 3.35, 0.00, null,
                -3.35, 1000.00);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 63, 137, 50.00, 50.00, null, 0.86397406, 43.20, 3044.81, 3.30, 0.00, null,
                -3.30, 1000.00);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 62, 138, 50.00, 50.00, null, 0.86305248, 43.15, 2998.06, 3.25, 0.00, null,
                -3.25, 1000.00);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 61, 139, 50.00, 50.00, null, 0.86213188, 43.11, 2951.26, 3.20, 0.00, null,
                -3.20, 1000.00);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 60, 140, 50.00, 50.00, null, 0.86121227, 43.06, 2904.42, 3.15, 0.00, null,
                -3.15, 1000.00);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 59, 141, 50.00, 50.00, null, 0.86029363, 43.01, 2857.52, 3.10, 0.00, null,
                -3.10, 1000.00);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 58, 142, 50.00, 50.00, null, 0.85937598, 42.97, 2810.57, 3.05, 0.00, null,
                -3.05, 1000.00);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 57, 143, 50.00, 50.00, null, 0.85845930, 42.92, 2763.57, 3.00, 0.00, null,
                -3.00, 1000.00);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 56, 144, 50.00, 50.00, null, 0.85754361, 42.88, 2716.52, 2.95, 0.00, null,
                -2.95, 1000.00);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 55, 145, 50.00, 50.00, null, 0.85662889, 42.83, 2669.42, 2.90, 0.00, null,
                -2.90, 1000.00);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 54, 146, 50.00, 50.00, null, 0.85571514, 42.79, 2622.27, 2.85, 0.00, null,
                -2.85, 1000.00);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 53, 147, 50.00, 50.00, null, 0.85480237, 42.74, 2575.07, 2.80, 0.00, null,
                -2.80, 1000.00);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 52, 148, 50.00, 50.00, null, 0.85389057, 42.69, 2527.82, 2.75, 0.00, null,
                -2.75, 1000.00);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 51, 149, 50.00, 50.00, null, 0.85297975, 42.65, 2480.52, 2.70, 0.00, null,
                -2.70, 1000.00);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 50, 150, 50.00, 50.00, null, 0.85206990, 42.60, 2433.17, 2.65, 0.00, null,
                -2.65, 1000.00);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 49, 151, 50.00, 50.00, null, 0.85116101, 42.56, 2385.77, 2.60, 0.00, null,
                -2.60, 1000.00);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 48, 152, 50.00, 50.00, null, 0.85025310, 42.51, 2338.31, 2.55, 0.00, null,
                -2.55, 1000.00);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 47, 153, 50.00, 50.00, null, 0.84934616, 42.47, 2290.81, 2.50, 0.00, null,
                -2.50, 1000.00);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 46, 154, 50.00, 50.00, null, 0.84844018, 42.42, 2243.26, 2.45, 0.00, null,
                -2.45, 1000.00);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 45, 155, 50.00, 50.00, null, 0.84753517, 42.38, 2195.65, 2.40, 0.00, null,
                -2.40, 1000.00);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 44, 156, 50.00, 50.00, null, 0.84663113, 42.33, 2148.00, 2.34, 0.00, null,
                -2.34, 1000.00);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 43, 157, 50.00, 50.00, null, 0.84572805, 42.29, 2100.29, 2.29, 0.00, null,
                -2.29, 1000.00);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 42, 158, 50.00, 50.00, null, 0.84482593, 42.24, 2052.53, 2.24, 0.00, null,
                -2.24, 1000.00);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 41, 159, 50.00, 50.00, null, 0.84392477, 42.20, 2004.73, 2.19, 0.00, null,
                -2.19, 1000.00);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 40, 160, 50.00, 50.00, null, 0.84302458, 42.15, 1956.87, 2.14, 0.00, null,
                -2.14, 1000.00);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 39, 161, 50.00, 50.00, null, 0.84212535, 42.11, 1908.96, 2.09, 0.00, null,
                -2.09, 1000.00);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 38, 162, 50.00, 50.00, null, 0.84122707, 42.06, 1860.99, 2.04, 0.00, null,
                -2.04, 1000.00);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 37, 163, 50.00, 50.00, null, 0.84032975, 42.02, 1812.98, 1.99, 0.00, null,
                -1.99, 1000.00);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 36, 164, 50.00, 50.00, null, 0.83943340, 41.97, 1764.92, 1.94, 0.00, null,
                -1.94, 1000.00);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 35, 165, 50.00, 50.00, null, 0.83853799, 41.93, 1716.80, 1.88, 0.00, null,
                -1.88, 1000.00);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 34, 166, 50.00, 50.00, null, 0.83764354, 41.88, 1668.64, 1.83, 0.00, null,
                -1.83, 1000.00);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 33, 167, 50.00, 50.00, null, 0.83675005, 41.84, 1620.42, 1.78, 0.00, null,
                -1.78, 1000.00);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 32, 168, 50.00, 50.00, null, 0.83585751, 41.79, 1572.15, 1.73, 0.00, null,
                -1.73, 1000.00);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 31, 169, 50.00, 50.00, null, 0.83496592, 41.75, 1523.83, 1.68, 0.00, null,
                -1.68, 1000.00);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 30, 170, 50.00, 50.00, null, 0.83407528, 41.70, 1475.45, 1.63, 0.00, null,
                -1.63, 1000.00);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 29, 171, 50.00, 50.00, null, 0.83318560, 41.66, 1427.03, 1.58, 0.00, null,
                -1.58, 1000.00);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 28, 172, 50.00, 50.00, null, 0.83229686, 41.61, 1378.55, 1.52, 0.00, null,
                -1.52, 1000.00);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 27, 173, 50.00, 50.00, null, 0.83140907, 41.57, 1330.02, 1.47, 0.00, null,
                -1.47, 1000.00);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 26, 174, 50.00, 50.00, null, 0.83052222, 41.53, 1281.45, 1.42, 0.00, null,
                -1.42, 1000.00);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 25, 175, 50.00, 50.00, null, 0.82963633, 41.48, 1232.81, 1.37, 0.00, null,
                -1.37, 1000.00);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 24, 176, 50.00, 50.00, null, 0.82875137, 41.44, 1184.13, 1.32, 0.00, null,
                -1.32, 1000.00);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 23, 177, 50.00, 50.00, null, 0.82786736, 41.39, 1135.39, 1.26, 0.00, null,
                -1.26, 1000.00);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 22, 178, 50.00, 50.00, null, 0.82698430, 41.35, 1086.61, 1.21, 0.00, null,
                -1.21, 1000.00);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 21, 179, 50.00, 50.00, null, 0.82610217, 41.31, 1037.77, 1.16, 0.00, null,
                -1.16, 1000.00);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 20, 180, 50.00, 50.00, null, 0.82522099, 41.26, 988.88, 1.11, 0.00, null,
                -1.11, 1000.00);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 19, 181, 50.00, 50.00, null, 0.82434075, 41.22, 939.93, 1.06, 0.00, null,
                -1.06, 1000.00);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 18, 182, 50.00, 50.00, null, 0.82346144, 41.17, 890.93, 1.00, 0.00, null,
                -1.00, 1000.00);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 17, 183, 50.00, 50.00, null, 0.82258308, 41.13, 841.89, 0.95, 0.00, null,
                -0.95, 1000.00);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 16, 184, 50.00, 50.00, null, 0.82170565, 41.09, 792.79, 0.90, 0.00, null,
                -0.90, 1000.00);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 15, 185, 50.00, 50.00, null, 0.82082916, 41.04, 743.63, 0.85, 0.00, null,
                -0.85, 1000.00);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 14, 186, 50.00, 50.00, null, 0.81995360, 41.00, 694.43, 0.79, 0.00, null,
                -0.79, 1000.00);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 13, 187, 50.00, 50.00, null, 0.81907897, 40.95, 645.17, 0.74, 0.00, null,
                -0.74, 1000.00);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 12, 188, 50.00, 50.00, null, 0.81820528, 40.91, 595.86, 0.69, 0.00, null,
                -0.69, 1000.00);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 11, 189, 50.00, 50.00, null, 0.81733252, 40.87, 546.49, 0.64, 0.00, null,
                -0.64, 1000.00);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 10, 190, 50.00, 50.00, null, 0.81646069, 40.82, 497.08, 0.58, 0.00, null,
                -0.58, 1000.00);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 9, 191, 50.00, 50.00, null, 0.81558979, 40.78, 447.61, 0.53, 0.00, null,
                -0.53, 1000.00);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 8, 192, 50.00, 50.00, null, 0.81471983, 40.74, 398.08, 0.48, 0.00, null,
                -0.48, 1000.00);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 7, 193, 50.00, 50.00, null, 0.81385078, 40.69, 348.51, 0.43, 0.00, null,
                -0.43, 1000.00);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 6, 194, 50.00, 50.00, null, 0.81298267, 40.65, 298.88, 0.37, 0.00, null,
                -0.37, 1000.00);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 5, 195, 50.00, 50.00, null, 0.81211548, 40.61, 249.20, 0.32, 0.00, null,
                -0.32, 1000.00);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 4, 196, 50.00, 50.00, null, 0.81124922, 40.56, 199.47, 0.27, 0.00, null,
                -0.27, 1000.00);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 3, 197, 50.00, 50.00, null, 0.81038388, 40.52, 149.68, 0.21, 0.00, null,
                -0.21, 1000.00);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 2, 198, 50.00, 50.00, null, 0.80951946, 40.48, 99.84, 0.16, 0.00, null, -0.16,
                1000.00);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 1, 199, 50.00, 50.00, null, 0.80865597, 40.43, 49.95, 0.11, 0.00, null, -0.11,
                1000.00);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 0, 200, 50.00, 50.00, null, 0.80779339, 40.39, 0.00, 0.05, 0.00, null, -0.05,
                1000.00);
    }

    @Test
    void testOnTimePayment_term200_originationFee1000_netDisbursement9000_pay50_50() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("50"));
        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(2), new BigDecimal("50"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 202, 0, -9000.00, null, null, 1.00000000, -9000.00, 9000.00, null, null, null,
                null, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 201, 0, 50.00, 50.00, 50.00, 1.00000000, 50.00, 8959.61, 9.61, 19.18, 9.61, 0.00,
                990.39);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 200, 0, 50.00, 50.00, 50.00, 1.00000000, 50.00, 8919.18, 9.57, 9.57, 9.57, 0.00,
                980.82);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 199, 1, 50.00, 50.00, null, 0.99893332, 49.95, 8878.70, 9.52, 0.00, null, -9.52,
                980.82);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 198, 2, 50.00, 50.00, null, 0.99786779, 49.89, 8838.18, 9.48, 0.00, null, -9.48,
                980.82);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 197, 3, 50.00, 50.00, null, 0.99680339, 49.84, 8797.62, 9.44, 0.00, null, -9.44,
                980.82);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 196, 4, 50.00, 50.00, null, 0.99574012, 49.79, 8757.01, 9.39, 0.00, null, -9.39,
                980.82);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 195, 5, 50.00, 50.00, null, 0.99467799, 49.73, 8716.36, 9.35, 0.00, null, -9.35,
                980.82);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 194, 6, 50.00, 50.00, null, 0.99361699, 49.68, 8675.67, 9.31, 0.00, null, -9.31,
                980.82);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 193, 7, 50.00, 50.00, null, 0.99255712, 49.63, 8634.94, 9.26, 0.00, null, -9.26,
                980.82);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 192, 8, 50.00, 50.00, null, 0.99149839, 49.57, 8594.16, 9.22, 0.00, null, -9.22,
                980.82);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 191, 9, 50.00, 50.00, null, 0.99044078, 49.52, 8553.33, 9.18, 0.00, null, -9.18,
                980.82);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 190, 10, 50.00, 50.00, null, 0.98938430, 49.47, 8512.47, 9.13, 0.00, null,
                -9.13, 980.82);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 189, 11, 50.00, 50.00, null, 0.98832895, 49.42, 8471.56, 9.09, 0.00, null,
                -9.09, 980.82);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 188, 12, 50.00, 50.00, null, 0.98727472, 49.36, 8430.60, 9.05, 0.00, null,
                -9.05, 980.82);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 187, 13, 50.00, 50.00, null, 0.98622162, 49.31, 8389.61, 9.00, 0.00, null,
                -9.00, 980.82);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 186, 14, 50.00, 50.00, null, 0.98516964, 49.26, 8348.56, 8.96, 0.00, null,
                -8.96, 980.82);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 185, 15, 50.00, 50.00, null, 0.98411879, 49.21, 8307.48, 8.91, 0.00, null,
                -8.91, 980.82);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 184, 16, 50.00, 50.00, null, 0.98306905, 49.15, 8266.35, 8.87, 0.00, null,
                -8.87, 980.82);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 183, 17, 50.00, 50.00, null, 0.98202044, 49.10, 8225.18, 8.83, 0.00, null,
                -8.83, 980.82);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 182, 18, 50.00, 50.00, null, 0.98097294, 49.05, 8183.96, 8.78, 0.00, null,
                -8.78, 980.82);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 181, 19, 50.00, 50.00, null, 0.97992656, 49.00, 8142.70, 8.74, 0.00, null,
                -8.74, 980.82);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 180, 20, 50.00, 50.00, null, 0.97888129, 48.94, 8101.39, 8.69, 0.00, null,
                -8.69, 980.82);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 179, 21, 50.00, 50.00, null, 0.97783715, 48.89, 8060.04, 8.65, 0.00, null,
                -8.65, 980.82);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 178, 22, 50.00, 50.00, null, 0.97679411, 48.84, 8018.65, 8.61, 0.00, null,
                -8.61, 980.82);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 177, 23, 50.00, 50.00, null, 0.97575219, 48.79, 7977.21, 8.56, 0.00, null,
                -8.56, 980.82);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 176, 24, 50.00, 50.00, null, 0.97471138, 48.74, 7935.73, 8.52, 0.00, null,
                -8.52, 980.82);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 175, 25, 50.00, 50.00, null, 0.97367168, 48.68, 7894.21, 8.47, 0.00, null,
                -8.47, 980.82);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 174, 26, 50.00, 50.00, null, 0.97263309, 48.63, 7852.63, 8.43, 0.00, null,
                -8.43, 980.82);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 173, 27, 50.00, 50.00, null, 0.97159560, 48.58, 7811.02, 8.39, 0.00, null,
                -8.39, 980.82);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 172, 28, 50.00, 50.00, null, 0.97055922, 48.53, 7769.36, 8.34, 0.00, null,
                -8.34, 980.82);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 171, 29, 50.00, 50.00, null, 0.96952395, 48.48, 7727.66, 8.30, 0.00, null, -8.30,
                980.82);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 170, 30, 50.00, 50.00, null, 0.96848979, 48.42, 7685.91, 8.25, 0.00, null, -8.25,
                980.82);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 169, 31, 50.00, 50.00, null, 0.96745672, 48.37, 7644.12, 8.21, 0.00, null, -8.21,
                980.82);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 168, 32, 50.00, 50.00, null, 0.96642476, 48.32, 7602.28, 8.16, 0.00, null, -8.16,
                980.82);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 167, 33, 50.00, 50.00, null, 0.96539390, 48.27, 7560.40, 8.12, 0.00, null, -8.12,
                980.82);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 166, 34, 50.00, 50.00, null, 0.96436413, 48.22, 7518.47, 8.07, 0.00, null, -8.07,
                980.82);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 165, 35, 50.00, 50.00, null, 0.96333547, 48.17, 7476.50, 8.03, 0.00, null, -8.03,
                980.82);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 164, 36, 50.00, 50.00, null, 0.96230790, 48.12, 7434.48, 7.98, 0.00, null, -7.98,
                980.82);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 163, 37, 50.00, 50.00, null, 0.96128143, 48.06, 7392.42, 7.94, 0.00, null, -7.94,
                980.82);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 162, 38, 50.00, 50.00, null, 0.96025606, 48.01, 7350.31, 7.89, 0.00, null,
                -7.89, 980.82);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 161, 39, 50.00, 50.00, null, 0.95923178, 47.96, 7308.16, 7.85, 0.00, null,
                -7.85, 980.82);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 160, 40, 50.00, 50.00, null, 0.95820859, 47.91, 7265.97, 7.80, 0.00, null,
                -7.80, 980.82);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 159, 41, 50.00, 50.00, null, 0.95718649, 47.86, 7223.72, 7.76, 0.00, null,
                -7.76, 980.82);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 158, 42, 50.00, 50.00, null, 0.95616548, 47.81, 7181.44, 7.71, 0.00, null,
                -7.71, 980.82);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 157, 43, 50.00, 50.00, null, 0.95514557, 47.76, 7139.11, 7.67, 0.00, null,
                -7.67, 980.82);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 156, 44, 50.00, 50.00, null, 0.95412674, 47.71, 7096.73, 7.62, 0.00, null,
                -7.62, 980.82);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 155, 45, 50.00, 50.00, null, 0.95310899, 47.66, 7054.31, 7.58, 0.00, null,
                -7.58, 980.82);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 154, 46, 50.00, 50.00, null, 0.95209233, 47.60, 7011.84, 7.53, 0.00, null,
                -7.53, 980.82);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 153, 47, 50.00, 50.00, null, 0.95107676, 47.55, 6969.33, 7.49, 0.00, null,
                -7.49, 980.82);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 152, 48, 50.00, 50.00, null, 0.95006227, 47.50, 6926.77, 7.44, 0.00, null,
                -7.44, 980.82);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 151, 49, 50.00, 50.00, null, 0.94904886, 47.45, 6884.17, 7.40, 0.00, null,
                -7.40, 980.82);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 150, 50, 50.00, 50.00, null, 0.94803653, 47.40, 6841.52, 7.35, 0.00, null,
                -7.35, 980.82);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 149, 51, 50.00, 50.00, null, 0.94702529, 47.35, 6798.82, 7.31, 0.00, null,
                -7.31, 980.82);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 148, 52, 50.00, 50.00, null, 0.94601512, 47.30, 6756.08, 7.26, 0.00, null,
                -7.26, 980.82);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 147, 53, 50.00, 50.00, null, 0.94500603, 47.25, 6713.30, 7.21, 0.00, null,
                -7.21, 980.82);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 146, 54, 50.00, 50.00, null, 0.94399801, 47.20, 6670.47, 7.17, 0.00, null,
                -7.17, 980.82);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 145, 55, 50.00, 50.00, null, 0.94299107, 47.15, 6627.59, 7.12, 0.00, null,
                -7.12, 980.82);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 144, 56, 50.00, 50.00, null, 0.94198521, 47.10, 6584.67, 7.08, 0.00, null,
                -7.08, 980.82);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 143, 57, 50.00, 50.00, null, 0.94098042, 47.05, 6541.70, 7.03, 0.00, null, -7.03,
                980.82);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 142, 58, 50.00, 50.00, null, 0.93997669, 47.00, 6498.68, 6.99, 0.00, null, -6.99,
                980.82);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 141, 59, 50.00, 50.00, null, 0.93897404, 46.95, 6455.62, 6.94, 0.00, null, -6.94,
                980.82);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 140, 60, 50.00, 50.00, null, 0.93797246, 46.90, 6412.51, 6.89, 0.00, null, -6.89,
                980.82);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 139, 61, 50.00, 50.00, null, 0.93697195, 46.85, 6369.36, 6.85, 0.00, null, -6.85,
                980.82);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 138, 62, 50.00, 50.00, null, 0.93597251, 46.80, 6326.16, 6.80, 0.00, null, -6.80,
                980.82);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 137, 63, 50.00, 50.00, null, 0.93497413, 46.75, 6282.92, 6.76, 0.00, null, -6.76,
                980.82);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 136, 64, 50.00, 50.00, null, 0.93397681, 46.70, 6239.63, 6.71, 0.00, null, -6.71,
                980.82);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 135, 65, 50.00, 50.00, null, 0.93298056, 46.65, 6196.29, 6.66, 0.00, null, -6.66,
                980.82);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 134, 66, 50.00, 50.00, null, 0.93198538, 46.60, 6152.91, 6.62, 0.00, null,
                -6.62, 980.82);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 133, 67, 50.00, 50.00, null, 0.93099125, 46.55, 6109.48, 6.57, 0.00, null,
                -6.57, 980.82);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 132, 68, 50.00, 50.00, null, 0.92999818, 46.50, 6066.00, 6.52, 0.00, null,
                -6.52, 980.82);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 131, 69, 50.00, 50.00, null, 0.92900618, 46.45, 6022.48, 6.48, 0.00, null,
                -6.48, 980.82);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 130, 70, 50.00, 50.00, null, 0.92801523, 46.40, 5978.91, 6.43, 0.00, null,
                -6.43, 980.82);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 129, 71, 50.00, 50.00, null, 0.92702534, 46.35, 5935.29, 6.38, 0.00, null,
                -6.38, 980.82);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 128, 72, 50.00, 50.00, null, 0.92603650, 46.30, 5891.63, 6.34, 0.00, null,
                -6.34, 980.82);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 127, 73, 50.00, 50.00, null, 0.92504872, 46.25, 5847.92, 6.29, 0.00, null,
                -6.29, 980.82);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 126, 74, 50.00, 50.00, null, 0.92406200, 46.20, 5804.17, 6.24, 0.00, null,
                -6.24, 980.82);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 125, 75, 50.00, 50.00, null, 0.92307632, 46.15, 5760.36, 6.20, 0.00, null,
                -6.20, 980.82);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 124, 76, 50.00, 50.00, null, 0.92209170, 46.10, 5716.52, 6.15, 0.00, null,
                -6.15, 980.82);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 123, 77, 50.00, 50.00, null, 0.92110813, 46.06, 5672.62, 6.10, 0.00, null,
                -6.10, 980.82);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 122, 78, 50.00, 50.00, null, 0.92012560, 46.01, 5628.68, 6.06, 0.00, null,
                -6.06, 980.82);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 121, 79, 50.00, 50.00, null, 0.91914413, 45.96, 5584.69, 6.01, 0.00, null,
                -6.01, 980.82);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 120, 80, 50.00, 50.00, null, 0.91816370, 45.91, 5540.65, 5.96, 0.00, null,
                -5.96, 980.82);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 119, 81, 50.00, 50.00, null, 0.91718432, 45.86, 5496.57, 5.92, 0.00, null,
                -5.92, 980.82);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 118, 82, 50.00, 50.00, null, 0.91620598, 45.81, 5452.44, 5.87, 0.00, null,
                -5.87, 980.82);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 117, 83, 50.00, 50.00, null, 0.91522868, 45.76, 5408.26, 5.82, 0.00, null,
                -5.82, 980.82);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 116, 84, 50.00, 50.00, null, 0.91425243, 45.71, 5364.03, 5.78, 0.00, null,
                -5.78, 980.82);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 115, 85, 50.00, 50.00, null, 0.91327722, 45.66, 5319.76, 5.73, 0.00, null,
                -5.73, 980.82);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 114, 86, 50.00, 50.00, null, 0.91230305, 45.62, 5275.44, 5.68, 0.00, null,
                -5.68, 980.82);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 113, 87, 50.00, 50.00, null, 0.91132992, 45.57, 5231.08, 5.63, 0.00, null,
                -5.63, 980.82);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 112, 88, 50.00, 50.00, null, 0.91035783, 45.52, 5186.66, 5.59, 0.00, null, -5.59,
                980.82);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 111, 89, 50.00, 50.00, null, 0.90938677, 45.47, 5142.20, 5.54, 0.00, null, -5.54,
                980.82);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 110, 90, 50.00, 50.00, null, 0.90841675, 45.42, 5097.69, 5.49, 0.00, null, -5.49,
                980.82);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 109, 91, 50.00, 50.00, null, 0.90744776, 45.37, 5053.13, 5.44, 0.00, null, -5.44,
                980.82);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 108, 92, 50.00, 50.00, null, 0.90647981, 45.32, 5008.53, 5.40, 0.00, null, -5.40,
                980.82);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 107, 93, 50.00, 50.00, null, 0.90551289, 45.28, 4963.88, 5.35, 0.00, null, -5.35,
                980.82);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 106, 94, 50.00, 50.00, null, 0.90454700, 45.23, 4919.18, 5.30, 0.00, null, -5.30,
                980.82);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 105, 95, 50.00, 50.00, null, 0.90358215, 45.18, 4874.43, 5.25, 0.00, null, -5.25,
                980.82);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 104, 96, 50.00, 50.00, null, 0.90261832, 45.13, 4829.64, 5.20, 0.00, null, -5.20,
                980.82);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 103, 97, 50.00, 50.00, null, 0.90165552, 45.08, 4784.79, 5.16, 0.00, null,
                -5.16, 980.82);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 102, 98, 50.00, 50.00, null, 0.90069374, 45.03, 4739.90, 5.11, 0.00, null,
                -5.11, 980.82);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 101, 99, 50.00, 50.00, null, 0.89973299, 44.99, 4694.96, 5.06, 0.00, null,
                -5.06, 980.82);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 100, 100, 50.00, 50.00, null, 0.89877327, 44.94, 4649.98, 5.01, 0.00, null,
                -5.01, 980.82);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 99, 101, 50.00, 50.00, null, 0.89781457, 44.89, 4604.94, 4.97, 0.00, null,
                -4.97, 980.82);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 98, 102, 50.00, 50.00, null, 0.89685689, 44.84, 4559.86, 4.92, 0.00, null,
                -4.92, 980.82);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 97, 103, 50.00, 50.00, null, 0.89590024, 44.80, 4514.73, 4.87, 0.00, null,
                -4.87, 980.82);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 96, 104, 50.00, 50.00, null, 0.89494460, 44.75, 4469.55, 4.82, 0.00, null,
                -4.82, 980.82);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 95, 105, 50.00, 50.00, null, 0.89398999, 44.70, 4424.32, 4.77, 0.00, null,
                -4.77, 980.82);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 94, 106, 50.00, 50.00, null, 0.89303639, 44.65, 4379.05, 4.72, 0.00, null,
                -4.72, 980.82);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 93, 107, 50.00, 50.00, null, 0.89208381, 44.60, 4333.72, 4.68, 0.00, null,
                -4.68, 980.82);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 92, 108, 50.00, 50.00, null, 0.89113225, 44.56, 4288.35, 4.63, 0.00, null,
                -4.63, 980.82);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 91, 109, 50.00, 50.00, null, 0.89018170, 44.51, 4242.93, 4.58, 0.00, null,
                -4.58, 980.82);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 90, 110, 50.00, 50.00, null, 0.88923216, 44.46, 4197.46, 4.53, 0.00, null,
                -4.53, 980.82);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 89, 111, 50.00, 50.00, null, 0.88828364, 44.41, 4151.94, 4.48, 0.00, null,
                -4.48, 980.82);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 88, 112, 50.00, 50.00, null, 0.88733613, 44.37, 4106.38, 4.43, 0.00, null,
                -4.43, 980.82);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 87, 113, 50.00, 50.00, null, 0.88638963, 44.32, 4060.76, 4.38, 0.00, null,
                -4.38, 980.82);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 86, 114, 50.00, 50.00, null, 0.88544414, 44.27, 4015.10, 4.34, 0.00, null,
                -4.34, 980.82);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 85, 115, 50.00, 50.00, null, 0.88449966, 44.22, 3969.38, 4.29, 0.00, null,
                -4.29, 980.82);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 84, 116, 50.00, 50.00, null, 0.88355619, 44.18, 3923.62, 4.24, 0.00, null,
                -4.24, 980.82);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 83, 117, 50.00, 50.00, null, 0.88261372, 44.13, 3877.81, 4.19, 0.00, null,
                -4.19, 980.82);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 82, 118, 50.00, 50.00, null, 0.88167226, 44.08, 3831.95, 4.14, 0.00, null,
                -4.14, 980.82);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 81, 119, 50.00, 50.00, null, 0.88073180, 44.04, 3786.04, 4.09, 0.00, null,
                -4.09, 980.82);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 80, 120, 50.00, 50.00, null, 0.87979234, 43.99, 3740.09, 4.04, 0.00, null,
                -4.04, 980.82);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 79, 121, 50.00, 50.00, null, 0.87885389, 43.94, 3694.08, 3.99, 0.00, null,
                -3.99, 980.82);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 78, 122, 50.00, 50.00, null, 0.87791644, 43.90, 3648.03, 3.94, 0.00, null,
                -3.94, 980.82);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 77, 123, 50.00, 50.00, null, 0.87697999, 43.85, 3601.92, 3.90, 0.00, null,
                -3.90, 980.82);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 76, 124, 50.00, 50.00, null, 0.87604453, 43.80, 3555.77, 3.85, 0.00, null,
                -3.85, 980.82);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 75, 125, 50.00, 50.00, null, 0.87511008, 43.76, 3509.56, 3.80, 0.00, null,
                -3.80, 980.82);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 74, 126, 50.00, 50.00, null, 0.87417662, 43.71, 3463.31, 3.75, 0.00, null,
                -3.75, 980.82);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 73, 127, 50.00, 50.00, null, 0.87324416, 43.66, 3417.01, 3.70, 0.00, null,
                -3.70, 980.82);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 72, 128, 50.00, 50.00, null, 0.87231269, 43.62, 3370.66, 3.65, 0.00, null,
                -3.65, 980.82);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 71, 129, 50.00, 50.00, null, 0.87138221, 43.57, 3324.26, 3.60, 0.00, null,
                -3.60, 980.82);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 70, 130, 50.00, 50.00, null, 0.87045273, 43.52, 3277.81, 3.55, 0.00, null,
                -3.55, 980.82);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 69, 131, 50.00, 50.00, null, 0.86952424, 43.48, 3231.31, 3.50, 0.00, null,
                -3.50, 980.82);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 68, 132, 50.00, 50.00, null, 0.86859674, 43.43, 3184.76, 3.45, 0.00, null,
                -3.45, 980.82);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 67, 133, 50.00, 50.00, null, 0.86767023, 43.38, 3138.16, 3.40, 0.00, null,
                -3.40, 980.82);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 66, 134, 50.00, 50.00, null, 0.86674471, 43.34, 3091.51, 3.35, 0.00, null,
                -3.35, 980.82);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 65, 135, 50.00, 50.00, null, 0.86582017, 43.29, 3044.81, 3.30, 0.00, null,
                -3.30, 980.82);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 64, 136, 50.00, 50.00, null, 0.86489662, 43.24, 2998.06, 3.25, 0.00, null,
                -3.25, 980.82);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 63, 137, 50.00, 50.00, null, 0.86397406, 43.20, 2951.26, 3.20, 0.00, null,
                -3.20, 980.82);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 62, 138, 50.00, 50.00, null, 0.86305248, 43.15, 2904.42, 3.15, 0.00, null,
                -3.15, 980.82);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 61, 139, 50.00, 50.00, null, 0.86213188, 43.11, 2857.52, 3.10, 0.00, null,
                -3.10, 980.82);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 60, 140, 50.00, 50.00, null, 0.86121227, 43.06, 2810.57, 3.05, 0.00, null,
                -3.05, 980.82);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 59, 141, 50.00, 50.00, null, 0.86029363, 43.01, 2763.57, 3.00, 0.00, null,
                -3.00, 980.82);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 58, 142, 50.00, 50.00, null, 0.85937598, 42.97, 2716.52, 2.95, 0.00, null,
                -2.95, 980.82);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 57, 143, 50.00, 50.00, null, 0.85845930, 42.92, 2669.42, 2.90, 0.00, null,
                -2.90, 980.82);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 56, 144, 50.00, 50.00, null, 0.85754361, 42.88, 2622.27, 2.85, 0.00, null,
                -2.85, 980.82);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 55, 145, 50.00, 50.00, null, 0.85662889, 42.83, 2575.07, 2.80, 0.00, null,
                -2.80, 980.82);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 54, 146, 50.00, 50.00, null, 0.85571514, 42.79, 2527.82, 2.75, 0.00, null,
                -2.75, 980.82);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 53, 147, 50.00, 50.00, null, 0.85480237, 42.74, 2480.52, 2.70, 0.00, null,
                -2.70, 980.82);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 52, 148, 50.00, 50.00, null, 0.85389057, 42.69, 2433.17, 2.65, 0.00, null,
                -2.65, 980.82);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 51, 149, 50.00, 50.00, null, 0.85297975, 42.65, 2385.77, 2.60, 0.00, null,
                -2.60, 980.82);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 50, 150, 50.00, 50.00, null, 0.85206990, 42.60, 2338.31, 2.55, 0.00, null,
                -2.55, 980.82);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 49, 151, 50.00, 50.00, null, 0.85116101, 42.56, 2290.81, 2.50, 0.00, null,
                -2.50, 980.82);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 48, 152, 50.00, 50.00, null, 0.85025310, 42.51, 2243.26, 2.45, 0.00, null,
                -2.45, 980.82);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 47, 153, 50.00, 50.00, null, 0.84934616, 42.47, 2195.65, 2.40, 0.00, null,
                -2.40, 980.82);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 46, 154, 50.00, 50.00, null, 0.84844018, 42.42, 2148.00, 2.34, 0.00, null,
                -2.34, 980.82);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 45, 155, 50.00, 50.00, null, 0.84753517, 42.38, 2100.29, 2.29, 0.00, null,
                -2.29, 980.82);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 44, 156, 50.00, 50.00, null, 0.84663113, 42.33, 2052.53, 2.24, 0.00, null,
                -2.24, 980.82);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 43, 157, 50.00, 50.00, null, 0.84572805, 42.29, 2004.73, 2.19, 0.00, null,
                -2.19, 980.82);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 42, 158, 50.00, 50.00, null, 0.84482593, 42.24, 1956.87, 2.14, 0.00, null,
                -2.14, 980.82);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 41, 159, 50.00, 50.00, null, 0.84392477, 42.20, 1908.96, 2.09, 0.00, null,
                -2.09, 980.82);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 40, 160, 50.00, 50.00, null, 0.84302458, 42.15, 1860.99, 2.04, 0.00, null,
                -2.04, 980.82);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 39, 161, 50.00, 50.00, null, 0.84212535, 42.11, 1812.98, 1.99, 0.00, null,
                -1.99, 980.82);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 38, 162, 50.00, 50.00, null, 0.84122707, 42.06, 1764.92, 1.94, 0.00, null,
                -1.94, 980.82);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 37, 163, 50.00, 50.00, null, 0.84032975, 42.02, 1716.80, 1.88, 0.00, null,
                -1.88, 980.82);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 36, 164, 50.00, 50.00, null, 0.83943340, 41.97, 1668.64, 1.83, 0.00, null,
                -1.83, 980.82);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 35, 165, 50.00, 50.00, null, 0.83853799, 41.93, 1620.42, 1.78, 0.00, null,
                -1.78, 980.82);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 34, 166, 50.00, 50.00, null, 0.83764354, 41.88, 1572.15, 1.73, 0.00, null,
                -1.73, 980.82);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 33, 167, 50.00, 50.00, null, 0.83675005, 41.84, 1523.83, 1.68, 0.00, null,
                -1.68, 980.82);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 32, 168, 50.00, 50.00, null, 0.83585751, 41.79, 1475.45, 1.63, 0.00, null,
                -1.63, 980.82);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 31, 169, 50.00, 50.00, null, 0.83496592, 41.75, 1427.03, 1.58, 0.00, null,
                -1.58, 980.82);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 30, 170, 50.00, 50.00, null, 0.83407528, 41.70, 1378.55, 1.52, 0.00, null,
                -1.52, 980.82);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 29, 171, 50.00, 50.00, null, 0.83318560, 41.66, 1330.02, 1.47, 0.00, null,
                -1.47, 980.82);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 28, 172, 50.00, 50.00, null, 0.83229686, 41.61, 1281.45, 1.42, 0.00, null,
                -1.42, 980.82);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 27, 173, 50.00, 50.00, null, 0.83140907, 41.57, 1232.81, 1.37, 0.00, null,
                -1.37, 980.82);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 26, 174, 50.00, 50.00, null, 0.83052222, 41.53, 1184.13, 1.32, 0.00, null,
                -1.32, 980.82);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 25, 175, 50.00, 50.00, null, 0.82963633, 41.48, 1135.39, 1.26, 0.00, null,
                -1.26, 980.82);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 24, 176, 50.00, 50.00, null, 0.82875137, 41.44, 1086.61, 1.21, 0.00, null,
                -1.21, 980.82);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 23, 177, 50.00, 50.00, null, 0.82786736, 41.39, 1037.77, 1.16, 0.00, null,
                -1.16, 980.82);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 22, 178, 50.00, 50.00, null, 0.82698430, 41.35, 988.88, 1.11, 0.00, null,
                -1.11, 980.82);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 21, 179, 50.00, 50.00, null, 0.82610217, 41.31, 939.93, 1.06, 0.00, null,
                -1.06, 980.82);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 20, 180, 50.00, 50.00, null, 0.82522099, 41.26, 890.93, 1.00, 0.00, null,
                -1.00, 980.82);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 19, 181, 50.00, 50.00, null, 0.82434075, 41.22, 841.89, 0.95, 0.00, null,
                -0.95, 980.82);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 18, 182, 50.00, 50.00, null, 0.82346144, 41.17, 792.79, 0.90, 0.00, null,
                -0.90, 980.82);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 17, 183, 50.00, 50.00, null, 0.82258308, 41.13, 743.63, 0.85, 0.00, null,
                -0.85, 980.82);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 16, 184, 50.00, 50.00, null, 0.82170565, 41.09, 694.43, 0.79, 0.00, null,
                -0.79, 980.82);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 15, 185, 50.00, 50.00, null, 0.82082916, 41.04, 645.17, 0.74, 0.00, null,
                -0.74, 980.82);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 14, 186, 50.00, 50.00, null, 0.81995360, 41.00, 595.86, 0.69, 0.00, null,
                -0.69, 980.82);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 13, 187, 50.00, 50.00, null, 0.81907897, 40.95, 546.49, 0.64, 0.00, null,
                -0.64, 980.82);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 12, 188, 50.00, 50.00, null, 0.81820528, 40.91, 497.08, 0.58, 0.00, null,
                -0.58, 980.82);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 11, 189, 50.00, 50.00, null, 0.81733252, 40.87, 447.61, 0.53, 0.00, null,
                -0.53, 980.82);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 10, 190, 50.00, 50.00, null, 0.81646069, 40.82, 398.08, 0.48, 0.00, null,
                -0.48, 980.82);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 9, 191, 50.00, 50.00, null, 0.81558979, 40.78, 348.51, 0.43, 0.00, null,
                -0.43, 980.82);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 8, 192, 50.00, 50.00, null, 0.81471983, 40.74, 298.88, 0.37, 0.00, null,
                -0.37, 980.82);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 7, 193, 50.00, 50.00, null, 0.81385078, 40.69, 249.20, 0.32, 0.00, null,
                -0.32, 980.82);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 6, 194, 50.00, 50.00, null, 0.81298267, 40.65, 199.47, 0.27, 0.00, null,
                -0.27, 980.82);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 5, 195, 50.00, 50.00, null, 0.81211548, 40.61, 149.68, 0.21, 0.00, null,
                -0.21, 980.82);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 4, 196, 50.00, 50.00, null, 0.81124922, 40.56, 99.84, 0.16, 0.00, null, -0.16,
                980.82);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 3, 197, 50.00, 50.00, null, 0.81038388, 40.52, 49.95, 0.11, 0.00, null, -0.11,
                980.82);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 2, 198, 50.00, 50.00, null, 0.80951946, 40.48, 0.00, 0.05, 0.00, null, -0.05,
                980.82);

    }

    @Test
    void testExcessPayment_term200_originationFee1000_netDisbursement9000_pay70_80() {
        final ProjectedAmortizationScheduleModel model = generateModel();

        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("70"));
        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(2), new BigDecimal("80"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 202, 0, -9000.00, null, null, 1.00000000, -9000.00, 9000.00, null, null, null,
                null, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 201, 0, 50.00, 50.00, 70.00, 1.00000000, 70.00, 8959.61, 9.61, 28.70, 13.44, 3.83,
                986.56);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 200, 0, 50.00, 50.00, 80.00, 1.00000000, 80.00, 8919.18, 9.57, 15.26, 15.26, 5.70,
                971.30);

        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 199, 1, 50.00, 50.00, null, 0.99893332, 49.95, 8878.70, 9.52, 0.00, null, -9.52,
                971.30);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 198, 2, 50.00, 50.00, null, 0.99786779, 49.89, 8838.18, 9.48, 0.00, null, -9.48,
                971.30);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 197, 3, 50.00, 50.00, null, 0.99680339, 49.84, 8797.62, 9.44, 0.00, null, -9.44,
                971.30);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 196, 4, 50.00, 50.00, null, 0.99574012, 49.79, 8757.01, 9.39, 0.00, null, -9.39,
                971.30);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 195, 5, 50.00, 50.00, null, 0.99467799, 49.73, 8716.36, 9.35, 0.00, null, -9.35,
                971.30);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 194, 6, 50.00, 50.00, null, 0.99361699, 49.68, 8675.67, 9.31, 0.00, null, -9.31,
                971.30);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 193, 7, 50.00, 50.00, null, 0.99255712, 49.63, 8634.94, 9.26, 0.00, null, -9.26,
                971.30);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 192, 8, 50.00, 50.00, null, 0.99149839, 49.57, 8594.16, 9.22, 0.00, null, -9.22,
                971.30);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 191, 9, 50.00, 50.00, null, 0.99044078, 49.52, 8553.33, 9.18, 0.00, null, -9.18,
                971.30);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 190, 10, 50.00, 50.00, null, 0.98938430, 49.47, 8512.47, 9.13, 0.00, null,
                -9.13, 971.30);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 189, 11, 50.00, 50.00, null, 0.98832895, 49.42, 8471.56, 9.09, 0.00, null,
                -9.09, 971.30);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 188, 12, 50.00, 50.00, null, 0.98727472, 49.36, 8430.60, 9.05, 0.00, null,
                -9.05, 971.30);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 187, 13, 50.00, 50.00, null, 0.98622162, 49.31, 8389.61, 9.00, 0.00, null,
                -9.00, 971.30);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 186, 14, 50.00, 50.00, null, 0.98516964, 49.26, 8348.56, 8.96, 0.00, null,
                -8.96, 971.30);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 185, 15, 50.00, 50.00, null, 0.98411879, 49.21, 8307.48, 8.91, 0.00, null,
                -8.91, 971.30);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 184, 16, 50.00, 50.00, null, 0.98306905, 49.15, 8266.35, 8.87, 0.00, null,
                -8.87, 971.30);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 183, 17, 50.00, 50.00, null, 0.98202044, 49.10, 8225.18, 8.83, 0.00, null,
                -8.83, 971.30);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 182, 18, 50.00, 50.00, null, 0.98097294, 49.05, 8183.96, 8.78, 0.00, null,
                -8.78, 971.30);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 181, 19, 50.00, 50.00, null, 0.97992656, 49.00, 8142.70, 8.74, 0.00, null,
                -8.74, 971.30);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 180, 20, 50.00, 50.00, null, 0.97888129, 48.94, 8101.39, 8.69, 0.00, null,
                -8.69, 971.30);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 179, 21, 50.00, 50.00, null, 0.97783715, 48.89, 8060.04, 8.65, 0.00, null,
                -8.65, 971.30);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 178, 22, 50.00, 50.00, null, 0.97679411, 48.84, 8018.65, 8.61, 0.00, null,
                -8.61, 971.30);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 177, 23, 50.00, 50.00, null, 0.97575219, 48.79, 7977.21, 8.56, 0.00, null,
                -8.56, 971.30);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 176, 24, 50.00, 50.00, null, 0.97471138, 48.74, 7935.73, 8.52, 0.00, null,
                -8.52, 971.30);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 175, 25, 50.00, 50.00, null, 0.97367168, 48.68, 7894.21, 8.47, 0.00, null,
                -8.47, 971.30);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 174, 26, 50.00, 50.00, null, 0.97263309, 48.63, 7852.63, 8.43, 0.00, null,
                -8.43, 971.30);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 173, 27, 50.00, 50.00, null, 0.97159560, 48.58, 7811.02, 8.39, 0.00, null,
                -8.39, 971.30);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 172, 28, 50.00, 50.00, null, 0.97055922, 48.53, 7769.36, 8.34, 0.00, null,
                -8.34, 971.30);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 171, 29, 50.00, 50.00, null, 0.96952395, 48.48, 7727.66, 8.30, 0.00, null, -8.30,
                971.30);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 170, 30, 50.00, 50.00, null, 0.96848979, 48.42, 7685.91, 8.25, 0.00, null, -8.25,
                971.30);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 169, 31, 50.00, 50.00, null, 0.96745672, 48.37, 7644.12, 8.21, 0.00, null, -8.21,
                971.30);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 168, 32, 50.00, 50.00, null, 0.96642476, 48.32, 7602.28, 8.16, 0.00, null, -8.16,
                971.30);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 167, 33, 50.00, 50.00, null, 0.96539390, 48.27, 7560.40, 8.12, 0.00, null, -8.12,
                971.30);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 166, 34, 50.00, 50.00, null, 0.96436413, 48.22, 7518.47, 8.07, 0.00, null, -8.07,
                971.30);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 165, 35, 50.00, 50.00, null, 0.96333547, 48.17, 7476.50, 8.03, 0.00, null, -8.03,
                971.30);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 164, 36, 50.00, 50.00, null, 0.96230790, 48.12, 7434.48, 7.98, 0.00, null, -7.98,
                971.30);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 163, 37, 50.00, 50.00, null, 0.96128143, 48.06, 7392.42, 7.94, 0.00, null, -7.94,
                971.30);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 162, 38, 50.00, 50.00, null, 0.96025606, 48.01, 7350.31, 7.89, 0.00, null,
                -7.89, 971.30);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 161, 39, 50.00, 50.00, null, 0.95923178, 47.96, 7308.16, 7.85, 0.00, null,
                -7.85, 971.30);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 160, 40, 50.00, 50.00, null, 0.95820859, 47.91, 7265.97, 7.80, 0.00, null,
                -7.80, 971.30);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 159, 41, 50.00, 50.00, null, 0.95718649, 47.86, 7223.72, 7.76, 0.00, null,
                -7.76, 971.30);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 158, 42, 50.00, 50.00, null, 0.95616548, 47.81, 7181.44, 7.71, 0.00, null,
                -7.71, 971.30);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 157, 43, 50.00, 50.00, null, 0.95514557, 47.76, 7139.11, 7.67, 0.00, null,
                -7.67, 971.30);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 156, 44, 50.00, 50.00, null, 0.95412674, 47.71, 7096.73, 7.62, 0.00, null,
                -7.62, 971.30);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 155, 45, 50.00, 50.00, null, 0.95310899, 47.66, 7054.31, 7.58, 0.00, null,
                -7.58, 971.30);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 154, 46, 50.00, 50.00, null, 0.95209233, 47.60, 7011.84, 7.53, 0.00, null,
                -7.53, 971.30);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 153, 47, 50.00, 50.00, null, 0.95107676, 47.55, 6969.33, 7.49, 0.00, null,
                -7.49, 971.30);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 152, 48, 50.00, 50.00, null, 0.95006227, 47.50, 6926.77, 7.44, 0.00, null,
                -7.44, 971.30);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 151, 49, 50.00, 50.00, null, 0.94904886, 47.45, 6884.17, 7.40, 0.00, null,
                -7.40, 971.30);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 150, 50, 50.00, 50.00, null, 0.94803653, 47.40, 6841.52, 7.35, 0.00, null,
                -7.35, 971.30);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 149, 51, 50.00, 50.00, null, 0.94702529, 47.35, 6798.82, 7.31, 0.00, null,
                -7.31, 971.30);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 148, 52, 50.00, 50.00, null, 0.94601512, 47.30, 6756.08, 7.26, 0.00, null,
                -7.26, 971.30);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 147, 53, 50.00, 50.00, null, 0.94500603, 47.25, 6713.30, 7.21, 0.00, null,
                -7.21, 971.30);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 146, 54, 50.00, 50.00, null, 0.94399801, 47.20, 6670.47, 7.17, 0.00, null,
                -7.17, 971.30);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 145, 55, 50.00, 50.00, null, 0.94299107, 47.15, 6627.59, 7.12, 0.00, null,
                -7.12, 971.30);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 144, 56, 50.00, 50.00, null, 0.94198521, 47.10, 6584.67, 7.08, 0.00, null,
                -7.08, 971.30);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 143, 57, 50.00, 50.00, null, 0.94098042, 47.05, 6541.70, 7.03, 0.00, null, -7.03,
                971.30);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 142, 58, 50.00, 50.00, null, 0.93997669, 47.00, 6498.68, 6.99, 0.00, null, -6.99,
                971.30);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 141, 59, 50.00, 50.00, null, 0.93897404, 46.95, 6455.62, 6.94, 0.00, null, -6.94,
                971.30);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 140, 60, 50.00, 50.00, null, 0.93797246, 46.90, 6412.51, 6.89, 0.00, null, -6.89,
                971.30);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 139, 61, 50.00, 50.00, null, 0.93697195, 46.85, 6369.36, 6.85, 0.00, null, -6.85,
                971.30);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 138, 62, 50.00, 50.00, null, 0.93597251, 46.80, 6326.16, 6.80, 0.00, null, -6.80,
                971.30);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 137, 63, 50.00, 50.00, null, 0.93497413, 46.75, 6282.92, 6.76, 0.00, null, -6.76,
                971.30);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 136, 64, 50.00, 50.00, null, 0.93397681, 46.70, 6239.63, 6.71, 0.00, null, -6.71,
                971.30);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 135, 65, 50.00, 50.00, null, 0.93298056, 46.65, 6196.29, 6.66, 0.00, null, -6.66,
                971.30);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 134, 66, 50.00, 50.00, null, 0.93198538, 46.60, 6152.91, 6.62, 0.00, null,
                -6.62, 971.30);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 133, 67, 50.00, 50.00, null, 0.93099125, 46.55, 6109.48, 6.57, 0.00, null,
                -6.57, 971.30);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 132, 68, 50.00, 50.00, null, 0.92999818, 46.50, 6066.00, 6.52, 0.00, null,
                -6.52, 971.30);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 131, 69, 50.00, 50.00, null, 0.92900618, 46.45, 6022.48, 6.48, 0.00, null,
                -6.48, 971.30);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 130, 70, 50.00, 50.00, null, 0.92801523, 46.40, 5978.91, 6.43, 0.00, null,
                -6.43, 971.30);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 129, 71, 50.00, 50.00, null, 0.92702534, 46.35, 5935.29, 6.38, 0.00, null,
                -6.38, 971.30);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 128, 72, 50.00, 50.00, null, 0.92603650, 46.30, 5891.63, 6.34, 0.00, null,
                -6.34, 971.30);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 127, 73, 50.00, 50.00, null, 0.92504872, 46.25, 5847.92, 6.29, 0.00, null,
                -6.29, 971.30);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 126, 74, 50.00, 50.00, null, 0.92406200, 46.20, 5804.17, 6.24, 0.00, null,
                -6.24, 971.30);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 125, 75, 50.00, 50.00, null, 0.92307632, 46.15, 5760.36, 6.20, 0.00, null,
                -6.20, 971.30);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 124, 76, 50.00, 50.00, null, 0.92209170, 46.10, 5716.52, 6.15, 0.00, null,
                -6.15, 971.30);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 123, 77, 50.00, 50.00, null, 0.92110813, 46.06, 5672.62, 6.10, 0.00, null,
                -6.10, 971.30);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 122, 78, 50.00, 50.00, null, 0.92012560, 46.01, 5628.68, 6.06, 0.00, null,
                -6.06, 971.30);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 121, 79, 50.00, 50.00, null, 0.91914413, 45.96, 5584.69, 6.01, 0.00, null,
                -6.01, 971.30);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 120, 80, 50.00, 50.00, null, 0.91816370, 45.91, 5540.65, 5.96, 0.00, null,
                -5.96, 971.30);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 119, 81, 50.00, 50.00, null, 0.91718432, 45.86, 5496.57, 5.92, 0.00, null,
                -5.92, 971.30);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 118, 82, 50.00, 50.00, null, 0.91620598, 45.81, 5452.44, 5.87, 0.00, null,
                -5.87, 971.30);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 117, 83, 50.00, 50.00, null, 0.91522868, 45.76, 5408.26, 5.82, 0.00, null,
                -5.82, 971.30);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 116, 84, 50.00, 50.00, null, 0.91425243, 45.71, 5364.03, 5.78, 0.00, null,
                -5.78, 971.30);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 115, 85, 50.00, 50.00, null, 0.91327722, 45.66, 5319.76, 5.73, 0.00, null,
                -5.73, 971.30);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 114, 86, 50.00, 50.00, null, 0.91230305, 45.62, 5275.44, 5.68, 0.00, null,
                -5.68, 971.30);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 113, 87, 50.00, 50.00, null, 0.91132992, 45.57, 5231.08, 5.63, 0.00, null,
                -5.63, 971.30);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 112, 88, 50.00, 50.00, null, 0.91035783, 45.52, 5186.66, 5.59, 0.00, null, -5.59,
                971.30);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 111, 89, 50.00, 50.00, null, 0.90938677, 45.47, 5142.20, 5.54, 0.00, null, -5.54,
                971.30);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 110, 90, 50.00, 50.00, null, 0.90841675, 45.42, 5097.69, 5.49, 0.00, null, -5.49,
                971.30);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 109, 91, 50.00, 50.00, null, 0.90744776, 45.37, 5053.13, 5.44, 0.00, null, -5.44,
                971.30);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 108, 92, 50.00, 50.00, null, 0.90647981, 45.32, 5008.53, 5.40, 0.00, null, -5.40,
                971.30);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 107, 93, 50.00, 50.00, null, 0.90551289, 45.28, 4963.88, 5.35, 0.00, null, -5.35,
                971.30);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 106, 94, 50.00, 50.00, null, 0.90454700, 45.23, 4919.18, 5.30, 0.00, null, -5.30,
                971.30);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 105, 95, 50.00, 50.00, null, 0.90358215, 45.18, 4874.43, 5.25, 0.00, null, -5.25,
                971.30);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 104, 96, 50.00, 50.00, null, 0.90261832, 45.13, 4829.64, 5.20, 0.00, null, -5.20,
                971.30);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 103, 97, 50.00, 50.00, null, 0.90165552, 45.08, 4784.79, 5.16, 0.00, null,
                -5.16, 971.30);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 102, 98, 50.00, 50.00, null, 0.90069374, 45.03, 4739.90, 5.11, 0.00, null,
                -5.11, 971.30);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 101, 99, 50.00, 50.00, null, 0.89973299, 44.99, 4694.96, 5.06, 0.00, null,
                -5.06, 971.30);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 100, 100, 50.00, 50.00, null, 0.89877327, 44.94, 4649.98, 5.01, 0.00, null,
                -5.01, 971.30);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 99, 101, 50.00, 50.00, null, 0.89781457, 44.89, 4604.94, 4.97, 0.00, null,
                -4.97, 971.30);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 98, 102, 50.00, 50.00, null, 0.89685689, 44.84, 4559.86, 4.92, 0.00, null,
                -4.92, 971.30);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 97, 103, 50.00, 50.00, null, 0.89590024, 44.80, 4514.73, 4.87, 0.00, null,
                -4.87, 971.30);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 96, 104, 50.00, 50.00, null, 0.89494460, 44.75, 4469.55, 4.82, 0.00, null,
                -4.82, 971.30);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 95, 105, 50.00, 50.00, null, 0.89398999, 44.70, 4424.32, 4.77, 0.00, null,
                -4.77, 971.30);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 94, 106, 50.00, 50.00, null, 0.89303639, 44.65, 4379.05, 4.72, 0.00, null,
                -4.72, 971.30);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 93, 107, 50.00, 50.00, null, 0.89208381, 44.60, 4333.72, 4.68, 0.00, null,
                -4.68, 971.30);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 92, 108, 50.00, 50.00, null, 0.89113225, 44.56, 4288.35, 4.63, 0.00, null,
                -4.63, 971.30);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 91, 109, 50.00, 50.00, null, 0.89018170, 44.51, 4242.93, 4.58, 0.00, null,
                -4.58, 971.30);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 90, 110, 50.00, 50.00, null, 0.88923216, 44.46, 4197.46, 4.53, 0.00, null,
                -4.53, 971.30);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 89, 111, 50.00, 50.00, null, 0.88828364, 44.41, 4151.94, 4.48, 0.00, null,
                -4.48, 971.30);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 88, 112, 50.00, 50.00, null, 0.88733613, 44.37, 4106.38, 4.43, 0.00, null,
                -4.43, 971.30);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 87, 113, 50.00, 50.00, null, 0.88638963, 44.32, 4060.76, 4.38, 0.00, null,
                -4.38, 971.30);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 86, 114, 50.00, 50.00, null, 0.88544414, 44.27, 4015.10, 4.34, 0.00, null,
                -4.34, 971.30);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 85, 115, 50.00, 50.00, null, 0.88449966, 44.22, 3969.38, 4.29, 0.00, null,
                -4.29, 971.30);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 84, 116, 50.00, 50.00, null, 0.88355619, 44.18, 3923.62, 4.24, 0.00, null,
                -4.24, 971.30);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 83, 117, 50.00, 50.00, null, 0.88261372, 44.13, 3877.81, 4.19, 0.00, null,
                -4.19, 971.30);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 82, 118, 50.00, 50.00, null, 0.88167226, 44.08, 3831.95, 4.14, 0.00, null,
                -4.14, 971.30);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 81, 119, 50.00, 50.00, null, 0.88073180, 44.04, 3786.04, 4.09, 0.00, null,
                -4.09, 971.30);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 80, 120, 50.00, 50.00, null, 0.87979234, 43.99, 3740.09, 4.04, 0.00, null,
                -4.04, 971.30);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 79, 121, 50.00, 50.00, null, 0.87885389, 43.94, 3694.08, 3.99, 0.00, null,
                -3.99, 971.30);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 78, 122, 50.00, 50.00, null, 0.87791644, 43.90, 3648.03, 3.94, 0.00, null,
                -3.94, 971.30);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 77, 123, 50.00, 50.00, null, 0.87697999, 43.85, 3601.92, 3.90, 0.00, null,
                -3.90, 971.30);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 76, 124, 50.00, 50.00, null, 0.87604453, 43.80, 3555.77, 3.85, 0.00, null,
                -3.85, 971.30);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 75, 125, 50.00, 50.00, null, 0.87511008, 43.76, 3509.56, 3.80, 0.00, null,
                -3.80, 971.30);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 74, 126, 50.00, 50.00, null, 0.87417662, 43.71, 3463.31, 3.75, 0.00, null,
                -3.75, 971.30);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 73, 127, 50.00, 50.00, null, 0.87324416, 43.66, 3417.01, 3.70, 0.00, null,
                -3.70, 971.30);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 72, 128, 50.00, 50.00, null, 0.87231269, 43.62, 3370.66, 3.65, 0.00, null,
                -3.65, 971.30);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 71, 129, 50.00, 50.00, null, 0.87138221, 43.57, 3324.26, 3.60, 0.00, null,
                -3.60, 971.30);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 70, 130, 50.00, 50.00, null, 0.87045273, 43.52, 3277.81, 3.55, 0.00, null,
                -3.55, 971.30);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 69, 131, 50.00, 50.00, null, 0.86952424, 43.48, 3231.31, 3.50, 0.00, null,
                -3.50, 971.30);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 68, 132, 50.00, 50.00, null, 0.86859674, 43.43, 3184.76, 3.45, 0.00, null,
                -3.45, 971.30);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 67, 133, 50.00, 50.00, null, 0.86767023, 43.38, 3138.16, 3.40, 0.00, null,
                -3.40, 971.30);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 66, 134, 50.00, 50.00, null, 0.86674471, 43.34, 3091.51, 3.35, 0.00, null,
                -3.35, 971.30);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 65, 135, 50.00, 50.00, null, 0.86582017, 43.29, 3044.81, 3.30, 0.00, null,
                -3.30, 971.30);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 64, 136, 50.00, 50.00, null, 0.86489662, 43.24, 2998.06, 3.25, 0.00, null,
                -3.25, 971.30);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 63, 137, 50.00, 50.00, null, 0.86397406, 43.20, 2951.26, 3.20, 0.00, null,
                -3.20, 971.30);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 62, 138, 50.00, 50.00, null, 0.86305248, 43.15, 2904.42, 3.15, 0.00, null,
                -3.15, 971.30);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 61, 139, 50.00, 50.00, null, 0.86213188, 43.11, 2857.52, 3.10, 0.00, null,
                -3.10, 971.30);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 60, 140, 50.00, 50.00, null, 0.86121227, 43.06, 2810.57, 3.05, 0.00, null,
                -3.05, 971.30);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 59, 141, 50.00, 50.00, null, 0.86029363, 43.01, 2763.57, 3.00, 0.00, null,
                -3.00, 971.30);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 58, 142, 50.00, 50.00, null, 0.85937598, 42.97, 2716.52, 2.95, 0.00, null,
                -2.95, 971.30);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 57, 143, 50.00, 50.00, null, 0.85845930, 42.92, 2669.42, 2.90, 0.00, null,
                -2.90, 971.30);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 56, 144, 50.00, 50.00, null, 0.85754361, 42.88, 2622.27, 2.85, 0.00, null,
                -2.85, 971.30);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 55, 145, 50.00, 50.00, null, 0.85662889, 42.83, 2575.07, 2.80, 0.00, null,
                -2.80, 971.30);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 54, 146, 50.00, 50.00, null, 0.85571514, 42.79, 2527.82, 2.75, 0.00, null,
                -2.75, 971.30);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 53, 147, 50.00, 50.00, null, 0.85480237, 42.74, 2480.52, 2.70, 0.00, null,
                -2.70, 971.30);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 52, 148, 50.00, 50.00, null, 0.85389057, 42.69, 2433.17, 2.65, 0.00, null,
                -2.65, 971.30);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 51, 149, 50.00, 50.00, null, 0.85297975, 42.65, 2385.77, 2.60, 0.00, null,
                -2.60, 971.30);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 50, 150, 50.00, 50.00, null, 0.85206990, 42.60, 2338.31, 2.55, 0.00, null,
                -2.55, 971.30);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 49, 151, 50.00, 50.00, null, 0.85116101, 42.56, 2290.81, 2.50, 0.00, null,
                -2.50, 971.30);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 48, 152, 50.00, 50.00, null, 0.85025310, 42.51, 2243.26, 2.45, 0.00, null,
                -2.45, 971.30);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 47, 153, 50.00, 50.00, null, 0.84934616, 42.47, 2195.65, 2.40, 0.00, null,
                -2.40, 971.30);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 46, 154, 50.00, 50.00, null, 0.84844018, 42.42, 2148.00, 2.34, 0.00, null,
                -2.34, 971.30);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 45, 155, 50.00, 50.00, null, 0.84753517, 42.38, 2100.29, 2.29, 0.00, null,
                -2.29, 971.30);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 44, 156, 50.00, 50.00, null, 0.84663113, 42.33, 2052.53, 2.24, 0.00, null,
                -2.24, 971.30);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 43, 157, 50.00, 50.00, null, 0.84572805, 42.29, 2004.73, 2.19, 0.00, null,
                -2.19, 971.30);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 42, 158, 50.00, 50.00, null, 0.84482593, 42.24, 1956.87, 2.14, 0.00, null,
                -2.14, 971.30);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 41, 159, 50.00, 50.00, null, 0.84392477, 42.20, 1908.96, 2.09, 0.00, null,
                -2.09, 971.30);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 40, 160, 50.00, 50.00, null, 0.84302458, 42.15, 1860.99, 2.04, 0.00, null,
                -2.04, 971.30);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 39, 161, 50.00, 50.00, null, 0.84212535, 42.11, 1812.98, 1.99, 0.00, null,
                -1.99, 971.30);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 38, 162, 50.00, 50.00, null, 0.84122707, 42.06, 1764.92, 1.94, 0.00, null,
                -1.94, 971.30);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 37, 163, 50.00, 50.00, null, 0.84032975, 42.02, 1716.80, 1.88, 0.00, null,
                -1.88, 971.30);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 36, 164, 50.00, 50.00, null, 0.83943340, 41.97, 1668.64, 1.83, 0.00, null,
                -1.83, 971.30);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 35, 165, 50.00, 50.00, null, 0.83853799, 41.93, 1620.42, 1.78, 0.00, null,
                -1.78, 971.30);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 34, 166, 50.00, 50.00, null, 0.83764354, 41.88, 1572.15, 1.73, 0.00, null,
                -1.73, 971.30);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 33, 167, 50.00, 50.00, null, 0.83675005, 41.84, 1523.83, 1.68, 0.00, null,
                -1.68, 971.30);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 32, 168, 50.00, 50.00, null, 0.83585751, 41.79, 1475.45, 1.63, 0.00, null,
                -1.63, 971.30);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 31, 169, 50.00, 50.00, null, 0.83496592, 41.75, 1427.03, 1.58, 0.00, null,
                -1.58, 971.30);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 30, 170, 50.00, 50.00, null, 0.83407528, 41.70, 1378.55, 1.52, 0.00, null,
                -1.52, 971.30);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 29, 171, 50.00, 50.00, null, 0.83318560, 41.66, 1330.02, 1.47, 0.00, null,
                -1.47, 971.30);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 28, 172, 50.00, 50.00, null, 0.83229686, 41.61, 1281.45, 1.42, 0.00, null,
                -1.42, 971.30);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 27, 173, 50.00, 50.00, null, 0.83140907, 41.57, 1232.81, 1.37, 0.00, null,
                -1.37, 971.30);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 26, 174, 50.00, 50.00, null, 0.83052222, 41.53, 1184.13, 1.32, 0.00, null,
                -1.32, 971.30);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 25, 175, 50.00, 50.00, null, 0.82963633, 41.48, 1135.39, 1.26, 0.00, null,
                -1.26, 971.30);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 24, 176, 50.00, 50.00, null, 0.82875137, 41.44, 1086.61, 1.21, 0.00, null,
                -1.21, 971.30);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 23, 177, 50.00, 50.00, null, 0.82786736, 41.39, 1037.77, 1.16, 0.00, null,
                -1.16, 971.30);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 22, 178, 50.00, 50.00, null, 0.82698430, 41.35, 988.88, 1.11, 0.00, null,
                -1.11, 971.30);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 21, 179, 50.00, 50.00, null, 0.82610217, 41.31, 939.93, 1.06, 0.00, null,
                -1.06, 971.30);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 20, 180, 50.00, 50.00, null, 0.82522099, 41.26, 890.93, 1.00, 0.00, null,
                -1.00, 971.30);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 19, 181, 50.00, 50.00, null, 0.82434075, 41.22, 841.89, 0.95, 0.00, null,
                -0.95, 971.30);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 18, 182, 50.00, 50.00, null, 0.82346144, 41.17, 792.79, 0.90, 0.00, null,
                -0.90, 971.30);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 17, 183, 50.00, 50.00, null, 0.82258308, 41.13, 743.63, 0.85, 0.00, null,
                -0.85, 971.30);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 16, 184, 50.00, 50.00, null, 0.82170565, 41.09, 694.43, 0.79, 0.00, null,
                -0.79, 971.30);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 15, 185, 50.00, 50.00, null, 0.82082916, 41.04, 645.17, 0.74, 0.00, null,
                -0.74, 971.30);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 14, 186, 50.00, 50.00, null, 0.81995360, 41.00, 595.86, 0.69, 0.00, null,
                -0.69, 971.30);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 13, 187, 50.00, 50.00, null, 0.81907897, 40.95, 546.49, 0.64, 0.00, null,
                -0.64, 971.30);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 12, 188, 50.00, 50.00, null, 0.81820528, 40.91, 497.08, 0.58, 0.00, null,
                -0.58, 971.30);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 11, 189, 50.00, 50.00, null, 0.81733252, 40.87, 447.61, 0.53, 0.00, null,
                -0.53, 971.30);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 10, 190, 50.00, 50.00, null, 0.81646069, 40.82, 398.08, 0.48, 0.00, null,
                -0.48, 971.30);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 9, 191, 50.00, 50.00, null, 0.81558979, 40.78, 348.51, 0.43, 0.00, null,
                -0.43, 971.30);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 8, 192, 50.00, 50.00, null, 0.81471983, 40.74, 298.88, 0.37, 0.00, null,
                -0.37, 971.30);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 7, 193, 50.00, 50.00, null, 0.81385078, 40.69, 249.20, 0.32, 0.00, null,
                -0.32, 971.30);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 6, 194, 50.00, 50.00, null, 0.81298267, 40.65, 199.47, 0.27, 0.00, null,
                -0.27, 971.30);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 5, 195, 50.00, 50.00, null, 0.81211548, 40.61, 149.68, 0.21, 0.00, null,
                -0.21, 971.30);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 4, 196, 50.00, 50.00, null, 0.81124922, 40.56, 99.84, 0.16, 0.00, null, -0.16,
                971.30);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 3, 197, 50.00, 50.00, null, 0.81038388, 40.52, 49.95, 0.11, 0.00, null, -0.11,
                971.30);

        assertEquals(200, model.payments().size(), "disbursement + 199 regular (period 200 removed, forecast was 0)");
    }

    @Test
    void testLessPayment_term200_originationFee1000_netDisbursement9000_pay40() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("40"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 201, 0, -9000.00, null, null, 1.00000000, -9000.00, 9000.00, null, null, null,
                null, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 200, 0, 50.00, 50.00, 40.00, 1.00000000, 40.00, 8959.61, 9.61, 7.69, 7.69, -1.92,
                992.31);

        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 199, 1, 50.00, 50.00, null, 0.99893332, 49.95, 8919.18, 9.57, 0.00, null, -9.57,
                992.31);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 198, 2, 50.00, 50.00, null, 0.99786779, 49.89, 8878.70, 9.52, 0.00, null, -9.52,
                992.31);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 197, 3, 50.00, 50.00, null, 0.99680339, 49.84, 8838.18, 9.48, 0.00, null, -9.48,
                992.31);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 196, 4, 50.00, 50.00, null, 0.99574012, 49.79, 8797.62, 9.44, 0.00, null, -9.44,
                992.31);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 195, 5, 50.00, 50.00, null, 0.99467799, 49.73, 8757.01, 9.39, 0.00, null, -9.39,
                992.31);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 194, 6, 50.00, 50.00, null, 0.99361699, 49.68, 8716.36, 9.35, 0.00, null, -9.35,
                992.31);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 193, 7, 50.00, 50.00, null, 0.99255712, 49.63, 8675.67, 9.31, 0.00, null, -9.31,
                992.31);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 192, 8, 50.00, 50.00, null, 0.99149839, 49.57, 8634.94, 9.26, 0.00, null, -9.26,
                992.31);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 191, 9, 50.00, 50.00, null, 0.99044078, 49.52, 8594.16, 9.22, 0.00, null, -9.22,
                992.31);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 190, 10, 50.00, 50.00, null, 0.98938430, 49.47, 8553.33, 9.18, 0.00, null,
                -9.18, 992.31);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 189, 11, 50.00, 50.00, null, 0.98832895, 49.42, 8512.47, 9.13, 0.00, null,
                -9.13, 992.31);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 188, 12, 50.00, 50.00, null, 0.98727472, 49.36, 8471.56, 9.09, 0.00, null,
                -9.09, 992.31);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 187, 13, 50.00, 50.00, null, 0.98622162, 49.31, 8430.60, 9.05, 0.00, null,
                -9.05, 992.31);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 186, 14, 50.00, 50.00, null, 0.98516964, 49.26, 8389.61, 9.00, 0.00, null,
                -9.00, 992.31);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 185, 15, 50.00, 50.00, null, 0.98411879, 49.21, 8348.56, 8.96, 0.00, null,
                -8.96, 992.31);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 184, 16, 50.00, 50.00, null, 0.98306905, 49.15, 8307.48, 8.91, 0.00, null,
                -8.91, 992.31);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 183, 17, 50.00, 50.00, null, 0.98202044, 49.10, 8266.35, 8.87, 0.00, null,
                -8.87, 992.31);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 182, 18, 50.00, 50.00, null, 0.98097294, 49.05, 8225.18, 8.83, 0.00, null,
                -8.83, 992.31);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 181, 19, 50.00, 50.00, null, 0.97992656, 49.00, 8183.96, 8.78, 0.00, null,
                -8.78, 992.31);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 180, 20, 50.00, 50.00, null, 0.97888129, 48.94, 8142.70, 8.74, 0.00, null,
                -8.74, 992.31);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 179, 21, 50.00, 50.00, null, 0.97783715, 48.89, 8101.39, 8.69, 0.00, null,
                -8.69, 992.31);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 178, 22, 50.00, 50.00, null, 0.97679411, 48.84, 8060.04, 8.65, 0.00, null,
                -8.65, 992.31);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 177, 23, 50.00, 50.00, null, 0.97575219, 48.79, 8018.65, 8.61, 0.00, null,
                -8.61, 992.31);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 176, 24, 50.00, 50.00, null, 0.97471138, 48.74, 7977.21, 8.56, 0.00, null,
                -8.56, 992.31);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 175, 25, 50.00, 50.00, null, 0.97367168, 48.68, 7935.73, 8.52, 0.00, null,
                -8.52, 992.31);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 174, 26, 50.00, 50.00, null, 0.97263309, 48.63, 7894.21, 8.47, 0.00, null,
                -8.47, 992.31);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 173, 27, 50.00, 50.00, null, 0.97159560, 48.58, 7852.63, 8.43, 0.00, null,
                -8.43, 992.31);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 172, 28, 50.00, 50.00, null, 0.97055922, 48.53, 7811.02, 8.39, 0.00, null,
                -8.39, 992.31);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 171, 29, 50.00, 50.00, null, 0.96952395, 48.48, 7769.36, 8.34, 0.00, null,
                -8.34, 992.31);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 170, 30, 50.00, 50.00, null, 0.96848979, 48.42, 7727.66, 8.30, 0.00, null, -8.30,
                992.31);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 169, 31, 50.00, 50.00, null, 0.96745672, 48.37, 7685.91, 8.25, 0.00, null, -8.25,
                992.31);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 168, 32, 50.00, 50.00, null, 0.96642476, 48.32, 7644.12, 8.21, 0.00, null, -8.21,
                992.31);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 167, 33, 50.00, 50.00, null, 0.96539390, 48.27, 7602.28, 8.16, 0.00, null, -8.16,
                992.31);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 166, 34, 50.00, 50.00, null, 0.96436413, 48.22, 7560.40, 8.12, 0.00, null, -8.12,
                992.31);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 165, 35, 50.00, 50.00, null, 0.96333547, 48.17, 7518.47, 8.07, 0.00, null, -8.07,
                992.31);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 164, 36, 50.00, 50.00, null, 0.96230790, 48.12, 7476.50, 8.03, 0.00, null, -8.03,
                992.31);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 163, 37, 50.00, 50.00, null, 0.96128143, 48.06, 7434.48, 7.98, 0.00, null, -7.98,
                992.31);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 162, 38, 50.00, 50.00, null, 0.96025606, 48.01, 7392.42, 7.94, 0.00, null, -7.94,
                992.31);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 161, 39, 50.00, 50.00, null, 0.95923178, 47.96, 7350.31, 7.89, 0.00, null,
                -7.89, 992.31);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 160, 40, 50.00, 50.00, null, 0.95820859, 47.91, 7308.16, 7.85, 0.00, null,
                -7.85, 992.31);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 159, 41, 50.00, 50.00, null, 0.95718649, 47.86, 7265.97, 7.80, 0.00, null,
                -7.80, 992.31);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 158, 42, 50.00, 50.00, null, 0.95616548, 47.81, 7223.72, 7.76, 0.00, null,
                -7.76, 992.31);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 157, 43, 50.00, 50.00, null, 0.95514557, 47.76, 7181.44, 7.71, 0.00, null,
                -7.71, 992.31);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 156, 44, 50.00, 50.00, null, 0.95412674, 47.71, 7139.11, 7.67, 0.00, null,
                -7.67, 992.31);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 155, 45, 50.00, 50.00, null, 0.95310899, 47.66, 7096.73, 7.62, 0.00, null,
                -7.62, 992.31);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 154, 46, 50.00, 50.00, null, 0.95209233, 47.60, 7054.31, 7.58, 0.00, null,
                -7.58, 992.31);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 153, 47, 50.00, 50.00, null, 0.95107676, 47.55, 7011.84, 7.53, 0.00, null,
                -7.53, 992.31);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 152, 48, 50.00, 50.00, null, 0.95006227, 47.50, 6969.33, 7.49, 0.00, null,
                -7.49, 992.31);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 151, 49, 50.00, 50.00, null, 0.94904886, 47.45, 6926.77, 7.44, 0.00, null,
                -7.44, 992.31);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 150, 50, 50.00, 50.00, null, 0.94803653, 47.40, 6884.17, 7.40, 0.00, null,
                -7.40, 992.31);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 149, 51, 50.00, 50.00, null, 0.94702529, 47.35, 6841.52, 7.35, 0.00, null,
                -7.35, 992.31);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 148, 52, 50.00, 50.00, null, 0.94601512, 47.30, 6798.82, 7.31, 0.00, null,
                -7.31, 992.31);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 147, 53, 50.00, 50.00, null, 0.94500603, 47.25, 6756.08, 7.26, 0.00, null,
                -7.26, 992.31);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 146, 54, 50.00, 50.00, null, 0.94399801, 47.20, 6713.30, 7.21, 0.00, null,
                -7.21, 992.31);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 145, 55, 50.00, 50.00, null, 0.94299107, 47.15, 6670.47, 7.17, 0.00, null,
                -7.17, 992.31);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 144, 56, 50.00, 50.00, null, 0.94198521, 47.10, 6627.59, 7.12, 0.00, null,
                -7.12, 992.31);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 143, 57, 50.00, 50.00, null, 0.94098042, 47.05, 6584.67, 7.08, 0.00, null,
                -7.08, 992.31);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 142, 58, 50.00, 50.00, null, 0.93997669, 47.00, 6541.70, 7.03, 0.00, null, -7.03,
                992.31);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 141, 59, 50.00, 50.00, null, 0.93897404, 46.95, 6498.68, 6.99, 0.00, null, -6.99,
                992.31);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 140, 60, 50.00, 50.00, null, 0.93797246, 46.90, 6455.62, 6.94, 0.00, null, -6.94,
                992.31);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 139, 61, 50.00, 50.00, null, 0.93697195, 46.85, 6412.51, 6.89, 0.00, null, -6.89,
                992.31);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 138, 62, 50.00, 50.00, null, 0.93597251, 46.80, 6369.36, 6.85, 0.00, null, -6.85,
                992.31);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 137, 63, 50.00, 50.00, null, 0.93497413, 46.75, 6326.16, 6.80, 0.00, null, -6.80,
                992.31);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 136, 64, 50.00, 50.00, null, 0.93397681, 46.70, 6282.92, 6.76, 0.00, null, -6.76,
                992.31);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 135, 65, 50.00, 50.00, null, 0.93298056, 46.65, 6239.63, 6.71, 0.00, null, -6.71,
                992.31);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 134, 66, 50.00, 50.00, null, 0.93198538, 46.60, 6196.29, 6.66, 0.00, null, -6.66,
                992.31);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 133, 67, 50.00, 50.00, null, 0.93099125, 46.55, 6152.91, 6.62, 0.00, null,
                -6.62, 992.31);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 132, 68, 50.00, 50.00, null, 0.92999818, 46.50, 6109.48, 6.57, 0.00, null,
                -6.57, 992.31);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 131, 69, 50.00, 50.00, null, 0.92900618, 46.45, 6066.00, 6.52, 0.00, null,
                -6.52, 992.31);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 130, 70, 50.00, 50.00, null, 0.92801523, 46.40, 6022.48, 6.48, 0.00, null,
                -6.48, 992.31);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 129, 71, 50.00, 50.00, null, 0.92702534, 46.35, 5978.91, 6.43, 0.00, null,
                -6.43, 992.31);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 128, 72, 50.00, 50.00, null, 0.92603650, 46.30, 5935.29, 6.38, 0.00, null,
                -6.38, 992.31);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 127, 73, 50.00, 50.00, null, 0.92504872, 46.25, 5891.63, 6.34, 0.00, null,
                -6.34, 992.31);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 126, 74, 50.00, 50.00, null, 0.92406200, 46.20, 5847.92, 6.29, 0.00, null,
                -6.29, 992.31);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 125, 75, 50.00, 50.00, null, 0.92307632, 46.15, 5804.17, 6.24, 0.00, null,
                -6.24, 992.31);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 124, 76, 50.00, 50.00, null, 0.92209170, 46.10, 5760.36, 6.20, 0.00, null,
                -6.20, 992.31);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 123, 77, 50.00, 50.00, null, 0.92110813, 46.06, 5716.52, 6.15, 0.00, null,
                -6.15, 992.31);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 122, 78, 50.00, 50.00, null, 0.92012560, 46.01, 5672.62, 6.10, 0.00, null,
                -6.10, 992.31);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 121, 79, 50.00, 50.00, null, 0.91914413, 45.96, 5628.68, 6.06, 0.00, null,
                -6.06, 992.31);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 120, 80, 50.00, 50.00, null, 0.91816370, 45.91, 5584.69, 6.01, 0.00, null,
                -6.01, 992.31);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 119, 81, 50.00, 50.00, null, 0.91718432, 45.86, 5540.65, 5.96, 0.00, null,
                -5.96, 992.31);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 118, 82, 50.00, 50.00, null, 0.91620598, 45.81, 5496.57, 5.92, 0.00, null,
                -5.92, 992.31);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 117, 83, 50.00, 50.00, null, 0.91522868, 45.76, 5452.44, 5.87, 0.00, null,
                -5.87, 992.31);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 116, 84, 50.00, 50.00, null, 0.91425243, 45.71, 5408.26, 5.82, 0.00, null,
                -5.82, 992.31);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 115, 85, 50.00, 50.00, null, 0.91327722, 45.66, 5364.03, 5.78, 0.00, null,
                -5.78, 992.31);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 114, 86, 50.00, 50.00, null, 0.91230305, 45.62, 5319.76, 5.73, 0.00, null,
                -5.73, 992.31);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 113, 87, 50.00, 50.00, null, 0.91132992, 45.57, 5275.44, 5.68, 0.00, null,
                -5.68, 992.31);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 112, 88, 50.00, 50.00, null, 0.91035783, 45.52, 5231.08, 5.63, 0.00, null,
                -5.63, 992.31);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 111, 89, 50.00, 50.00, null, 0.90938677, 45.47, 5186.66, 5.59, 0.00, null, -5.59,
                992.31);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 110, 90, 50.00, 50.00, null, 0.90841675, 45.42, 5142.20, 5.54, 0.00, null, -5.54,
                992.31);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 109, 91, 50.00, 50.00, null, 0.90744776, 45.37, 5097.69, 5.49, 0.00, null, -5.49,
                992.31);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 108, 92, 50.00, 50.00, null, 0.90647981, 45.32, 5053.13, 5.44, 0.00, null, -5.44,
                992.31);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 107, 93, 50.00, 50.00, null, 0.90551289, 45.28, 5008.53, 5.40, 0.00, null, -5.40,
                992.31);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 106, 94, 50.00, 50.00, null, 0.90454700, 45.23, 4963.88, 5.35, 0.00, null, -5.35,
                992.31);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 105, 95, 50.00, 50.00, null, 0.90358215, 45.18, 4919.18, 5.30, 0.00, null, -5.30,
                992.31);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 104, 96, 50.00, 50.00, null, 0.90261832, 45.13, 4874.43, 5.25, 0.00, null, -5.25,
                992.31);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 103, 97, 50.00, 50.00, null, 0.90165552, 45.08, 4829.64, 5.20, 0.00, null, -5.20,
                992.31);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 102, 98, 50.00, 50.00, null, 0.90069374, 45.03, 4784.79, 5.16, 0.00, null,
                -5.16, 992.31);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 101, 99, 50.00, 50.00, null, 0.89973299, 44.99, 4739.90, 5.11, 0.00, null,
                -5.11, 992.31);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 100, 100, 50.00, 50.00, null, 0.89877327, 44.94, 4694.96, 5.06, 0.00, null,
                -5.06, 992.31);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 99, 101, 50.00, 50.00, null, 0.89781457, 44.89, 4649.98, 5.01, 0.00, null,
                -5.01, 992.31);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 98, 102, 50.00, 50.00, null, 0.89685689, 44.84, 4604.94, 4.97, 0.00, null,
                -4.97, 992.31);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 97, 103, 50.00, 50.00, null, 0.89590024, 44.80, 4559.86, 4.92, 0.00, null,
                -4.92, 992.31);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 96, 104, 50.00, 50.00, null, 0.89494460, 44.75, 4514.73, 4.87, 0.00, null,
                -4.87, 992.31);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 95, 105, 50.00, 50.00, null, 0.89398999, 44.70, 4469.55, 4.82, 0.00, null,
                -4.82, 992.31);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 94, 106, 50.00, 50.00, null, 0.89303639, 44.65, 4424.32, 4.77, 0.00, null,
                -4.77, 992.31);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 93, 107, 50.00, 50.00, null, 0.89208381, 44.60, 4379.05, 4.72, 0.00, null,
                -4.72, 992.31);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 92, 108, 50.00, 50.00, null, 0.89113225, 44.56, 4333.72, 4.68, 0.00, null,
                -4.68, 992.31);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 91, 109, 50.00, 50.00, null, 0.89018170, 44.51, 4288.35, 4.63, 0.00, null,
                -4.63, 992.31);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 90, 110, 50.00, 50.00, null, 0.88923216, 44.46, 4242.93, 4.58, 0.00, null,
                -4.58, 992.31);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 89, 111, 50.00, 50.00, null, 0.88828364, 44.41, 4197.46, 4.53, 0.00, null,
                -4.53, 992.31);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 88, 112, 50.00, 50.00, null, 0.88733613, 44.37, 4151.94, 4.48, 0.00, null,
                -4.48, 992.31);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 87, 113, 50.00, 50.00, null, 0.88638963, 44.32, 4106.38, 4.43, 0.00, null,
                -4.43, 992.31);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 86, 114, 50.00, 50.00, null, 0.88544414, 44.27, 4060.76, 4.38, 0.00, null,
                -4.38, 992.31);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 85, 115, 50.00, 50.00, null, 0.88449966, 44.22, 4015.10, 4.34, 0.00, null,
                -4.34, 992.31);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 84, 116, 50.00, 50.00, null, 0.88355619, 44.18, 3969.38, 4.29, 0.00, null,
                -4.29, 992.31);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 83, 117, 50.00, 50.00, null, 0.88261372, 44.13, 3923.62, 4.24, 0.00, null,
                -4.24, 992.31);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 82, 118, 50.00, 50.00, null, 0.88167226, 44.08, 3877.81, 4.19, 0.00, null,
                -4.19, 992.31);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 81, 119, 50.00, 50.00, null, 0.88073180, 44.04, 3831.95, 4.14, 0.00, null,
                -4.14, 992.31);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 80, 120, 50.00, 50.00, null, 0.87979234, 43.99, 3786.04, 4.09, 0.00, null,
                -4.09, 992.31);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 79, 121, 50.00, 50.00, null, 0.87885389, 43.94, 3740.09, 4.04, 0.00, null,
                -4.04, 992.31);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 78, 122, 50.00, 50.00, null, 0.87791644, 43.90, 3694.08, 3.99, 0.00, null,
                -3.99, 992.31);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 77, 123, 50.00, 50.00, null, 0.87697999, 43.85, 3648.03, 3.94, 0.00, null,
                -3.94, 992.31);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 76, 124, 50.00, 50.00, null, 0.87604453, 43.80, 3601.92, 3.90, 0.00, null,
                -3.90, 992.31);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 75, 125, 50.00, 50.00, null, 0.87511008, 43.76, 3555.77, 3.85, 0.00, null,
                -3.85, 992.31);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 74, 126, 50.00, 50.00, null, 0.87417662, 43.71, 3509.56, 3.80, 0.00, null,
                -3.80, 992.31);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 73, 127, 50.00, 50.00, null, 0.87324416, 43.66, 3463.31, 3.75, 0.00, null,
                -3.75, 992.31);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 72, 128, 50.00, 50.00, null, 0.87231269, 43.62, 3417.01, 3.70, 0.00, null,
                -3.70, 992.31);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 71, 129, 50.00, 50.00, null, 0.87138221, 43.57, 3370.66, 3.65, 0.00, null,
                -3.65, 992.31);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 70, 130, 50.00, 50.00, null, 0.87045273, 43.52, 3324.26, 3.60, 0.00, null,
                -3.60, 992.31);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 69, 131, 50.00, 50.00, null, 0.86952424, 43.48, 3277.81, 3.55, 0.00, null,
                -3.55, 992.31);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 68, 132, 50.00, 50.00, null, 0.86859674, 43.43, 3231.31, 3.50, 0.00, null,
                -3.50, 992.31);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 67, 133, 50.00, 50.00, null, 0.86767023, 43.38, 3184.76, 3.45, 0.00, null,
                -3.45, 992.31);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 66, 134, 50.00, 50.00, null, 0.86674471, 43.34, 3138.16, 3.40, 0.00, null,
                -3.40, 992.31);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 65, 135, 50.00, 50.00, null, 0.86582017, 43.29, 3091.51, 3.35, 0.00, null,
                -3.35, 992.31);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 64, 136, 50.00, 50.00, null, 0.86489662, 43.24, 3044.81, 3.30, 0.00, null,
                -3.30, 992.31);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 63, 137, 50.00, 50.00, null, 0.86397406, 43.20, 2998.06, 3.25, 0.00, null,
                -3.25, 992.31);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 62, 138, 50.00, 50.00, null, 0.86305248, 43.15, 2951.26, 3.20, 0.00, null,
                -3.20, 992.31);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 61, 139, 50.00, 50.00, null, 0.86213188, 43.11, 2904.42, 3.15, 0.00, null,
                -3.15, 992.31);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 60, 140, 50.00, 50.00, null, 0.86121227, 43.06, 2857.52, 3.10, 0.00, null,
                -3.10, 992.31);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 59, 141, 50.00, 50.00, null, 0.86029363, 43.01, 2810.57, 3.05, 0.00, null,
                -3.05, 992.31);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 58, 142, 50.00, 50.00, null, 0.85937598, 42.97, 2763.57, 3.00, 0.00, null,
                -3.00, 992.31);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 57, 143, 50.00, 50.00, null, 0.85845930, 42.92, 2716.52, 2.95, 0.00, null,
                -2.95, 992.31);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 56, 144, 50.00, 50.00, null, 0.85754361, 42.88, 2669.42, 2.90, 0.00, null,
                -2.90, 992.31);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 55, 145, 50.00, 50.00, null, 0.85662889, 42.83, 2622.27, 2.85, 0.00, null,
                -2.85, 992.31);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 54, 146, 50.00, 50.00, null, 0.85571514, 42.79, 2575.07, 2.80, 0.00, null,
                -2.80, 992.31);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 53, 147, 50.00, 50.00, null, 0.85480237, 42.74, 2527.82, 2.75, 0.00, null,
                -2.75, 992.31);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 52, 148, 50.00, 50.00, null, 0.85389057, 42.69, 2480.52, 2.70, 0.00, null,
                -2.70, 992.31);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 51, 149, 50.00, 50.00, null, 0.85297975, 42.65, 2433.17, 2.65, 0.00, null,
                -2.65, 992.31);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 50, 150, 50.00, 50.00, null, 0.85206990, 42.60, 2385.77, 2.60, 0.00, null,
                -2.60, 992.31);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 49, 151, 50.00, 50.00, null, 0.85116101, 42.56, 2338.31, 2.55, 0.00, null,
                -2.55, 992.31);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 48, 152, 50.00, 50.00, null, 0.85025310, 42.51, 2290.81, 2.50, 0.00, null,
                -2.50, 992.31);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 47, 153, 50.00, 50.00, null, 0.84934616, 42.47, 2243.26, 2.45, 0.00, null,
                -2.45, 992.31);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 46, 154, 50.00, 50.00, null, 0.84844018, 42.42, 2195.65, 2.40, 0.00, null,
                -2.40, 992.31);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 45, 155, 50.00, 50.00, null, 0.84753517, 42.38, 2148.00, 2.34, 0.00, null,
                -2.34, 992.31);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 44, 156, 50.00, 50.00, null, 0.84663113, 42.33, 2100.29, 2.29, 0.00, null,
                -2.29, 992.31);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 43, 157, 50.00, 50.00, null, 0.84572805, 42.29, 2052.53, 2.24, 0.00, null,
                -2.24, 992.31);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 42, 158, 50.00, 50.00, null, 0.84482593, 42.24, 2004.73, 2.19, 0.00, null,
                -2.19, 992.31);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 41, 159, 50.00, 50.00, null, 0.84392477, 42.20, 1956.87, 2.14, 0.00, null,
                -2.14, 992.31);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 40, 160, 50.00, 50.00, null, 0.84302458, 42.15, 1908.96, 2.09, 0.00, null,
                -2.09, 992.31);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 39, 161, 50.00, 50.00, null, 0.84212535, 42.11, 1860.99, 2.04, 0.00, null,
                -2.04, 992.31);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 38, 162, 50.00, 50.00, null, 0.84122707, 42.06, 1812.98, 1.99, 0.00, null,
                -1.99, 992.31);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 37, 163, 50.00, 50.00, null, 0.84032975, 42.02, 1764.92, 1.94, 0.00, null,
                -1.94, 992.31);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 36, 164, 50.00, 50.00, null, 0.83943340, 41.97, 1716.80, 1.88, 0.00, null,
                -1.88, 992.31);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 35, 165, 50.00, 50.00, null, 0.83853799, 41.93, 1668.64, 1.83, 0.00, null,
                -1.83, 992.31);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 34, 166, 50.00, 50.00, null, 0.83764354, 41.88, 1620.42, 1.78, 0.00, null,
                -1.78, 992.31);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 33, 167, 50.00, 50.00, null, 0.83675005, 41.84, 1572.15, 1.73, 0.00, null,
                -1.73, 992.31);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 32, 168, 50.00, 50.00, null, 0.83585751, 41.79, 1523.83, 1.68, 0.00, null,
                -1.68, 992.31);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 31, 169, 50.00, 50.00, null, 0.83496592, 41.75, 1475.45, 1.63, 0.00, null,
                -1.63, 992.31);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 30, 170, 50.00, 50.00, null, 0.83407528, 41.70, 1427.03, 1.58, 0.00, null,
                -1.58, 992.31);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 29, 171, 50.00, 50.00, null, 0.83318560, 41.66, 1378.55, 1.52, 0.00, null,
                -1.52, 992.31);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 28, 172, 50.00, 50.00, null, 0.83229686, 41.61, 1330.02, 1.47, 0.00, null,
                -1.47, 992.31);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 27, 173, 50.00, 50.00, null, 0.83140907, 41.57, 1281.45, 1.42, 0.00, null,
                -1.42, 992.31);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 26, 174, 50.00, 50.00, null, 0.83052222, 41.53, 1232.81, 1.37, 0.00, null,
                -1.37, 992.31);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 25, 175, 50.00, 50.00, null, 0.82963633, 41.48, 1184.13, 1.32, 0.00, null,
                -1.32, 992.31);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 24, 176, 50.00, 50.00, null, 0.82875137, 41.44, 1135.39, 1.26, 0.00, null,
                -1.26, 992.31);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 23, 177, 50.00, 50.00, null, 0.82786736, 41.39, 1086.61, 1.21, 0.00, null,
                -1.21, 992.31);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 22, 178, 50.00, 50.00, null, 0.82698430, 41.35, 1037.77, 1.16, 0.00, null,
                -1.16, 992.31);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 21, 179, 50.00, 50.00, null, 0.82610217, 41.31, 988.88, 1.11, 0.00, null,
                -1.11, 992.31);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 20, 180, 50.00, 50.00, null, 0.82522099, 41.26, 939.93, 1.06, 0.00, null,
                -1.06, 992.31);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 19, 181, 50.00, 50.00, null, 0.82434075, 41.22, 890.93, 1.00, 0.00, null,
                -1.00, 992.31);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 18, 182, 50.00, 50.00, null, 0.82346144, 41.17, 841.89, 0.95, 0.00, null,
                -0.95, 992.31);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 17, 183, 50.00, 50.00, null, 0.82258308, 41.13, 792.79, 0.90, 0.00, null,
                -0.90, 992.31);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 16, 184, 50.00, 50.00, null, 0.82170565, 41.09, 743.63, 0.85, 0.00, null,
                -0.85, 992.31);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 15, 185, 50.00, 50.00, null, 0.82082916, 41.04, 694.43, 0.79, 0.00, null,
                -0.79, 992.31);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 14, 186, 50.00, 50.00, null, 0.81995360, 41.00, 645.17, 0.74, 0.00, null,
                -0.74, 992.31);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 13, 187, 50.00, 50.00, null, 0.81907897, 40.95, 595.86, 0.69, 0.00, null,
                -0.69, 992.31);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 12, 188, 50.00, 50.00, null, 0.81820528, 40.91, 546.49, 0.64, 0.00, null,
                -0.64, 992.31);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 11, 189, 50.00, 50.00, null, 0.81733252, 40.87, 497.08, 0.58, 0.00, null,
                -0.58, 992.31);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 10, 190, 50.00, 50.00, null, 0.81646069, 40.82, 447.61, 0.53, 0.00, null,
                -0.53, 992.31);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 9, 191, 50.00, 50.00, null, 0.81558979, 40.78, 398.08, 0.48, 0.00, null,
                -0.48, 992.31);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 8, 192, 50.00, 50.00, null, 0.81471983, 40.74, 348.51, 0.43, 0.00, null,
                -0.43, 992.31);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 7, 193, 50.00, 50.00, null, 0.81385078, 40.69, 298.88, 0.37, 0.00, null,
                -0.37, 992.31);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 6, 194, 50.00, 50.00, null, 0.81298267, 40.65, 249.20, 0.32, 0.00, null,
                -0.32, 992.31);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 5, 195, 50.00, 50.00, null, 0.81211548, 40.61, 199.47, 0.27, 0.00, null,
                -0.27, 992.31);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 4, 196, 50.00, 50.00, null, 0.81124922, 40.56, 149.68, 0.21, 0.00, null,
                -0.21, 992.31);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 3, 197, 50.00, 50.00, null, 0.81038388, 40.52, 99.84, 0.16, 0.00, null, -0.16,
                992.31);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 2, 198, 50.00, 50.00, null, 0.80951946, 40.48, 49.95, 0.11, 0.00, null, -0.11,
                992.31);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 1, 199, 50.00, 50.00, null, 0.80865597, 40.43, 0.00, 0.05, 0.00, null, -0.05,
                992.31);

        assertEquals(202, model.payments().size(), "disbursement + 200 regular + 1 additional");
        checkInst(model, 201, 201, LocalDate.of(2019, 7, 21), 0, 200, null, 10.00, null, 0.80779339, 8.08, null, null, 0.00, null, null,
                null);
    }

    @Test
    void testNoPayment_term200_originationFee1000_netDisbursement9000_pay0_0_50() {
        final ProjectedAmortizationScheduleModel model = generateModel();
        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(1), BigDecimal.ZERO);
        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(2), BigDecimal.ZERO);
        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(3), new BigDecimal("50"));

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 203, 0, -9000.00, null, null, 1.00000000, -9000.00, 9000.00, null, null, null,
                null, 1000.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 202, 0, 50.00, 50.00, 0.00, 1.00000000, 0.00, 8959.61, 9.61, 9.61, 0.00, -9.61,
                1000.00);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 201, 0, 50.00, 50.00, 0.00, 1.00000000, 0.00, 8919.18, 9.57, 9.61, 0.00, -9.57,
                1000.00);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 200, 0, 50.00, 50.00, 50.00, 1.00000000, 50.00, 8878.70, 9.52, 9.61, 9.61, 0.09,
                990.39);

        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 199, 1, 50.00, 50.00, null, 0.99893332, 49.95, 8838.18, 9.48, 0.00, null, -9.48,
                990.39);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 198, 2, 50.00, 50.00, null, 0.99786779, 49.89, 8797.62, 9.44, 0.00, null, -9.44,
                990.39);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 197, 3, 50.00, 50.00, null, 0.99680339, 49.84, 8757.01, 9.39, 0.00, null, -9.39,
                990.39);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 196, 4, 50.00, 50.00, null, 0.99574012, 49.79, 8716.36, 9.35, 0.00, null, -9.35,
                990.39);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 195, 5, 50.00, 50.00, null, 0.99467799, 49.73, 8675.67, 9.31, 0.00, null, -9.31,
                990.39);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 194, 6, 50.00, 50.00, null, 0.99361699, 49.68, 8634.94, 9.26, 0.00, null, -9.26,
                990.39);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 193, 7, 50.00, 50.00, null, 0.99255712, 49.63, 8594.16, 9.22, 0.00, null, -9.22,
                990.39);
        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 192, 8, 50.00, 50.00, null, 0.99149839, 49.57, 8553.33, 9.18, 0.00, null, -9.18,
                990.39);
        checkInst(model, 12, 12, LocalDate.of(2019, 1, 13), 191, 9, 50.00, 50.00, null, 0.99044078, 49.52, 8512.47, 9.13, 0.00, null, -9.13,
                990.39);
        checkInst(model, 13, 13, LocalDate.of(2019, 1, 14), 190, 10, 50.00, 50.00, null, 0.98938430, 49.47, 8471.56, 9.09, 0.00, null,
                -9.09, 990.39);
        checkInst(model, 14, 14, LocalDate.of(2019, 1, 15), 189, 11, 50.00, 50.00, null, 0.98832895, 49.42, 8430.60, 9.05, 0.00, null,
                -9.05, 990.39);
        checkInst(model, 15, 15, LocalDate.of(2019, 1, 16), 188, 12, 50.00, 50.00, null, 0.98727472, 49.36, 8389.61, 9.00, 0.00, null,
                -9.00, 990.39);
        checkInst(model, 16, 16, LocalDate.of(2019, 1, 17), 187, 13, 50.00, 50.00, null, 0.98622162, 49.31, 8348.56, 8.96, 0.00, null,
                -8.96, 990.39);
        checkInst(model, 17, 17, LocalDate.of(2019, 1, 18), 186, 14, 50.00, 50.00, null, 0.98516964, 49.26, 8307.48, 8.91, 0.00, null,
                -8.91, 990.39);
        checkInst(model, 18, 18, LocalDate.of(2019, 1, 19), 185, 15, 50.00, 50.00, null, 0.98411879, 49.21, 8266.35, 8.87, 0.00, null,
                -8.87, 990.39);
        checkInst(model, 19, 19, LocalDate.of(2019, 1, 20), 184, 16, 50.00, 50.00, null, 0.98306905, 49.15, 8225.18, 8.83, 0.00, null,
                -8.83, 990.39);
        checkInst(model, 20, 20, LocalDate.of(2019, 1, 21), 183, 17, 50.00, 50.00, null, 0.98202044, 49.10, 8183.96, 8.78, 0.00, null,
                -8.78, 990.39);
        checkInst(model, 21, 21, LocalDate.of(2019, 1, 22), 182, 18, 50.00, 50.00, null, 0.98097294, 49.05, 8142.70, 8.74, 0.00, null,
                -8.74, 990.39);
        checkInst(model, 22, 22, LocalDate.of(2019, 1, 23), 181, 19, 50.00, 50.00, null, 0.97992656, 49.00, 8101.39, 8.69, 0.00, null,
                -8.69, 990.39);
        checkInst(model, 23, 23, LocalDate.of(2019, 1, 24), 180, 20, 50.00, 50.00, null, 0.97888129, 48.94, 8060.04, 8.65, 0.00, null,
                -8.65, 990.39);
        checkInst(model, 24, 24, LocalDate.of(2019, 1, 25), 179, 21, 50.00, 50.00, null, 0.97783715, 48.89, 8018.65, 8.61, 0.00, null,
                -8.61, 990.39);
        checkInst(model, 25, 25, LocalDate.of(2019, 1, 26), 178, 22, 50.00, 50.00, null, 0.97679411, 48.84, 7977.21, 8.56, 0.00, null,
                -8.56, 990.39);
        checkInst(model, 26, 26, LocalDate.of(2019, 1, 27), 177, 23, 50.00, 50.00, null, 0.97575219, 48.79, 7935.73, 8.52, 0.00, null,
                -8.52, 990.39);
        checkInst(model, 27, 27, LocalDate.of(2019, 1, 28), 176, 24, 50.00, 50.00, null, 0.97471138, 48.74, 7894.21, 8.47, 0.00, null,
                -8.47, 990.39);
        checkInst(model, 28, 28, LocalDate.of(2019, 1, 29), 175, 25, 50.00, 50.00, null, 0.97367168, 48.68, 7852.63, 8.43, 0.00, null,
                -8.43, 990.39);
        checkInst(model, 29, 29, LocalDate.of(2019, 1, 30), 174, 26, 50.00, 50.00, null, 0.97263309, 48.63, 7811.02, 8.39, 0.00, null,
                -8.39, 990.39);
        checkInst(model, 30, 30, LocalDate.of(2019, 1, 31), 173, 27, 50.00, 50.00, null, 0.97159560, 48.58, 7769.36, 8.34, 0.00, null,
                -8.34, 990.39);
        checkInst(model, 31, 31, LocalDate.of(2019, 2, 1), 172, 28, 50.00, 50.00, null, 0.97055922, 48.53, 7727.66, 8.30, 0.00, null, -8.30,
                990.39);
        checkInst(model, 32, 32, LocalDate.of(2019, 2, 2), 171, 29, 50.00, 50.00, null, 0.96952395, 48.48, 7685.91, 8.25, 0.00, null, -8.25,
                990.39);
        checkInst(model, 33, 33, LocalDate.of(2019, 2, 3), 170, 30, 50.00, 50.00, null, 0.96848979, 48.42, 7644.12, 8.21, 0.00, null, -8.21,
                990.39);
        checkInst(model, 34, 34, LocalDate.of(2019, 2, 4), 169, 31, 50.00, 50.00, null, 0.96745672, 48.37, 7602.28, 8.16, 0.00, null, -8.16,
                990.39);
        checkInst(model, 35, 35, LocalDate.of(2019, 2, 5), 168, 32, 50.00, 50.00, null, 0.96642476, 48.32, 7560.40, 8.12, 0.00, null, -8.12,
                990.39);
        checkInst(model, 36, 36, LocalDate.of(2019, 2, 6), 167, 33, 50.00, 50.00, null, 0.96539390, 48.27, 7518.47, 8.07, 0.00, null, -8.07,
                990.39);
        checkInst(model, 37, 37, LocalDate.of(2019, 2, 7), 166, 34, 50.00, 50.00, null, 0.96436413, 48.22, 7476.50, 8.03, 0.00, null, -8.03,
                990.39);
        checkInst(model, 38, 38, LocalDate.of(2019, 2, 8), 165, 35, 50.00, 50.00, null, 0.96333547, 48.17, 7434.48, 7.98, 0.00, null, -7.98,
                990.39);
        checkInst(model, 39, 39, LocalDate.of(2019, 2, 9), 164, 36, 50.00, 50.00, null, 0.96230790, 48.12, 7392.42, 7.94, 0.00, null, -7.94,
                990.39);
        checkInst(model, 40, 40, LocalDate.of(2019, 2, 10), 163, 37, 50.00, 50.00, null, 0.96128143, 48.06, 7350.31, 7.89, 0.00, null,
                -7.89, 990.39);
        checkInst(model, 41, 41, LocalDate.of(2019, 2, 11), 162, 38, 50.00, 50.00, null, 0.96025606, 48.01, 7308.16, 7.85, 0.00, null,
                -7.85, 990.39);
        checkInst(model, 42, 42, LocalDate.of(2019, 2, 12), 161, 39, 50.00, 50.00, null, 0.95923178, 47.96, 7265.97, 7.80, 0.00, null,
                -7.80, 990.39);
        checkInst(model, 43, 43, LocalDate.of(2019, 2, 13), 160, 40, 50.00, 50.00, null, 0.95820859, 47.91, 7223.72, 7.76, 0.00, null,
                -7.76, 990.39);
        checkInst(model, 44, 44, LocalDate.of(2019, 2, 14), 159, 41, 50.00, 50.00, null, 0.95718649, 47.86, 7181.44, 7.71, 0.00, null,
                -7.71, 990.39);
        checkInst(model, 45, 45, LocalDate.of(2019, 2, 15), 158, 42, 50.00, 50.00, null, 0.95616548, 47.81, 7139.11, 7.67, 0.00, null,
                -7.67, 990.39);
        checkInst(model, 46, 46, LocalDate.of(2019, 2, 16), 157, 43, 50.00, 50.00, null, 0.95514557, 47.76, 7096.73, 7.62, 0.00, null,
                -7.62, 990.39);
        checkInst(model, 47, 47, LocalDate.of(2019, 2, 17), 156, 44, 50.00, 50.00, null, 0.95412674, 47.71, 7054.31, 7.58, 0.00, null,
                -7.58, 990.39);
        checkInst(model, 48, 48, LocalDate.of(2019, 2, 18), 155, 45, 50.00, 50.00, null, 0.95310899, 47.66, 7011.84, 7.53, 0.00, null,
                -7.53, 990.39);
        checkInst(model, 49, 49, LocalDate.of(2019, 2, 19), 154, 46, 50.00, 50.00, null, 0.95209233, 47.60, 6969.33, 7.49, 0.00, null,
                -7.49, 990.39);
        checkInst(model, 50, 50, LocalDate.of(2019, 2, 20), 153, 47, 50.00, 50.00, null, 0.95107676, 47.55, 6926.77, 7.44, 0.00, null,
                -7.44, 990.39);
        checkInst(model, 51, 51, LocalDate.of(2019, 2, 21), 152, 48, 50.00, 50.00, null, 0.95006227, 47.50, 6884.17, 7.40, 0.00, null,
                -7.40, 990.39);
        checkInst(model, 52, 52, LocalDate.of(2019, 2, 22), 151, 49, 50.00, 50.00, null, 0.94904886, 47.45, 6841.52, 7.35, 0.00, null,
                -7.35, 990.39);
        checkInst(model, 53, 53, LocalDate.of(2019, 2, 23), 150, 50, 50.00, 50.00, null, 0.94803653, 47.40, 6798.82, 7.31, 0.00, null,
                -7.31, 990.39);
        checkInst(model, 54, 54, LocalDate.of(2019, 2, 24), 149, 51, 50.00, 50.00, null, 0.94702529, 47.35, 6756.08, 7.26, 0.00, null,
                -7.26, 990.39);
        checkInst(model, 55, 55, LocalDate.of(2019, 2, 25), 148, 52, 50.00, 50.00, null, 0.94601512, 47.30, 6713.30, 7.21, 0.00, null,
                -7.21, 990.39);
        checkInst(model, 56, 56, LocalDate.of(2019, 2, 26), 147, 53, 50.00, 50.00, null, 0.94500603, 47.25, 6670.47, 7.17, 0.00, null,
                -7.17, 990.39);
        checkInst(model, 57, 57, LocalDate.of(2019, 2, 27), 146, 54, 50.00, 50.00, null, 0.94399801, 47.20, 6627.59, 7.12, 0.00, null,
                -7.12, 990.39);
        checkInst(model, 58, 58, LocalDate.of(2019, 2, 28), 145, 55, 50.00, 50.00, null, 0.94299107, 47.15, 6584.67, 7.08, 0.00, null,
                -7.08, 990.39);
        checkInst(model, 59, 59, LocalDate.of(2019, 3, 1), 144, 56, 50.00, 50.00, null, 0.94198521, 47.10, 6541.70, 7.03, 0.00, null, -7.03,
                990.39);
        checkInst(model, 60, 60, LocalDate.of(2019, 3, 2), 143, 57, 50.00, 50.00, null, 0.94098042, 47.05, 6498.68, 6.99, 0.00, null, -6.99,
                990.39);
        checkInst(model, 61, 61, LocalDate.of(2019, 3, 3), 142, 58, 50.00, 50.00, null, 0.93997669, 47.00, 6455.62, 6.94, 0.00, null, -6.94,
                990.39);
        checkInst(model, 62, 62, LocalDate.of(2019, 3, 4), 141, 59, 50.00, 50.00, null, 0.93897404, 46.95, 6412.51, 6.89, 0.00, null, -6.89,
                990.39);
        checkInst(model, 63, 63, LocalDate.of(2019, 3, 5), 140, 60, 50.00, 50.00, null, 0.93797246, 46.90, 6369.36, 6.85, 0.00, null, -6.85,
                990.39);
        checkInst(model, 64, 64, LocalDate.of(2019, 3, 6), 139, 61, 50.00, 50.00, null, 0.93697195, 46.85, 6326.16, 6.80, 0.00, null, -6.80,
                990.39);
        checkInst(model, 65, 65, LocalDate.of(2019, 3, 7), 138, 62, 50.00, 50.00, null, 0.93597251, 46.80, 6282.92, 6.76, 0.00, null, -6.76,
                990.39);
        checkInst(model, 66, 66, LocalDate.of(2019, 3, 8), 137, 63, 50.00, 50.00, null, 0.93497413, 46.75, 6239.63, 6.71, 0.00, null, -6.71,
                990.39);
        checkInst(model, 67, 67, LocalDate.of(2019, 3, 9), 136, 64, 50.00, 50.00, null, 0.93397681, 46.70, 6196.29, 6.66, 0.00, null, -6.66,
                990.39);
        checkInst(model, 68, 68, LocalDate.of(2019, 3, 10), 135, 65, 50.00, 50.00, null, 0.93298056, 46.65, 6152.91, 6.62, 0.00, null,
                -6.62, 990.39);
        checkInst(model, 69, 69, LocalDate.of(2019, 3, 11), 134, 66, 50.00, 50.00, null, 0.93198538, 46.60, 6109.48, 6.57, 0.00, null,
                -6.57, 990.39);
        checkInst(model, 70, 70, LocalDate.of(2019, 3, 12), 133, 67, 50.00, 50.00, null, 0.93099125, 46.55, 6066.00, 6.52, 0.00, null,
                -6.52, 990.39);
        checkInst(model, 71, 71, LocalDate.of(2019, 3, 13), 132, 68, 50.00, 50.00, null, 0.92999818, 46.50, 6022.48, 6.48, 0.00, null,
                -6.48, 990.39);
        checkInst(model, 72, 72, LocalDate.of(2019, 3, 14), 131, 69, 50.00, 50.00, null, 0.92900618, 46.45, 5978.91, 6.43, 0.00, null,
                -6.43, 990.39);
        checkInst(model, 73, 73, LocalDate.of(2019, 3, 15), 130, 70, 50.00, 50.00, null, 0.92801523, 46.40, 5935.29, 6.38, 0.00, null,
                -6.38, 990.39);
        checkInst(model, 74, 74, LocalDate.of(2019, 3, 16), 129, 71, 50.00, 50.00, null, 0.92702534, 46.35, 5891.63, 6.34, 0.00, null,
                -6.34, 990.39);
        checkInst(model, 75, 75, LocalDate.of(2019, 3, 17), 128, 72, 50.00, 50.00, null, 0.92603650, 46.30, 5847.92, 6.29, 0.00, null,
                -6.29, 990.39);
        checkInst(model, 76, 76, LocalDate.of(2019, 3, 18), 127, 73, 50.00, 50.00, null, 0.92504872, 46.25, 5804.17, 6.24, 0.00, null,
                -6.24, 990.39);
        checkInst(model, 77, 77, LocalDate.of(2019, 3, 19), 126, 74, 50.00, 50.00, null, 0.92406200, 46.20, 5760.36, 6.20, 0.00, null,
                -6.20, 990.39);
        checkInst(model, 78, 78, LocalDate.of(2019, 3, 20), 125, 75, 50.00, 50.00, null, 0.92307632, 46.15, 5716.52, 6.15, 0.00, null,
                -6.15, 990.39);
        checkInst(model, 79, 79, LocalDate.of(2019, 3, 21), 124, 76, 50.00, 50.00, null, 0.92209170, 46.10, 5672.62, 6.10, 0.00, null,
                -6.10, 990.39);
        checkInst(model, 80, 80, LocalDate.of(2019, 3, 22), 123, 77, 50.00, 50.00, null, 0.92110813, 46.06, 5628.68, 6.06, 0.00, null,
                -6.06, 990.39);
        checkInst(model, 81, 81, LocalDate.of(2019, 3, 23), 122, 78, 50.00, 50.00, null, 0.92012560, 46.01, 5584.69, 6.01, 0.00, null,
                -6.01, 990.39);
        checkInst(model, 82, 82, LocalDate.of(2019, 3, 24), 121, 79, 50.00, 50.00, null, 0.91914413, 45.96, 5540.65, 5.96, 0.00, null,
                -5.96, 990.39);
        checkInst(model, 83, 83, LocalDate.of(2019, 3, 25), 120, 80, 50.00, 50.00, null, 0.91816370, 45.91, 5496.57, 5.92, 0.00, null,
                -5.92, 990.39);
        checkInst(model, 84, 84, LocalDate.of(2019, 3, 26), 119, 81, 50.00, 50.00, null, 0.91718432, 45.86, 5452.44, 5.87, 0.00, null,
                -5.87, 990.39);
        checkInst(model, 85, 85, LocalDate.of(2019, 3, 27), 118, 82, 50.00, 50.00, null, 0.91620598, 45.81, 5408.26, 5.82, 0.00, null,
                -5.82, 990.39);
        checkInst(model, 86, 86, LocalDate.of(2019, 3, 28), 117, 83, 50.00, 50.00, null, 0.91522868, 45.76, 5364.03, 5.78, 0.00, null,
                -5.78, 990.39);
        checkInst(model, 87, 87, LocalDate.of(2019, 3, 29), 116, 84, 50.00, 50.00, null, 0.91425243, 45.71, 5319.76, 5.73, 0.00, null,
                -5.73, 990.39);
        checkInst(model, 88, 88, LocalDate.of(2019, 3, 30), 115, 85, 50.00, 50.00, null, 0.91327722, 45.66, 5275.44, 5.68, 0.00, null,
                -5.68, 990.39);
        checkInst(model, 89, 89, LocalDate.of(2019, 3, 31), 114, 86, 50.00, 50.00, null, 0.91230305, 45.62, 5231.08, 5.63, 0.00, null,
                -5.63, 990.39);
        checkInst(model, 90, 90, LocalDate.of(2019, 4, 1), 113, 87, 50.00, 50.00, null, 0.91132992, 45.57, 5186.66, 5.59, 0.00, null, -5.59,
                990.39);
        checkInst(model, 91, 91, LocalDate.of(2019, 4, 2), 112, 88, 50.00, 50.00, null, 0.91035783, 45.52, 5142.20, 5.54, 0.00, null, -5.54,
                990.39);
        checkInst(model, 92, 92, LocalDate.of(2019, 4, 3), 111, 89, 50.00, 50.00, null, 0.90938677, 45.47, 5097.69, 5.49, 0.00, null, -5.49,
                990.39);
        checkInst(model, 93, 93, LocalDate.of(2019, 4, 4), 110, 90, 50.00, 50.00, null, 0.90841675, 45.42, 5053.13, 5.44, 0.00, null, -5.44,
                990.39);
        checkInst(model, 94, 94, LocalDate.of(2019, 4, 5), 109, 91, 50.00, 50.00, null, 0.90744776, 45.37, 5008.53, 5.40, 0.00, null, -5.40,
                990.39);
        checkInst(model, 95, 95, LocalDate.of(2019, 4, 6), 108, 92, 50.00, 50.00, null, 0.90647981, 45.32, 4963.88, 5.35, 0.00, null, -5.35,
                990.39);
        checkInst(model, 96, 96, LocalDate.of(2019, 4, 7), 107, 93, 50.00, 50.00, null, 0.90551289, 45.28, 4919.18, 5.30, 0.00, null, -5.30,
                990.39);
        checkInst(model, 97, 97, LocalDate.of(2019, 4, 8), 106, 94, 50.00, 50.00, null, 0.90454700, 45.23, 4874.43, 5.25, 0.00, null, -5.25,
                990.39);
        checkInst(model, 98, 98, LocalDate.of(2019, 4, 9), 105, 95, 50.00, 50.00, null, 0.90358215, 45.18, 4829.64, 5.20, 0.00, null, -5.20,
                990.39);
        checkInst(model, 99, 99, LocalDate.of(2019, 4, 10), 104, 96, 50.00, 50.00, null, 0.90261832, 45.13, 4784.79, 5.16, 0.00, null,
                -5.16, 990.39);
        checkInst(model, 100, 100, LocalDate.of(2019, 4, 11), 103, 97, 50.00, 50.00, null, 0.90165552, 45.08, 4739.90, 5.11, 0.00, null,
                -5.11, 990.39);
        checkInst(model, 101, 101, LocalDate.of(2019, 4, 12), 102, 98, 50.00, 50.00, null, 0.90069374, 45.03, 4694.96, 5.06, 0.00, null,
                -5.06, 990.39);
        checkInst(model, 102, 102, LocalDate.of(2019, 4, 13), 101, 99, 50.00, 50.00, null, 0.89973299, 44.99, 4649.98, 5.01, 0.00, null,
                -5.01, 990.39);
        checkInst(model, 103, 103, LocalDate.of(2019, 4, 14), 100, 100, 50.00, 50.00, null, 0.89877327, 44.94, 4604.94, 4.97, 0.00, null,
                -4.97, 990.39);
        checkInst(model, 104, 104, LocalDate.of(2019, 4, 15), 99, 101, 50.00, 50.00, null, 0.89781457, 44.89, 4559.86, 4.92, 0.00, null,
                -4.92, 990.39);
        checkInst(model, 105, 105, LocalDate.of(2019, 4, 16), 98, 102, 50.00, 50.00, null, 0.89685689, 44.84, 4514.73, 4.87, 0.00, null,
                -4.87, 990.39);
        checkInst(model, 106, 106, LocalDate.of(2019, 4, 17), 97, 103, 50.00, 50.00, null, 0.89590024, 44.80, 4469.55, 4.82, 0.00, null,
                -4.82, 990.39);
        checkInst(model, 107, 107, LocalDate.of(2019, 4, 18), 96, 104, 50.00, 50.00, null, 0.89494460, 44.75, 4424.32, 4.77, 0.00, null,
                -4.77, 990.39);
        checkInst(model, 108, 108, LocalDate.of(2019, 4, 19), 95, 105, 50.00, 50.00, null, 0.89398999, 44.70, 4379.05, 4.72, 0.00, null,
                -4.72, 990.39);
        checkInst(model, 109, 109, LocalDate.of(2019, 4, 20), 94, 106, 50.00, 50.00, null, 0.89303639, 44.65, 4333.72, 4.68, 0.00, null,
                -4.68, 990.39);
        checkInst(model, 110, 110, LocalDate.of(2019, 4, 21), 93, 107, 50.00, 50.00, null, 0.89208381, 44.60, 4288.35, 4.63, 0.00, null,
                -4.63, 990.39);
        checkInst(model, 111, 111, LocalDate.of(2019, 4, 22), 92, 108, 50.00, 50.00, null, 0.89113225, 44.56, 4242.93, 4.58, 0.00, null,
                -4.58, 990.39);
        checkInst(model, 112, 112, LocalDate.of(2019, 4, 23), 91, 109, 50.00, 50.00, null, 0.89018170, 44.51, 4197.46, 4.53, 0.00, null,
                -4.53, 990.39);
        checkInst(model, 113, 113, LocalDate.of(2019, 4, 24), 90, 110, 50.00, 50.00, null, 0.88923216, 44.46, 4151.94, 4.48, 0.00, null,
                -4.48, 990.39);
        checkInst(model, 114, 114, LocalDate.of(2019, 4, 25), 89, 111, 50.00, 50.00, null, 0.88828364, 44.41, 4106.38, 4.43, 0.00, null,
                -4.43, 990.39);
        checkInst(model, 115, 115, LocalDate.of(2019, 4, 26), 88, 112, 50.00, 50.00, null, 0.88733613, 44.37, 4060.76, 4.38, 0.00, null,
                -4.38, 990.39);
        checkInst(model, 116, 116, LocalDate.of(2019, 4, 27), 87, 113, 50.00, 50.00, null, 0.88638963, 44.32, 4015.10, 4.34, 0.00, null,
                -4.34, 990.39);
        checkInst(model, 117, 117, LocalDate.of(2019, 4, 28), 86, 114, 50.00, 50.00, null, 0.88544414, 44.27, 3969.38, 4.29, 0.00, null,
                -4.29, 990.39);
        checkInst(model, 118, 118, LocalDate.of(2019, 4, 29), 85, 115, 50.00, 50.00, null, 0.88449966, 44.22, 3923.62, 4.24, 0.00, null,
                -4.24, 990.39);
        checkInst(model, 119, 119, LocalDate.of(2019, 4, 30), 84, 116, 50.00, 50.00, null, 0.88355619, 44.18, 3877.81, 4.19, 0.00, null,
                -4.19, 990.39);
        checkInst(model, 120, 120, LocalDate.of(2019, 5, 1), 83, 117, 50.00, 50.00, null, 0.88261372, 44.13, 3831.95, 4.14, 0.00, null,
                -4.14, 990.39);
        checkInst(model, 121, 121, LocalDate.of(2019, 5, 2), 82, 118, 50.00, 50.00, null, 0.88167226, 44.08, 3786.04, 4.09, 0.00, null,
                -4.09, 990.39);
        checkInst(model, 122, 122, LocalDate.of(2019, 5, 3), 81, 119, 50.00, 50.00, null, 0.88073180, 44.04, 3740.09, 4.04, 0.00, null,
                -4.04, 990.39);
        checkInst(model, 123, 123, LocalDate.of(2019, 5, 4), 80, 120, 50.00, 50.00, null, 0.87979234, 43.99, 3694.08, 3.99, 0.00, null,
                -3.99, 990.39);
        checkInst(model, 124, 124, LocalDate.of(2019, 5, 5), 79, 121, 50.00, 50.00, null, 0.87885389, 43.94, 3648.03, 3.94, 0.00, null,
                -3.94, 990.39);
        checkInst(model, 125, 125, LocalDate.of(2019, 5, 6), 78, 122, 50.00, 50.00, null, 0.87791644, 43.90, 3601.92, 3.90, 0.00, null,
                -3.90, 990.39);
        checkInst(model, 126, 126, LocalDate.of(2019, 5, 7), 77, 123, 50.00, 50.00, null, 0.87697999, 43.85, 3555.77, 3.85, 0.00, null,
                -3.85, 990.39);
        checkInst(model, 127, 127, LocalDate.of(2019, 5, 8), 76, 124, 50.00, 50.00, null, 0.87604453, 43.80, 3509.56, 3.80, 0.00, null,
                -3.80, 990.39);
        checkInst(model, 128, 128, LocalDate.of(2019, 5, 9), 75, 125, 50.00, 50.00, null, 0.87511008, 43.76, 3463.31, 3.75, 0.00, null,
                -3.75, 990.39);
        checkInst(model, 129, 129, LocalDate.of(2019, 5, 10), 74, 126, 50.00, 50.00, null, 0.87417662, 43.71, 3417.01, 3.70, 0.00, null,
                -3.70, 990.39);
        checkInst(model, 130, 130, LocalDate.of(2019, 5, 11), 73, 127, 50.00, 50.00, null, 0.87324416, 43.66, 3370.66, 3.65, 0.00, null,
                -3.65, 990.39);
        checkInst(model, 131, 131, LocalDate.of(2019, 5, 12), 72, 128, 50.00, 50.00, null, 0.87231269, 43.62, 3324.26, 3.60, 0.00, null,
                -3.60, 990.39);
        checkInst(model, 132, 132, LocalDate.of(2019, 5, 13), 71, 129, 50.00, 50.00, null, 0.87138221, 43.57, 3277.81, 3.55, 0.00, null,
                -3.55, 990.39);
        checkInst(model, 133, 133, LocalDate.of(2019, 5, 14), 70, 130, 50.00, 50.00, null, 0.87045273, 43.52, 3231.31, 3.50, 0.00, null,
                -3.50, 990.39);
        checkInst(model, 134, 134, LocalDate.of(2019, 5, 15), 69, 131, 50.00, 50.00, null, 0.86952424, 43.48, 3184.76, 3.45, 0.00, null,
                -3.45, 990.39);
        checkInst(model, 135, 135, LocalDate.of(2019, 5, 16), 68, 132, 50.00, 50.00, null, 0.86859674, 43.43, 3138.16, 3.40, 0.00, null,
                -3.40, 990.39);
        checkInst(model, 136, 136, LocalDate.of(2019, 5, 17), 67, 133, 50.00, 50.00, null, 0.86767023, 43.38, 3091.51, 3.35, 0.00, null,
                -3.35, 990.39);
        checkInst(model, 137, 137, LocalDate.of(2019, 5, 18), 66, 134, 50.00, 50.00, null, 0.86674471, 43.34, 3044.81, 3.30, 0.00, null,
                -3.30, 990.39);
        checkInst(model, 138, 138, LocalDate.of(2019, 5, 19), 65, 135, 50.00, 50.00, null, 0.86582017, 43.29, 2998.06, 3.25, 0.00, null,
                -3.25, 990.39);
        checkInst(model, 139, 139, LocalDate.of(2019, 5, 20), 64, 136, 50.00, 50.00, null, 0.86489662, 43.24, 2951.26, 3.20, 0.00, null,
                -3.20, 990.39);
        checkInst(model, 140, 140, LocalDate.of(2019, 5, 21), 63, 137, 50.00, 50.00, null, 0.86397406, 43.20, 2904.42, 3.15, 0.00, null,
                -3.15, 990.39);
        checkInst(model, 141, 141, LocalDate.of(2019, 5, 22), 62, 138, 50.00, 50.00, null, 0.86305248, 43.15, 2857.52, 3.10, 0.00, null,
                -3.10, 990.39);
        checkInst(model, 142, 142, LocalDate.of(2019, 5, 23), 61, 139, 50.00, 50.00, null, 0.86213188, 43.11, 2810.57, 3.05, 0.00, null,
                -3.05, 990.39);
        checkInst(model, 143, 143, LocalDate.of(2019, 5, 24), 60, 140, 50.00, 50.00, null, 0.86121227, 43.06, 2763.57, 3.00, 0.00, null,
                -3.00, 990.39);
        checkInst(model, 144, 144, LocalDate.of(2019, 5, 25), 59, 141, 50.00, 50.00, null, 0.86029363, 43.01, 2716.52, 2.95, 0.00, null,
                -2.95, 990.39);
        checkInst(model, 145, 145, LocalDate.of(2019, 5, 26), 58, 142, 50.00, 50.00, null, 0.85937598, 42.97, 2669.42, 2.90, 0.00, null,
                -2.90, 990.39);
        checkInst(model, 146, 146, LocalDate.of(2019, 5, 27), 57, 143, 50.00, 50.00, null, 0.85845930, 42.92, 2622.27, 2.85, 0.00, null,
                -2.85, 990.39);
        checkInst(model, 147, 147, LocalDate.of(2019, 5, 28), 56, 144, 50.00, 50.00, null, 0.85754361, 42.88, 2575.07, 2.80, 0.00, null,
                -2.80, 990.39);
        checkInst(model, 148, 148, LocalDate.of(2019, 5, 29), 55, 145, 50.00, 50.00, null, 0.85662889, 42.83, 2527.82, 2.75, 0.00, null,
                -2.75, 990.39);
        checkInst(model, 149, 149, LocalDate.of(2019, 5, 30), 54, 146, 50.00, 50.00, null, 0.85571514, 42.79, 2480.52, 2.70, 0.00, null,
                -2.70, 990.39);
        checkInst(model, 150, 150, LocalDate.of(2019, 5, 31), 53, 147, 50.00, 50.00, null, 0.85480237, 42.74, 2433.17, 2.65, 0.00, null,
                -2.65, 990.39);
        checkInst(model, 151, 151, LocalDate.of(2019, 6, 1), 52, 148, 50.00, 50.00, null, 0.85389057, 42.69, 2385.77, 2.60, 0.00, null,
                -2.60, 990.39);
        checkInst(model, 152, 152, LocalDate.of(2019, 6, 2), 51, 149, 50.00, 50.00, null, 0.85297975, 42.65, 2338.31, 2.55, 0.00, null,
                -2.55, 990.39);
        checkInst(model, 153, 153, LocalDate.of(2019, 6, 3), 50, 150, 50.00, 50.00, null, 0.85206990, 42.60, 2290.81, 2.50, 0.00, null,
                -2.50, 990.39);
        checkInst(model, 154, 154, LocalDate.of(2019, 6, 4), 49, 151, 50.00, 50.00, null, 0.85116101, 42.56, 2243.26, 2.45, 0.00, null,
                -2.45, 990.39);
        checkInst(model, 155, 155, LocalDate.of(2019, 6, 5), 48, 152, 50.00, 50.00, null, 0.85025310, 42.51, 2195.65, 2.40, 0.00, null,
                -2.40, 990.39);
        checkInst(model, 156, 156, LocalDate.of(2019, 6, 6), 47, 153, 50.00, 50.00, null, 0.84934616, 42.47, 2148.00, 2.34, 0.00, null,
                -2.34, 990.39);
        checkInst(model, 157, 157, LocalDate.of(2019, 6, 7), 46, 154, 50.00, 50.00, null, 0.84844018, 42.42, 2100.29, 2.29, 0.00, null,
                -2.29, 990.39);
        checkInst(model, 158, 158, LocalDate.of(2019, 6, 8), 45, 155, 50.00, 50.00, null, 0.84753517, 42.38, 2052.53, 2.24, 0.00, null,
                -2.24, 990.39);
        checkInst(model, 159, 159, LocalDate.of(2019, 6, 9), 44, 156, 50.00, 50.00, null, 0.84663113, 42.33, 2004.73, 2.19, 0.00, null,
                -2.19, 990.39);
        checkInst(model, 160, 160, LocalDate.of(2019, 6, 10), 43, 157, 50.00, 50.00, null, 0.84572805, 42.29, 1956.87, 2.14, 0.00, null,
                -2.14, 990.39);
        checkInst(model, 161, 161, LocalDate.of(2019, 6, 11), 42, 158, 50.00, 50.00, null, 0.84482593, 42.24, 1908.96, 2.09, 0.00, null,
                -2.09, 990.39);
        checkInst(model, 162, 162, LocalDate.of(2019, 6, 12), 41, 159, 50.00, 50.00, null, 0.84392477, 42.20, 1860.99, 2.04, 0.00, null,
                -2.04, 990.39);
        checkInst(model, 163, 163, LocalDate.of(2019, 6, 13), 40, 160, 50.00, 50.00, null, 0.84302458, 42.15, 1812.98, 1.99, 0.00, null,
                -1.99, 990.39);
        checkInst(model, 164, 164, LocalDate.of(2019, 6, 14), 39, 161, 50.00, 50.00, null, 0.84212535, 42.11, 1764.92, 1.94, 0.00, null,
                -1.94, 990.39);
        checkInst(model, 165, 165, LocalDate.of(2019, 6, 15), 38, 162, 50.00, 50.00, null, 0.84122707, 42.06, 1716.80, 1.88, 0.00, null,
                -1.88, 990.39);
        checkInst(model, 166, 166, LocalDate.of(2019, 6, 16), 37, 163, 50.00, 50.00, null, 0.84032975, 42.02, 1668.64, 1.83, 0.00, null,
                -1.83, 990.39);
        checkInst(model, 167, 167, LocalDate.of(2019, 6, 17), 36, 164, 50.00, 50.00, null, 0.83943340, 41.97, 1620.42, 1.78, 0.00, null,
                -1.78, 990.39);
        checkInst(model, 168, 168, LocalDate.of(2019, 6, 18), 35, 165, 50.00, 50.00, null, 0.83853799, 41.93, 1572.15, 1.73, 0.00, null,
                -1.73, 990.39);
        checkInst(model, 169, 169, LocalDate.of(2019, 6, 19), 34, 166, 50.00, 50.00, null, 0.83764354, 41.88, 1523.83, 1.68, 0.00, null,
                -1.68, 990.39);
        checkInst(model, 170, 170, LocalDate.of(2019, 6, 20), 33, 167, 50.00, 50.00, null, 0.83675005, 41.84, 1475.45, 1.63, 0.00, null,
                -1.63, 990.39);
        checkInst(model, 171, 171, LocalDate.of(2019, 6, 21), 32, 168, 50.00, 50.00, null, 0.83585751, 41.79, 1427.03, 1.58, 0.00, null,
                -1.58, 990.39);
        checkInst(model, 172, 172, LocalDate.of(2019, 6, 22), 31, 169, 50.00, 50.00, null, 0.83496592, 41.75, 1378.55, 1.52, 0.00, null,
                -1.52, 990.39);
        checkInst(model, 173, 173, LocalDate.of(2019, 6, 23), 30, 170, 50.00, 50.00, null, 0.83407528, 41.70, 1330.02, 1.47, 0.00, null,
                -1.47, 990.39);
        checkInst(model, 174, 174, LocalDate.of(2019, 6, 24), 29, 171, 50.00, 50.00, null, 0.83318560, 41.66, 1281.45, 1.42, 0.00, null,
                -1.42, 990.39);
        checkInst(model, 175, 175, LocalDate.of(2019, 6, 25), 28, 172, 50.00, 50.00, null, 0.83229686, 41.61, 1232.81, 1.37, 0.00, null,
                -1.37, 990.39);
        checkInst(model, 176, 176, LocalDate.of(2019, 6, 26), 27, 173, 50.00, 50.00, null, 0.83140907, 41.57, 1184.13, 1.32, 0.00, null,
                -1.32, 990.39);
        checkInst(model, 177, 177, LocalDate.of(2019, 6, 27), 26, 174, 50.00, 50.00, null, 0.83052222, 41.53, 1135.39, 1.26, 0.00, null,
                -1.26, 990.39);
        checkInst(model, 178, 178, LocalDate.of(2019, 6, 28), 25, 175, 50.00, 50.00, null, 0.82963633, 41.48, 1086.61, 1.21, 0.00, null,
                -1.21, 990.39);
        checkInst(model, 179, 179, LocalDate.of(2019, 6, 29), 24, 176, 50.00, 50.00, null, 0.82875137, 41.44, 1037.77, 1.16, 0.00, null,
                -1.16, 990.39);
        checkInst(model, 180, 180, LocalDate.of(2019, 6, 30), 23, 177, 50.00, 50.00, null, 0.82786736, 41.39, 988.88, 1.11, 0.00, null,
                -1.11, 990.39);
        checkInst(model, 181, 181, LocalDate.of(2019, 7, 1), 22, 178, 50.00, 50.00, null, 0.82698430, 41.35, 939.93, 1.06, 0.00, null,
                -1.06, 990.39);
        checkInst(model, 182, 182, LocalDate.of(2019, 7, 2), 21, 179, 50.00, 50.00, null, 0.82610217, 41.31, 890.93, 1.00, 0.00, null,
                -1.00, 990.39);
        checkInst(model, 183, 183, LocalDate.of(2019, 7, 3), 20, 180, 50.00, 50.00, null, 0.82522099, 41.26, 841.89, 0.95, 0.00, null,
                -0.95, 990.39);
        checkInst(model, 184, 184, LocalDate.of(2019, 7, 4), 19, 181, 50.00, 50.00, null, 0.82434075, 41.22, 792.79, 0.90, 0.00, null,
                -0.90, 990.39);
        checkInst(model, 185, 185, LocalDate.of(2019, 7, 5), 18, 182, 50.00, 50.00, null, 0.82346144, 41.17, 743.63, 0.85, 0.00, null,
                -0.85, 990.39);
        checkInst(model, 186, 186, LocalDate.of(2019, 7, 6), 17, 183, 50.00, 50.00, null, 0.82258308, 41.13, 694.43, 0.79, 0.00, null,
                -0.79, 990.39);
        checkInst(model, 187, 187, LocalDate.of(2019, 7, 7), 16, 184, 50.00, 50.00, null, 0.82170565, 41.09, 645.17, 0.74, 0.00, null,
                -0.74, 990.39);
        checkInst(model, 188, 188, LocalDate.of(2019, 7, 8), 15, 185, 50.00, 50.00, null, 0.82082916, 41.04, 595.86, 0.69, 0.00, null,
                -0.69, 990.39);
        checkInst(model, 189, 189, LocalDate.of(2019, 7, 9), 14, 186, 50.00, 50.00, null, 0.81995360, 41.00, 546.49, 0.64, 0.00, null,
                -0.64, 990.39);
        checkInst(model, 190, 190, LocalDate.of(2019, 7, 10), 13, 187, 50.00, 50.00, null, 0.81907897, 40.95, 497.08, 0.58, 0.00, null,
                -0.58, 990.39);
        checkInst(model, 191, 191, LocalDate.of(2019, 7, 11), 12, 188, 50.00, 50.00, null, 0.81820528, 40.91, 447.61, 0.53, 0.00, null,
                -0.53, 990.39);
        checkInst(model, 192, 192, LocalDate.of(2019, 7, 12), 11, 189, 50.00, 50.00, null, 0.81733252, 40.87, 398.08, 0.48, 0.00, null,
                -0.48, 990.39);
        checkInst(model, 193, 193, LocalDate.of(2019, 7, 13), 10, 190, 50.00, 50.00, null, 0.81646069, 40.82, 348.51, 0.43, 0.00, null,
                -0.43, 990.39);
        checkInst(model, 194, 194, LocalDate.of(2019, 7, 14), 9, 191, 50.00, 50.00, null, 0.81558979, 40.78, 298.88, 0.37, 0.00, null,
                -0.37, 990.39);
        checkInst(model, 195, 195, LocalDate.of(2019, 7, 15), 8, 192, 50.00, 50.00, null, 0.81471983, 40.74, 249.20, 0.32, 0.00, null,
                -0.32, 990.39);
        checkInst(model, 196, 196, LocalDate.of(2019, 7, 16), 7, 193, 50.00, 50.00, null, 0.81385078, 40.69, 199.47, 0.27, 0.00, null,
                -0.27, 990.39);
        checkInst(model, 197, 197, LocalDate.of(2019, 7, 17), 6, 194, 50.00, 50.00, null, 0.81298267, 40.65, 149.68, 0.21, 0.00, null,
                -0.21, 990.39);
        checkInst(model, 198, 198, LocalDate.of(2019, 7, 18), 5, 195, 50.00, 50.00, null, 0.81211548, 40.61, 99.84, 0.16, 0.00, null, -0.16,
                990.39);
        checkInst(model, 199, 199, LocalDate.of(2019, 7, 19), 4, 196, 50.00, 50.00, null, 0.81124922, 40.56, 49.95, 0.11, 0.00, null, -0.11,
                990.39);
        checkInst(model, 200, 200, LocalDate.of(2019, 7, 20), 3, 197, 50.00, 50.00, null, 0.81038388, 40.52, 0.00, 0.05, 0.00, null, -0.05,
                990.39);

        assertEquals(203, model.payments().size(), "disbursement + 200 regular + 2 additional");
        checkInst(model, 201, 201, LocalDate.of(2019, 7, 21), 2, 198, null, 50.00, null, 0.80951946, 40.48, null, null, 0.00, null, null,
                null);
        checkInst(model, 202, 202, LocalDate.of(2019, 7, 22), 1, 199, null, 50.00, null, 0.80865597, 40.43, null, null, 0.00, null, null,
                null);
    }

    @Test
    void testLessPayment_term10_originationFee50_netDisbursement450_pay40() {
        final BigDecimal smallOriginationFee = new BigDecimal("50");
        final BigDecimal smallNetDisbursement = new BigDecimal("450");
        final ProjectedAmortizationScheduleModel initial = calculator.generateModel(smallOriginationFee, smallNetDisbursement, TPV, RATE,
                DAY_COUNT, EXPECTED_DISBURSEMENT_DATE, MC, CURRENCY);
        final ProjectedAmortizationScheduleModel model = calculator.addDisbursement(initial, smallOriginationFee, smallNetDisbursement,
                EXPECTED_DISBURSEMENT_DATE);

        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("40"));

        assertEquals(12, model.payments().size(), "disbursement + 10 regular + 1 tail");

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 11, 0, -450.00, null, null, 1.00000000, -450.00, 450.00, null, null, null, null,
                50.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 10, 0, 50.00, 50.00, 40.00, 1.00000000, 40.00, 408.83, 8.83, 7.07, 7.07, -1.77,
                42.93);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 9, 1, 50.00, 50.00, null, 0.98074794, 49.04, 366.86, 8.03, 0.00, null, -8.03,
                42.93);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 8, 2, 50.00, 50.00, null, 0.96186652, 48.09, 324.06, 7.20, 0.00, null, -7.20,
                42.93);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 7, 3, 50.00, 50.00, null, 0.94334860, 47.17, 280.42, 6.36, 0.00, null, -6.36,
                42.93);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 6, 4, 50.00, 50.00, null, 0.92518720, 46.26, 235.93, 5.50, 0.00, null, -5.50,
                42.93);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 5, 5, 50.00, 50.00, null, 0.90737544, 45.37, 190.56, 4.63, 0.00, null, -4.63,
                42.93);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 4, 6, 50.00, 50.00, null, 0.88990659, 44.50, 144.30, 3.74, 0.00, null, -3.74,
                42.93);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 3, 7, 50.00, 50.00, null, 0.87277405, 43.64, 97.13, 2.83, 0.00, null, -2.83,
                42.93);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 2, 8, 50.00, 50.00, null, 0.85597135, 42.80, 49.04, 1.91, 0.00, null, -1.91,
                42.93);
        checkInst(model, 10, 10, LocalDate.of(2019, 1, 11), 1, 9, 50.00, 50.00, null, 0.83949214, 41.97, 0.00, 0.96, 0.00, null, -0.96,
                42.93);

        checkInst(model, 11, 11, LocalDate.of(2019, 1, 12), 0, 10, null, 10.00, null, 0.82333018, 8.23, null, null, 0.00, null, null, null);
    }

    @Test
    void testExcessPayment_term10_originationFee50_netDisbursement450_pay110() {
        final BigDecimal smallOriginationFee = new BigDecimal("50");
        final BigDecimal smallNetDisbursement = new BigDecimal("450");
        final ProjectedAmortizationScheduleModel initial = calculator.generateModel(smallOriginationFee, smallNetDisbursement, TPV, RATE,
                DAY_COUNT, EXPECTED_DISBURSEMENT_DATE, MC, CURRENCY);
        final ProjectedAmortizationScheduleModel model = calculator.addDisbursement(initial, smallOriginationFee, smallNetDisbursement,
                EXPECTED_DISBURSEMENT_DATE);

        calculator.applyPayment(model, EXPECTED_DISBURSEMENT_DATE.plusDays(1), new BigDecimal("110"));

        assertEquals(10, model.payments().size(), "disbursement + 9 regular (period 10 removed, forecast was 0)");

        checkInst(model, 0, 0, EXPECTED_DISBURSEMENT_DATE, 11, 0, -450.00, null, null, 1.00000000, -450.00, 450.00, null, null, null, null,
                50.00);

        checkInst(model, 1, 1, LocalDate.of(2019, 1, 2), 10, 0, 50.00, 50.00, 110.00, 1.00000000, 110.00, 408.83, 8.83, 18.30, 18.30, 9.47,
                31.70);
        checkInst(model, 2, 2, LocalDate.of(2019, 1, 3), 9, 1, 50.00, 50.00, null, 0.98074794, 49.04, 366.86, 8.03, 0.00, null, -8.03,
                31.70);
        checkInst(model, 3, 3, LocalDate.of(2019, 1, 4), 8, 2, 50.00, 50.00, null, 0.96186652, 48.09, 324.06, 7.20, 0.00, null, -7.20,
                31.70);
        checkInst(model, 4, 4, LocalDate.of(2019, 1, 5), 7, 3, 50.00, 50.00, null, 0.94334860, 47.17, 280.42, 6.36, 0.00, null, -6.36,
                31.70);
        checkInst(model, 5, 5, LocalDate.of(2019, 1, 6), 6, 4, 50.00, 50.00, null, 0.92518720, 46.26, 235.93, 5.50, 0.00, null, -5.50,
                31.70);
        checkInst(model, 6, 6, LocalDate.of(2019, 1, 7), 5, 5, 50.00, 50.00, null, 0.90737544, 45.37, 190.56, 4.63, 0.00, null, -4.63,
                31.70);
        checkInst(model, 7, 7, LocalDate.of(2019, 1, 8), 4, 6, 50.00, 50.00, null, 0.88990659, 44.50, 144.30, 3.74, 0.00, null, -3.74,
                31.70);
        checkInst(model, 8, 8, LocalDate.of(2019, 1, 9), 3, 7, 50.00, 50.00, null, 0.87277405, 43.64, 97.13, 2.83, 0.00, null, -2.83,
                31.70);
        checkInst(model, 9, 9, LocalDate.of(2019, 1, 10), 2, 8, 50.00, 40.00, null, 0.85597135, 34.24, 49.04, 1.91, 0.00, null, -1.91,
                31.70);
    }

    private ProjectedAmortizationScheduleModel generateModel() {
        final ProjectedAmortizationScheduleModel model = calculator.generateModel(ORIGINATION_FEE, NET_DISBURSEMENT, TPV, RATE, DAY_COUNT,
                EXPECTED_DISBURSEMENT_DATE, MC, CURRENCY);
        return calculator.addDisbursement(model, ORIGINATION_FEE, NET_DISBURSEMENT, EXPECTED_DISBURSEMENT_DATE);
    }

    private void checkInst(final ProjectedAmortizationScheduleModel model, final int index, final int expectedNo,
            final LocalDate expectedDate, final long expectedCount, final long expectedPaymentsLeft, final Double expectedPayment,
            final Double expectedForecastPayment, final Double expectedActualPayment, final Double expectedDiscountFactor,
            final Double expectedNpvValue, final Double expectedBalance, final Double expectedAmortization,
            final Double expectedNetAmortization, final Double expectedActualAmortization, final Double expectedIncomeModification,
            final Double expectedDeferredBalance) {
        final ProjectedPayment inst = model.payments().get(index);
        final String p = "inst " + expectedNo + ": ";

        assertEquals(expectedNo, inst.paymentNo(), p + "paymentNo");
        assertEquals(expectedDate, inst.date(), p + "date");
        assertEquals(expectedCount, inst.count(), p + "count");
        assertEquals(expectedPaymentsLeft, inst.paymentsLeft(), p + "paymentsLeft");
        assertMoneyValue(expectedPayment, inst.expectedPaymentAmount(), 2, p + "expectedPayment");
        assertMoneyValue(expectedForecastPayment, inst.forecastPaymentAmount(), 2, p + "forecastPayment");
        assertMoneyValue(expectedActualPayment, inst.actualPaymentAmount(), 2, p + "actualPayment");
        assertValue(expectedDiscountFactor, inst.discountFactor(), 8, p + "discountFactor");
        assertMoneyValue(expectedNpvValue, inst.npvValue(), 2, p + "npvValue");
        assertMoneyValue(expectedBalance, inst.balance(), 2, p + "balance");
        assertMoneyValue(expectedAmortization, inst.expectedAmortizationAmount(), 2, p + "expectedAmortization");
        assertMoneyValue(expectedNetAmortization, inst.netAmortizationAmount(), 2, p + "netAmortization");
        assertMoneyValue(expectedActualAmortization, inst.actualAmortizationAmount(), 2, p + "actualAmort");
        assertMoneyValue(expectedIncomeModification, inst.incomeModification(), 2, p + "incomeModification");
        assertMoneyValue(expectedDeferredBalance, inst.deferredBalance(), 2, p + "deferredBalance");
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
