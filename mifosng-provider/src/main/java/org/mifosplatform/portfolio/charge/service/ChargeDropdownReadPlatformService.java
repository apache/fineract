/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public interface ChargeDropdownReadPlatformService {

    List<EnumOptionData> retrieveCalculationTypes();

    List<EnumOptionData> retrieveApplicableToTypes();

    List<EnumOptionData> retrieveCollectionTimeTypes();

    List<EnumOptionData> retrivePaymentModes();

    List<EnumOptionData> retrieveLoanCalculationTypes();

    List<EnumOptionData> retrieveLoanCollectionTimeTypes();

    List<EnumOptionData> retrieveSavingsCalculationTypes();

    List<EnumOptionData> retrieveSavingsCollectionTimeTypes();

}
