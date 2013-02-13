/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link CommandSerializer} that serializes
 * the commands into JSON using google-gson.
 */
@Component
public class CommandSerializerDefaultToJson implements CommandSerializer {

    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff;

    @Autowired
    public CommandSerializerDefaultToJson(final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff) {
        this.excludeNothingWithPrettyPrintingOff = excludeNothingWithPrettyPrintingOff;
    }

    @Override
    public String serializeCommandToJson(Object command) {
        return excludeNothingWithPrettyPrintingOff.serialize(command);
    }
}