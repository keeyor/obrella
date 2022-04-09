package org.opendelos.legacydomain.dlmusers;

import java.util.List;

import org.opendelos.legacydomain.institutions.Institutions;


public class InstitutionListJsonObject {

	 int iTotalRecords;

	    int iTotalDisplayRecords;

	    String sEcho;

	    String sColumns;

	    List<Institutions>  aaData;

	    public int getiTotalRecords() {
	    return iTotalRecords;
	    }

	    public void setiTotalRecords(int iTotalRecords) {
	    this.iTotalRecords = iTotalRecords;
	    }

	    public int getiTotalDisplayRecords() {
	    return iTotalDisplayRecords;
	    }

	    public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
	    this.iTotalDisplayRecords = iTotalDisplayRecords;
	    }

	    public String getsEcho() {
	    return sEcho;
	    }

	    public void setsEcho(String sEcho) {
	    this.sEcho = sEcho;
	    }

	    public String getsColumns() {
	    return sColumns;
	    }

	    public void setsColumns(String sColumns) {
	    this.sColumns = sColumns;
	    }

	    public  List<Institutions>  getAaData() {
	        return aaData;
	    }

	    public void setAaData( List<Institutions>  aaData) {
	        this.aaData = aaData;
	    }

	    
}
