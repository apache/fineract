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
 * Distinguishes nothing: every command gets the empty string, so an idempotency key is unique per tenant across all
 * commands, and a key reused on a different endpoint is refused rather than executed.
 */
public final class NoCustomIdempotencyDistinctor implements CustomIdempotencyDistinctor {

    public static final String NONE = "";

    @Override
    public String distinctorFor(Command<?> command) {
        return NONE;
    }
}
