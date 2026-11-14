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
package org.apache.fineract.command.core;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public abstract class Command<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long commandId;

    private String idempotencyKey;

    /**
     * What {@link #idempotencyKey} is unique together with: a command row is unique on the pair, and a repeat is
     * matched only against the row with the same key and the same distinctor. Set when the key is claimed; the empty
     * string, the column's default, when nothing distinguishes it - including for a command that carries no key at all.
     */
    private String customIdempotencyDistinctor = "";

    private String ipAddress;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant executedAt;

    private Instant approvedAt;

    private Instant rejectedAt;

    private String initiatedByUsername;

    private String executedByUsername;

    private String approvedByUsername;

    private String rejectedByUsername;

    private String error;

    private T payload;

    /**
     * The entity this command acts on, in the vocabulary of {@code m_permission} - {@code DOCUMENT},
     * {@code BUSINESS_DATE}, {@code CLIENT_COLLATERAL_PRODUCT}. Recorded against the command so that it can be found in
     * an audit and, once maker-checker reaches this framework, so that the permission governing its approval can be
     * derived the way the legacy processing derives it.
     *
     * <p>
     * Abstract rather than defaulted on purpose: a command whose name is missing would be written with no entity, and a
     * row that no checker inbox and no audit search can find is worse than a compile error. For a command that acts on
     * different entities depending on its payload - a note, an image - this is computed from that payload, which is set
     * before the command is stored.
     */
    public abstract String entityName();

    /**
     * The column is {@code NOT NULL}, and an absent distinctor means "none", so {@code null} is stored as the empty
     * string rather than rejected by the database.
     */
    public void setCustomIdempotencyDistinctor(String customIdempotencyDistinctor) {
        this.customIdempotencyDistinctor = customIdempotencyDistinctor == null ? "" : customIdempotencyDistinctor;
    }

    /** The action this command performs on {@link #entityName()}, again as {@code m_permission} spells it. */
    public abstract String actionName();
}
