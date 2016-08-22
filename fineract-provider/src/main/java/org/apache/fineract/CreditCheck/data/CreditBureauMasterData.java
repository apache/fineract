package org.apache.fineract.CreditCheck.data;

public class CreditBureauMasterData {
    
private final long cbID;

private final String cbName;

private final String country;



public static CreditBureauMasterData instance(final Long cbID, final String cbName,final String country) {
    return new CreditBureauMasterData(cbID, cbName,country);
}

private CreditBureauMasterData(final Long cbID, final String cbName,final String country) {
    this.cbID = cbID;
    this.cbName = cbName;
    this.country=country;
    
}




public String getCbName() {
    return this.cbName;
}


public String getCountry() {
    return this.country;
}

public Long getcbID() {
    return this.cbID;
}


}
