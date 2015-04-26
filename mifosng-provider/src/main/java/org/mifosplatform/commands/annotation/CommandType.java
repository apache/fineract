/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.annotation;

import java.lang.annotation.*;

/**
 * Specifies the command type for the annotated class.<br/>
 * <br/>
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
