package org.mifosplatform.accounting;

public class AccountingConstants {

    /*** Categories for Accounting **/
    public static enum GL_ACCOUNT_CLASSIFICATION {
        ASSET, LIABILITY, INCOME, EXPENSE, EQUITY;

        @Override
        public String toString() {
            return name().toString();
        }
    }

    /** Types of Journal Entries **/
    public static enum JOURNAL_ENTRY_TYPE {
        DEBIT, CREDIT;

        @Override
        public String toString() {
            return name().toString();
        }
    }

    /** USAGES of Accounts **/
    public static enum GL_ACCOUNT_USAGE {
        HEADER, DETAIL;

        @Override
        public String toString() {
            return name().toString();
        }
    }

}
