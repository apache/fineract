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

import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.domain.FineractEvent;

/**
 * Carries a {@link NoteCreateRequest} together with the {@link FineractContext} captured on the publishing thread, so
 * that {@code NoteListener} can restore the tenant context before processing on the shared asynchronous event executor.
 */
@Getter
public final class NoteCreateEvent extends FineractEvent {

    private final NoteCreateRequest request;

    public NoteCreateEvent(final Object source, final NoteCreateRequest request, final FineractContext context) {
        super(source, context);
        this.request = request;
    }
}
