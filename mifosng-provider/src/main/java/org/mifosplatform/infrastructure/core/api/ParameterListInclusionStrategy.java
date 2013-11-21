/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.api;

import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ParameterListInclusionStrategy implements ExclusionStrategy {

    private final Set<String> parameterNamesToInclude;

    public ParameterListInclusionStrategy(final Set<String> parameterNamesToSkip) {
        this.parameterNamesToInclude = parameterNamesToSkip;
    }

    @Override
    public boolean shouldSkipField(final FieldAttributes f) {
        return !this.parameterNamesToInclude.contains(f.getName());
    }

    @SuppressWarnings("unused")
    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        return false;
    }
}