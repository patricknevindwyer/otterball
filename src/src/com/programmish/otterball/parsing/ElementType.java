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
	
	public boolean isBlockStart() {
		if ( ( this == OBJ_START) || (this == LIST_START) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isBlockEnd() {
		if ( (this == OBJ_END) || (this == LIST_END) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean closes(ElementType e) {
		
		if ( (this == OBJ_END) && (e == OBJ_START) ) {
			return true;
		}
		else if ( (this == LIST_END) && (e == LIST_START) ) {
			return true;
		}
		
		return false;
	}
}
