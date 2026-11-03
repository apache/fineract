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
 * Implemented by commands whose successful execution must notify the webhooks registered for its event, as the legacy
 * command processing does for every command. The names are the entity and action a hook subscribes to, e.g.
 * {@code DOCUMENT} and {@code CREATE}.
 */
public interface HookableCommand {

    String hookEntityName();

    String hookActionName();
}
