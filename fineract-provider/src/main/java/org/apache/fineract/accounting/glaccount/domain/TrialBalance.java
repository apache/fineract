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

package org.apache.fineract.accounting.glaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_trial_balance")
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class TrialBalance extends AbstractPersistableCustom {

    @Column(name = "office_id", nullable = false)
    private Long officeId;

    @Column(name = "account_id", nullable = false)
    private Long glAccountId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "created_date", nullable = true)
    private LocalDate transactionDate;

    @Column(name = "closing_balance", nullable = false)
    private BigDecimal closingBalance;

    public static TrialBalance getInstance(final Long officeId, final Long glAccountId, final BigDecimal amount, final LocalDate entryDate,
            final LocalDate transactionDate) {
        //@formatter:off
        return new TrialBalance()
                .setOfficeId(officeId)
                .setGlAccountId(glAccountId)
                .setAmount(amount)
                .setEntryDate(entryDate)
                .setTransactionDate(transactionDate);
        //@formatter:on
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrialBalance)) {
            return false;
        }
        TrialBalance other = (TrialBalance) obj;
        return Objects.equals(other.officeId, officeId) && Objects.equals(other.glAccountId, glAccountId)
                && Objects.equals(other.amount, amount) && other.entryDate.compareTo(entryDate) == 0 ? Boolean.TRUE
                        : Boolean.FALSE && other.transactionDate.compareTo(transactionDate) == 0 ? Boolean.TRUE
                                : Boolean.FALSE && Objects.equals(other.closingBalance, closingBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(officeId, glAccountId, amount, entryDate, transactionDate, closingBalance);
    }
}
