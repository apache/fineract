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
package org.apache.fineract.portfolio.interestratechart;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface InterestIncentiveApiConstants {

    public static final String idParamName = "id";
    public static final String entityTypeParamName = "entityType";
    public static final String attributeNameParamName = "attributeName";
    public static final String conditionTypeParamName = "conditionType";
    public static final String attributeValueParamName = "attributeValue";
    public static final String incentiveTypeparamName = "incentiveType";
    public static final String amountParamName = "amount";
    public static final String deleteParamName = "delete";
    
    public static final String INCENTIVE_RESOURCE_NAME = "interest.rate.incentives";

    public static final Set<String> INTERESTRATE_INCENTIVE_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(idParamName,
            entityTypeParamName, attributeNameParamName, conditionTypeParamName, attributeValueParamName, incentiveTypeparamName,
            amountParamName));

}
