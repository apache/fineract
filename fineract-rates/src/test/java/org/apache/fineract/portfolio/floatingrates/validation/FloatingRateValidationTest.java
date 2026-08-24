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
package org.apache.fineract.portfolio.floatingrates.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateRequest;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class FloatingRateValidationTest {

    private static final String NAME_NOT_BLANK = "{org.apache.fineract.portfolio.floatingrate.name.not-blank}";
    private static final String RATE_PERIODS_NOT_EMPTY = "{org.apache.fineract.portfolio.floatingrate.rate-periods.not-empty}";
    private static final String FROM_DATE_INVALID = "{org.apache.fineract.portfolio.floatingrate.from-date.invalid}";
    private static final String FROM_DATE_FUTURE = "{org.apache.fineract.portfolio.floatingrate.from-date.future}";
    private static final String FROM_DATE_DUPLICATE = "{org.apache.fineract.portfolio.floatingrate.from-date.duplicate}";
    private static final String INTEREST_RATE_POSITIVE_OR_ZERO = "{org.apache.fineract.portfolio.floatingrate.interest-rate.positive-or-zero}";
    private static final String BLR_DUPLICATE = "{org.apache.fineract.portfolio.floatingrate.base-lending-rate.duplicate}";
    private static final String BLR_LINKED = "{org.apache.fineract.portfolio.floatingrate.base-lending-rate.linked}";
    private static final String DIFFERENTIAL_NOT_AVAILABLE = "{org.apache.fineract.portfolio.floatingrate.differential-to-base-lending-rate.not-available}";
    private static final String DIFFERENTIAL_INVALID_FOR_BLR = "{org.apache.fineract.portfolio.floatingrate.differential-to-base-lending-rate.invalid-for-base-lending-rate}";

    private final LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
    private FloatingRateRepository repository;
    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));
        repository = mock(FloatingRateRepository.class);
        validatorFactory = Validation.byDefaultProvider().configure().constraintValidatorFactory(new ConstraintValidatorFactory() {

            @Override
            @SuppressWarnings("unchecked")
            public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
                if (key.equals(FloatingRateCreateValidator.class)) {
                    return (T) new FloatingRateCreateValidator(repository);
                }
                if (key.equals(FloatingRateUpdateValidator.class)) {
                    return (T) new FloatingRateUpdateValidator(repository);
                }
                try {
                    return key.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void releaseInstance(final ConstraintValidator<?, ?> instance) {}
        }).buildValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    public void tearDown() {
        validatorFactory.close();
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void createValidRequestHasNoViolations() {
        final FloatingRateCreateRequest request = createRequest("Floating Rate", null,
                List.of(period(businessDate.plusDays(1), "10", null)));

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void createBlankNameFails() {
        final FloatingRateCreateRequest request = createRequest(" ", null, null);

        assertThat(validate(request)).containsExactly(NAME_NOT_BLANK);
    }

    @Test
    public void createEmptyRatePeriodsFails() {
        final FloatingRateCreateRequest request = createRequest("Floating Rate", null, List.of());

        assertThat(validate(request)).containsExactly(RATE_PERIODS_NOT_EMPTY);
    }

    @Test
    public void createNonFutureFromDateFails() {
        final FloatingRateCreateRequest request = createRequest("Floating Rate", null, List.of(period(businessDate, "10", null)));

        assertThat(validate(request)).containsExactly(FROM_DATE_FUTURE);
    }

    @Test
    public void createUnparseableFromDateFails() {
        final FloatingRatePeriodRequest period = new FloatingRatePeriodRequest("not-a-date", BigDecimal.TEN, null, null, null);
        final FloatingRateCreateRequest request = createRequest("Floating Rate", null, List.of(period));

        assertThat(validate(request)).containsExactly(FROM_DATE_INVALID);
    }

    @Test
    public void createDuplicateFromDatesFail() {
        final LocalDate fromDate = businessDate.plusDays(5);
        final FloatingRateCreateRequest request = createRequest("Floating Rate", null,
                List.of(period(fromDate, "10", null), period(fromDate, "11", null)));

        assertThat(validate(request)).containsExactly(FROM_DATE_DUPLICATE);
    }

    @Test
    public void createNegativeInterestRateFails() {
        final FloatingRateCreateRequest request = createRequest("Floating Rate", null,
                List.of(period(businessDate.plusDays(1), "-1", null)));

        assertThat(validate(request)).containsExactly(INTEREST_RATE_POSITIVE_OR_ZERO);
    }

    @Test
    public void createSecondBaseLendingRateFails() {
        when(repository.retrieveBaseLendingRate()).thenReturn(baseLendingRate(1L));
        final FloatingRateCreateRequest request = createRequest("Floating Rate", true, null);

        assertThat(validate(request)).containsExactly(BLR_DUPLICATE);
    }

    @Test
    public void createDifferentialPeriodWithoutBaseLendingRateFails() {
        final FloatingRateCreateRequest request = createRequest("Floating Rate", null,
                List.of(period(businessDate.plusDays(1), "10", true)));

        assertThat(validate(request)).containsExactly(DIFFERENTIAL_NOT_AVAILABLE);
    }

    @Test
    public void createDifferentialPeriodOnBaseLendingRateItselfFails() {
        when(repository.retrieveBaseLendingRate()).thenReturn(baseLendingRate(1L));
        final FloatingRateCreateRequest request = createRequest("Floating Rate", true,
                List.of(period(businessDate.plusDays(1), "10", true)));

        assertThat(validate(request)).contains(DIFFERENTIAL_INVALID_FOR_BLR);
    }

    @Test
    public void updateBaseLendingRateItselfIsNotADuplicate() {
        final FloatingRate blr = baseLendingRate(1L);
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(blr));
        when(repository.retrieveBaseLendingRate()).thenReturn(blr);
        final FloatingRateUpdateRequest request = updateRequest(1L, "New name", true, true, null);

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void updateOtherRateToBaseLendingRateFails() {
        final FloatingRate other = floatingRate(2L, false, true);
        when(repository.findById(2L)).thenReturn(java.util.Optional.of(other));
        when(repository.retrieveBaseLendingRate()).thenReturn(baseLendingRate(1L));
        final FloatingRateUpdateRequest request = updateRequest(2L, null, true, null, null);

        assertThat(validate(request)).containsExactly(BLR_DUPLICATE);
    }

    @Test
    public void updateDeactivatingLinkedBaseLendingRateFails() {
        final FloatingRate blr = baseLendingRate(1L);
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(blr));
        when(repository.retrieveBaseLendingRate()).thenReturn(blr);
        when(repository.retrieveFloatingRatesLinkedToBLR()).thenReturn(List.of(floatingRate(2L, false, true)));
        final FloatingRateUpdateRequest request = updateRequest(1L, null, null, false, null);

        assertThat(validate(request)).containsExactly(BLR_LINKED);
    }

    @Test
    public void updateBlankNameFails() {
        final FloatingRate rate = floatingRate(2L, false, true);
        when(repository.findById(2L)).thenReturn(java.util.Optional.of(rate));
        final FloatingRateUpdateRequest request = updateRequest(2L, " ", null, null, null);

        assertThat(validate(request)).containsExactly(NAME_NOT_BLANK);
    }

    private Set<String> validate(final Object request) {
        return validator.validate(request).stream().map(ConstraintViolation::getMessageTemplate).collect(Collectors.toSet());
    }

    private FloatingRateCreateRequest createRequest(final String name, final Boolean isBaseLendingRate,
            final List<FloatingRatePeriodRequest> ratePeriods) {
        return new FloatingRateCreateRequest(name, isBaseLendingRate, null, ratePeriods);
    }

    private FloatingRateUpdateRequest updateRequest(final Long id, final String name, final Boolean isBaseLendingRate,
            final Boolean isActive, final List<FloatingRatePeriodRequest> ratePeriods) {
        return new FloatingRateUpdateRequest(id, name, isBaseLendingRate, isActive, ratePeriods);
    }

    private FloatingRatePeriodRequest period(final LocalDate fromDate, final String interestRate,
            final Boolean isDifferentialToBaseLendingRate) {
        return new FloatingRatePeriodRequest(fromDate.toString(), new BigDecimal(interestRate), isDifferentialToBaseLendingRate, null,
                null);
    }

    private FloatingRate baseLendingRate(final Long id) {
        return floatingRate(id, true, true);
    }

    private FloatingRate floatingRate(final Long id, final boolean isBaseLendingRate, final boolean isActive) {
        final FloatingRate floatingRate = new FloatingRate("Rate " + id, isBaseLendingRate, isActive, null);
        ReflectionTestUtils.setField(floatingRate, "id", id);
        return floatingRate;
    }
}
