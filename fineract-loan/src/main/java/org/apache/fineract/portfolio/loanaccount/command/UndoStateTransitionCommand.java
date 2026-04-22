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
package org.apache.fineract.portfolio.loanaccount.command;

/**
 * Immutable command for undo'ing a state transition.
 */
public class UndoStateTransitionCommand {

    private final Long loanId;
    private final String note;

    public UndoStateTransitionCommand(final Long loanId, final String note) {
        this.loanId = loanId;
        this.note = note;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public String getNote() {
        return this.note;
    }
}
