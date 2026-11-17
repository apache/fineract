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
 * Runs the hooks of one phase - before, after or error - in their order.
 *
 * <p>
 * A hook that throws is not isolated: the exception ends the phase and propagates to the dispatcher, which treats it
 * like a failure of the command. A hook can stop the remaining hooks of a phase with {@code skipHooks} on the
 * {@link CommandContext}; that request is cleared when the phase ends.
 */
public interface CommandHookManager {

    void before(CommandContext<?, ?> ctx);

    void after(CommandContext<?, ?> ctx);

    void error(CommandContext<?, ?> ctx);
}
