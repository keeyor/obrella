package org.opendelos.legacydomain.dlmusers;

import java.util.List;

import org.opendelos.legacydomain.stafflist.ColType;


public class StaffListJsonObject {

	 int iTotalRecords;

	    int iTotalDisplayRecords;

	    String sEcho;

	    String sColumns;

	    List<ColType>  aaData;

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

	    public  List<ColType>  getAaData() {
	        return aaData;
	    }

	    public void setAaData( List<ColType>  aaData) {
	        this.aaData = aaData;
	    }

	    
}
