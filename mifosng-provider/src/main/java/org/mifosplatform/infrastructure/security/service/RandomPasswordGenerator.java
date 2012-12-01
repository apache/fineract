package org.mifosplatform.infrastructure.security.service;

public class RandomPasswordGenerator {

    private final int numberOfCharactersInPassword;

    public RandomPasswordGenerator(final int numberOfCharactersInPassword) {
        this.numberOfCharactersInPassword = numberOfCharactersInPassword;
    }

    public String generate() {

        StringBuilder passwordBuilder = new StringBuilder(this.numberOfCharactersInPassword);
        for (int i = 0; i < this.numberOfCharactersInPassword; i++) {
            passwordBuilder.append((char) ((int) (Math.random() * 26) + 97));
        }
        return passwordBuilder.toString();
    }
}