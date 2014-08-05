/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.data;

public class NamespaceData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String prefix;
    private final String url;

    public NamespaceData(final Long id, final String prefix, final String url) {

        this.id = id;
        this.prefix = prefix;
        this.url = url;
    }

    public String url() {
        return this.url;
    }
}