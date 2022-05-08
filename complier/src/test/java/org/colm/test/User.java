package org.colm.test;

/**
 * Sample class as JavaBean.
 * 
 * @author michael
 * @see <a href="https://github.com/barrywang88/compiler">barrywang88/compiler</a>
 */
public class User {

	private String id;
	private String name;
	private long created;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

}
