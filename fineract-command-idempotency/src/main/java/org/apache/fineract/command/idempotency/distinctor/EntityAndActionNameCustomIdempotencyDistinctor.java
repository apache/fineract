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
package org.apache.fineract.command.idempotency.distinctor;

import org.apache.fineract.command.core.Command;

/**
 * Scopes an idempotency key to what the command does and to what: the command's entity name and action name, as
 * {@code m_permission} spells them, joined with {@value #SEPARATOR} - {@code LOAN|CREATE}, {@code PAYMENTTYPE|UPDATE}.
 *
 * <p>
 * This is how the legacy command processing scopes a key, so the same key may be used once per entity and action: a key
 * reused on a different endpoint is executed as a new command rather than refused.
 */
public final class EntityAndActionNameCustomIdempotencyDistinctor implements CustomIdempotencyDistinctor {

    public static final String SEPARATOR = "|";

    @Override
    public String distinctorFor(Command<?> command) {
        return command.entityName() + SEPARATOR + command.actionName();
    }
}
