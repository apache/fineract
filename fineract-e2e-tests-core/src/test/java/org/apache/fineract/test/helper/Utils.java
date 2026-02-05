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
package org.apache.fineract.test.helper;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public final class Utils {

    private static final SecureRandom random = new SecureRandom();
    private static final List<String> firstNames = List.of("Adam", "Alex", "Andrew", "Anthony", "Arthur", "Benjamin", "Brian", "Brandon",
            "Bruce", "Caleb", "Charles", "Christian", "Christopher", "Daniel", "David", "Dennis", "Dominic", "Edward", "Ethan", "Felix",
            "Frank", "Gabriel", "George", "Gregory", "Harry", "Henry", "Isaac", "Jack", "Jacob", "James", "Jason", "John", "Jonathan",
            "Joseph", "Joshua", "Julian", "Kevin", "Kyle", "Leo", "Liam", "Logan", "Lucas", "Luke", "Marcus", "Mark", "Martin", "Matthew",
            "Michael", "Nathan", "Nicholas", "Noah", "Oliver", "Oscar", "Patrick", "Paul", "Peter", "Philip", "Raymond", "Richard",
            "Robert", "Ryan", "Samuel", "Scott", "Sean", "Simon", "Stephen", "Steven", "Thomas", "Timothy", "Victor", "William", "Zachary",
            "Aaron", "Adrian", "Alan", "Albert", "Allen", "Antonio", "Austin", "Blake", "Cameron", "Carlos", "Colin", "Dylan", "Eric",
            "Harrison", "Ian", "Jeremy", "Jordan", "Kevin", "Louis", "Mitchell", "Neil", "Roger", "Trevor");

    private static final List<String> lastNames = List.of("Anderson", "Armstrong", "Baker", "Barnes", "Bell", "Bennett", "Brooks", "Brown",
            "Bryant", "Butler", "Campbell", "Carter", "Clark", "Collins", "Cook", "Cooper", "Cox", "Davis", "Diaz", "Edwards", "Evans",
            "Fisher", "Foster", "Garcia", "Gomez", "Gonzalez", "Gray", "Green", "Hall", "Harris", "Hernandez", "Hill", "Howard", "Hughes",
            "Jackson", "James", "Jenkins", "Johnson", "Jones", "Kelly", "Kim", "King", "Lee", "Lewis", "Lopez", "Martin", "Martinez",
            "Miller", "Mitchell", "Moore", "Morgan", "Morris", "Murphy", "Nelson", "Nguyen", "Parker", "Perez", "Peterson", "Phillips",
            "Powell", "Price", "Ramirez", "Reed", "Richardson", "Rivera", "Roberts", "Robinson", "Rodriguez", "Rogers", "Ross", "Russell",
            "Sanchez", "Scott", "Smith", "Stewart", "Taylor", "Thomas", "Thompson", "Torres", "Turner", "Walker", "Ward", "Watson", "White",
            "Williams", "Wilson", "Wood", "Wright", "Young", "Zhang");

    private Utils() {}

    public static String randomStringGenerator(final String prefix, final int len, final String sourceSetString) {
        final int lengthOfSource = sourceSetString.length();
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(sourceSetString.charAt(random.nextInt(lengthOfSource)));
        }
        return prefix + sb;
    }

    public static String randomStringGenerator(final String prefix, final int len) {
        return randomStringGenerator(prefix, len, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    public static String randomFirstNameGenerator() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }

    public static String randomLastNameGenerator() {
        return lastNames.get(random.nextInt(lastNames.size()));
    }

    public static LocalDate now() {
        return LocalDate.now(Clock.systemUTC());
    }

    /**
     * A record that formats a double value based on whether it's a whole number or not.
     * <p>
     * If the value is a whole number, the output will have one decimal place (e.g., 16.0). Otherwise, it will have two
     * decimal places (e.g., 16.90), but if the second decimal place is zero, it will be removed (so 16.90 becomes
     * 16.9).
     */
    public record DoubleFormatter(double value) {

        public String format() {
            boolean isWholeNumber = (value % 1.0 == 0);

            String result = isWholeNumber ? String.format("%.1f", value) : String.format("%.2f", value);

            // For non-whole numbers, remove trailing '0' if it exists
            if (!isWholeNumber && result.endsWith("0")) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        }
    }
}
