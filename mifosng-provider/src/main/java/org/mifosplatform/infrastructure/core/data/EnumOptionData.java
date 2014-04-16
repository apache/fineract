/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.data;

/**
 * <p>
 * Immutable data object representing generic enumeration value.
 * </p>
 */
public class EnumOptionData {

    private final Long id;
    private final String code;
    private final String value;

    public EnumOptionData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }
    
    public String getValue() {
        return this.value;
    }
    
    
}