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
package org.apache.fineract.infrastructure.gcm.domain;

import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_CANONICAL_IDS;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_ERROR;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_FAILURE;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_MESSAGE_ID;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_MULTICAST_ID;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_BADGE;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_BODY;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_BODY_LOC_ARGS;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_BODY_LOC_KEY;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_CLICK_ACTION;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_COLOR;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_ICON;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_SOUND;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_TAG;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_TITLE;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_TITLE_LOC_ARGS;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_NOTIFICATION_TITLE_LOC_KEY;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_PAYLOAD;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_REGISTRATION_IDS;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_TO;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_RESULTS;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.JSON_SUCCESS;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.PARAM_COLLAPSE_KEY;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.PARAM_DELAY_WHILE_IDLE;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.PARAM_DRY_RUN;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.PARAM_PRIORITY;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.PARAM_CONTENT_AVAILABLE;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.PARAM_RESTRICTED_PACKAGE_NAME;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.PARAM_TIME_TO_LIVE;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.TOKEN_CANONICAL_REG_ID;
import static org.apache.fineract.infrastructure.gcm.GcmConstants.TOPIC_PREFIX;

import org.apache.fineract.infrastructure.gcm.GcmConstants;
import org.apache.fineract.infrastructure.gcm.exception.InvalidRequestException;
/*import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;*/






import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to send messages to the GCM service using an API Key.
 */
public class Sender {

	protected static final String UTF8 = "UTF-8";

	/**
	 * Initial delay before first retry, without jitter.
	 */
	protected static final int BACKOFF_INITIAL_DELAY = 1000;
	/**
	 * Maximum delay before a retry.
	 */
	protected static final int MAX_BACKOFF_DELAY = 1024000;

	protected final Random random = new Random();
	protected static final Logger logger = Logger.getLogger(Sender.class
			.getName());

	private final String key;

	private String endpoint;

	private int connectTimeout;
	private int readTimeout;
	
	/**
	 * Full options constructor.
	 *
	 * @param key
	 *            FCM Server Key obtained through the Firebase Web Console.
	 * @param endpoint
	 *            Endpoint to use when sending the message.
	 */
	public Sender(String key, String endpoint) {
		this.key = nonNull(key);
		this.endpoint = nonNull(endpoint);
	}

	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * Set the underlying URLConnection's connect timeout (in milliseconds). A
	 * timeout value of 0 specifies an infinite timeout.
	 * <p>
	 * Default is the system's default timeout.
	 *
	 * @see java.net.URLConnection#setConnectTimeout(int)
	 */
	public final void setConnectTimeout(int connectTimeout) {
		if (connectTimeout < 0) {
			throw new IllegalArgumentException("timeout can not be negative");
		}
		this.connectTimeout = connectTimeout;
	}

	/**
	 * Set the underlying URLConnection's read timeout (in milliseconds). A
	 * timeout value of 0 specifies an infinite timeout.
	 * <p>
	 * Default is the system's default timeout.
	 *
	 * @see java.net.URLConnection#setReadTimeout(int)
	 */
	public final void setReadTimeout(int readTimeout) {
		if (readTimeout < 0) {
			throw new IllegalArgumentException("timeout can not be negative");
		}
		this.readTimeout = readTimeout;
	}

