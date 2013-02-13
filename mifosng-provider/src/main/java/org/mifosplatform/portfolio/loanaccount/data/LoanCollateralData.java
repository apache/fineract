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