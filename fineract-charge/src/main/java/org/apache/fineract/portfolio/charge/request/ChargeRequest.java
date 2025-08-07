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
package org.apache.fineract.portfolio.charge.request;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ChargeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer chargeAppliesTo;
    private String name;
    private String currencyCode;
    private Integer chargeTimeType;
    private Integer chargeCalculationType;
    private Double amount;
    private Boolean active;
    private Boolean penalty;
    private Integer chargePaymentMode;
    private String monthDayFormat;
    private String locale;
    private String feeOnMonthDay;
    private String feeInterval;
    private String feeFrequency;
    private Long paymentTypeId;
    private Boolean enablePaymentType;
    private BigDecimal minCap;
    private BigDecimal maxCap;
    private Long taxGroupId;

}
