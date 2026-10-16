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
package org.apache.fineract.portfolio.loanproduct.serialization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingHelper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class LoanProductDataValidatorDaysInYearCustomStrategyTest {

    private static final String PARAM = LoanProductConstants.DAYS_IN_YEAR_CUSTOM_STRATEGY_TYPE_PARAMETER_NAME;

    // The code under test dereferences enum, BigDecimal and Money getters of the mocked domain objects unconditionally.
    private static final Answer<Object> DOMAIN_DEFAULTS = new Answer<>() {

        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable {
            final Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType.isEnum()) {
                return returnType.getEnumConstants()[0];
            }
            if (returnType == BigDecimal.class) {
                return BigDecimal.ZERO;
            }
            if (returnType == Money.class) {
                return mock(Money.class, this);
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        }
    };

    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    @Mock
    private AdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;
    @Mock
    private AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;
    @Mock
    private ProductToGLAccountMappingHelper productToGLAccountMappingHelper;
    private LoanProduct loanProduct;
    private LoanProductRelatedDetail loanProductRelatedDetail;

    private LoanProductDataValidator underTest;

    @BeforeEach
    void setUp() {
        underTest = new LoanProductDataValidator(new FromJsonHelper(), loanRepaymentScheduleTransactionProcessorFactory,
                advancedPaymentAllocationsJsonParser, advancedPaymentAllocationsValidator, productToGLAccountMappingHelper);
        loanProduct = mock(LoanProduct.class, withSettings().defaultAnswer(DOMAIN_DEFAULTS));
        loanProductRelatedDetail = mock(LoanProductRelatedDetail.class, withSettings().defaultAnswer(DOMAIN_DEFAULTS));
        lenient().when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        lenient().when(loanProductRelatedDetail.getLoanScheduleType()).thenReturn(LoanScheduleType.PROGRESSIVE);
        lenient().when(loanProductRelatedDetail.getLoanScheduleProcessingType()).thenReturn(LoanScheduleProcessingType.HORIZONTAL);
        lenient().when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        lenient().when(loanProductRelatedDetail.getDaysInYearCustomStrategy()).thenReturn(DaysInYearCustomStrategyType.FULL_LEAP_YEAR);
    }

    @Test
    void updateRejectsInvalidValue() {
        assertTrue(hasErrorFor(validateForUpdate("{\"" + PARAM + "\": \"NOT_A_STRATEGY\"}"), "is.not.one.of.expected.enumerations"));
    }

    @Test
    void updateAllowsClearing() {
        assertFalse(hasErrorFor(validateForUpdate("{\"" + PARAM + "\": null}"), ""));
    }

    @Test
    void createRejectsInvalidValue() {
        final PlatformApiDataValidationException thrown = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateForCreate(JsonCommand.from("{\"" + PARAM + "\": \"NOT_A_STRATEGY\"}")));

        assertTrue(hasErrorFor(thrown.getErrors(), "is.not.one.of.expected.enumerations"));
    }

    @Test
    void combinationRejectsNonActualDaysInYearTypeForCurrentStrategy() {
        assertTrue(hasErrorFor(validateCombination("{\"daysInYearType\": 360, \"locale\": \"en\"}", LoanScheduleType.PROGRESSIVE,
                DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY), "is.only.applicable.for.actual.days.in.year.type"));
    }

    @Test
    void combinationAllowsNonActualDaysInYearTypeWhenStrategyIsCleared() {
        assertFalse(hasErrorFor(validateCombination("{\"" + PARAM + "\": null, \"daysInYearType\": 360, \"locale\": \"en\"}",
                LoanScheduleType.PROGRESSIVE, DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY), ""));
    }

    @Test
    void combinationRejectsStrategyForCumulativeSchedule() {
        assertTrue(hasErrorFor(validateCombination("{\"" + PARAM + "\": \"FULL_LEAP_YEAR\"}", LoanScheduleType.CUMULATIVE, null),
                "is.only.supported.for.progressive.loan.schedule.type"));
    }

    @Test
    void combinationIsNotCheckedWhenNeitherParameterIsPassed() {
        assertFalse(hasErrorFor(validateCombination("{}", LoanScheduleType.CUMULATIVE, DaysInYearCustomStrategyType.FULL_LEAP_YEAR), ""));
    }

    private List<ApiParameterError> validateCombination(final String json, final LoanScheduleType currentLoanScheduleType,
            final DaysInYearCustomStrategyType currentDaysInYearCustomStrategy) {
        final List<ApiParameterError> errors = new ArrayList<>();
        underTest.validateDaysInYearCustomStrategy(new FromJsonHelper().parse(json), true, currentLoanScheduleType,
                DaysInYearType.ACTUAL.getValue(), currentDaysInYearCustomStrategy, new DataValidatorBuilder(errors).resource("loan"));
        return errors;
    }

    private List<ApiParameterError> validateForUpdate(final String json) {
        try {
            underTest.validateForUpdate(JsonCommand.from(json), loanProduct);
            return List.of();
        } catch (PlatformApiDataValidationException e) {
            return e.getErrors();
        }
    }

    private static boolean hasErrorFor(final List<ApiParameterError> errors, final String codeSuffix) {
        return errors.stream()
                .anyMatch(error -> PARAM.equals(error.getParameterName()) && error.getUserMessageGlobalisationCode().endsWith(codeSuffix));
    }
}
