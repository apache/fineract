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
package org.apache.fineract.portfolio.savings.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;

@Entity
@Table(name = "m_savings_account_block_narration_history")
public class SavingsAccountBlockNarrationHistory extends AbstractAuditableCustom {

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private SavingsAccount savingsAccount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "block_narration_comment")
    private String blockNarrationComment;

    @Column(name = "block_narration_id")
    private Long blockNarration;

    @Column(name = "sub_status")
    private String subStatus;

    public Long getBlockNarration() {
        return blockNarration;
    }

    public void setBlockNarration(Long blockNarration) {
        this.blockNarration = blockNarration;
    }

    public String getBlockNarrationComment() {
        return blockNarrationComment;
    }

    public void setBlockNarrationComment(String blockNarrationComment) {
        this.blockNarrationComment = blockNarrationComment;
    }

    public static SavingsAccountBlockNarrationHistory createNew(final SavingsAccount account, final Long blockNarration,
            final String pndComment, String subStatus) {
        return new SavingsAccountBlockNarrationHistory(account, blockNarration, pndComment, subStatus, new Date(), null);
    }

    protected SavingsAccountBlockNarrationHistory() {
        //
    }

    private SavingsAccountBlockNarrationHistory(final SavingsAccount account, final Long blockNarration, final String blockNarrationComment,
            final String subStatus, final Date startDate, final Date endDate) {
        this.savingsAccount = account;
        this.blockNarration = blockNarration;
        this.blockNarrationComment = blockNarrationComment;
        this.subStatus = subStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
