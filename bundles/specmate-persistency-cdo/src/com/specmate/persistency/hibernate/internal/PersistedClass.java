package com.specmate.persistency.hibernate.internal;

public class PersistedClass {
	private int id;
	private String string;
	private boolean flag;

	public PersistedClass(int id, String string, boolean flag) {
		super();
		this.id = id;
		this.string = string;
		this.flag = flag;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}
