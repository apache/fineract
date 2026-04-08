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
package org.apache.fineract.infrastructure.core.service;

import java.util.Locale;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;

@Getter
@Setter
@RequiredArgsConstructor
public class PagedLocalRequest<T> extends PagedRequest<T> {

    private String dateFormat;

    private String dateTimeFormat;

    private String locale;

    public Locale getLocaleObject() {
        return locale == null ? null : JsonParserHelper.localeFromString(locale);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof PagedLocalRequest)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PagedLocalRequest<?> that = (PagedLocalRequest<?>) o;
        return Objects.equals(dateFormat, that.dateFormat) && Objects.equals(dateTimeFormat, that.dateTimeFormat)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dateFormat, dateTimeFormat, locale);
    }
}
