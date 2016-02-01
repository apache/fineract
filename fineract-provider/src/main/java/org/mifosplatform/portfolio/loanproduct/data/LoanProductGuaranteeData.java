/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.data;

import java.math.BigDecimal;

public class LoanProductGuaranteeData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long productId;
    @SuppressWarnings("unused")
    private final BigDecimal mandatoryGuarantee;
    @SuppressWarnings("unused")
    private final BigDecimal minimumGuaranteeFromOwnFunds;
    @SuppressWarnings("unused")
    private final BigDecimal minimumGuaranteeFromGuarantor;

    public static LoanProductGuaranteeData instance(final Long id, final Long productId, final BigDecimal mandatoryGuarantee,
            final BigDecimal minimumGuaranteeFromOwnFunds, final BigDecimal minimumGuaranteeFromGuarantor) {
        return new LoanProductGuaranteeData(id, productId, mandatoryGuarantee, minimumGuaranteeFromOwnFunds, minimumGuaranteeFromGuarantor);
    }

    public static LoanProductGuaranteeData sensibleDefaultsForNewLoanProductCreation() {
        return new LoanProductGuaranteeData(null, null, null, null, null);
    }

    private LoanProductGuaranteeData(final Long id, final Long productId, final BigDecimal mandatoryGuarantee,
            final BigDecimal minimumGuaranteeFromOwnFunds, final BigDecimal minimumGuaranteeFromGuarantor) {
        this.id = id;
        this.productId = productId;
        this.mandatoryGuarantee = mandatoryGuarantee;
        this.minimumGuaranteeFromGuarantor = minimumGuaranteeFromGuarantor;
        this.minimumGuaranteeFromOwnFunds = minimumGuaranteeFromOwnFunds;
    }

}
