package org.mifosng.platform.loan.domain;

public enum SavingTypeMethod {
	FIXEDTERM_DEPOSITS(0,"savingtype.fixedterm"),OTHER(1,"savingtype.other"),INVALID(2,"savingtype.invalid");
	
	private final Integer value;
	private final String code;
	
	private SavingTypeMethod(final Integer value,final String code){
		
		this.value=value;
		this.code=code;
	}
	
    public Integer getValue() {
        return this.value;
    }
    
    public String getCode() {
		return code;
	}
    
    public static SavingTypeMethod fromInt(final Integer selectedType){
    	SavingTypeMethod savingTypeMethod=null;
    	switch (selectedType) {
		case 0: 
			savingTypeMethod=SavingTypeMethod.FIXEDTERM_DEPOSITS;
			break;
		case 1:
			savingTypeMethod=SavingTypeMethod.OTHER;
			break;

		default:
			savingTypeMethod=SavingTypeMethod.INVALID;
			break;
		}
    	
    	return savingTypeMethod;
    }
	

}
