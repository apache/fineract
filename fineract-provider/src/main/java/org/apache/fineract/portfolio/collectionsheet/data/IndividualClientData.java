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
package org.apache.fineract.portfolio.collectionsheet.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Immutable data object for clients with loans due for disbursement or collection.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public final class IndividualClientData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long clientId;
    private final String clientName;
    private List<LoanDueData> loans;
    private List<SavingsDueData> savings;

    public static IndividualClientData instance(final Long clientId, final String clientName) {
        return new IndividualClientData(clientId, clientName, new ArrayList<>(), new ArrayList<>());
    }

    public static IndividualClientData withSavings(final IndividualClientData client, final List<SavingsDueData> savings) {
        return new IndividualClientData(client.clientId, client.clientName, client.loans, savings);
    }

    public static IndividualClientData withLoans(final IndividualClientData client, final List<LoanDueData> loans) {
        return new IndividualClientData(client.clientId, client.clientName, loans, client.savings);
    }

    public void addLoans(LoanDueData loans) {
        if (this.loans != null) {
            this.loans.add(loans);
        }
    }

    public void addSavings(SavingsDueData savings) {
        if (this.savings != null) {
            this.savings.add(savings);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof IndividualClientData)) {
            return false;
        }
        final IndividualClientData clientData = (IndividualClientData) obj;
        return clientData.clientId.compareTo(this.clientId) == 0;
    }

    @Override
    public int hashCode() {
        return this.clientId.hashCode();
    }
}
