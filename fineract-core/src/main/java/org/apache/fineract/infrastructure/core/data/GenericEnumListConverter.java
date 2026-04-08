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
package org.apache.fineract.infrastructure.core.data;

import jakarta.persistence.AttributeConverter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public abstract class GenericEnumListConverter<E extends Enum<E>> implements AttributeConverter<List<E>, String> {

    private static final String SPLIT_CHAR = ",";

    private final Class<E> clazz;

    public boolean isUnique() {
        return false;
    }

    protected GenericEnumListConverter(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String convertToDatabaseColumn(List<E> values) {
        if (values.isEmpty()) {
            return null;
        }
        Stream<E> valueStream;
        if (isUnique()) {
            valueStream = values.stream().distinct();
        } else {
            valueStream = values.stream();
        }
        return valueStream.map(Enum::name).collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public List<E> convertToEntityAttribute(String string) {
        if (StringUtils.isBlank(string)) {
            return List.of();
        }
        Stream<E> stream = Stream.of(string.split(SPLIT_CHAR)).map(e -> Enum.valueOf(clazz, e));
        if (isUnique()) {
            return stream.distinct().toList();
        }
        return stream.toList();
    }
}
