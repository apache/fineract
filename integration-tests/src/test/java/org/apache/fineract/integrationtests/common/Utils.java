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
package org.apache.fineract.integrationtests.common;

import static io.restassured.RestAssured.given;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.ConfigProperties;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

/**
 * Util for RestAssured tests. This class here in src/integrationTest is copy/pasted to src/test; please keep them in
 * sync.
 */
@SuppressWarnings("unchecked")
public final class Utils {

    public static final String TENANT_PARAM_NAME = "tenantIdentifier";
    public static final String DEFAULT_TENANT = ConfigProperties.Backend.TENANT;
    public static final String TENANT_IDENTIFIER = TENANT_PARAM_NAME + '=' + DEFAULT_TENANT;
    private static final String LOGIN_URL = "/fineract-provider/api/v1/authentication?" + TENANT_IDENTIFIER;
    public static final String TENANT_TIME_ZONE = "Asia/Kolkata";
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm";
    public static final String LOCALE = "en";
    public static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();
    public static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern(DATE_TIME_FORMAT).toFormatter();
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final SecureRandom random = new SecureRandom();
    private static final Gson gson = new Gson();
    private static final String HEALTH_URL = "/fineract-provider/actuator/health";

    private static final ConcurrentHashMap<String, Set<String>> uniqueRandomStringContainer = new ConcurrentHashMap<>();
    public static final String SOURCE_SET_NUMBERS_AND_LETTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String SOURCE_SET_NUMBERS = "1234567890";

