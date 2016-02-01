/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for Collateral data.
 */
public class CollateralData {

    private final Long id;
    private final CodeValueData type;
    private final BigDecimal value;
    private final String description;
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> allowedCollateralTypes;
    private final CurrencyData currency;

    public static CollateralData instance(final Long id, final CodeValueData type, final BigDecimal value, final String description,
            final CurrencyData currencyData) {
        return new CollateralData(id, type, value, description, currencyData);
    }

    public static CollateralData template(final Collection<CodeValueData> codeValues) {
        return new CollateralData(null, null, null, null, null, codeValues);
    }

    private CollateralData(final Long id, final CodeValueData type, final BigDecimal value, final String description,
            final CurrencyData currencyData) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
        this.currency = currencyData;
        this.allowedCollateralTypes = null;
    }

    private CollateralData(final Long id, final CodeValueData type, final BigDecimal value, final String description,
            final CurrencyData currencyData, final Collection<CodeValueData> allowedCollateralTypes) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
        this.currency = currencyData;
        this.allowedCollateralTypes = allowedCollateralTypes;
    }

    public CollateralData template(final CollateralData collateralData, final Collection<CodeValueData> codeValues) {
        return new CollateralData(collateralData.id, collateralData.type, collateralData.value, collateralData.description,
                collateralData.currency, codeValues);
    }
}