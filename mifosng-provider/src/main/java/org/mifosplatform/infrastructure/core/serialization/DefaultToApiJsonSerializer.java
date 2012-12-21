package org.mifosplatform.infrastructure.core.serialization;

import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * An abstract helper implementation of {@link ToApiJsonSerializer} for
 * resources to serialize their Java data objects into JSON.
 */
@Component
public final class DefaultToApiJsonSerializer<T> implements ToApiJsonSerializer<T> {

    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff;
    private final ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOn;
    private final CommandProcessingResultJsonSerializer commandProcessingResultSerializer;
    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public DefaultToApiJsonSerializer(
            final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff,
            final ExcludeNothingWithPrettyPrintingOnJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOn,
            final CommandProcessingResultJsonSerializer commandProcessingResultSerializer,
            final GoogleGsonSerializerHelper helper) {
        this.excludeNothingWithPrettyPrintingOff = excludeNothingWithPrettyPrintingOff;
        this.excludeNothingWithPrettyPrintingOn = excludeNothingWithPrettyPrintingOn;
        this.commandProcessingResultSerializer = commandProcessingResultSerializer;
        this.helper = helper;
    }
    
    @Override
    public String serializeResult(final Object object) {
        return this.commandProcessingResultSerializer.serialize(object);
    }

    @Override
    public String serialize(final Object object) {
        return this.excludeNothingWithPrettyPrintingOff.serialize(object);
    }
    
    @Override
    public String serializePretty(boolean prettyOn, final Object object) {
        String json = "";
        
        if (prettyOn) {
            json = this.excludeNothingWithPrettyPrintingOn.serialize(object);
        } else {
            json = serialize(object);
        }
        return json;
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final Collection<T> collection,
            final Set<String> supportedResponseParameters) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
        return serializeWithSettings(delegatedSerializer, settings, collection.toArray());
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final T singleObject,
            final Set<String> supportedResponseParameters) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
        return serializeWithSettings(delegatedSerializer, settings, singleObject);
    }

    private String serializeWithSettings(final Gson gson, final ApiRequestJsonSerializationSettings settings, final Object[] dataObject) {
        String json = null;
        if (gson != null) {
            json = helper.serializedJsonFrom(gson, dataObject);
        } else {
            if (settings.isPrettyPrint()) {
                json = this.excludeNothingWithPrettyPrintingOn.serialize(dataObject);
            } else {
                json = serialize(dataObject);
            }
        }
        return json;
    }
    
    private String serializeWithSettings(final Gson gson, final ApiRequestJsonSerializationSettings settings, final Object dataObject) {
        String json = null;
        if (gson != null) {
            json = helper.serializedJsonFrom(gson, dataObject);
        } else {
            if (settings.isPrettyPrint()) {
                json = this.excludeNothingWithPrettyPrintingOn.serialize(dataObject);
            } else {
                json = serialize(dataObject);
            }
        }
        return json;
    }

    private Gson findAppropriateSerializer(final ApiRequestJsonSerializationSettings settings, final Set<String> supportedResponseParameters) {
        Gson gson = null;
        if (settings.isPartialResponseRequired()) {
            gson = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(supportedResponseParameters,
                    settings.isPrettyPrint(), settings.getParametersForPartialResponse());
        }
        return gson;
    }
}