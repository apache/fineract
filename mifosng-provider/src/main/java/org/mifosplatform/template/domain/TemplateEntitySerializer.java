/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.template.domain;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class TemplateEntitySerializer extends JsonSerializer<TemplateEntity> {

    @Override
    public void serialize(final TemplateEntity value, final JsonGenerator generator, @SuppressWarnings("unused") final SerializerProvider provider)
            throws IOException, JsonProcessingException {

        generator.writeStartObject();
        generator.writeFieldName("id");
        generator.writeNumber(value.getId());
        generator.writeFieldName("name");
        generator.writeString(value.getName());
        generator.writeEndObject();
    }
}
