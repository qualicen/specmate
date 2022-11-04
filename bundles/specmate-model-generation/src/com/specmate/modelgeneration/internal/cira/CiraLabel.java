package com.specmate.modelgeneration.internal.cira;

class CiraLabel {
	int begin;
	int end;
	String label;
	String id;

	public CiraLabel(int begin, int end, String label, String id) {
		super();
		this.begin = begin;
		this.end = end;
		this.label = label;
		this.id = id;
	}

}