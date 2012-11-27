package org.mifosplatform.accounting;

public class AccountingConstants {

    /*** Categories for Accounting **/
    public static enum GL_ACCOUNT_CLASSIFICATION {
        ASSETS, LIABILITIES, INCOME, EXPENDITURE, EQUITY;

        @Override
        public String toString() {
            return name().toString();
        }
    }

}
