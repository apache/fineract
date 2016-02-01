/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

public class PaidInAdvanceData {
        
        private final BigDecimal paidInAdvance;
        
        public PaidInAdvanceData(final BigDecimal paidInAdvance) {
                this.paidInAdvance = paidInAdvance;
        }

        public BigDecimal getPaidInAdvance() {
                return paidInAdvance;
        }
        
        
}
