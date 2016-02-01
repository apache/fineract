/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

/**
 * Service for serializing commands into another format.
 * 
 * <p>
 * Known implementations:
 * </p>
 * 
 * @see CommandSerializerDefaultToJson
 */
public interface CommandSerializer {

    String serializeCommandToJson(Object command);
}