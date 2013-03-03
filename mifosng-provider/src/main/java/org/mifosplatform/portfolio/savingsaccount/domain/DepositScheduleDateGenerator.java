/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;

public interface DepositScheduleDateGenerator {

    List<LocalDate> generate(LocalDate startDate, Integer paymentPeriods, Integer depositFrequency, SavingFrequencyType savingFrequencyType);
}