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
package org.apache.fineract.portfolio.loanproduct.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanproduct.domain.LendingStrategy;

public final class LendingStrategyEnumerations {

    private LendingStrategyEnumerations() {

    }

    public static EnumOptionData lendingStrategy(final Integer id) {
        return lendingStrategy(LendingStrategy.fromInt(id));
    }

    public static EnumOptionData lendingStrategy(final LendingStrategy type) {
        return switch (type) {
            case INDIVIDUAL_LOAN -> new EnumOptionData(type.getId().longValue(), type.getCode(), "Individual loan");
            case GROUP_LOAN -> new EnumOptionData(type.getId().longValue(), type.getCode(), "Group loan");
            case JOINT_LIABILITY_LOAN -> new EnumOptionData(type.getId().longValue(), type.getCode(), "Joint liability loan");
            case LINKED_LOAN -> new EnumOptionData(type.getId().longValue(), type.getCode(), "Linked loan");
            default -> new EnumOptionData(LendingStrategy.INVALID.getId().longValue(), LendingStrategy.INVALID.getCode(), "Invalid");
        };
    }

}
