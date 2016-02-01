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
package org.apache.fineract.portfolio.account.data;

import java.util.Date;

import org.apache.fineract.infrastructure.core.service.SearchParameters;

public class StandingInstructionDTO {

    final SearchParameters searchParameters;
    final Long clientId;
    final String clientName;
    final Integer transferType;
    final Integer fromAccountType;
    final Long fromAccount;
    final Date startDateRange;
    final Date endDateRange;

    public StandingInstructionDTO(final SearchParameters searchParameters, final Integer transferType, final String clientName,
            final Long clientId, final Long fromAccount, final Integer fromAccountType, final Date startDateRange, final Date endDateRange) {
        this.searchParameters = searchParameters;
        this.transferType = transferType;
        this.clientName = clientName;
        this.clientId = clientId;
        this.fromAccount = fromAccount;
        this.fromAccountType = fromAccountType;
        this.startDateRange = startDateRange;
        this.endDateRange = endDateRange;
    }

    public SearchParameters searchParameters() {
        return this.searchParameters;
    }

    public Long clientId() {
        return this.clientId;
    }

    public String clientName() {
        return this.clientName;
    }

    public Integer transferType() {
        return this.transferType;
    }

    public Long fromAccount() {
        return this.fromAccount;
    }

    public Integer fromAccountType() {
        return this.fromAccountType;
    }

    public Integer getTransferType() {
        return this.transferType;
    }

    public Date startDateRange() {
        return this.startDateRange;
    }

    
    public Date endDateRange() {
        return this.endDateRange;
    }

}
