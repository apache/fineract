/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.data;

/**
 * Immutable data object represent code-value data in system.
 */
public class CodeValueData {

    @SuppressWarnings("unused")
    private final Long id;

    @SuppressWarnings("unused")
    private final String name;

    @SuppressWarnings("unused")
    private final Integer position;

    public static CodeValueData instance(final Long id, final String name, final Integer position) {
        return new CodeValueData(id, name, position);
    }

    public static CodeValueData instance(final Long id, final String name) {
        return new CodeValueData(id, name, null);
    }

    private CodeValueData(final Long id, final String name, final Integer position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }
}