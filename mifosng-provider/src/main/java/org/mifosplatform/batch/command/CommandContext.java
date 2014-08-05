/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.command;

/**
 * Provides an object to {@link org.mifosplatform.batch.service.BatchApiService}
 * to get the proper commandStrategy for each request in BatchRequest. It uses
 * Builder pattern to create object of this type.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.service.BatchApiService
 */
public class CommandContext {

    /**
     * Static Builder class to provide a Build method for CommandContext.
     * 
     * @author Rishabh Shukla
     */
    public static class Builder {

        private String resource;
        private String method;

        private Builder(final String resource) {
            this.resource = resource;
        }

        public Builder method(final String method) {
            this.method = method;
            return this;
        }

        public CommandContext build() {
            return new CommandContext(this.resource, this.method);
        }

    }

    private final String resource;
    private final String method;

    private CommandContext(final String resource, final String method) {

        this.resource = resource;
        this.method = method;
    }

    public static Builder resource(final String resource) {
        return new Builder(resource);
    }

    /**
     * Returns a boolean value if the relativeUrl 'matches' one of the regex
     * keys in the available commandStrategies. It take CommandContext object as
     * parameter which contains a 'resource' member as a regex key for available
     * commandStrategies.
     * 
     * @param other
     * @return boolean
     */
    public boolean matcher(CommandContext other) {
        if (this.resource.matches(other.resource) && this.method.equals(other.method)) { return true; }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.method == null) ? 0 : this.method.hashCode());
        result = prime * result + ((this.resource == null) ? 0 : this.resource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CommandContext other = (CommandContext) obj;
        if (this.method == null) {
            if (other.method != null) return false;
        } else if (!this.method.equals(other.method)) return false;
        if (this.resource == null) {
            if (other.resource != null) return false;
        } else if (!this.resource.equals(other.resource)) return false;
        return true;
    }

}
