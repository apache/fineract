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

/**
 * Decides whether a command failed in a way that a retry under the same idempotency key should execute again, rather
 * than be answered with the stored failure.
 *
 * <p>
 * A failed command normally owns its idempotency key for good: every repeat is answered with the same failure. That is
 * right for a failure the request itself caused, and wrong for one a retry could get past - a lock that could not be
 * taken, a dependency that was briefly unavailable, a business rule the client was told to wait out. For those the key
 * is released, so that the retry the client was right to make is executed.
 *
 * <p>
 * The idempotency module falls back to treating nothing as recoverable. The application supplies the real rules, which
 * in Fineract are the same ones the legacy command processing applies.
 */
@FunctionalInterface
public interface RecoverableCommandErrorClassifier {

    boolean isRecoverable(Throwable error);
}
