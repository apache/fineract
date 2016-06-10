package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.fineract.portfolio.loanaccount.exception.EachClientShareMustBeGreaterThanZeroException;
import org.apache.fineract.portfolio.loanaccount.exception.SumOfEachClientShareMustBeEqualToPrincipalAmountException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class GroupLoanIndividualMonitoringDataValidator {
	
	private final FromJsonHelper fromApiJsonHelper;
    
    @Autowired
    public GroupLoanIndividualMonitoringDataValidator(final FromJsonHelper fromApiJsonHelper){
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public static void validateForGroupLoanIndividualMonitoring(final JsonCommand command, String totalAmountType) {        
    	JsonArray clientMembers = command.arrayOfParameterNamed(LoanApiConstants.clientMembersParamName);
    	if(!isClientsDoNotHaveAmount(clientMembers)){
    		if(isClientsAmountValid(clientMembers)){
    			BigDecimal totalAmount = BigDecimal.ZERO;
            	for(JsonElement clientMember :clientMembers) {            	
                	JsonObject member = clientMember.getAsJsonObject();
                		totalAmount = totalAmount.add(member.get(LoanApiConstants.amountParamName).getAsBigDecimal());
            	}
            	if(command.bigDecimalValueOfParameterNamed(totalAmountType).doubleValue()!=totalAmount.doubleValue()){
                	throw new SumOfEachClientShareMustBeEqualToPrincipalAmountException();
                }
    		}else{
    			throw new EachClientShareMustBeGreaterThanZeroException();
    		}
    	}
    	
    }

    public static boolean isClientsAmountValid(JsonArray clientMembers){
    	boolean isValidAmount = true;
    	for(JsonElement clientMember :clientMembers) {            	
           	JsonObject member = clientMember.getAsJsonObject();
           	if(!(member.has(LoanApiConstants.amountParamName) &&
               			member.get(LoanApiConstants.amountParamName).getAsBigDecimal().doubleValue()>0)){
               		isValidAmount = false;
              }
          }    	
    	
    	return isValidAmount;
    }
    
    public static boolean isClientsDoNotHaveAmount(JsonArray clientMembers){
    	boolean isAmountNotPresent = true;
    	for(JsonElement clientMember :clientMembers) {            	
        	JsonObject member = clientMember.getAsJsonObject();
        	if(member.has(LoanApiConstants.amountParamName)){
        		isAmountNotPresent = false;break;
        	}
        }
    	return isAmountNotPresent;
    }
    
}
