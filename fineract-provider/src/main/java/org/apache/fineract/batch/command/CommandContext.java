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
package org.apache.fineract.batch.command;

/**
 * Provides an object to {@link org.apache.fineract.batch.service.BatchApiService}
 * to get the proper commandStrategy for each request in BatchRequest. It uses
 * Builder pattern to create object of this type.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.apache.fineract.batch.service.BatchApiService
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
