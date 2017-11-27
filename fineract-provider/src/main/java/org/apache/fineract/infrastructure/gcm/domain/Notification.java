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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * GCM message notification part.
 *
 * <p>
 * Instances of this class are immutable and should be created using a
 * {@link Builder}. Examples:
 *
 * <strong>Simplest notification:</strong>
 * 
 * <pre>
 * <code>
 * Notification notification = new Notification.Builder("myicon").build();
 * </pre>
 * 
 * </code>
 *
 * <strong>Notification with optional attributes:</strong>
 * 
 * <pre>
 * <code>
 * Notification notification = new Notification.Builder("myicon")
 *    .title("Hello world!")
 *    .body("Here is a more detailed description")
 *    .build();
 * </pre>
 * 
 * </code>
 */
public final class Notification implements Serializable {

	private final String title;
	private final String body;
	private final String icon;
	private final String sound;
	private final Integer badge;
	private final String tag;
	private final String color;
	private final String clickAction;
	private final String bodyLocKey;
	private final List<String> bodyLocArgs;
	private final String titleLocKey;
	private final List<String> titleLocArgs;

	public static final class Builder {

		// required parameters
		private final String icon;

		// optional parameters
		private String title;
		private String body;
		private String sound;
		private Integer badge;
		private String tag;
		private String color;
		private String clickAction;
		private String bodyLocKey;
		private List<String> bodyLocArgs;
		private String titleLocKey;
		private List<String> titleLocArgs;

		public Builder(String icon) {
			this.icon = icon;
			this.sound = "default"; // the only currently supported value
		}

		/**
		 * Sets the title property.
		 */
		public Builder title(String value) {
			title = value;
			return this;
		}

		/**
		 * Sets the body property.
		 */
		public Builder body(String value) {
			body = value;
			return this;
		}

		/**
		 * Sets the sound property (default value is {@literal default}).
		 */
		public Builder sound(String value) {
			sound = value;
			return this;
		}

		/**
		 * Sets the badge property.
		 */
		public Builder badge(int value) {
			badge = value;
			return this;
		}

		/**
		 * Sets the tag property.
		 */
		public Builder tag(String value) {
			tag = value;
			return this;
		}

		/**
		 * Sets the color property in {@literal #rrggbb} format.
		 */
		public Builder color(String value) {
			color = value;
			return this;
		}

		/**
		 * Sets the click action property.
		 */
		public Builder clickAction(String value) {
			clickAction = value;
			return this;
		}

		/**
		 * Sets the body localization key property.
		 */
		public Builder bodyLocKey(String value) {
			bodyLocKey = value;
			return this;
		}

		/**
		 * Sets the body localization values property.
		 */
		public Builder bodyLocArgs(List<String> value) {
			bodyLocArgs = Collections.unmodifiableList(value);
			return this;
		}

		/**
		 * Sets the title localization key property.
		 */
		public Builder titleLocKey(String value) {
			titleLocKey = value;
			return this;
		}

		/**
		 * Sets the title localization values property.
		 */
		public Builder titleLocArgs(List<String> value) {
			titleLocArgs = Collections.unmodifiableList(value);
			return this;
		}

		public Notification build() {
			return new Notification(this);
		}

	}

	private Notification(Builder builder) {
		title = builder.title;
		body = builder.body;
		icon = builder.icon;
		sound = builder.sound;
		badge = builder.badge;
		tag = builder.tag;
		color = builder.color;
		clickAction = builder.clickAction;
		bodyLocKey = builder.bodyLocKey;
		bodyLocArgs = builder.bodyLocArgs;
		titleLocKey = builder.titleLocKey;
		titleLocArgs = builder.titleLocArgs;
	}

	/**
	 * Gets the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the body.
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Gets the icon.
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Gets the sound.
	 */
	public String getSound() {
		return sound;
	}

	/**
	 * Gets the badge.
	 */
	public Integer getBadge() {
		return badge;
	}

	/**
	 * Gets the tag.
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Gets the color.
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Gets the click action.
	 */
	public String getClickAction() {
		return clickAction;
	}

	/**
	 * Gets the body localization key.
	 */
	public String getBodyLocKey() {
		return bodyLocKey;
	}

	/**
	 * Gets the body localization values list, which is immutable.
	 */
	public List<String> getBodyLocArgs() {
		return bodyLocArgs;
	}

	/**
	 * Gets the title localization key.
	 */
	public String getTitleLocKey() {
		return titleLocKey;
	}

	/**
	 * Gets the title localization values list, which is immutable.
	 */
	public List<String> getTitleLocArgs() {
		return titleLocArgs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Notification(");
		if (title != null) {
			builder.append("title=").append(title).append(", ");
		}
		if (body != null) {
			builder.append("body=").append(body).append(", ");
		}
		if (icon != null) {
			builder.append("icon=").append(icon).append(", ");
		}
		if (sound != null) {
			builder.append("sound=").append(sound).append(", ");
		}
		if (badge != null) {
			builder.append("badge=").append(badge).append(", ");
		}
		if (tag != null) {
			builder.append("tag=").append(tag).append(", ");
		}
		if (color != null) {
			builder.append("color=").append(color).append(", ");
		}
		if (clickAction != null) {
			builder.append("clickAction=").append(clickAction).append(", ");
		}
		if (bodyLocKey != null) {
			builder.append("bodyLocKey=").append(bodyLocKey).append(", ");
		}
		if (bodyLocArgs != null) {
			builder.append("bodyLocArgs=").append(bodyLocArgs).append(", ");
		}
		if (titleLocKey != null) {
			builder.append("titleLocKey=").append(titleLocKey).append(", ");
		}
		if (titleLocArgs != null) {
			builder.append("titleLocArgs=").append(titleLocArgs).append(", ");
		}
		if (builder.charAt(builder.length() - 1) == ' ') {
			builder.delete(builder.length() - 2, builder.length());
		}
		builder.append(")");
		return builder.toString();
	}

}