	/**
	 * Sends a message to one device, retrying in case of unavailability.
	 *
	 * <p>
	 * <strong>Note: </strong> this method uses exponential back-off to retry in
	 * case of service unavailability and hence could block the calling thread
	 * for many seconds.
	 *
	 * @param message
	 *            message to be sent, including the device's registration id.
	 * @param to
	 *            registration token, notification key, or topic where the
	 *            message will be sent.
	 * @param retries
	 *            number of retries in case of service unavailability errors.
	 *
	 * @return result of the request (see its javadoc for more details).
	 *
	 * @throws IllegalArgumentException
	 *             if to is {@literal null}.
	 * @throws InvalidRequestException
	 *             if GCM didn't returned a 200 or 5xx status.
	 * @throws IOException
	 *             if message could not be sent.
	 */
	public Result send(Message message, String to, int retries)
			throws IOException {
		int attempt = 0;
		Result result;
		int backoff = BACKOFF_INITIAL_DELAY;
		boolean tryAgain;
		do {
			attempt++;
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Attempt #" + attempt + " to send message "
						+ message + " to regIds " + to);
			}
			result = sendNoRetry(message, to);
			tryAgain = result == null && attempt <= retries;
			if (tryAgain) {
				int sleepTime = backoff / 2 + random.nextInt(backoff);
				sleep(sleepTime);
				if (2 * backoff < MAX_BACKOFF_DELAY) {
					backoff *= 2;
				}
			}
		} while (tryAgain);
		if (result == null) {
			throw new IOException("Could not send message after " + attempt
					+ " attempts");
		}
		return result;
	}

	/**
	 * Sends a message without retrying in case of service unavailability. See
	 * {@link #send(Message, String, int)} for more info.
	 *
	 * @return result of the post, or {@literal null} if the GCM service was
	 *         unavailable or any network exception caused the request to fail,
	 *         or if the response contains more than one result.
	 *
	 * @throws InvalidRequestException
	 *             if GCM didn't returned a 200 status.
	 * @throws IllegalArgumentException
	 *             if to is {@literal null}.
	 */
	public Result sendNoRetry(Message message, String to) throws IOException {
		nonNull(to);
		Map<Object, Object> jsonRequest = new HashMap<>();
		messageToMap(message, jsonRequest);
		jsonRequest.put(JSON_TO, to);
		Map<String , Object> responseMap = makeGcmHttpRequest(jsonRequest);
		String responseBody = null;
		if (responseMap.get("responseBody") != null) {
			responseBody = (String) responseMap.get("responseBody");
		}
		int status = (int) responseMap.get("status");
		//responseBody
		if (responseBody == null) {
			return null;
		}
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonResponse;
		try {
			jsonResponse = (JsonObject) jsonParser.parse(responseBody);
			Result.Builder resultBuilder = new Result.Builder();
			if (jsonResponse.has("results")) {
				// Handle response from message sent to specific device.
				JsonArray jsonResults = (JsonArray) jsonResponse.get("results");
				if (jsonResults.size() == 1) {
					JsonObject jsonResult = (JsonObject) jsonResults.get(0);
					String messageId = null;
					String canonicalRegId = null;
					String error = null;
					if(jsonResult.has(JSON_MESSAGE_ID)){
						messageId = jsonResult.get(JSON_MESSAGE_ID).getAsString();
					}
					if(jsonResult.has(TOKEN_CANONICAL_REG_ID)){
						canonicalRegId = jsonResult
								.get(TOKEN_CANONICAL_REG_ID).getAsString();
					}
					if(jsonResult.has(JSON_ERROR)){
						error = (String) jsonResult.get(JSON_ERROR).getAsString();
					}
					int success = 0;
					int failure = 0;
					if(jsonResponse.get("success") != null){
						success = Integer.parseInt(jsonResponse.get("success").toString());
					}
					if(jsonResponse.get("failure") != null){
						failure = Integer.parseInt(jsonResponse.get("failure").toString());
					}
					resultBuilder.messageId(messageId)
							.canonicalRegistrationId(canonicalRegId)
							.success(success)
							.failure(failure)
							.status(status)
							.errorCode(error);
				} else {
					logger.log(Level.WARNING,
							"Found null or " + jsonResults.size()
									+ " results, expected one");
					return null;
				}
			} else if (to.startsWith(TOPIC_PREFIX)) {
				if (jsonResponse.has(JSON_MESSAGE_ID)) {
					// message_id is expected when this is the response from a
					// topic message.
					Long messageId = jsonResponse.get(JSON_MESSAGE_ID).getAsLong();
					resultBuilder.messageId(messageId.toString());
				} else if (jsonResponse.has(JSON_ERROR)) {
					String error = jsonResponse.get(JSON_ERROR).getAsString();
					resultBuilder.errorCode(error);
				} else {
					logger.log(Level.WARNING, "Expected " + JSON_MESSAGE_ID
							+ " or " + JSON_ERROR + " found: " + responseBody);
					return null;
				}
			} else if (jsonResponse.has(JSON_SUCCESS)
					&& jsonResponse.has(JSON_FAILURE)) {
				// success and failure are expected when response is from group
				// message.
				int success = getNumber(responseMap, JSON_SUCCESS).intValue();
				int failure = getNumber(responseMap, JSON_FAILURE).intValue();
				List<String> failedIds = null;
				if (jsonResponse.has("failed_registration_ids")) {
					JsonArray jFailedIds = (JsonArray) jsonResponse
							.get("failed_registration_ids").getAsJsonArray();
					failedIds = new ArrayList<>();
					for (int i = 0; i < jFailedIds.size(); i++) {
						failedIds.add(jFailedIds.get(i).getAsString());
					}
				}
				resultBuilder.success(success).failure(failure)
						.failedRegistrationIds(failedIds);
			} else {
				logger.warning("Unrecognized response: " + responseBody);
				throw newIoException(responseBody, new Exception(
						"Unrecognized response."));
			}
			return resultBuilder.build();
		} catch (CustomParserException e) {
			throw newIoException(responseBody, e);
		}
	}

	/**
	 * Sends a message to many devices, retrying in case of unavailability.
	 *
	 * <p>
	 * <strong>Note: </strong> this method uses exponential back-off to retry in
	 * case of service unavailability and hence could block the calling thread
	 * for many seconds.
	 *
	 * @param message
	 *            message to be sent.
	 * @param regIds
	 *            registration id of the devices that will receive the message.
	 * @param retries
	 *            number of retries in case of service unavailability errors.
	 *
	 * @return combined result of all requests made.
	 *
	 * @throws IllegalArgumentException
	 *             if registrationIds is {@literal null} or empty.
	 * @throws InvalidRequestException
	 *             if GCM didn't returned a 200 or 503 status.
	 * @throws IOException
	 *             if message could not be sent.
	 */
	public MulticastResult send(Message message, List<String> regIds,
			int retries) throws IOException {
		int attempt = 0;
		MulticastResult multicastResult;
		int backoff = BACKOFF_INITIAL_DELAY;
		// Map of results by registration id, it will be updated after each
		// attempt
		// to send the messages
		Map<String, Result> results = new HashMap<>();
		List<String> unsentRegIds = new ArrayList<>(regIds);
		boolean tryAgain;
		List<Long> multicastIds = new ArrayList<>();
		do {
			multicastResult = null;
			attempt++;
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Attempt #" + attempt + " to send message "
						+ message + " to regIds " + unsentRegIds);
			}
			try {
				multicastResult = sendNoRetry(message, unsentRegIds);
			} catch (IOException e) {
				// no need for WARNING since exception might be already logged
				logger.log(Level.FINEST, "IOException on attempt " + attempt, e);
			}
			if (multicastResult != null) {
				long multicastId = multicastResult.getMulticastId();
				logger.fine("multicast_id on attempt # " + attempt + ": "
						+ multicastId);
				multicastIds.add(multicastId);
				unsentRegIds = updateStatus(unsentRegIds, results,
						multicastResult);
				tryAgain = !unsentRegIds.isEmpty() && attempt <= retries;
			} else {
				tryAgain = attempt <= retries;
			}
			if (tryAgain) {
				int sleepTime = backoff / 2 + random.nextInt(backoff);
				sleep(sleepTime);
				if (2 * backoff < MAX_BACKOFF_DELAY) {
					backoff *= 2;
				}
			}
		} while (tryAgain);
		if (multicastIds.isEmpty()) {
			// all JSON posts failed due to GCM unavailability
			throw new IOException("Could not post JSON requests to GCM after "
					+ attempt + " attempts");
		}
		// calculate summary
		int success = 0, failure = 0, canonicalIds = 0;
		for (Result result : results.values()) {
			if (result.getMessageId() != null) {
				success++;
				if (result.getCanonicalRegistrationId() != null) {
					canonicalIds++;
				}
			} else {
				failure++;
			}
		}
		// build a new object with the overall result
		long multicastId = multicastIds.remove(0);
		MulticastResult.Builder builder = new MulticastResult.Builder(success,
				failure, canonicalIds, multicastId)
				.retryMulticastIds(multicastIds);
		// add results, in the same order as the input
		for (String regId : regIds) {
			Result result = results.get(regId);
			builder.addResult(result);
		}
		return builder.build();
	}

	/**
	 * Updates the status of the messages sent to devices and the list of
	 * devices that should be retried.
	 *
	 * @param unsentRegIds
	 *            list of devices that are still pending an update.
	 * @param allResults
	 *            map of status that will be updated.
	 * @param multicastResult
	 *            result of the last multicast sent.
	 *
	 * @return updated version of devices that should be retried.
	 */
	private List<String> updateStatus(List<String> unsentRegIds,
			Map<String, Result> allResults, MulticastResult multicastResult) {
		List<Result> results = multicastResult.getResults();
		if (results.size() != unsentRegIds.size()) {
			// should never happen, unless there is a flaw in the algorithm
			throw new RuntimeException("Internal error: sizes do not match. "
					+ "currentResults: " + results + "; unsentRegIds: "
					+ unsentRegIds);
		}
		List<String> newUnsentRegIds = new ArrayList<>();
		for (int i = 0; i < unsentRegIds.size(); i++) {
			String regId = unsentRegIds.get(i);
			Result result = results.get(i);
			allResults.put(regId, result);
			String error = result.getErrorCodeName();
			if (error != null
					&& (error.equals(GcmConstants.ERROR_UNAVAILABLE) || error
							.equals(GcmConstants.ERROR_INTERNAL_SERVER_ERROR))) {
				newUnsentRegIds.add(regId);
			}
		}
		return newUnsentRegIds;
	}

	/**
	 * Sends a message without retrying in case of service unavailability. See
	 * {@link #send(Message, List, int)} for more info.
	 *
	 * @return multicast results if the message was sent successfully,
	 *         {@literal null} if it failed but could be retried.
	 *
	 * @throws IllegalArgumentException
	 *             if registrationIds is {@literal null} or empty.
	 * @throws InvalidRequestException
	 *             if GCM didn't returned a 200 status.
	 * @throws IOException
	 *             if there was a JSON parsing error
	 */
	public MulticastResult sendNoRetry(Message message,
			List<String> registrationIds) throws IOException {
		if (nonNull(registrationIds).isEmpty()) {
			throw new IllegalArgumentException(
					"registrationIds cannot be empty");
		}
		Map<Object, Object> jsonRequest = new HashMap<>();
		messageToMap(message, jsonRequest);
		jsonRequest.put(JSON_REGISTRATION_IDS, registrationIds);
		Map<String , Object> responseMap = makeGcmHttpRequest(jsonRequest);
		String responseBody = null;
		if (responseMap.get("responseBody") != null) {
			responseBody = (String) responseMap.get("responseBody");
		}
		if (responseBody == null) {
			return null;
		}
		
		JsonParser parser = new JsonParser();
		JsonObject jsonResponse;
		try {
			jsonResponse = (JsonObject) parser.parse(responseBody);
			int success = getNumber(responseMap, JSON_SUCCESS).intValue();
			int failure = getNumber(responseMap, JSON_FAILURE).intValue();
			int canonicalIds = getNumber(responseMap, JSON_CANONICAL_IDS)
					.intValue();
			long multicastId = getNumber(responseMap, JSON_MULTICAST_ID)
					.longValue();
			MulticastResult.Builder builder = new MulticastResult.Builder(
					success, failure, canonicalIds, multicastId);
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> results = (List<Map<String, Object>>) jsonResponse
					.get(JSON_RESULTS);
			if (results != null) {
				for (Map<String, Object> jsonResult : results) {
					String messageId = (String) jsonResult.get(JSON_MESSAGE_ID);
					String canonicalRegId = (String) jsonResult
							.get(TOKEN_CANONICAL_REG_ID);
					String error = (String) jsonResult.get(JSON_ERROR);
					Result result = new Result.Builder().messageId(messageId)
							.canonicalRegistrationId(canonicalRegId)
							.errorCode(error).build();
					builder.addResult(result);
				}
			}
			return builder.build();
		} catch (CustomParserException e) {
			throw newIoException(responseBody, e);
		}
	}

	private Map<String , Object> makeGcmHttpRequest(Map<Object, Object> jsonRequest)
			throws InvalidRequestException {
		String requestBody = new Gson().toJson(jsonRequest);
		logger.finest("JSON request: " + requestBody);
		HttpURLConnection conn;
		int status;
		try {
			conn = post(getEndpoint(), "application/json", requestBody);
			status = conn.getResponseCode();
		} catch (IOException e) {
			logger.log(Level.FINE, "IOException posting to GCM", e);
			return null;
		}
		String responseBody;
		if (status != 200) {
			try {
				responseBody = getAndClose(conn.getErrorStream());
				logger.finest("JSON error response: " + responseBody);
			} catch (IOException e) {
				// ignore the exception since it will thrown an
				// InvalidRequestException
				// anyways
				responseBody = "N/A";
				logger.log(Level.FINE, "Exception reading response: ", e);
			}
			throw new InvalidRequestException(status, responseBody);
		}
		try {
			responseBody = getAndClose(conn.getInputStream());
		} catch (IOException e) {
			logger.log(Level.WARNING, "IOException reading response", e);
			return null;
		}
		logger.finest("JSON response: " + responseBody);
		Map<String , Object> map = new HashMap<>();
		map.put("responseBody", responseBody);
		map.put("status", status);
		
		return map;
	}

	/**
	 * Populate Map with message.
	 *
	 * @param message
	 *            Message used to populate Map.
	 * @param mapRequest
	 *            Map populated by Message.
	 */
	private void messageToMap(Message message, Map<Object, Object> mapRequest) {
		if (message == null || mapRequest == null) {
			return;
		}
		setJsonField(mapRequest, PARAM_PRIORITY, message.getPriority());
		setJsonField(mapRequest, PARAM_CONTENT_AVAILABLE,
				message.getContentAvailable());
		setJsonField(mapRequest, PARAM_TIME_TO_LIVE, message.getTimeToLive());
		setJsonField(mapRequest, PARAM_COLLAPSE_KEY, message.getCollapseKey());
		setJsonField(mapRequest, PARAM_RESTRICTED_PACKAGE_NAME,
				message.getRestrictedPackageName());
		setJsonField(mapRequest, PARAM_DELAY_WHILE_IDLE,
				message.isDelayWhileIdle());
		setJsonField(mapRequest, PARAM_DRY_RUN, message.isDryRun());
		Map<String, String> payload = message.getData();
		if (!payload.isEmpty()) {
			mapRequest.put(JSON_PAYLOAD, payload);
		}
		if (message.getNotification() != null) {
			Notification notification = message.getNotification();
			Map<Object, Object> nMap = new HashMap<>();
			if (notification.getBadge() != null) {
				setJsonField(nMap, JSON_NOTIFICATION_BADGE, notification
						.getBadge().toString());
			}
			setJsonField(nMap, JSON_NOTIFICATION_BODY, notification.getBody());
			setJsonField(nMap, JSON_NOTIFICATION_BODY_LOC_ARGS,
					notification.getBodyLocArgs());
			setJsonField(nMap, JSON_NOTIFICATION_BODY_LOC_KEY,
					notification.getBodyLocKey());
			setJsonField(nMap, JSON_NOTIFICATION_CLICK_ACTION,
					notification.getClickAction());
			setJsonField(nMap, JSON_NOTIFICATION_COLOR, notification.getColor());
			setJsonField(nMap, JSON_NOTIFICATION_ICON, notification.getIcon());
			setJsonField(nMap, JSON_NOTIFICATION_SOUND, notification.getSound());
			setJsonField(nMap, JSON_NOTIFICATION_TAG, notification.getTag());
			setJsonField(nMap, JSON_NOTIFICATION_TITLE, notification.getTitle());
			setJsonField(nMap, JSON_NOTIFICATION_TITLE_LOC_ARGS,
					notification.getTitleLocArgs());
			setJsonField(nMap, JSON_NOTIFICATION_TITLE_LOC_KEY,
					notification.getTitleLocKey());
			mapRequest.put(JSON_NOTIFICATION, nMap);
		}
	}

	private IOException newIoException(String responseBody, Exception e) {
		// log exception, as IOException constructor that takes a message and
		// cause
		// is only available on Java 6
		String msg = "Error parsing JSON response (" + responseBody + ")";
		logger.log(Level.WARNING, msg, e);
		return new IOException(msg + ":" + e);
	}

	private static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignore error
				logger.log(Level.FINEST, "IOException closing stream", e);
			}
		}
	}

	/**
	 * Sets a JSON field, but only if the value is not {@literal null}.
	 */
	private void setJsonField(Map<Object, Object> json, String field,
			Object value) {
		if (value != null) {
			json.put(field, value);
		}
	}

	private Number getNumber(Map<?, ?> json, String field) {
		Object value = json.get(field);
		if (value == null) {
			throw new CustomParserException("Missing field: " + field);
		}
		if (!(value instanceof Number)) {
			throw new CustomParserException("Field " + field
					+ " does not contain a number: " + value);
		}
		return (Number) value;
	}

	class CustomParserException extends RuntimeException {
		CustomParserException(String message) {
			super(message);
		}
	}

	/**
	 * Make an HTTP post to a given URL.
	 *
	 * @return HTTP response.
	 */
	protected HttpURLConnection post(String url, String body)
			throws IOException {
		return post(url, "application/x-www-form-urlencoded;charset=UTF-8",
				body);
	}

	/**
	 * Makes an HTTP POST request to a given endpoint.
	 *
	 * <p>
	 * <strong>Note: </strong> the returned connected should not be
	 * disconnected, otherwise it would kill persistent connections made using
	 * Keep-Alive.
	 *
	 * @param url
	 *            endpoint to post the request.
	 * @param contentType
	 *            type of request.
	 * @param body
	 *            body of the request.
	 *
	 * @return the underlying connection.
	 *
	 * @throws IOException
	 *             propagated from underlying methods.
	 */
	protected HttpURLConnection post(String url, String contentType, String body)
			throws IOException {
		if (url == null || contentType == null || body == null) {
			throw new IllegalArgumentException("arguments cannot be null");
		}
		if (!url.startsWith("https://")) {
			logger.warning("URL does not use https: " + url);
		}
		logger.fine("Sending POST to " + url);
		logger.finest("POST body: " + body);
		byte[] bytes = body.getBytes(UTF8);
		HttpURLConnection conn = getConnection(url);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setFixedLengthStreamingMode(bytes.length);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", contentType);
		conn.setRequestProperty("Authorization", "key=" + key);
		OutputStream out = conn.getOutputStream();
		try {
			out.write(bytes);
		} finally {
			close(out);
		}
		return conn;
	}

	/**
	 * Creates a map with just one key-value pair.
	 */
	protected static final Map<String, String> newKeyValues(String key,
			String value) {
		Map<String, String> keyValues = new HashMap<>(1);
		keyValues.put(nonNull(key), nonNull(value));
		return keyValues;
	}

	/**
	 * Creates a {@link StringBuilder} to be used as the body of an HTTP POST.
	 *
	 * @param name
	 *            initial parameter for the POST.
	 * @param value
	 *            initial value for that parameter.
	 * @return StringBuilder to be used an HTTP POST body.
	 */
	protected static StringBuilder newBody(String name, String value) {
		return new StringBuilder(nonNull(name)).append('=').append(
				nonNull(value));
	}

	/**
	 * Adds a new parameter to the HTTP POST body.
	 *
	 * @param body
	 *            HTTP POST body.
	 * @param name
	 *            parameter's name.
	 * @param value
	 *            parameter's value.
	 */
	protected static void addParameter(StringBuilder body, String name,
			String value) {
		nonNull(body).append('&').append(nonNull(name)).append('=')
				.append(nonNull(value));
	}

	/**
	 * Gets an {@link HttpURLConnection} given an URL.
	 */
	protected HttpURLConnection getConnection(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		return conn;
	}

	/**
	 * Convenience method to convert an InputStream to a String.
	 * <p>
	 * If the stream ends in a newline character, it will be stripped.
	 * <p>
	 * If the stream is {@literal null}, returns an empty string.
	 */
	protected static String getString(InputStream stream) throws IOException {
		if (stream == null) {
			return "";
		}
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		StringBuilder content = new StringBuilder();
		String newLine;
		do {
			newLine = reader.readLine();
			if (newLine != null) {
				content.append(newLine).append('\n');
			}
		} while (newLine != null);
		if (content.length() > 0) {
			// strip last newline
			content.setLength(content.length() - 1);
		}
		return content.toString();
	}

	private static String getAndClose(InputStream stream) throws IOException {
		try {
			return getString(stream);
		} finally {
			if (stream != null) {
				close(stream);
			}
		}
	}

	static <T> T nonNull(T argument) {
		if (argument == null) {
			throw new IllegalArgumentException("argument cannot be null");
		}
		return argument;
	}

	void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