    private static final List<String> firstNames = List.of("Aarav", "Abel", "Abner", "Abram", "Ace", "Aden", "Adler", "Adonis", "Ainsley",
            "Alaric", "Alastair", "Alberto", "Alden", "Alecander", "Alessandro", "Alfonso", "Alfredo", "Alistair", "Alonzo", "Amari",
            "Ambrose", "Amir", "Ansel", "Archer", "Archie", "Arjun", "Arlo", "Armando", "Arnav", "Aron", "Arturo", "Aryan", "Asa",
            "Atticus", "Aubrey", "August", "Augustus", "Aurelio", "Avi", "Axel", "Bailey", "Basil", "Beau", "Beckett", "Benicio", "Benson",
            "Blaine", "Bo", "Bode", "Boris", "Bowen", "Brady", "Branson", "Brayan", "Briggs", "Brock", "Brody", "Bronson", "Bryce", "Byron",
            "Cade", "Caiden", "Cairo", "Callan", "Callum", "Cannon", "Carlton", "Carmelo", "Casey", "Cassius", "Cedrick", "Chance",
            "Channing", "Chase", "Cillian", "Cohen", "Conrad", "Corbin", "Cory", "Cruz", "Dakota", "Dalton", "Dane", "Dario", "Darrell",
            "Darwin", "Dash", "Davis", "Deacon", "Deandre", "Declan", "Demetrius", "Denver", "Devin", "Dexter", "Dominik", "Dorian",
            "Drake", "Duncan", "Edison", "Elian", "Elias", "Eliezer", "Elisha", "Elmer", "Elon", "Elvis", "Emilio", "Emory", "Ephraim",
            "Erik", "Ernest", "Esteban", "Eugene", "Ewan", "Ezra", "Fabian", "Faisal", "Finn", "Fletcher", "Ford", "Franco", "Gael", "Gage",
            "Gannon", "Gary", "Gideon", "Gianni", "Giovanni", "Glen", "Grady", "Grayson", "Guillermo", "Gunnar", "Gustav", "Hank", "Harlan",
            "Hendrix", "Hugo", "Hunter", "Ibrahim", "Idris", "Ignacio", "Imran", "Indiana", "Ismael", "Israel", "Jace", "Jaime", "Jalen",
            "Jamal", "Jax", "Jaxon", "Jay", "Jayce", "Jayden", "Jensen", "Jermaine", "Jett", "Jimmy", "Joaquin", "Johan", "Johnny", "Jonas",
            "Jorge", "Josiah", "Juan", "Justice", "Kade", "Kai", "Kaiser", "Kareem", "Karter", "Kash", "Kason", "Keegan", "Keon", "Khalil",
            "Kieran", "Knox", "Kobe", "Kody", "Kole", "Korbin", "Kristian", "Kurt", "Kyler", "Lachlan", "Lamar", "Landon", "Langston",
            "Lars", "Lawrence", "Layton", "Leandro", "Lee", "Lennon", "Leroy", "Liaman", "Lorenzo", "Luciano", "Luis", "Maddox", "Malachi",
            "Marek", "Marioh", "Matias", "Matteo", "Mauricio", "Maverick", "Memphis", "Milan", "Miller", "Mohamed", "Montgomery", "Moses",
            "Murphy", "Mustafa", "Nadir", "Nathanael", "Nehemiah", "Nico", "Nikolai", "Noel", "Nova", "Octavio", "Odell", "Odin", "Onyx",
            "Orion", "Otis", "Paolo", "Percy", "Phoenix", "Pierce", "Porter", "Prince", "Quincy", "Raiden", "Ramon", "Raphael", "Reid",
            "Remington", "Rhett", "Rocco", "Rodrigo", "Royce", "Ruben", "Rudy", "Ryker", "Salvatore", "Santino", "Saul", "Sebastian",
            "Sergio", "Shane", "Shiloh", "Silas", "Sincere", "Skyler", "Soren", "Sterling", "Stone", "Sullivan", "Sutton", "Talon", "Tate",
            "Tatum", "Thiago", "Tobias", "Tomas", "Trace", "Tucker", "Turner", "Ulises", "Uriah", "Valentin", "Van", "Vance", "Vicente",
            "Viktor", "Warren", "Watson", "Weston", "Willis", "Wilson", "Winston", "Wyatt", "Xander", "Yael", "Yahir", "Yusuf", "Zaire",
            "Zander", "Zion", "Zayn", "Abdiel", "Aditya", "Akeem", "Alvaro", "Amadeo", "Anders", "Anwar", "Armani", "Ayaan", "Aziel",
            "Azriel", "Baker", "Benedict", "Bishop", "Blaise", "Bridger", "Cadeo", "Camilo", "Casen", "Caspian", "Cayson", "Cesar",
            "Chandler", "Cortez", "Damari", "Dangelo", "Darioh", "Davion", "Dilan", "Dion", "Eliam", "Elio", "Emiliano", "Enrique",
            "Ezekiel", "Farhan", "Fisher", "Gino", "Griffin", "Hamza", "Harley", "Henrik", "Iker", "Ira", "Jabari", "Jair", "Javon",
            "Jiraiya", "Kaison", "Kamdyn", "Kasen", "Kendrick", "Kian", "Kye", "Lennox", "Leonidas", "Lucian", "Makai", "Marcelo",
            "Marcellus", "Marvin", "Massimo", "Misael", "Moises", "Nasir", "Naveen", "Niklaus", "Nixon", "Omari", "Osiris", "Ozzy",
            "Palmer", "Paxton", "Quade", "Rayan", "Reuben", "Ronan", "Roscoe", "Samir", "Santana", "Seven", "Shepherd", "Simeon", "Solomon",
            "Thaddeus", "Titan", "Trey", "Tripp", "Ty", "Ulysses", "Valor", "Vihaan", "Wilder", "Zev", "Aarush", "Abhiram", "Adem",
            "Adriel", "Aeson", "Ahmad", "Aldo", "Alvaro", "Anakin", "Aramis", "Ares", "Axton", "Baylor", "Benton", "Brax", "Briar", "Bruno",
            "Calloway", "Cassian", "Cayson", "Crosby", "Cullen", "Dariel", "Davian", "Dax", "Daxter", "Dior", "Eithan", "Eren", "Eros",
            "Evander", "Faris", "Finley", "Gatlin", "Grey", "Hollis", "Izan", "Jovanni", "Kael", "Kairo", "Kellan", "Khaled", "Koa", "Krew",
            "Kyrie", "Leif", "Lochlan", "Lucius", "Mael", "Malakai", "Marquez", "Matheo", "Neo", "Nikolas", "Ocean", "Osman", "Raul",
            "Reign", "Riven", "Rory", "Rowen", "Sage", "Salem", "Shawnte", "Stellan", "Tadeo", "Teo", "Thierry", "Torin", "Zaid");

