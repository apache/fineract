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
package org.apache.fineract.interoperation.data;

import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_BALANCE_OF_PAYMENTS;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_INITIATOR;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_INITIATOR_TYPE;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_REFUND_INFO;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_SCENARIO;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_SUB_SCENARIO;

import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.interoperation.domain.InteropInitiatorType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.interoperation.domain.InteropTransactionScenario;

public class InteropTransactionTypeData {

    public static final List<String> PARAMS = List.copyOf(Arrays.asList(PARAM_SCENARIO, PARAM_SUB_SCENARIO, PARAM_INITIATOR,
            PARAM_INITIATOR_TYPE, PARAM_REFUND_INFO, PARAM_BALANCE_OF_PAYMENTS));

    @NotNull
    private final InteropTransactionScenario scenario;

    private final String subScenario;
    @NotNull
    private final InteropTransactionRole initiator;
    @NotNull
    private final InteropInitiatorType initiatorType;
    // TODO: SKIP FOR NOW
    @Valid
    private InteropRefundData refundInfo;

    private String balanceOfPayments; // 3 digits number, see
                                      // https://www.imf.org/external/np/sta/bopcode/

    InteropTransactionTypeData(InteropTransactionScenario scenario, String subScenario, InteropTransactionRole initiator,
            InteropInitiatorType initiatorType, InteropRefundData refundInfo, String balanceOfPayments) {
        this.scenario = scenario;
        this.subScenario = subScenario;
        this.initiator = initiator;
        this.initiatorType = initiatorType;
        this.refundInfo = refundInfo;
        this.balanceOfPayments = balanceOfPayments;
    }

    private InteropTransactionTypeData(InteropTransactionScenario scenario, String subScenario, InteropTransactionRole initiator,
            InteropInitiatorType initiatorType) {
        this(scenario, subScenario, initiator, initiatorType, null, null);
    }

    public InteropTransactionScenario getScenario() {
        return scenario;
    }

    public String getSubScenario() {
        return subScenario;
    }

    public InteropTransactionRole getInitiator() {
        return initiator;
    }

    public InteropInitiatorType getInitiatorType() {
        return initiatorType;
    }

    public static InteropTransactionTypeData validateAndParse(DataValidatorBuilder dataValidator, JsonObject element,
            FromJsonHelper jsonHelper) {
        if (element == null) {
            return null;
        }

        jsonHelper.checkForUnsupportedParameters(element, PARAMS);

        String scenarioString = jsonHelper.extractStringNamed(PARAM_SCENARIO, element);
        DataValidatorBuilder dataValidatorCopy = dataValidator.reset().parameter(PARAM_SCENARIO).value(scenarioString).notBlank();
        InteropTransactionScenario scenario = InteropTransactionScenario.valueOf(scenarioString);

        String subScenario = jsonHelper.extractStringNamed(PARAM_SUB_SCENARIO, element);

        String initiatorString = jsonHelper.extractStringNamed(PARAM_INITIATOR, element);
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_INITIATOR).value(initiatorString).notBlank();
        InteropTransactionRole initiator = InteropTransactionRole.valueOf(initiatorString);

        String initiatorTypeString = jsonHelper.extractStringNamed(PARAM_INITIATOR_TYPE, element);
        dataValidatorCopy = dataValidatorCopy.reset().parameter(PARAM_INITIATOR_TYPE).value(initiatorTypeString).notBlank();
        InteropInitiatorType initiatorType = InteropInitiatorType.valueOf(initiatorTypeString);

        dataValidator.merge(dataValidatorCopy);
        return dataValidator.hasError() ? null : new InteropTransactionTypeData(scenario, subScenario, initiator, initiatorType);
    }
}
