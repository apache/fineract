/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

public class RandomPasswordGenerator {

    private final int numberOfCharactersInPassword;

    public RandomPasswordGenerator(final int numberOfCharactersInPassword) {
        this.numberOfCharactersInPassword = numberOfCharactersInPassword;
    }

    public String generate() {

        final StringBuilder passwordBuilder = new StringBuilder(this.numberOfCharactersInPassword);
        for (int i = 0; i < this.numberOfCharactersInPassword; i++) {
            passwordBuilder.append((char) ((int) (Math.random() * 26) + 97));
        }
        return passwordBuilder.toString();
    }
}