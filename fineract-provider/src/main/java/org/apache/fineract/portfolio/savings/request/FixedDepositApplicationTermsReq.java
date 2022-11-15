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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.autoRolloverParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.maxDepositTermTypeIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.minDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.minDepositTermTypeIdParamName;

import org.apache.fineract.infrastructure.core.api.JsonCommand;

public class FixedDepositApplicationTermsReq {

    private Integer minDepositTerm = null;
    private boolean minDepositTermSet = false;
    private Integer maxDepositTerm = null;
    private boolean maxDepositTermSet = false;
    private Integer minDepositTermTypeId = null;
    private boolean minDepositTermTypeIdSet = false;
    private Integer maxDepositTermTypeId = null;
    private boolean maxDepositTermTypeIdSet = false;
    private Integer inMultiplesOfDepositTerm = null;
    private boolean inMultiplesOfDepositTermSet = false;
    private Integer inMultiplesOfDepositTermTypeId = null;
    private boolean inMultiplesOfDepositTermTypeIdSet = false;
    private boolean autoRollover = false;

    public static FixedDepositApplicationTermsReq instance(JsonCommand command) {
        FixedDepositApplicationTermsReq instance = new FixedDepositApplicationTermsReq();

        if (command.parameterExists(minDepositTermParamName)) {
            instance.minDepositTermSet = true;
            instance.minDepositTerm = command.integerValueOfParameterNamed(minDepositTermParamName);
        }
        if (command.parameterExists(maxDepositTermParamName)) {
            instance.maxDepositTermSet = true;
            instance.maxDepositTerm = command.integerValueOfParameterNamed(maxDepositTermParamName);
        }
        if (command.parameterExists(minDepositTermTypeIdParamName)) {
            instance.minDepositTermTypeIdSet = true;
            instance.minDepositTermTypeId = command.integerValueOfParameterNamed(minDepositTermTypeIdParamName);
        }
        if (command.parameterExists(maxDepositTermTypeIdParamName)) {
            instance.maxDepositTermTypeIdSet = true;
            instance.maxDepositTermTypeId = command.integerValueOfParameterNamed(maxDepositTermTypeIdParamName);
        }
        if (command.parameterExists(inMultiplesOfDepositTermParamName)) {
            instance.inMultiplesOfDepositTermSet = true;
            instance.inMultiplesOfDepositTerm = command.integerValueOfParameterNamed(inMultiplesOfDepositTermParamName);
        }
        if (command.parameterExists(inMultiplesOfDepositTermTypeIdParamName)) {
            instance.inMultiplesOfDepositTermTypeIdSet = true;
            instance.inMultiplesOfDepositTermTypeId = command.integerValueOfParameterNamed(inMultiplesOfDepositTermTypeIdParamName);
        }
        if (command.parameterExists(autoRolloverParamName)) {
            instance.autoRollover = command.booleanObjectValueOfParameterNamed(autoRolloverParamName);
        }

        return instance;
    }

    public Integer getMinDepositTerm() {
        return minDepositTerm;
    }

    public void setMinDepositTerm(Integer minDepositTerm) {
        this.minDepositTerm = minDepositTerm;
    }

    public boolean isMinDepositTermSet() {
        return minDepositTermSet;
    }

    public Integer getMaxDepositTerm() {
        return maxDepositTerm;
    }

    public void setMaxDepositTerm(Integer maxDepositTerm) {
        this.maxDepositTerm = maxDepositTerm;
    }

    public boolean isMaxDepositTermSet() {
        return maxDepositTermSet;
    }

    public Integer getMinDepositTermTypeId() {
        return minDepositTermTypeId;
    }

    public void setMinDepositTermTypeId(Integer minDepositTermTypeId) {
        this.minDepositTermTypeId = minDepositTermTypeId;
    }

    public boolean isMinDepositTermTypeIdSet() {
        return minDepositTermTypeIdSet;
    }

    public Integer getMaxDepositTermTypeId() {
        return maxDepositTermTypeId;
    }

    public void setMaxDepositTermTypeId(Integer maxDepositTermTypeId) {
        this.maxDepositTermTypeId = maxDepositTermTypeId;
    }

    public boolean isMaxDepositTermTypeIdSet() {
        return maxDepositTermTypeIdSet;
    }

    public Integer getInMultiplesOfDepositTerm() {
        return inMultiplesOfDepositTerm;
    }

    public void setInMultiplesOfDepositTerm(Integer inMultiplesOfDepositTerm) {
        this.inMultiplesOfDepositTerm = inMultiplesOfDepositTerm;
    }

    public boolean isInMultiplesOfDepositTermSet() {
        return inMultiplesOfDepositTermSet;
    }

    public Integer getInMultiplesOfDepositTermTypeId() {
        return inMultiplesOfDepositTermTypeId;
    }

    public void setInMultiplesOfDepositTermTypeId(Integer inMultiplesOfDepositTermTypeId) {
        this.inMultiplesOfDepositTermTypeId = inMultiplesOfDepositTermTypeId;
    }

    public boolean isInMultiplesOfDepositTermTypeIdSet() {
        return inMultiplesOfDepositTermTypeIdSet;
    }

    public boolean isAutoRollover() {
        return autoRollover;
    }
}
