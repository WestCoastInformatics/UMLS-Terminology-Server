package com.wci.umls.server.mojo.model;

public abstract class AbstractSctComponent {
	
	abstract String printForExcel();

	protected String processForExcel(String str) {
		if (str != null) {
			return str + "\t";
		}
		return "\t";
	}

	protected String processForExcel(boolean val) {
		if (val) {
			return "X\t";
		}
		return "\t";
	}

	protected String processForExcel(int roleGroup) {
		return roleGroup + "\t";
	}
}
