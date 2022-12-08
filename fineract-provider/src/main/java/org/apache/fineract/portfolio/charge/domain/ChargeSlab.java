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

package org.apache.fineract.portfolio.charge.domain;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_fee_charge_slab")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ChargeSlab extends AbstractPersistableCustom {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_id")
    private Charge charge;

    @Column(name = "period_type_enum")
    private Integer periodType;

    @Column(name = "from_period")
    private Integer fromPeriod;

    @Column(name = "to_period")
    private Integer toPeriod;

    @Column(name = "value")
    private BigDecimal value;

    public static List<ChargeSlab> assembleFrom(JsonCommand command, Charge charge) {

        final List chartList = new ArrayList<>();

        JsonArray array = command.jsonElement("chart").getAsJsonObject().getAsJsonArray("chartSlabs");
        for (JsonElement jsonElement : array) {
            JsonObject obj = jsonElement.getAsJsonObject();

            final Integer fromPeriod = obj.get("fromPeriod").getAsInt();
            Integer toPeriod = null;

            if (obj.has("toPeriod")) {
                toPeriod = obj.get("toPeriod").getAsInt();
            }

            final BigDecimal value = obj.get("feeAmount").getAsBigDecimal();
            chartList.add(new ChargeSlab(charge, null, fromPeriod, toPeriod, value));
        }

        return chartList;
    }
}
