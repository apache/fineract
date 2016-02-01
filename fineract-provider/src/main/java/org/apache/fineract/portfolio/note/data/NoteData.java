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

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.DateTime;

/**
 * Immutable data object represent note or case information about a client, loan
 * or loan transaction.
 */
public class NoteData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long clientId;
    @SuppressWarnings("unused")
    private final Long groupId;
    @SuppressWarnings("unused")
    private final Long loanId;
    @SuppressWarnings("unused")
    private final Long loanTransactionId;
    @SuppressWarnings("unused")
    private final Long depositAccountId;
    @SuppressWarnings("unused")
    private final Long savingAccountId;
    @SuppressWarnings("unused")
    private final EnumOptionData noteType;
    @SuppressWarnings("unused")
    private final String note;
    @SuppressWarnings("unused")
    private final Long createdById;
    @SuppressWarnings("unused")
    private final String createdByUsername;
    @SuppressWarnings("unused")
    private final DateTime createdOn;
    @SuppressWarnings("unused")
    private final Long updatedById;
    @SuppressWarnings("unused")
    private final String updatedByUsername;
    @SuppressWarnings("unused")
    private final DateTime updatedOn;

    public NoteData(final Long id, final Long clientId, final Long groupId, final Long loanId, final Long transactionId,
            final Long depositAccountId, final Long savingAccountId, final EnumOptionData noteType, final String note,
            final DateTime createdDate, final Long createdById, final String createdByUsername, final DateTime lastModifiedDate,
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