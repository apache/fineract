package org.mifosng.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.LocalDate;

@XmlRootElement(name = "office")
@JsonFilter("myFilter")
public class OfficeData implements Serializable {

	private Long id;
	private String name;
	private String externalId;
	private LocalDate openingDate;
	private String hierarchy;
	private String parentName;
	private Long parentId;
	
	private List<OfficeData> allowedParents = new ArrayList<OfficeData>();

	public OfficeData() {
		//
	}

	public OfficeData(final Long id, final String name,
			final String externalId, final LocalDate openingDate,
			String hierarchy, final Long parentId, final String parentName) {
		this.id = id;
		this.name = name;
		this.externalId = externalId;
		this.openingDate = openingDate;
		this.hierarchy = hierarchy;
		this.parentName = parentName;
		this.parentId = parentId;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public LocalDate getOpeningDate() {
		return this.openingDate;
	}

	public Long getParentId() {
		return this.parentId;
	}

	public String getParentName() {
		return this.parentName;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setExternalId(final String externalId) {
		this.externalId = externalId;
	}

	public void setOpeningDate(final LocalDate openingDate) {
		this.openingDate = openingDate;
	}

	public void setParentName(final String parentName) {
		this.parentName = parentName;
	}

	public void setParentId(final Long parentId) {
		this.parentId = parentId;
	}

	public List<OfficeData> getAllowedParents() {
		return allowedParents;
	}

	public void setAllowedParents(List<OfficeData> allowedParents) {
		this.allowedParents = allowedParents;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}
}