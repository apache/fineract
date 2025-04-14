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
package org.apache.fineract.portfolio.interestratechart.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestRateChartsRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "Chart - 2014")
    private String name;
    @Schema(example = "This chart is applicable for year 2014")
    private String description;
    @Schema(example = "en")
    private String locale;
    @Schema(example = "dd MMMM yyyy")
    private String dateFormat;
    @Schema(example = "01 Jan 2014")
    private String fromDate;
    private String endDate;
    private Boolean isPrimaryGroupingByAmount;
    private List<InterestRateChartStabDTO> chartSlabs;
}
