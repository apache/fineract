package org.mifosplatform.integrationtests.common.accounting;

public class Account {

    public enum AccountType {
        ASSET("1"), INCOME("4"), EXPENSE("5"), LIABILITY("2"),EQUITY("3");

        private final String accountValue ;

        AccountType(final String accountValue){
          this.accountValue=accountValue;
        }
        @Override
        public String toString() {
            return accountValue;
        }
    }

    private final AccountType accountType;
    private final Integer accountID;


    public Account(Integer accountID, AccountType accountType) {
        this.accountID = accountID;
        this.accountType = accountType;
    }

    public AccountType getAccountType(){
        return this.accountType;
    }

    public Integer getAccountID(){
        return this.accountID;
    }
}
