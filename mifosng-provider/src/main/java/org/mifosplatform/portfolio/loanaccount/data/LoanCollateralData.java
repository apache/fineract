/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object for loan collateral data.
 */
public class LoanCollateralData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final CodeValueData type;
    @SuppressWarnings("unused")
    private final String description;

    public static LoanCollateralData instance(final Long id, final CodeValueData type, final String description) {
        return new LoanCollateralData(id, type, description);
    }

    private LoanCollateralData(final Long id, final CodeValueData type, final String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }
}