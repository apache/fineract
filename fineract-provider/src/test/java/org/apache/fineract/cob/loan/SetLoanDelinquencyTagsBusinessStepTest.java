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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for {@link SetLoanDelinquencyTagsBusinessStep}
 */
@ExtendWith(MockitoExtension.class)
public class SetLoanDelinquencyTagsBusinessStepTest {

    /**
     * The mock {@link LoanAccountDomainService} class.
     */
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    @Mock
    private DelinquencyReadPlatformService delinquencyReadPlatformService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    /**
     * The class under test.
     */
    private SetLoanDelinquencyTagsBusinessStep underTest;

    /**
     * The loan delinquency classification step enum name.
     */
    private static final String BUSINESS_STEP_ENUM_NAME = "LOAN_DELINQUENCY_CLASSIFICATION";

    /**
     * The loan delinquency classification step readable name.
     */
    private static final String BUSINESS_STEP_READABLE_NAME = "Loan Delinquency Classification";

    /**
     * Setup context before each test.
     */
    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()),
                BusinessDateType.COB_DATE, LocalDate.now(ZoneId.systemDefault()))));
        underTest = new SetLoanDelinquencyTagsBusinessStep(loanAccountDomainService, delinquencyEffectivePauseHelper,
                delinquencyReadPlatformService, businessEventNotifierService);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    /**
     * Tests {@link SetLoanDelinquencyTagsBusinessStep#execute(Loan)} success scenario.
     *
     * @throws Exception
     *             for any failures.
     */
    @Test
    public void testExecuteSuccessScenario() throws Exception {
        // given
        doNothing().when(loanAccountDomainService).setLoanDelinquencyTag(any(Loan.class), any(LocalDate.class), anyList());
        Loan loanForProcessing = createLoan();

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);

        // then
        verify(loanAccountDomainService).setLoanDelinquencyTag(any(Loan.class), any(LocalDate.class), anyList());
        assertEquals(processedLoan, loanForProcessing);
    }

    /**
     * Tests {@link SetLoanDelinquencyTagsBusinessStep#execute(Loan)} when loan is null.
     *
     * @throws Exception
     *             for any failures.
     */
    @Test
    public void testNullLoanScenario() {
        // given
        Loan loanForProcessing = null;

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);

        // then
        verifyNoInteractions(loanAccountDomainService);
        assertNull(processedLoan);
    }

    /**
     * Tests {@link SetLoanDelinquencyTagsBusinessStep#execute(Loan)} when exception is thrown.
     *
     * @throws Exception
     *             for any failures.
     */
    @Test
    public void testExecuteWhenSetLoanDelinquencyTagFails() throws Exception {
        // given
        doThrow(new RuntimeException()).when(loanAccountDomainService).setLoanDelinquencyTag(any(Loan.class), any(LocalDate.class),
                anyList());
        Loan loanForProcessing = createLoan();

        // when
        final Throwable thrownException = assertThrows(RuntimeException.class, () -> underTest.execute(loanForProcessing));

        // then
        verify(loanAccountDomainService).setLoanDelinquencyTag(any(Loan.class), any(LocalDate.class), anyList());
        assertTrue(thrownException.getClass().isAssignableFrom(RuntimeException.class));
    }

    /**
     * Tests {@link SetLoanDelinquencyTagsBusinessStep#getEnumStyledName()}
     */
    @Test
    public void testGetEnumStyledNameSuccessScenario() {
        final String actualEnumName = underTest.getEnumStyledName();

        assertNotNull(actualEnumName);
        assertEquals(BUSINESS_STEP_ENUM_NAME, actualEnumName);
    }

    /**
     * Tests {@link SetLoanDelinquencyTagsBusinessStep#getHumanReadableName()}
     */
    @Test
    public void testGetHumanReadableNameSuccessScenario() {
        final String actualEnumName = underTest.getHumanReadableName();

        assertNotNull(actualEnumName);
        assertEquals(BUSINESS_STEP_READABLE_NAME, actualEnumName);
    }

    /**
     * creates a new {@link Loan} with random values.
     *
     * @return the new {@link Loan} instance
     * @throws Exception
     *             for any failures
     */
    private Loan createLoan() throws Exception {
        Map<String, Object> loanDataMap = new HashMap<>();
        loanDataMap.put("id", RandomUtils.nextLong(1, 1000));
        loanDataMap.put("externalId", ExternalId.generate());
        loanDataMap.put("accountNumber", RandomStringUtils.randomNumeric(10));
        return setupLoanData(loanDataMap);
    }

    /**
     * Sets up and returns the {@link Loan} with provided product details.
     *
     * @param loanDataMap
     *            map with loan key value
     * @return the {@link Loan} instance
     * @throws Exception
     *             for any failures
     */
    protected Loan setupLoanData(final Map<String, Object> loanDataMap) throws Exception {
        final Constructor<Loan> constructor = Loan.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final Loan loan = constructor.newInstance();
        loanDataMap.forEach((key, value) -> ReflectionTestUtils.setField(loan, key, value));
        return loan;
    }
}
