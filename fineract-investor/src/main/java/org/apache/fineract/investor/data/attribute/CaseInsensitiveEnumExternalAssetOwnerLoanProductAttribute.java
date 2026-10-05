/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.investor.data.attribute;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public interface CaseInsensitiveEnumExternalAssetOwnerLoanProductAttribute<E extends Enum<E> & CaseInsensitiveEnumExternalAssetOwnerLoanProductAttribute<E>>
        extends ExternalAssetOwnerLoanProductAttribute {

    String name();

    Class<E> getDeclaringClass();

    @Override
    default String getAttributeValue() {
        return name();
    }

    @Override
    default List<String> getAttributeValues() {
        return Arrays.stream(getDeclaringClass().getEnumConstants()).map(Enum::name).toList();
    }

    @Override
    default boolean validate(final String attributeValue) {
        return getAttributeValue().equals(normalize(attributeValue));
    }

    @Override
    default boolean isMultiValue() {
        return false;
    }

    @Override
    default String normalize(final String attributeValue) {
        return attributeValue.trim().toUpperCase(Locale.ROOT);
    }
}