    private static final List<String> lastNames = List.of("Abbott", "Acevedo", "Acosta", "Adams", "Adkins", "Aguilar", "Aguirre", "Albert",
            "Alexander", "Alford", "Allen", "Allison", "Alston", "Alvarado", "Alvarez", "Ambrose", "Ames", "Anthony", "Archer", "Arias",
            "Arnold", "Ashley", "Atkins", "Atkinson", "Austin", "Avery", "Avila", "Ayala", "Baird", "Baldwin", "Ball", "Ballard", "Banks",
            "Barber", "Barker", "Barlow", "Barrett", "Barron", "Barry", "Bartlett", "Barton", "Bass", "Bates", "Battle", "Bauer", "Baxter",
            "Beach", "Beard", "Beasley", "Beck", "Becker", "Bellamy", "Bender", "Benjamin", "Benson", "Bentley", "Berg", "Berger",
            "Bernard", "Berry", "Best", "Bird", "Bishop", "Black", "Blackburn", "Blackwell", "Blair", "Blake", "Blanchard", "Blankenship",
            "Blevins", "Bolton", "Bond", "Bonner", "Booker", "Boone", "Bowen", "Bowers", "Bowman", "Boyd", "Boyle", "Bradford", "Bradley",
            "Branch", "Bray", "Brennan", "Brewer", "Bridges", "Briggs", "Bright", "Britt", "Brock", "Brockman", "Brooksbank", "Browning",
            "Bruce", "Bryan", "Buck", "Buckley", "Bullock", "Burch", "Burgess", "Burke", "Burnett", "Burns", "Burris", "Bush", "Byers",
            "Byrd", "Cabrera", "Cain", "Calderon", "Callahan", "Calhoun", "Camacho", "Cameron", "Campos", "Cannon", "Cantrell", "Cardenas",
            "Carey", "Carlson", "Carney", "Carpenter", "Carr", "Carrillo", "Carroll", "Carson", "Carteret", "Case", "Casey", "Castaneda",
            "Castillo", "Castro", "Cervantes", "Chambers", "Chan", "Chandler", "Chang", "Chapman", "Charles", "Chase", "Chavez", "Chen",
            "Cherry", "Christian", "Church", "Cisneros", "Clarke", "Clay", "Clayton", "Clements", "Clemons", "Cleveland", "Cline", "Cobb",
            "Cochran", "Coffey", "Cohen", "Cole", "Combs", "Compton", "Conley", "Conner", "Conrad", "Contreras", "Conway", "Cooke",
            "Copeland", "Cortez", "Cotton", "Cowan", "Craig", "Crane", "Crawford", "Crosby", "Cross", "Cruz", "Cummings", "Cunningham",
            "Curry", "Curtis", "Dalton", "Daniel", "Daniels", "Daugherty", "Davenport", "David", "Davidson", "Day", "Dean", "Decker",
            "Delacruz", "Dennis", "Deleon", "Delgado", "Dempsey", "Depp", "Devin", "Dickerson", "Dillard", "Dillon", "Dixon", "Dodson",
            "Dominguez", "Donaldson", "Donovan", "Dorsey", "Dotson", "Douglas", "Downs", "Doyle", "Drake", "Dudley", "Duffy", "Duke",
            "Dunlap", "Dunn", "Durham", "Dyer", "Eaton", "Elliott", "Ellis", "Ellison", "Emerson", "England", "English", "Erickson",
            "Espinoza", "Estes", "Estrada", "Ewing", "Farley", "Farmer", "Farrell", "Faulkner", "Ferguson", "Fernandez", "Ferrell",
            "Fields", "Finch", "Finley", "Fitzgerald", "Fleming", "Flores", "Flynn", "Foley", "Forbes", "Ford", "Foreman", "Fowler", "Fox",
            "Francis", "Franco", "Frank", "Franklin", "Franks", "Frazier", "Frederick", "French", "Frost", "Fry", "Fuentes", "Fuller",
            "Gaines", "Gallagher", "Galloway", "Gamble", "Garner", "Garrett", "Garrison", "Garza", "Gates", "Gay", "George", "Gibbs",
            "Gibson", "Gilbert", "Giles", "Gill", "Gillespie", "Gilliam", "Glass", "Glenn", "Glover", "Golden", "Good", "Goodman",
            "Goodwin", "Gordon", "Gould", "Grady", "Graham", "Grant", "Graves", "Greene", "Griffin", "Griffith", "Gross", "Guerra",
            "Guerrero", "Guthrie", "Gutierrez", "Hahn", "Haley", "Hampton", "Hancock", "Haney", "Hansen", "Hardin", "Harding", "Hardy",
            "Harmon", "Harper", "Harrington", "Hart", "Hartman", "Harvey", "Hatfield", "Hawkins", "Hayden", "Hayes", "Haynes", "Heath",
            "Henderson", "Hendricks", "Henry", "Henson", "Herman", "Herrera", "Hess", "Hickman", "Hicks", "Higgins", "Hines", "Hinton",
            "Hobbs", "Hodge", "Hodges", "Hoffman", "Holder", "Holland", "Holman", "Holmes", "Holt", "Hood", "Hooper", "Hoover", "Hopkins",
            "Horne", "Horton", "House", "Houston", "Howell", "Hubbard", "Hudson", "Huff", "Huffman", "Hunt", "Hunter", "Hurley", "Hurst",
            "Ingram", "Irwin", "Jacobs", "Jacoby", "Jarvis", "Jefferson", "Jennings", "Jimenez", "Johns", "Johnston", "Joyce", "Juarez",
            "Justice", "Kane", "Kaufman", "Keith", "Keller", "Kelley", "Kemp", "Kennedy", "Kent", "Kerr", "Key", "Kidd", "Kinney", "Kirby",
            "Kirk", "Klein", "Knapp", "Knight", "Knowles", "Lamb", "Lambert", "Lancaster", "Landry", "Lane", "Lang", "Langley", "Larsen",
            "Lawrence", "Lawson", "Leach", "Leblanc", "Levine", "Levy", "Lindsey", "Little", "Livingston", "Lloyd", "Logan", "Long", "Love",
            "Lowe", "Lucas", "Luna", "Lynch", "Macias", "Mack", "Maddox", "Maldonado", "Malone", "Mann", "Manning", "Marks", "Marquez",
            "Marshall", "Mason", "Massey", "Mathews", "Mathis", "Mccall", "Mccarthy", "Mccormick", "Mcdaniel", "Mcdonald", "Meyer", "Miles",
            "Montoya", "Montgomery", "Montes", "Moon", "Morales", "Moreno", "Moss", "Mueller", "Mullen", "Mullins", "Munoz", "Myers",
            "Nash", "Navarro", "Neal", "Newton", "Nichols", "Noble", "Nolan", "Norris", "Norton", "Novak", "Ochoa", "Oconnor", "Odom",
            "Oliver", "Olson", "Oneal", "Ortega", "Ortiz", "Osborne", "Owen", "Owens", "Pace", "Pacheco", "Padilla", "Page", "Palmer",
            "Park", "Parsons", "Patel", "Patrick", "Paul", "Payne", "Pearson", "Peck", "Pena", "Pennington", "Perkins", "Phelps", "Pittman",
            "Poole", "Pope", "Porter", "Potter", "Pratt", "Preston", "Pruitt", "Quinn", "Ramos", "Randall", "Rasmussen", "Ray", "Raymond",
            "Reeves", "Reid", "Reyes", "Reynolds", "Rios", "Roach", "Robbins", "Rocha", "Roman", "Romero", "Rosa", "Rowe", "Roy", "Ruiz",
            "Rush", "Rutledge", "Salazar", "Salinas", "Sanders", "Santana", "Santiago", "Santos", "Savage", "Schmidt", "Schneider",
            "Schroeder", "Sharp", "Shaw", "Shelton", "Shepard", "Shepherd", "Sheppard", "Sherman", "Shields", "Short", "Silva", "Simmons",
            "Simon", "Simpson", "Sims", "Skinner", "Slater", "Sloan", "Small", "Snow", "Snyder", "Solomon", "Sosa", "Sparks", "Spears",
            "Spence", "Stafford", "Stanley", "Stark", "Steele", "Stephens", "Stephenson", "Stone", "Strickland", "Strong", "Suarez",
            "Sullivan", "Summers", "Sutton", "Swanson", "Sweeney", "Sweet", "Tanner", "Tate", "Terrell", "Terry", "Todd", "Townsend",
            "Travis", "Trujillo", "Underwood", "Valdez", "Valencia", "Vargas", "Vasquez", "Vaughn", "Vega", "Velasquez", "Villarreal",
            "Vincent", "Vinson", "Wade", "Wagner", "Wall", "Walsh", "Walter", "Walters", "Walton", "Warner", "Warren", "Waters", "Weaver",
            "Webb", "Weber", "Weeks", "Weiss", "Wells", "Wheeler", "Whitaker", "Whitfield", "Wilcox", "Wilkerson", "Wilkins", "Wilkinson",
            "Willis", "Winters", "Wise", "Wolfe", "Wong", "Woodard", "Woods", "Wooten", "Workman", "Wyatt", "Yates", "York", "Zamora",
            "Zimmerman");

