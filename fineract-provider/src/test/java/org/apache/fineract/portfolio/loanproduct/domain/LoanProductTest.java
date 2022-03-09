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
package org.apache.fineract.portfolio.loanproduct.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import org.apache.fineract.portfolio.loanproduct.exception.LoanProductGeneralRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanProductTest {

    @Test
    public void shouldThrowExceptionWhenAllowMultipleDisbursalsFalseAndDisallowExpectedDisbursementsIsTrue() {

        LoanProduct lp = stubLoanProduct(false);
        lp.setDisallowExpectedDisbursements(true);

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(),
                is("error.msg.allowMultipleDisbursals.not.set.disallowExpectedDisbursements.cant.be.set"));

    }

    @Test
    public void shouldThrowExceptionWhenDisallowExpectedDisbursementIsFalseAndAllowApprovedDisbursedAmountsOverAppliedIsTrue() {

        LoanProduct lp = stubLoanProduct(true);
        lp.setDisallowExpectedDisbursements(false);
        lp.setAllowApprovedDisbursedAmountsOverApplied(true);

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(),
                is("error.msg.disallowExpectedDisbursements.not.set.allowApprovedDisbursedAmountsOverApplied.cant.be.set"));

    }

    @Test
    public void shouldThrowExceptionWhenAllowApprovedDisbursedAmountsOverAppliedIsTrueAndAppliedCalculationTypeIsNull() {

        LoanProduct lp = stubLoanProduct(true);
        lp.setDisallowExpectedDisbursements(true);
        lp.setAllowApprovedDisbursedAmountsOverApplied(true);
        lp.setOverAppliedCalculationType(null);

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(),
                is("error.msg.allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedCalculationType.is.mandatory"));
    }

    @Test
    public void shouldThrowExceptionWhenAllowApprovedDisbursedAmountsOverAppliedIsTrueAndAppliedCalculationTypeIsEmpty() {

        LoanProduct lp = stubLoanProduct(true);
        lp.setDisallowExpectedDisbursements(true);
        lp.setAllowApprovedDisbursedAmountsOverApplied(true);
        lp.setOverAppliedCalculationType("");

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(),
                is("error.msg.allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedCalculationType.is.mandatory"));
    }

    @Test
    public void shouldThrowExceptionWhenAllowApprovedDisbursedAmountsOverAppliedIsFalseAndAppliedCalculationTypeIsNotEmpty() {

        LoanProduct lp = stubLoanProduct(true);
        lp.setDisallowExpectedDisbursements(true);
        lp.setAllowApprovedDisbursedAmountsOverApplied(false);
        lp.setOverAppliedCalculationType("flat");

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(),
                is("error.msg.allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedCalculationType.cant.be.entered"));
    }

    @Test
    public void shouldThrowExceptionWhenOverAppliedCalculationTypeNotValid() {

        LoanProduct lp = stubLoanProduct(true);
        lp.setDisallowExpectedDisbursements(true);
        lp.setAllowApprovedDisbursedAmountsOverApplied(true);
        lp.setOverAppliedCalculationType("notflat");

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(), is("error.msg.overAppliedCalculationType.must.be.percentage.or.flat"));
    }

    @Test
    public void shouldThrowExceptionWhenAllowApprovedDisbursedAmountsOverAppliedIsTrueAndAppliedCalculationNumberIsNull() {

        LoanProduct lp = stubLoanProduct(true);
        lp.setDisallowExpectedDisbursements(true);
        lp.setAllowApprovedDisbursedAmountsOverApplied(true);
        lp.setOverAppliedCalculationType("flat");
        lp.setOverAppliedNumber(null);

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(),
                is("error.msg.allowApprovedDisbursedAmountsOverApplied.is.set.overAppliedNumber.is.mandatory"));
    }

    @Test
    public void shouldThrowExceptionWhenAllowApprovedDisbursedAmountsOverAppliedIsFalseAndAppliedCalculationNumberIsNotNull() {

        LoanProduct lp = stubLoanProduct(true);
        lp.setDisallowExpectedDisbursements(true);
        lp.setAllowApprovedDisbursedAmountsOverApplied(false);
        lp.setOverAppliedNumber(80);

        LoanProductGeneralRuleException expectedEx = assertThrows(LoanProductGeneralRuleException.class,
                () -> lp.validateLoanProductPreSave());

        assertThat(expectedEx.getGlobalisationMessageCode(),
                is("error.msg.allowApprovedDisbursedAmountsOverApplied.is.not.set.overAppliedNumber.cant.be.entered"));
    }

    private LoanProduct stubLoanProduct(boolean allowMultipleDisbursals) {
        LoanProduct lp = new LoanProduct();
        lp.setLoanProducTrancheDetails(new LoanProductTrancheDetails(allowMultipleDisbursals, null, null));
        return lp;
    }

}
