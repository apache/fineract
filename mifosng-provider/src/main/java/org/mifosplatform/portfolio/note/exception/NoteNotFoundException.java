/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when note resources are not found.
 */
public class NoteNotFoundException extends AbstractPlatformResourceNotFoundException {

    public NoteNotFoundException(final Long id) {
        super("error.msg.note.id.invalid", "Note with identifier " + id + " does not exist", id);
    }

    public NoteNotFoundException(final Long id, final Long resourceId, final String resource) {
        super("error.msg." + resource + ".note.id.invalid", "Note with identifier " + id + " does not exist for " + resource
                + " with identifier " + resourceId, id, resourceId);
    }

}