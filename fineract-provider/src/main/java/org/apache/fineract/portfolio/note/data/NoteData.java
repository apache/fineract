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
package org.apache.fineract.portfolio.note.data;

import java.time.OffsetDateTime;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object represent note or case information about a client, loan or loan transaction.
 */
@Getter
public class NoteData {

    private final Long id;
    private final Long clientId;
    private final Long groupId;
    private final Long loanId;
    private final Long loanTransactionId;
    private final Long depositAccountId;
    private final Long savingAccountId;
    private final EnumOptionData noteType;
    private final String note;
    private final Long createdById;
    private final String createdByUsername;
    private final OffsetDateTime createdOn;
    private final Long updatedById;
    private final String updatedByUsername;
    private final OffsetDateTime updatedOn;

    public NoteData(final Long id, final Long clientId, final Long groupId, final Long loanId, final Long transactionId,
            final Long depositAccountId, final Long savingAccountId, final EnumOptionData noteType, final String note,
            final OffsetDateTime createdDate, final Long createdById, final String createdByUsername, final OffsetDateTime lastModifiedDate,
            final Long lastModifiedById, final String updatedByUsername) {
        this.id = id;
        this.clientId = clientId;
        this.groupId = groupId;
        this.loanId = loanId;
        this.loanTransactionId = transactionId;
        this.depositAccountId = depositAccountId;
        this.savingAccountId = savingAccountId;
        this.noteType = noteType;
        this.note = note;
        this.createdOn = createdDate;
        this.createdById = createdById;
        this.createdByUsername = createdByUsername;
        this.updatedOn = lastModifiedDate;
        this.updatedById = lastModifiedById;
        this.updatedByUsername = updatedByUsername;
    }
}
