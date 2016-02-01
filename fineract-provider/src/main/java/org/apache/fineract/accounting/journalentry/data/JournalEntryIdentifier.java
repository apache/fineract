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
package org.apache.fineract.accounting.journalentry.data;

/**
 * Represents the successful result of an REST API call.
 */
public class JournalEntryIdentifier {

    private String entityId;

    // TODO - Rename variable to commandId or taskId or something that shows
    // this is the id of a command in a table/queue for processing.
    @SuppressWarnings("unused")
    private Long makerCheckerId;

    public static JournalEntryIdentifier makerChecker(final Long makerCheckerId) {
        return new JournalEntryIdentifier(null, makerCheckerId);
    }

    public static JournalEntryIdentifier makerChecker(final String resourceId, final Long makerCheckerId) {
        return new JournalEntryIdentifier(resourceId, makerCheckerId);
    }

    public JournalEntryIdentifier() {
        //
    }

    public JournalEntryIdentifier(final String entityId) {
        this.entityId = entityId;
    }

    private JournalEntryIdentifier(final String entityId, final Long makerCheckerId) {
        this.entityId = entityId;
        this.makerCheckerId = makerCheckerId;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }
}
