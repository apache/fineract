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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

/**
 * Transaction portions the foreclosure income calculation needs.
 *
 * A record built by a JPQL constructor expression rather than a Spring Data interface projection: the interface form
 * required result variables ({@code AS} aliases) in the SELECT clause to bind the columns to the accessors, and
 * EclipseLink rejects that select clause when parsing the query.
 */
public record TransactionPortionsForForeclosure(LoanTransactionType transactionType, BigDecimal interestPortion,
        BigDecimal feeChargesPortion, BigDecimal penaltyChargesPortion) {
}
