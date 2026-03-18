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
package org.apache.fineract.portfolio.search.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdHocQuerySearchRequest implements Serializable {

    private String locale;
    private String dateFormat;
    private List<String> entities;
    private List<String> loanStatus;
    private List<Long> loanProducts;
    private List<Long> offices;
    private String loanDateOption;
    private LocalDate loanFromDate;
    private LocalDate loanToDate;
    private Boolean includeOutStandingAmountPercentage;
    private String outStandingAmountPercentageCondition;
    private BigDecimal minOutStandingAmountPercentage;
    private BigDecimal maxOutStandingAmountPercentage;
    private BigDecimal outStandingAmountPercentage;
    private Boolean includeOutstandingAmount;
    private String outstandingAmountCondition;
    private BigDecimal minOutstandingAmount;
    private BigDecimal maxOutstandingAmount;
    private BigDecimal outstandingAmount;
}
