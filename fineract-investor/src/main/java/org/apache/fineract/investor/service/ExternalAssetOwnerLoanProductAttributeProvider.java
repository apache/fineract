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
package org.apache.fineract.investor.service;

import static org.reflections.scanners.Scanners.SubTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.fineract.investor.data.attribute.ExternalAssetOwnerLoanProductAttribute;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

/**
 * Discovers every {@link ExternalAssetOwnerLoanProductAttribute} implementation on the classpath and exposes them as
 * ready to use instances. Enum implementations contribute one instance per constant, non enum implementations are
 * instantiated through their no-arg constructor.
 */
@Component
public final class ExternalAssetOwnerLoanProductAttributeProvider {

    private static final String INVESTOR_PATH = "org.apache.fineract.investor";

    private final List<ExternalAssetOwnerLoanProductAttribute> attributes;

    public ExternalAssetOwnerLoanProductAttributeProvider() {
        Set<Class<?>> implementingClasses = new Reflections(INVESTOR_PATH)
                .get(SubTypes.of(ExternalAssetOwnerLoanProductAttribute.class).asClass());
        List<ExternalAssetOwnerLoanProductAttribute> resolved = new ArrayList<>();
        for (Class<?> implementingClass : implementingClasses) {
            if (implementingClass.isEnum()) {
                Arrays.stream(implementingClass.getEnumConstants()).map(ExternalAssetOwnerLoanProductAttribute.class::cast)
                        .forEach(resolved::add);
            } else {
                resolved.add(createAttribute(implementingClass));
            }
        }
        this.attributes = List.copyOf(resolved);
    }

    /**
     * @return every discovered attribute instance; enum implementations are expanded to their constants.
     */
    public List<ExternalAssetOwnerLoanProductAttribute> retrieveAll() {
        return attributes;
    }

    private static ExternalAssetOwnerLoanProductAttribute createAttribute(final Class<?> implementingClass) {
        try {
            return (ExternalAssetOwnerLoanProductAttribute) implementingClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException exception) {
            throw new IllegalStateException("Unable to create external asset owner loan product attribute: " + implementingClass.getName(),
                    exception);
        }
    }
}
