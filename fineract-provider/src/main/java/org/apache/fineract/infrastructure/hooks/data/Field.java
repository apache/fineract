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
package org.apache.fineract.infrastructure.hooks.data;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@SuppressWarnings("unused")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class Field implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fieldName;
    private String fieldValue;
    private String fieldType;
    private Boolean optional;
    private String placeholder;

    public static Field fromConfig(final String fieldName, final String fieldValue) {
        return new Field().setFieldName(fieldName).setFieldValue(fieldValue);
    }

    public static Field fromSchema(final String fieldType, final String fieldName, final Boolean optional, final String placeholder) {
        return new Field().setFieldName(fieldName).setFieldType(fieldType).setOptional(optional).setPlaceholder(placeholder);
    }
}
