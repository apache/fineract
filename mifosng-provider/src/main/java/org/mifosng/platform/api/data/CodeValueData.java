package org.mifosng.platform.api.data;

public class CodeValueData {

	private Long id;
	private String name;
	private Integer position;

	public CodeValueData(final Long id, final String name,
			final Integer position) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}