/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.service;

import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;

public interface CurrencyReadPlatformService {

    Collection<CurrencyData> retrieveAllowedCurrencies();

    Collection<CurrencyData> retrieveAllPlatformCurrencies();
}