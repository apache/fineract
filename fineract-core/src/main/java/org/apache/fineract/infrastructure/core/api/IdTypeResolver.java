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
package org.apache.fineract.infrastructure.core.api;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DefaultOption;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class IdTypeResolver {

    public enum IdType implements DefaultOption {

        ID, //
        EXTERNAL_ID, //
        SHORT_NAME,; //

        @Override
        public boolean isDefault() {
            return this == ID;
        }

    }

    @NotNull
    public static IdType resolveDefault() {
        return IdType.ID;
    }

    public static IdType resolve(String idType) {
        return resolve(IdType.class, idType);
    }

    public static <T extends Enum<T>> T resolve(@NotNull Class<T> clazz, String idType) {
        if (idType == null) {
            return clazz.isAssignableFrom(DefaultOption.class) ? (T) DefaultOption.getDefault((Class) clazz) : null;
        }
        idType = formatIdType(idType);
        try {
            return Enum.valueOf(clazz, idType);
        } catch (IllegalArgumentException e) {
            throw resolveFailed(idType, e);
        }
    }

    public static String formatIdType(String idType) {
        return idType == null ? null : idType.replaceAll("-", "_").toUpperCase();
    }

    public static RuntimeException resolveFailed(String idType, Exception e) {
        return new PlatformApiDataValidationException("error.msg.id.type.not.found", "Provided type " + idType + " is not supported",
                "idType", e, idType);
    }
}
