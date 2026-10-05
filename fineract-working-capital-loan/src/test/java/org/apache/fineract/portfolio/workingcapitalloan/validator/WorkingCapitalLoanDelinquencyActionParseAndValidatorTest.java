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
package org.apache.fineract.portfolio.workingcapitalloan.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanDelinquencyActionParseAndValidatorTest {

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository actionRepository;
    @Mock
    private WorkingCapitalLoan workingCapitalLoan;

    @ParameterizedTest
    @ArgumentsSource(PauseActionArgumentsSource.class)
    void validatePauseValidationIsCorrect(final String name, final WorkingCapitalLoanDelinquencyAction action,
            final List<WorkingCapitalLoanDelinquencyAction> existingActions, final String expectedMessage) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>())
                .resource("workingCapitalLoanDelinquencyAction");
        final WorkingCapitalLoanDelinquencyActionParseAndValidator validator = new WorkingCapitalLoanDelinquencyActionParseAndValidator(
                new FromJsonHelper(), rangeScheduleRepository, actionRepository);

        lenient().when(workingCapitalLoan.getFirstActualDisbursementDate()).thenReturn(LocalDate.of(2026, 9, 1));

        validator.validatePause(action, workingCapitalLoan, existingActions, dataValidator);

        if (expectedMessage != null) {
            assertThat(dataValidator.hasError()).isTrue();
            assertThat(dataValidator.getDataValidationErrors()).hasSize(1);
            assertThat(dataValidator.getDataValidationErrors().get(0).getUserMessageGlobalisationCode()).isEqualTo(expectedMessage);
        } else {
            assertThat(dataValidator.hasError()).isFalse();
            assertThat(dataValidator.getDataValidationErrors()).hasSize(0);
        }
    }

    private static WorkingCapitalLoanDelinquencyAction reset(final LocalDate startDate) {
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(DelinquencyAction.RESET);
        action.setStartDate(startDate);
        return action;
    }

    private static WorkingCapitalLoanDelinquencyAction undoneReset(final LocalDate startDate) {
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(DelinquencyAction.RESET);
        action.setStartDate(startDate);
        action.setEndDate(startDate);
        return action;
    }

    private static WorkingCapitalLoanDelinquencyAction resume(final LocalDate startDate) {
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(DelinquencyAction.RESUME);
        action.setStartDate(startDate);
        return action;
    }

    private static WorkingCapitalLoanDelinquencyAction pause(final LocalDate startDate, final LocalDate endDate) {
        final WorkingCapitalLoanDelinquencyAction action = new WorkingCapitalLoanDelinquencyAction();
        action.setAction(DelinquencyAction.PAUSE);
        action.setStartDate(startDate);
        action.setEndDate(endDate);
        return action;
    }

    private static final class PauseActionArgumentsSource implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) {
            return Stream.of(
                    Arguments.of("overlapping pause indicates validation error",
                            pause(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 20)),
                            List.of(pause(LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 25))),
                            "validation.msg.workingCapitalLoanDelinquencyAction.overlapping"),
                    Arguments.of("missing start date indicates validation error", pause(null, LocalDate.of(2026, 9, 20)), List.of(),
                            "validation.msg.workingCapitalLoanDelinquencyAction.startDate.cannot.be.blank"),
                    Arguments.of("missing end date indicates validation error", pause(LocalDate.of(2026, 9, 15), null), List.of(),
                            "validation.msg.workingCapitalLoanDelinquencyAction.endDate.cannot.be.blank"),
                    Arguments.of("missing start date indicates validation error", pause(null, LocalDate.of(2026, 9, 20)), List.of(),
                            "validation.msg.workingCapitalLoanDelinquencyAction.startDate.cannot.be.blank"),
                    Arguments.of("missing end date indicates validation error", pause(LocalDate.of(2026, 9, 15), null), List.of(),
                            "validation.msg.workingCapitalLoanDelinquencyAction.endDate.cannot.be.blank"),
                    Arguments.of("start date after end date indicates validation error",
                            pause(LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 15)), List.of(),
                            "validation.msg.workingCapitalLoanDelinquencyAction.invalid.start.date.and.end.date"),
                    Arguments.of("start date before first disbursement indicates validation error",
                            pause(LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 5)), List.of(),
                            "validation.msg.workingCapitalLoanDelinquencyAction.startDate.must.be.after.first.disbursal.date"),
                    Arguments.of("no overlapping pauses has no validation error",
                            pause(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 20)),
                            List.of(pause(LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 30))), null),
                    Arguments.of("existing pause shortened by resume does not overlap",
                            pause(LocalDate.of(2026, 9, 13), LocalDate.of(2026, 9, 20)),
                            List.of(pause(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 20)), resume(LocalDate.of(2026, 9, 12))), null),
                    Arguments.of("no existing reset has no validation error", pause(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 20)),
                            List.of(), null),
                    Arguments.of("existing reset before action has no validation error",
                            pause(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 20)), List.of(reset(LocalDate.of(2026, 9, 10))), null),
                    Arguments.of("existing reset on same date has no validation error",
                            pause(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 20)), List.of(reset(LocalDate.of(2026, 9, 15))), null),
                    Arguments.of("existing reset after action start date has validation error",
                            pause(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 20)), List.of(reset(LocalDate.of(2026, 9, 17))),
                            "validation.msg.workingCapitalLoanDelinquencyAction.startDate.reset.exists.after.pause"),
                    Arguments.of("existing undone reset after action start date has no validation error",
                            pause(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 20)), List.of(undoneReset(LocalDate.of(2026, 9, 17))),
                            null));
        }
    }
}
