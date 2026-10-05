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
package org.apache.fineract.cob.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanModelProcessingService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.listener.StepListener;
import org.springframework.batch.core.listener.StepListenerFactoryBean;

/**
 * Guards the COB item components against double step-listener registration.
 * <p>
 * {@code ChunkOrientedStepBuilder.build()} passes the reader, processor and writer through
 * {@code addAsStreamAndListener}, which registers a component twice when it both implements
 * {@link StepExecutionListener} <em>and</em> carries {@code @BeforeStep}/{@code @AfterStep}: once as the instance
 * itself, and once as the distinct proxy that {@link StepListenerFactoryBean#getListener(Object)} builds for the
 * annotations. Two entries in the builder's listener set means {@code beforeStep}/{@code afterStep} fire twice per step
 * - for {@code LoanItemReader} that is two extra SELECTs per partition and a {@code remainingData} queue rebuilt from a
 * second lock snapshot.
 * <p>
 * Implementing the interface <em>without</em> the annotations makes {@code getListener} return the delegate itself, so
 * the builder's {@code LinkedHashSet} collapses both registrations into one. This test pins that shape: it fails if
 * anyone re-adds the annotations alongside the interface.
 */
class CobStepListenerRegistrationTest {

    private static Stream<Arguments> itemComponents() {
        LoanRepository loanRepository = mock(LoanRepository.class);
        BeforeStepLockingItemReaderHelper lockingHelper = mock(BeforeStepLockingItemReaderHelper.class);
        COBBusinessStepService businessStepService = mock(COBBusinessStepService.class);
        ProgressiveLoanModelProcessingService modelProcessingService = mock(ProgressiveLoanModelProcessingService.class);

        return Stream.of(//
                Arguments.of("LoanItemReader", new LoanItemReader(loanRepository, lockingHelper)), //
                Arguments.of("InlineCOBLoanItemReader", new InlineCOBLoanItemReader(loanRepository)), //
                Arguments.of("LoanItemProcessor", new LoanItemProcessor(businessStepService, modelProcessingService)), //
                Arguments.of("InlineCOBLoanItemProcessor", new InlineCOBLoanItemProcessor(businessStepService, modelProcessingService)));
    }

    @ParameterizedTest(name = "{0} registers exactly once as a step listener")
    @MethodSource("itemComponents")
    void shouldRegisterExactlyOnceAsStepListener(String name, Object itemComponent) {
        assertThat(itemComponent).as("%s must implement StepExecutionListener so the builder can register it", name)
                .isInstanceOf(StepExecutionListener.class);
        assertThat(StepListenerFactoryBean.isListener(itemComponent)).as("%s must be recognised as a step listener", name).isTrue();

        // getListener must hand back the delegate itself, not an annotation proxy - a proxy is a second,
        // distinct object and would survive alongside the instance in the builder's listener set.
        StepListener resolved = StepListenerFactoryBean.getListener(itemComponent);
        assertThat(resolved)
                .as("%s resolves to an annotation proxy, so it would be registered twice - "
                        + "remove the @BeforeStep/@AfterStep annotations and rely on the StepExecutionListener interface", name)
                .isSameAs(itemComponent);

        // Mirrors how ChunkOrientedStepBuilder.addAsStreamAndListener accumulates listeners.
        Set<StepListener> stepListeners = new LinkedHashSet<>();
        stepListeners.add((StepListener) itemComponent);
        stepListeners.add(StepListenerFactoryBean.getListener(itemComponent));
        assertThat(stepListeners).as("%s would fire beforeStep/afterStep more than once per step", name).hasSize(1);
    }
}
