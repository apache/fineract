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
package org.apache.fineract.command.idempotency;

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTIES_PREFIX;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.command.idempotency.distinctor.CustomIdempotencyDistinctorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ConfigurationProperties(prefix = COMMAND_IDEMPOTENCY_PROPERTIES_PREFIX)
public final class IdempotencyCommandProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Idempotency is on as soon as the module is on the classpath; it has to be switched off explicitly. A repeated
     * request quietly executing twice should be a decision, not the consequence of an unset property. These defaults
     * are kept in step with {@code matchIfMissing} on the corresponding {@code @ConditionalOnProperty}; changing one
     * without the other makes this class describe behaviour the application does not have.
     */
    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean hookKeyHeaderPre = true;

    @Builder.Default
    private Boolean hookCheckPre = true;

    @Builder.Default
    private String keyHeaderName = "Idempotency-Key";

    /**
     * How long a claimed command may stay unfinished before a repeat of its key is told the original will never
     * complete, rather than being told to come back later.
     *
     * <p>
     * A claim that is never resolved - the process died between claiming the key and recording the outcome, or the
     * store gave up after its retries - would otherwise answer every repeat {@code 425 Too Early} for ever, and a
     * client following that advice retries for ever. Long enough that a slow command is not declared dead: the key is
     * never re-executed, but a repeat past this point is refused, and the caller has to use a new one.
     *
     * <p>
     * Zero or empty switches the timeout off: a repeat of an unfinished claim is then answered {@code 425 Too Early}
     * for as long as it stays unfinished, as in the legacy command processing. A negative value is refused at startup.
     */
    @Builder.Default
    private Duration claimTimeout = Duration.ofMinutes(10);

    /**
     * What an idempotency key is unique together with. {@code ENTITY_NAME_AND_ACTION_NAME}, the default, scopes it to
     * the command's entity and action, the way the legacy command processing does; {@code NONE} makes a key unique per
     * tenant across all commands. See {@link CustomIdempotencyDistinctorType}.
     */
    @Builder.Default
    private CustomIdempotencyDistinctorType customDistinctor = CustomIdempotencyDistinctorType.ENTITY_NAME_AND_ACTION_NAME;
}
