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
package org.apache.fineract.command.core.exception;

import java.util.List;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;

public class DuplicateCommandHandlerException extends RuntimeException {

    public DuplicateCommandHandlerException(Command<?> command, List<? extends CommandHandler> duplicates) {
        super("Multiple handlers found for " + command.getClass().getSimpleName() + " command: "
                + duplicates.stream().map(h -> h.getClass().getSimpleName()).toList());
    }
}
