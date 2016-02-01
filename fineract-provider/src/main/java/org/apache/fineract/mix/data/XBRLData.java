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
package org.apache.fineract.mix.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;

public class XBRLData {

    private final HashMap<MixTaxonomyData, BigDecimal> resultMap;
    private final Date startDate;
    private final Date endDate;
    private final String currency;

    public XBRLData(final HashMap<MixTaxonomyData, BigDecimal> resultMap, final Date startDate, final Date endDate, final String currency) {
        this.resultMap = resultMap;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currency = currency;
    }

    public HashMap<MixTaxonomyData, BigDecimal> getResultMap() {
        return this.resultMap;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public String getCurrency() {
        return this.currency;
    }
}