/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;

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

    public static CollateralData instance(final Long id, final CodeValueData type, final BigDecimal value, final String description) {
        return new CollateralData(id, type, value, description);
    }

    public static CollateralData template(final Collection<CodeValueData> codeValues) {
        return new CollateralData(null, null, null, null, codeValues);
    }

    private CollateralData(final Long id, final CodeValueData type, final BigDecimal value, final String description) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
        this.allowedCollateralTypes = null;
    }

    private CollateralData(final Long id, final CodeValueData type, final BigDecimal value, final String description,
            Collection<CodeValueData> allowedCollateralTypes) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
        this.allowedCollateralTypes = allowedCollateralTypes;
    }

    public CollateralData template(CollateralData collateralData, Collection<CodeValueData> codeValues) {
        return new CollateralData(collateralData.id, collateralData.type, collateralData.value, collateralData.description, codeValues);
    }
}