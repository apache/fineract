/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.data;


public class ShareChargeData {

    private Long accountId ;
    
    private Long chargeId ;
    
    public ShareChargeData(final Long accountId, final Long chargeId) {
        this.accountId = accountId ;
        this.chargeId = chargeId ;
    }
}
