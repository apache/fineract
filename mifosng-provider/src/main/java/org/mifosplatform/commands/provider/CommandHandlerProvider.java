/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.provider;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * {@link CommandHandlerProvider} provides {@link NewCommandSourceHandler}s for a given entity and action.<br/>
 * <br/>
 * A {@link NewCommandSourceHandler} can be registered and the annotation {@link CommandType} is used to determine
 * the entity and the action the handler is capable to process.
 *
 * @author Markus Geiss
 * @version 1.0
 * @since 15.06
 * @see NewCommandSourceHandler
 * @see CommandType
 */
@Component
@Scope("singleton")
public class CommandHandlerProvider implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandlerProvider.class);

    private ApplicationContext applicationContext;
    private HashMap<String, String> registeredHandlers;

    CommandHandlerProvider() {
        super();
    }

    /**
     * Returns a handler for the given entity and action.<br/>
     * <br/>
     * Throws an {@link UnsupportedCommandException} if no handler
     * for the given entity, action combination can be found.
     * @param entity the entity to lookup the handler, must be given.
     * @param action the action to lookup the handler, must be given.
     */
    @Nonnull
    public NewCommandSourceHandler getHandler (@Nonnull final String entity, @Nonnull final String action) {
        Preconditions.checkArgument(StringUtils.isNoneEmpty(entity), "An entity must be given!");
        Preconditions.checkArgument(StringUtils.isNoneEmpty(action), "An action must be given!");

        final String key =  entity + "|" + action;
        if (!this.registeredHandlers.containsKey(key)) {
            throw new UnsupportedCommandException(key);
        }
        return (NewCommandSourceHandler)this.applicationContext.getBean(this.registeredHandlers.get(key));
    }

    private void initializeHandlerRegistry() {
        if (this.registeredHandlers == null) {
            this.registeredHandlers = new HashMap<>();

            final String[] commandHandlerBeans = this.applicationContext.getBeanNamesForAnnotation(CommandType.class);
            if (ArrayUtils.isNotEmpty(commandHandlerBeans)) {
                for (final String commandHandlerName : commandHandlerBeans) {
                    LOGGER.info("Register command handler '" + commandHandlerName + "' ...");
                    final CommandType commandType = this.applicationContext.getType(commandHandlerName).getAnnotation(CommandType.class);
                    try {
                        this.registeredHandlers.put(commandType.entity() + "|" + commandType.action(), commandHandlerName);
                    } catch (final Throwable th) {
                        LOGGER.error("Unable to register command handler '" + commandHandlerName + "'!", th);
                    }
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.initializeHandlerRegistry();
    }
}
