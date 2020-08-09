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

package org.apache.fineract.portfolio.rate.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.rate.domain.RateAppliesTo;

public final class RateEnumerations {

    private RateEnumerations() {

    }

    public static EnumOptionData rateAppliesTo(final int id) {
        return rateAppliesTo(RateAppliesTo.fromInt(id));
    }

    public static EnumOptionData rateAppliesTo(final RateAppliesTo type) {
        EnumOptionData optionData = null;
        switch (type) {
            case LOAN:
                optionData = new EnumOptionData(RateAppliesTo.LOAN.getValue().longValue(), RateAppliesTo.LOAN.getCode(), "Loan");
            break;
            default:
                optionData = new EnumOptionData(RateAppliesTo.INVALID.getValue().longValue(), RateAppliesTo.INVALID.getCode(), "Invalid");
            break;
        }
        return optionData;
    }

}
