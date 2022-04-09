package org.opendelos.legacydomain.dlmusers;

 

import java.util.List;

import org.opendelos.legacydomain.dlmuser.DlmUser;


public class DlmJsonObject {

    int iTotalRecords;

    int iTotalDisplayRecords;

    String sEcho;

    String sColumns;

    List<DlmUser> aaData;

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

    public List<DlmUser> getAaData() {
        return aaData;
    }

    public void setAaData(List<DlmUser> aaData) {
        this.aaData = aaData;
    }

    
}