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
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object represent note or case information about a client, loan or loan transaction.
 */
@Getter
@Builder
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
}
