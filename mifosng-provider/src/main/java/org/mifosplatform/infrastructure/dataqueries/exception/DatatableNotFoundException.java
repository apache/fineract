/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when datatable resources are not found.
 */
public class DatatableNotFoundException extends AbstractPlatformResourceNotFoundException {

    public DatatableNotFoundException(final String datatable, final Long id) {
        super("error.msg.datatable.data.not.found", "Data not found for datatable: ", datatable + "  Id:" + id);
    }

    public DatatableNotFoundException(final String datatable) {
        super("error.msg.datatable.not.found", "Datatable not found.", datatable);
    }
}