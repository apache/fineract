package org.apache.fineract.CreditCheck.data;

import java.sql.Blob;

public class CreditResponse 
{
private final double outstandingLoanAmount;
private final int numberOfActiveLoans;
private final int loansInBadStanding;
private final int numberOfMissedEMI;
private final double totalEMIperMonth;
private final Blob document;

public CreditResponse(final double outstandingLoanAmount,final int numberOfActiveLoans,final int loansInBadStanding,
        final int numberOfMissedEMI,final double totalEMIperMonth,final Blob document )
{
            this.outstandingLoanAmount=outstandingLoanAmount;
            this.numberOfActiveLoans=numberOfActiveLoans;
            this.loansInBadStanding=loansInBadStanding;
            this.numberOfMissedEMI=numberOfMissedEMI;
            this.totalEMIperMonth=totalEMIperMonth;
            this.document=document;
}


public double getOutstandingLoanAmount() {
    return this.outstandingLoanAmount;
}


public int getNumberOfActiveLoans() {
    return this.numberOfActiveLoans;
}


public int getLoansInBadStanding() {
    return this.loansInBadStanding;
}


public int getNumberOfMissedEMI() {
    return this.numberOfMissedEMI;
}


public double getTotalEMIperMonth() {
    return this.totalEMIperMonth;
}


public Blob getDocument() {
    return this.document;
}





}
