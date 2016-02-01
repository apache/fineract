/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.portfolio.charge.domain.Charge;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_shareaccounts_charges")
public class ShareAccountCharge extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private ShareAccount shareAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable = false)
    private Charge charge;

    protected ShareAccountCharge() {

    }

    public ShareAccountCharge(final ShareAccount shareAccount, final Charge charge) {
        this.shareAccount = shareAccount;
        this.charge = charge;
    }

    public ShareAccountCharge(final Charge charge) {
        this.charge = charge;
    }

    public void setShareAccount(ShareAccount shareAccount) {
        this.shareAccount = shareAccount;
    }
    
    public Long getAccountId() {
        return this.shareAccount.getId() ;
    }
    
    public Long getChargeId() {
        return this.charge.getId() ;
    }
}
