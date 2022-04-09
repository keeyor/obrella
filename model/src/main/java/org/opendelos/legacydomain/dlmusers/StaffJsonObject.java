package org.opendelos.legacydomain.dlmusers;

import java.util.List;

import org.opendelos.legacydomain.xstaffmembers.XStaffMembers;


public class StaffJsonObject {
	  int iTotalRecords;

	    int iTotalDisplayRecords;

	    String sEcho;

	    String sColumns;

	    List<XStaffMembers> aaData;

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

	    public List<XStaffMembers> getAaData() {
	        return aaData;
	    }

	    public void setAaData(List<XStaffMembers> aaData) {
	        this.aaData = aaData;
	    }

	    
}
