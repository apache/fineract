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
package org.apache.fineract.organisation.teller.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.teller.domain.CashierSessionStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CashierSessionData implements Serializable {

    private Long id;
    private Long cashierId;
    private Long tellerId;
    private Long userId;
    private Long officeId;
    private LocalDate sessionDate;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private BigDecimal openingAllocation;
    private BigDecimal totalSettled;
    private CashierSessionStatus status;
    private Long openingTxnId;
    private Long closingTxnId;
    private String currencyCode;
}
