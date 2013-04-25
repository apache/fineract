package org.mifosplatform.infrastructure.core.service;

import java.util.Random;

public class RandomStringGenerator {
    public static Random random = new Random();

    /**
     * Generate a random String
     *
     * @return
     */
    public static String generateRandomString() {
        String characters = "abcdefghijklmnopqrstuvwxyz123456789";
        int length = generateRandomNumber();
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    /**
     * Generate a random number between 5 to 16
     *
     * @return
     */
    public static int generateRandomNumber() {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(11) + 5;
    }

}
