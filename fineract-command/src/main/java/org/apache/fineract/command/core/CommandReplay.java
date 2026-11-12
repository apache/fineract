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

import java.time.Instant;

/**
 * What is known about a command already stored under an idempotency key: enough to answer a repeated request exactly as
 * the original one was answered, without executing anything.
 *
 * @param state
 *            the state the stored command is in
 * @param statusCode
 *            the HTTP status the original request was answered with, {@code null} if it never completed
 * @param claimedAt
 *            when the command was claimed, used to tell a command that is still running from one that never finished
 * @param response
 *            the response object the original command produced, rehydrated from the command store, {@code null} if it
 *            never completed. It is handed back to the web layer as an object rather than as text so that the same
 *            serializer that rendered the original answer renders the replay, which is what makes the two identical.
 */
public record CommandReplay(CommandState state, Integer statusCode, Object response, Instant claimedAt) {
}
