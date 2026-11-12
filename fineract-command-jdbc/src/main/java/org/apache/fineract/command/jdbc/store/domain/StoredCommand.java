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
package org.apache.fineract.command.jdbc.store.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.fineract.command.core.Command;

/**
 * A command read back out of the command table.
 *
 * <p>
 * Every command declares its entity and action in code, which is what makes them impossible to forget. A command
 * reconstructed from a stored row has no code to ask - the class that declared them is not involved - so it carries the
 * names that were recorded with it instead.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoredCommand<T> extends Command<T> {

    private String entityName;

    private String actionName;

    @Override
    public String entityName() {
        return entityName;
    }

    @Override
    public String actionName() {
        return actionName;
    }
}
