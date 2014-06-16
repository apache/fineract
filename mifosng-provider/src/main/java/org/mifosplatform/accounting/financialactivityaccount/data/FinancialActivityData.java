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
