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

package org.apache.fineract.portfolio.rate.data;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Bowpi GT Created by Jose on 19/07/2017.
 */
@Getter
public final class RateData implements Serializable {

    private Long id;

    private String name;

    private BigDecimal percentage;

    private EnumOptionData productApply;

    private boolean active;

    public static RateData instance(final Long id, final String name, final BigDecimal percentage, final EnumOptionData productApply,
            final boolean active) {
        return new RateData(id, name, percentage, productApply, active);
    }

    private RateData(final Long id, final String name, final BigDecimal percentage, final EnumOptionData productApply,
            final boolean active) {
        this.id = id;
        this.name = name;
        this.percentage = percentage;
        this.productApply = productApply;
        this.active = active;
    }
}
