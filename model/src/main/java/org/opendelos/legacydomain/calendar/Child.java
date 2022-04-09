package org.opendelos.legacydomain.calendar;

/* 
Author: Michael Gatzonis - 1/11/2019 
OpenDelosDAC
*/

import java.io.Serializable;

public class Child implements Serializable {


private String text;  
private String id;
private String descr;


public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getText() {
	return text;
}
public void setText(String text) {
	this.text = text;
}
public String getDescr() {
	return descr;
}
public void setDescr(String descr) {
	this.descr = descr;
}

}