    private Utils() {}

    @Deprecated(forRemoval = true)
    public static void initializeRESTAssured() {
        RestAssured.baseURI = ConfigProperties.Backend.PROTOCOL + "://" + ConfigProperties.Backend.HOST;
        RestAssured.port = ConfigProperties.Backend.PORT;
        RestAssured.keyStore("src/main/resources/keystore.jks", "openmf");
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Deprecated(forRemoval = true)
    public static RequestSpecification initializeDefaultRequestSpecification() {
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        return requestSpec;
    }

    @Deprecated(forRemoval = true)
    public static ResponseSpecification initializeDefaultResponseSpecification() {
        return new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    private static void awaitSpringBootActuatorHealthyUp() {
        int attempt = 0;
        final int max_attempts = 10;
        Response response = null;
        Exception lastException = null;
        do {
            try {
                response = RestAssured.get(HEALTH_URL);
                int healthHttpStatus = response.statusCode();
                if (healthHttpStatus == 200) {
                    LOG.info("{} return HTTP 200, application is now ready for integration testing!", HEALTH_URL);
                    return;
                } else {
                    LOG.info("{} returned HTTP {}, going to wait and retry (attempt {})", HEALTH_URL, healthHttpStatus, attempt++);
                    sleep(3);
                }
            } catch (Exception e) {
                LOG.info("{} caused {}, going to wait and retry (attempt {})", HEALTH_URL, e.getMessage(), attempt++);
                lastException = e;
                sleep(3);
            }
        } while (attempt < max_attempts);

        if (lastException != null) {
            LOG.error("{} still not reachable, giving up", HEALTH_URL, lastException);
            throw new AssertionError(HEALTH_URL + " not reachable", lastException);
        } else {
            LOG.error("{} still has not returned HTTP 200, giving up (last) body: {}", HEALTH_URL, response.prettyPrint());
            fail(HEALTH_URL + " returned " + response.prettyPrint());
        }
    }

    /**
     * Wait until the given condition is true or the maxRun is reached.
     *
     * @param maxRun
     *            max number of times to run the condition
     * @param seconds
     *            wait time between evaluation in seconds
     * @param waitCondition
     *            condition to evaluate
     */
    public static void conditionalSleepWithMaxWait(int maxRun, int seconds, Supplier<Boolean> waitCondition) {
        do {
            sleep(seconds);
            maxRun--;
        } while (maxRun > 0 && waitCondition.get());
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            LOG.warn("Unexpected InterruptedException", e);
            throw new IllegalStateException("Unexpected InterruptedException", e);
        }
    }

    @Deprecated(forRemoval = true)
    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        return loginIntoServerAndGetBase64EncodedAuthenticationKey(ConfigProperties.Backend.USERNAME, ConfigProperties.Backend.PASSWORD);
    }

