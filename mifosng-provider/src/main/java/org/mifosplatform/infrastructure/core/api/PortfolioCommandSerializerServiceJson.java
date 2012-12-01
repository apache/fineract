package org.mifosplatform.infrastructure.core.api;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link PortfolioCommandSerializerService} that serializes
 * the commands into JSON using google-gson.
 */
@Service
public class PortfolioCommandSerializerServiceJson implements PortfolioCommandSerializerService {

    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public PortfolioCommandSerializerServiceJson(final GoogleGsonSerializerHelper helper) {
        this.helper = helper;
    }

    @Override
    public String serializeCommandToJson(Object command) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(new HashSet<String>(), false,
                new HashSet<String>());
        return helper.serializedJsonFrom(gsonDeserializer, command);
    }
}