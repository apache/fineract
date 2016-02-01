/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when Note does not support a resource.
 */
public class NoteResourceNotSupportedException extends AbstractPlatformResourceNotFoundException {

    public NoteResourceNotSupportedException(final String resource) {
        super("error.msg.note.resource.not.supported", "Note does not support resource " + resource);
    }
}