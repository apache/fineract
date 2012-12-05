package org.mifosplatform.infrastructure.core.serialization;

/**
 * Abstract implementation of {@link FromApiJsonDeserializer} that can be
 * extended for specific commands.
 */
public abstract class AbstractFromApiJsonDeserializer<T> implements FromApiJsonDeserializer<T> {

    private final CommandSerializer commandSerializerService;

    public AbstractFromApiJsonDeserializer(final CommandSerializer commandSerializerService) {
        this.commandSerializerService = commandSerializerService;
    }

    @Override
    public T commandFromApiJson(final String json) {
        return commandFromApiJson(null, json);
    }

    @Override
    public abstract T commandFromApiJson(final Long resourceId, final String json);

    @Override
    public String serializedCommandJsonFromApiJson(final String json) {
        final T command = commandFromApiJson(json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }

    @Override
    public String serializedCommandJsonFromApiJson(final Long resourceId, final String json) {
        final T command = commandFromApiJson(resourceId, json);
        return this.commandSerializerService.serializeCommandToJson(command);
    }
}