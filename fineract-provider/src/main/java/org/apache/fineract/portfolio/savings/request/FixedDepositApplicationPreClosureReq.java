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

package org.apache.fineract.portfolio.savings.request;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosureChargeApplicableParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalApplicableParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.preClosurePenalInterestParamName;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.api.JsonCommand;

public class FixedDepositApplicationPreClosureReq {

    private boolean preClosureChargeApplicable = false;
    private boolean preClosurePenalApplicable = false;
    private BigDecimal preClosurePenalInterest = null;
    private Integer preClosurePenalInterestOnTypeId = null;
    private boolean preClosurePenalApplicableParamSet = false;
    private boolean preClosurePenalInterestParamSet = false;
    private boolean preClosureChargeApplicableParamSet = false;
    private boolean preClosurePenalInterestOnTypeIdPramSet = false;

    public static FixedDepositApplicationPreClosureReq instance(JsonCommand command) {
        FixedDepositApplicationPreClosureReq instance = new FixedDepositApplicationPreClosureReq();

        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            instance.preClosurePenalApplicableParamSet = true;
            instance.preClosurePenalApplicable = command.booleanObjectValueOfParameterNamed(preClosurePenalApplicableParamName);
            if (instance.preClosurePenalApplicable) {
                if (command.parameterExists(preClosurePenalInterestParamName)) {
                    instance.preClosurePenalInterestParamSet = true;
                    instance.preClosurePenalInterest = command.bigDecimalValueOfParameterNamed(preClosurePenalInterestParamName);
                }
                if (command.parameterExists(preClosurePenalInterestParamName)) {
                    instance.preClosurePenalInterestOnTypeIdPramSet = true;
                    instance.preClosurePenalInterestOnTypeId = command
                            .integerValueOfParameterNamed(preClosurePenalInterestOnTypeIdParamName);
                }
            }
        }

        if (command.parameterExists(preClosurePenalApplicableParamName)) {
            instance.preClosureChargeApplicableParamSet = true;
            instance.preClosureChargeApplicable = command.booleanObjectValueOfParameterNamed(preClosureChargeApplicableParamName);
        }

        return instance;
    }

    public boolean isPreClosurePenalApplicable() {
        return preClosurePenalApplicable;
    }

    public void setPreClosurePenalApplicable(boolean preClosurePenalApplicable) {
        this.preClosurePenalApplicable = preClosurePenalApplicable;
    }

    public BigDecimal getPreClosurePenalInterest() {
        return preClosurePenalInterest;
    }

    public void setPreClosurePenalInterest(BigDecimal preClosurePenalInterest) {
        this.preClosurePenalInterest = preClosurePenalInterest;
    }

    public Integer getPreClosurePenalInterestOnTypeId() {
        return preClosurePenalInterestOnTypeId;
    }

    public void setPreClosurePenalInterestOnTypeId(Integer preClosurePenalInterestOnTypeId) {
        this.preClosurePenalInterestOnTypeId = preClosurePenalInterestOnTypeId;
    }

    public boolean isPreClosurePenalApplicableParamSet() {
        return preClosurePenalApplicableParamSet;
    }

    public void setPreClosurePenalApplicableParamSet(boolean preClosurePenalApplicableParamSet) {
        this.preClosurePenalApplicableParamSet = preClosurePenalApplicableParamSet;
    }

    public boolean isPreClosurePenalInterestParamSet() {
        return preClosurePenalInterestParamSet;
    }

    public void setPreClosurePenalInterestParamSet(boolean preClosurePenalInterestParamSet) {
        this.preClosurePenalInterestParamSet = preClosurePenalInterestParamSet;
    }

    public boolean isPreClosurePenalInterestOnTypeIdPramSet() {
        return preClosurePenalInterestOnTypeIdPramSet;
    }

    public void setPreClosurePenalInterestOnTypeIdPramSet(boolean preClosurePenalInterestOnTypeIdPramSet) {
        this.preClosurePenalInterestOnTypeIdPramSet = preClosurePenalInterestOnTypeIdPramSet;
    }

    public boolean isPreClosureChargeApplicable() {
        return preClosureChargeApplicable;
    }

    public boolean isPreClosureChargeApplicableParamSet() {
        return preClosureChargeApplicableParamSet;
    }

}
