package com.specmate.modelgeneration.internal.cira;

class Label {
	int begin;
	int end;
	String label;
	String id;

	public Label(int begin, int end, String label, String id) {
		super();
		this.begin = begin;
		this.end = end;
		this.label = label;
		this.id = id;
	}

}