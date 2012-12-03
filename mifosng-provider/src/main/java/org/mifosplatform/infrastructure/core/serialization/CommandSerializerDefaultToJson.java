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