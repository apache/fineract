/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "acc_gl_office_mapping")
public class OfficeToGLAccountMapping extends AbstractPersistable<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id")
    private GLAccount glAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private Office office;

    @Column(name = "financial_account_type", nullable = false)
    private int financialAccountType;

    public static OfficeToGLAccountMapping createNew(final GLAccount glAccount, final Office office, final int financialAccountType) {
        return new OfficeToGLAccountMapping(glAccount, office, financialAccountType);
    }

    protected OfficeToGLAccountMapping() {
        //
    }

    private OfficeToGLAccountMapping(final GLAccount glAccount, final Office office, final int financialAccountType) {
        this.glAccount = glAccount;
        this.office = office;
        this.financialAccountType = financialAccountType;
    }

    public GLAccount glAccount() {
        return this.glAccount;
    }

    public void updateGlAccount(final GLAccount glAccount) {
        this.glAccount = glAccount;
    }

    public Office office() {
        return this.office;
    }

    public void updateOffice(Office office) {
        this.office = office;
    }

    public int getFinancialAccountType() {
        return this.financialAccountType;
    }


}