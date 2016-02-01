/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.data;

import java.io.Serializable;

/**
 * Immutable data object represent code-value data in system.
 */
public class CodeValueData implements Serializable {

    private final Long id;

    private final String name;

    @SuppressWarnings("unused")
    private final Integer position;

    @SuppressWarnings("unused")
    private final String description;
    private final boolean isActive;

    public static CodeValueData instance(final Long id, final String name, final Integer position, final boolean isActive) {
        String description = null;
        return new CodeValueData(id, name, position, description,isActive);
    }

    public static CodeValueData instance(final Long id, final String name, final String description, final boolean isActive) {
        Integer position = null;
        return new CodeValueData(id, name, position, description,isActive);
    }

    public static CodeValueData instance(final Long id, final String name) {
        String description = null;
        Integer position = null;
        boolean isActive = false;
        return new CodeValueData(id, name, position, description, isActive);
    }

    public static CodeValueData instance(final Long id, final String name, final Integer position, final String description, final boolean isActive) {
        return new CodeValueData(id, name, position, description,isActive);
    }

    private CodeValueData(final Long id, final String name, final Integer position, final String description, final boolean isActive) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.description = description;
        this.isActive = isActive;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}