package org.mifosplatform.infrastructure.core.serialization;

/**
 * Abstract implementation of {@link FromCommandJsonDeserializer} that can be
 * extended for specific commands.
 */
public abstract class AbstractFromCommandJsonDeserializer<T> implements FromCommandJsonDeserializer<T> {

    @Override
    public T commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public T commandFromCommandJson(final Long codeId, final String commandAsJson) {
        return commandFromCommandJson(codeId, commandAsJson, false);
    }
}