/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.data;

import org.mifosplatform.accounting.glaccount.domain.GLAccountType;

public class FinancialActivityData {

    private final Integer id;
    private final String name;
    private final GLAccountType mappedGLAccountType;

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public GLAccountType getMappedGLAccountType() {
        return this.mappedGLAccountType;
    }

    public FinancialActivityData(Integer id, String name, GLAccountType mappedGLAccountType) {
        super();
        this.id = id;
        this.name = name;
        this.mappedGLAccountType = mappedGLAccountType;
    }

}
