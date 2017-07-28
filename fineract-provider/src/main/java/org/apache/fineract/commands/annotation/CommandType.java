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
package org.apache.fineract.commands.annotation;

import java.lang.annotation.*;

/**
 * Specifies the command type for the annotated class.<br>
 * <br>
 * The entity name (e.g. CLIENT, SAVINGSACCOUNT, LOANPRODUCT) and the action (e.g. CREATE, DELETE) must be given.
 *
 * @author Markus Geiss
 * @version 1.0
 * @since 15.06
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface CommandType {

    /**
     * Returns the name of the entity for this {@link CommandType}.
     */
    String entity();

    /**
     * Return the name of the action for this {@link CommandType}.
     */
    String action();
}
