/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.fineract.portfolio.loanaccount.exception.InvalidClientShareInGroupLoanException;
import org.apache.fineract.portfolio.loanaccount.exception.ClientSharesNotEqualToPrincipalAmountException;
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
                	throw new ClientSharesNotEqualToPrincipalAmountException();
                }
    		}else{
    			throw new InvalidClientShareInGroupLoanException();
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
