/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class CodeValueNotFoundException extends AbstractPlatformResourceNotFoundException {

    public CodeValueNotFoundException(final Long id) {
        super("error.msg.codevalue.id.invalid", "Code value with identifier " + id + " does not exist", id);
    }

    public CodeValueNotFoundException(final String codeName, final Long id) {
        super("error.msg.codevalue.codename.id.combination.invalid", "Code value with identifier " + id
                + " does not exist for a code with name " + codeName, id, codeName);
    }
    
    public CodeValueNotFoundException(final String codeName, final String label) {
        super("error.msg.codevalue.codename.id.combination.invalid", "Code value with label " + label
                + " does not exist for a code with name " + codeName, label, codeName);
    }
}