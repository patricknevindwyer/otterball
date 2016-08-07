package com.programmish.otterball.parsing;

public enum ElementType {
	OBJ_START		("{"),
	OBJ_END			("}"),
	OBJ_DELIMITER	(","),
	OBJ_SEPARATOR	(":"),
	
	LIST_START		("["),
	LIST_END		("]"),
	LIST_DELIMITER	(","),
	
	STRING			("<string>"),
	BOOLEAN			("<boolean>"),
	NULL			("<null>"),
	NUMBER			("<number>");
	
	private final String name;
	
	private ElementType(String s) {
		name = s;
	}
	
	public String toString() {
		return name;
	}
}