    @Deprecated(forRemoval = true)
    public static String loginIntoServerAndGetBase64EncodedAuthenticationKey(String username, String password) {
        awaitSpringBootActuatorHealthyUp();
        try {
            LOG.info("Logging in, for integration test...");
            // system.out.println("-----------------------------------LOGIN-----------------------------------------");
            String json = RestAssured.given().contentType(ContentType.JSON)
                    .body("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}").expect().log().ifError().when()
                    .post(LOGIN_URL).asString();
            assertThat("Failed to login into fineract platform", StringUtils.isBlank(json), is(false));
            String key = JsonPath.with(json).get("base64EncodedAuthenticationKey");
            assertThat("Failed to obtain key: " + json, StringUtils.isBlank(key), is(false));
            return key;
        } catch (final Exception e) {
            if (e instanceof HttpHostConnectException) {
                final HttpHostConnectException hh = (HttpHostConnectException) e;
                fail("Failed to connect to fineract platform:" + hh.getMessage());
            }

            throw new RuntimeException(e);
        }
    }

    @Deprecated(forRemoval = true)
    public static String performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String url) {
        return performServerGet(requestSpec, responseSpec, url, null);
    }

    @Deprecated(forRemoval = true)
    public static Response performServerGetRaw(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, Function<RequestSpecification, RequestSpecification> requestMapper) {
        return requestMapper.apply(given().spec(requestSpec)).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn();
    }

    @Deprecated(forRemoval = true)
    public static <T> T performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
        if (jsonAttributeToGetBack == null) {
            return (T) json;
        }
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

    @Deprecated(forRemoval = true)
    public static <T> T performServerPatch(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().patch(getURL).andReturn()
                .asString();
        if (jsonAttributeToGetBack == null) {
            return (T) json;
        }
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

    @Deprecated(forRemoval = true)
    public static List<String> performServerGetList(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final JsonPath jsonPath = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).jsonPath();
        List<String> items = jsonPath.getList(jsonAttributeToGetBack);
        return items;
    }

    @Deprecated(forRemoval = true)
    public static JsonElement performServerGetArray(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final int position, final String jsonAttributeToGetBack) {
        final JsonPath jsonPath = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).jsonPath();
        List<Map<String, Object>> items = jsonPath.getList("$");
        return gson.fromJson(((ArrayList) items.get(position).get(jsonAttributeToGetBack)).toString(), JsonArray.class);
    }

    @Deprecated(forRemoval = true)
    public static String performGetTextResponse(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL) {
        return given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
    }

    @Deprecated(forRemoval = true)
    public static byte[] performGetBinaryResponse(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL) {
        return given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asByteArray();
    }

    @Deprecated(forRemoval = true)
    public static String performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String postURL, final String jsonBodyToSend) {
        return performServerPost(requestSpec, responseSpec, postURL, jsonBodyToSend, null);
    }

    @Deprecated(forRemoval = true)
    public static <T> T performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String postURL, final String jsonBodyToSend, final String jsonAttributeToGetBack) {
        LOG.info("JSON {}", jsonBodyToSend);
        RequestSpecification spec = given().spec(requestSpec);
        if (StringUtils.isNotBlank(jsonBodyToSend)) {
            spec = spec.body(jsonBodyToSend);
        }
        final String json = spec.expect().spec(responseSpec).log().ifError().when().post(postURL).andReturn().asString();
        if (jsonAttributeToGetBack == null) {
            return (T) json;
        }
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

    @Deprecated(forRemoval = true)
    public static Response performServerPutRaw(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String putURL, Function<RequestSpecification, RequestSpecification> bodyMapper) {
        return bodyMapper.apply(given().spec(requestSpec)).expect().spec(responseSpec).log().ifError().when().put(putURL).andReturn();
    }

    @Deprecated(forRemoval = true)
    public static String performServerPut(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String putURL, final String jsonBodyToSend) {
        return performServerPut(requestSpec, responseSpec, putURL, jsonBodyToSend, null);
    }

    @Deprecated(forRemoval = true)
    public static <T> T performServerPut(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String putURL, final String jsonBodyToSend, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).body(jsonBodyToSend).expect().spec(responseSpec).log().ifError().when().put(putURL)
                .andReturn().asString();
        if (jsonAttributeToGetBack == null) {
            return (T) json;
        }
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

    @Deprecated(forRemoval = true)
    public static <T> T performServerDelete(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String deleteURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().delete(deleteURL).andReturn()
                .asString();
        if (jsonAttributeToGetBack == null) {
            return (T) json;
        }
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

    @Deprecated(forRemoval = true)
    public static <T> T performServerDelete(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String deleteURL, final String jsonBodyToSend, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).body(jsonBodyToSend).expect().spec(responseSpec).log().ifError().when()
                .delete(deleteURL).andReturn().asString();
        return (T) (jsonAttributeToGetBack == null ? json : JsonPath.from(json).get(jsonAttributeToGetBack));
    }

    public static String convertDateToURLFormat(final String dateToBeConvert) throws ParseException {
        final SimpleDateFormat oldFormat = new SimpleDateFormat("dd MMMMMM yyyy", Locale.US);
        final SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        String reformattedStr = "";

        reformattedStr = newFormat.format(oldFormat.parse(dateToBeConvert));

        return reformattedStr;
    }

    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public static String randomStringGenerator(final String prefix, final int len, final String sourceSetString) {
        final int lengthOfSource = sourceSetString.length();
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(sourceSetString.charAt(random.nextInt(lengthOfSource)));
        }
        return prefix + sb;
    }

    public static String randomStringGenerator(final String prefix, final int len) {
        return randomStringGenerator(prefix, len, SOURCE_SET_NUMBERS_AND_LETTERS);
    }

    public static String uniqueRandomStringGenerator(final String prefix, final int lenOfRandomSuffix) {
        return uniqueRandomGenerator(prefix, lenOfRandomSuffix, SOURCE_SET_NUMBERS_AND_LETTERS);
    }

    public static Integer uniqueRandomNumberGenerator(final int lenOfRandomSuffix) {
        return Integer.parseInt(uniqueRandomGenerator("", lenOfRandomSuffix, SOURCE_SET_NUMBERS));
    }

    public static String uniqueRandomGenerator(final String prefix, final int lenOfRandomSuffix, String sourceSet) {
        String response;
        String key = prefix + lenOfRandomSuffix;
        int i = 0;
        do {
            response = randomStringGenerator(prefix, lenOfRandomSuffix, sourceSet);
            uniqueRandomStringContainer.putIfAbsent(key, new HashSet<>());
            i++;

            if (i == 10000) {
                throw new IllegalStateException("Possible endless loop for: " + key);
            }
        } while (!uniqueRandomStringContainer.get(key).add(response));

        return response;
    }

    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public static Integer randomNumberGenerator(final int expectedLength) {
        String response = randomStringGenerator("", expectedLength, SOURCE_SET_NUMBERS);
        return Integer.parseInt(response);
    }

    public static Float randomDecimalGenerator(final int expectedWholeLength, final int expectedFractionLength) {
        final String source = SOURCE_SET_NUMBERS;
        final int lengthOfSource = source.length();

        StringBuilder stringBuilder = new StringBuilder(expectedWholeLength + expectedFractionLength + 1);
        for (int i = 0; i < expectedWholeLength; i++) {
            stringBuilder.append(source.charAt(random.nextInt(lengthOfSource)));
        }
        stringBuilder.append(".");
        for (int i = 0; i < expectedFractionLength; i++) {
            stringBuilder.append(source.charAt(random.nextInt(lengthOfSource)));
        }
        return Float.parseFloat(stringBuilder.toString());
    }

    public static String convertDateToURLFormat(final Calendar dateToBeConvert) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMMMM yyyy");
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        return dateFormat.format(dateToBeConvert.getTime());
    }

    public static String convertDateToURLFormat(final Calendar dateToBeConvert, final String dateGormat) {
        DateFormat dateFormat = new SimpleDateFormat(dateGormat);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        return dateFormat.format(dateToBeConvert.getTime());
    }

    @NonNull
    public static OffsetDateTime getAuditDateTimeToCompare() throws InterruptedException {
        OffsetDateTime now = DateUtils.getAuditOffsetDateTime();
        // Testing in minutes precision, but still need to take care around the end of the actual minute
        if (now.getSecond() > 56) {
            Thread.sleep(5000);
            now = DateUtils.getAuditOffsetDateTime();
        }
        return now;
    }

    public static TimeZone getTimeZoneOfTenant() {
        return TimeZone.getTimeZone(TENANT_TIME_ZONE);
    }

    public static ZoneId getZoneIdOfTenant() {
        return ZoneId.of(TENANT_TIME_ZONE);
    }

    public static LocalDate getLocalDateOfTenant() {
        return LocalDate.now(getZoneIdOfTenant());
    }

    public static LocalDateTime getLocalDateTimeOfTenant() {
        return LocalDateTime.now(getZoneIdOfTenant());
    }

    public static Date convertJsonElementAsDate(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, jsonArray.get(0).getAsInt());
            calendar.set(Calendar.MONTH, jsonArray.get(1).getAsInt() - 1);
            calendar.set(Calendar.DATE, jsonArray.get(2).getAsInt());
            // If the Array includes Time
            if (jsonArray.size() > 3) {
                calendar.set(Calendar.HOUR, jsonArray.get(3).getAsInt());
                calendar.set(Calendar.MINUTE, jsonArray.get(4).getAsInt());
                calendar.set(Calendar.SECOND, jsonArray.get(5).getAsInt());
            }
            return calendar.getTime();
        }
        return null;
    }

    @Deprecated(forRemoval = true)
    public static String performServerTemplatePost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String postURL, final String legalFormType, final File file, final String locale, final String dateFormat) {

        final String importDocumentId = given().spec(requestSpec).queryParam("legalFormType", legalFormType).multiPart("file", file)
                .formParam("locale", locale).formParam("dateFormat", dateFormat).expect().spec(responseSpec).log().ifError().when()
                .post(postURL).andReturn().asString();
        return importDocumentId;
    }

    @Deprecated(forRemoval = true)
    public static String performServerOutputTemplateLocationGet(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String getURL, final String importDocumentId) {
        return given().spec(requestSpec).queryParam("importDocumentId", importDocumentId).expect().spec(responseSpec).log().ifError().when()
                .get(getURL).andReturn().asString();
    }

    @Deprecated(forRemoval = true)
    public static String emptyJson() {
        return "{}";
    }

    public static String randomDateGenerator(String dateFormat) {
        DateTimeFormatter dateTimeFormatterBuilder = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                .appendPattern(dateFormat).optionalStart().appendPattern(" HH:mm:ss").optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
        LocalDate localDate = LocalDate.of(getYear(), getMonth(), getDay());
        return dateTimeFormatterBuilder.format(localDate);
    }

    public static String randomDateTimeGenerator(String dateFormat) {
        DateTimeFormatter dateTimeFormatterBuilder = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                .appendPattern(dateFormat).optionalStart().appendPattern(" HH:mm:ss").optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
        LocalDateTime localDate = LocalDateTime.of(getYear(), getMonth(), getDay(), getHour(), getMinute(), getSecond());
        return dateTimeFormatterBuilder.format(localDate);
    }

    private static int getYear() {
        return 2000 + random.nextInt(31);
    }

    private static int getMonth() {
        return 10 + random.nextInt(3);
    }

    private static int getDay() {
        return 10 + random.nextInt(16);
    }

    private static int getHour() {
        return 10 + random.nextInt(14);
    }

    private static int getMinute() {
        return 10 + random.nextInt(50);
    }

    private static int getSecond() {
        return 10 + random.nextInt(50);
    }

    public static String arrayDateToString(List intArray) {
        String[] strArray = (String[]) intArray.stream().map(String::valueOf).toArray(String[]::new);
        return String.join("-", strArray);
    }

    public static String arrayDateTimeToString(List<Integer> integerList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i < 2) {
                stringBuilder.append(integerList.get(i)).append("-");
            } else if (i == 2) {
                stringBuilder.append(integerList.get(i)).append(" ");
            } else if (i == 3) {
                stringBuilder.append(integerList.get(i));
            } else {
                stringBuilder.append(":").append(integerList.get(i));
            }
        }
        return stringBuilder.toString();
    }

    public static String convertToJson(HashMap<String, Object> map) {
        return new Gson().toJson(map);
    }

    public static String convertToJson(Object o) {
        return gson.toJson(o);
    }

    public static LocalDate getDateAsLocalDate(String dateAsString) {
        return LocalDate.parse(dateAsString, dateFormatter);
    }

    public static long getDifferenceInDays(final LocalDate localDateBefore, final LocalDate localDateAfter) {
        return DAYS.between(localDateBefore, localDateAfter);
    }

    public static long getDifferenceInWeeks(final LocalDate localDateBefore, final LocalDate localDateAfter) {
        return WEEKS.between(localDateBefore, localDateAfter);
    }

    public static long getDifferenceInMonths(final LocalDate localDateBefore, final LocalDate localDateAfter) {
        return MONTHS.between(localDateBefore, localDateAfter);
    }

    public static Double getDoubleValue(BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static String randomFirstNameGenerator() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }

    public static String randomLastNameGenerator() {
        return lastNames.get(random.nextInt(lastNames.size()));
    }
}
