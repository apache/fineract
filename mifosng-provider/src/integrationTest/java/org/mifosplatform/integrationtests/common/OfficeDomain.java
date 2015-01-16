/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import com.google.gson.Gson;

public class OfficeDomain {

	public static class Builder {

		private int id;
		private String name;
		private String nameDecorated;
		private String externalId;
		private String[] openingDate;
		private String hierarchy;

		private Builder(final int id, final String name,
				final String nameDecorated, final String externalId,
				final String[] openingDate, final String hierarchy) {
			this.id = id;
			this.name = name;
			this.nameDecorated = nameDecorated;
			this.externalId = externalId;
			this.openingDate = openingDate;
			this.hierarchy = hierarchy;
		}

		public OfficeDomain build() {
			return new OfficeDomain(this.id, this.name, this.nameDecorated,
					this.externalId, this.openingDate, this.hierarchy);
		}
	}

	private int id;
	private String name;
	private String nameDecorated;
	private String externalId;
	private String[] openingDate;
	private String hierarchy;

	OfficeDomain() {
		super();
	}

	private OfficeDomain(final int id, final String name,
			final String nameDecorated, final String externalId,
			final String[] openingDate, final String hierarchy) {
		super();
		this.id = id;
		this.name = name;
		this.nameDecorated = nameDecorated;
		this.externalId = externalId;
		this.openingDate = openingDate;
		this.hierarchy = hierarchy;
	}

	public String toJSON() {
		return new Gson().toJson(this);
	}

	public static OfficeDomain fromJSON(final String jsonData) {
		return new Gson().fromJson(jsonData, OfficeDomain.class);
	}

	public static Builder create(final int id, final String name,
			final String nameDecorated, final String externalId,
			final String[] openingDate, final String hierarchy) {
		return new Builder(id, name, nameDecorated, externalId, openingDate,
				hierarchy);
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getNameDecorated() {
		return this.nameDecorated;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public String[] getOpeningDate() {
		return this.openingDate;
	}

	public String getHierarchy() {
		return this.hierarchy;
	}

	@Override
	public int hashCode() {
		int hash = 1;

		if (this.id > 0)
			hash += this.id;
		if (this.name != null)
			hash += this.name.hashCode();
		if (this.nameDecorated != null)
			hash += this.nameDecorated.hashCode();
		if (this.externalId != null)
			hash += this.externalId.hashCode();
		if (this.openingDate != null)
			hash += this.openingDate.hashCode();
		if (this.hierarchy != null)
			hash += this.hierarchy.hashCode();

		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof OfficeDomain))
			return false;

		OfficeDomain od = (OfficeDomain) obj;

		if (this.id == od.getId() && this.name.equals(od.getName())
				&& this.nameDecorated.equals(od.getName())
				&& this.externalId.equals(od.getExternalId())
				&& this.openingDate.equals(od.getOpeningDate())
				&& this.hierarchy.equals(od.getHierarchy()))
			return true;

		return false;
	}
}