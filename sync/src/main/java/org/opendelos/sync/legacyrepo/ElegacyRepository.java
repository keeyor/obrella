/* 
     Author: Michael Gatzonis - 11/16/2018 
     OpenDelosDAC
*/
package org.opendelos.sync.legacyrepo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.exist.xmldb.EXistResource;
import org.opendelos.legacydomain.dlmusers.DlmUsers;
import org.opendelos.legacydomain.institution.Course;
import org.opendelos.legacydomain.institution.Department;
import org.opendelos.legacydomain.institution.Institution;
import org.opendelos.legacydomain.institution.School;
import org.opendelos.legacydomain.institution.StaffMember;
import org.opendelos.legacydomain.institution.Study;
import org.opendelos.legacydomain.institution.XStudies;
import org.opendelos.legacydomain.institution.XStudy;
import org.opendelos.legacydomain.queryresponse.QueryResponse;
import org.opendelos.legacydomain.scheduler.DsmCalendarXml;
import org.opendelos.legacydomain.scheduler.DsmCalendarXmls;
import org.opendelos.legacydomain.xcourse.XCourse;
import org.opendelos.legacydomain.xdepartment.XDepartment;
import org.opendelos.legacydomain.xevents.XEvents;
import org.opendelos.legacydomain.xstaffmember.XStaffMember;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import org.springframework.stereotype.Repository;

@Repository
public class ElegacyRepository {

    private final Logger logger = Logger.getLogger(ElegacyRepository.class.getName());

    public Institution getLegacyInstitution(String url, String username, String password, String databaseCollection, String resourceId) {

        Institution institution;

        try {
            Collection col = getDatabaseCollection(url, username, password, databaseCollection);
            institution = (Institution) GetDataBaseObject(col, resourceId, Institution.class);
            return institution;
        } catch (Exception e) {
            logger.severe("Error Code 200GLI:" + e.getMessage());
            return null;
        }
    }

    public DlmUsers getDlmUsers(String url, String username, String password, String databaseCollection, String access_type, String userType) {

        Collection col = null;
        ResourceSet result;

        DlmUsers dlmusers = new DlmUsers();

        try {
            Database database = (Database) Class.forName(
                    "org.exist.xmldb.DatabaseImpl").newInstance();
            DatabaseManager.registerDatabase(database);


            col = getDatabaseCollection(url, username, password, databaseCollection);

            if (col == null) {
                return null;
            }
            XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
            service.setProperty("indent", "yes");

            StringBuilder xQuery = new StringBuilder();
            xQuery.append(" declare namespace dl=\"http://gunet.gr/DlmUser\"; ");
            xQuery.append(" declare namespace dls=\"http://gunet.gr/DlmUsers\"; ");
            String typeCriteria = "";
            if (userType != null) {
                if (userType.equals("LDAP_USER")) {
                    typeCriteria = "[dl:UserType = 'LDAP_USER']";
                }
            }
            switch (access_type) {
                case "CM":
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role = 'CM']").append(typeCriteria);
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "IM":
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role = 'IM']");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "SM":
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role = 'SM']");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "DM":
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role = 'DM'] ");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "ALL":
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "nonStaffMembers":
                    /* EACH ROLE ONE LINE */
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role != 'CM'] ");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "":
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:Status='ACTIVE'] ");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "editors":
                    /* EACH ROLE ONE LINE */
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role != 'CM'][dl:UserRights/dl:Role != 'SP'] ");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "SA":
                    xQuery.append("let $recs:= collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role = 'SA'] ");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
                case "SP":
                    xQuery.append("let $recs := collection('").append(databaseCollection).append("')//dl:DlmUser[dl:UserRights/dl:Role = 'SP'] ");
                    xQuery.append(" return <dls:DlmUsers> ");
                    xQuery.append("	{for $p in $recs return <dls:User><dls:UserID>{$p/dl:Sid/text()}</dls:UserID>{$p}</dls:User>} ");
                    xQuery.append(" </dls:DlmUsers> ");
                    break;
            }
            // Query Database 
            result = service.query(xQuery.toString());

            // Parse Results 
            ResourceIterator i = result.getIterator();
            Resource res;

            while (i.hasMoreResources()) {
                res = i.nextResource();
                if (res == null) {
                    logger.severe("getDlmUsers::oops!document not found (wierd: this shouldn't happen");
                } else {
                    JAXBContext jc = JAXBContext.newInstance(DlmUsers.class);
                    // Create unmarshaller
                    Unmarshaller um = jc.createUnmarshaller();
                    // Unmarshal XML contents of res.getContent.toString() to Java Object
                    String xmlStr = res.getContent().toString();
                    dlmusers = (DlmUsers) um.unmarshal(new StreamSource(new StringReader(xmlStr)));
                }
            }
            return dlmusers;

        } catch (Exception e) {
            logger.severe("Error Code 201GLI:" + e.getMessage());
            return null;
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (XMLDBException xe) {
                    xe.printStackTrace();
                }
            }
        }
    }

    public XStaffMember getStaffMemberbyId(Institution institution, String uid) {

        XStaffMember xstaffmember = new XStaffMember();
        if (institution.getSchools() != null)
            for (org.opendelos.legacydomain.institution.School item : institution.getSchools().getSchool()) {
                if (item.getDepartments() != null)
                    for (org.opendelos.legacydomain.institution.Department depitem : item.getDepartments().getDepartment()) {
                        if (depitem.getStaffMembers() != null)
                            for (StaffMember sitem : depitem.getStaffMembers().getStaffMember()) {
                                if (sitem.getSId().equals(uid)) {
                                    xstaffmember.setStaffMember(sitem);
                                    xstaffmember.setInstitutionName(institution.getName());
                                    xstaffmember.setInstitutionId(institution.getId());
                                    xstaffmember.setSchoolId(item.getId());
                                    xstaffmember.setSchoolName(item.getName());
                                    xstaffmember.setDepartmentName(depitem.getName());
                                    xstaffmember.setDepartmentId(depitem.getId());
                                }
                            }
                    }

            }
        return xstaffmember;
    }

    public String VerifyDatabaseAndCollection(String url, String username, String password, String databaseCollection) {

        Collection col;

        try {
            Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();
            DatabaseManager.registerDatabase(database);
            col = DatabaseManager.getCollection(url + databaseCollection, username, password);

            if (col == null) {
                return "Collection NOT found";
            }
        } catch (XMLDBException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            return e.getMessage();
        }

        return "success";
    }

    public Object GetDataBaseObject(Collection col, String resourceId, Class oClass) throws Exception {

        Object object;
        XMLResource eventResource = this.GetDatabaseResourceById(col, resourceId);
        assert eventResource != null;
        object = this.UnmarshallResourceToObject(eventResource.getContent().toString(), oClass);

        return object;
    }

    public XMLResource GetDatabaseResourceById(Collection col, String ResourceIdentity) {

        XMLResource resource;
        try {

            String resourceIdentity = ResourceIdentity;
            if (!ResourceIdentity.endsWith(".xml"))
                resourceIdentity = ResourceIdentity + ".xml";

            resource = (XMLResource) col.getResource(resourceIdentity);

            return resource;

        } catch (XMLDBException e) {
            logger.severe("Error Code 203GLI:" + e.getMessage());
            return null;
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (XMLDBException xe) {
                    xe.printStackTrace();
                }
            }
        }
    }

    public Object QueryDatabase(String url , String username, String password, String DatabaseCollection, String Query, Class oClass) throws Exception {

        String xmlResult = null;
        ResourceSet resourceSet;
        try {
            Collection col = this.getDatabaseCollection(url,username,password,DatabaseCollection);
            resourceSet = this.QueryDatabaseByQueryString(col, Query);
            Resource res = null;
            ResourceIterator i = resourceSet.getIterator();

            while (i.hasMoreResources()) {
                try {
                    res = i.nextResource();
                    if (res == null) {
                        System.out.println("oops!document not found (wierd: this shouldn't happen");
                    } else {
                        xmlResult = res.getContent().toString();
                    }
                } finally {
                    //dont forget to cleanup resources
                    try {
                        if (res != null)
                            ((EXistResource)res).freeResources(); } catch(XMLDBException ignored) {}
                }
            }

        } catch (Exception e) {
            logger.severe("QueryDatabase (Class) Error: Query:" + Query + " .Error:" + e.getMessage());
            return null;
        }

        return this.UnmarshallResourceToObject(xmlResult, oClass);
    }

    public Collection getDatabaseCollection(String url, String username, String password, String DatabaseCollection) {

        Collection col;

        if (!DatabaseCollection.endsWith("/")) {
            DatabaseCollection += "/";
        }

        try {

            Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();
            DatabaseManager.registerDatabase(database);
            col = DatabaseManager.getCollection(url + DatabaseCollection, username, password);

        } catch (Exception e) {
            logger.severe("Error Code 204GLI:" + e.getMessage());
            return null;
        }

        return col;
    }

    private Object UnmarshallResourceToObject(String xmlString, Class eClass) throws Exception {

        Object object;

        JAXBContext jc = JAXBContext.newInstance(eClass);
        Unmarshaller um = jc.createUnmarshaller();
        StringBuffer xmlStr = new StringBuffer(xmlString);
        object = um.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));

        return object;
    }

    public QueryResponse findAllLectures(String url, String username, String password, String databaseCollection, String institutionId, int startAt, int maxResults) {

        QueryResponse queryResponse = null;
        Collection col = null;
        try {
            col = getDatabaseCollection(url, username, password, databaseCollection);
            String query_string = getVideolecturesQueryString(databaseCollection, institutionId, startAt,maxResults);

            ResourceSet resourceSet = QueryDatabaseByQueryString(col, query_string);

            Resource res = null;
            ResourceIterator i = resourceSet.getIterator();

            while (i.hasMoreResources()) {
                res = i.nextResource();
                if (res == null) {
                    logger.severe("oops!document not found (wierd: this shouldn't happen");
                } else {
                    JAXBContext jc = JAXBContext.newInstance(QueryResponse.class);
                    // Create unmarshaller
                    Unmarshaller um = jc.createUnmarshaller();
                    // Unmarshal XML contents of res.getContent.toString() to Java Object
                    StringBuffer xmlStr = new StringBuffer(res.getContent().toString());
                    queryResponse = (QueryResponse) um.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));

                }
            }
            //Attach Presentation
           /* String presentationCollection = databaseCollection + "/Presentations";
            for (int j=0; j<queryResponse.getResources().size(); j++) {
                String resourceId = "p_" + queryResponse.getResources().get(j).getResourceID();
                Presentation presentation = getResourcePresentation(url, username, password, presentationCollection, resourceId);
                if (presentation != null) {
                    queryResponse.getResources().get(j).setPresentation(presentation);
                }
            }*/
            return queryResponse;

        } catch (Exception e) {
            logger.severe("Error Code 2052GLI:" + e.getMessage());
            return null;
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (XMLDBException xe) {
                    xe.printStackTrace();
                }
            }
        }
    }

    public XEvents findAllEvents(String url, String username, String password, String databaseCollection, String institutionId) {

        XEvents xEvents = null;
        Collection col = null;
        try {
            col = getDatabaseCollection(url, username, password, databaseCollection);
            String query_string = getEventsQueryString(databaseCollection, institutionId);

            ResourceSet resourceSet = QueryDatabaseByQueryString(col, query_string);

            Resource res;
            ResourceIterator i = resourceSet.getIterator();

            while (i.hasMoreResources()) {
                res = i.nextResource();
                if (res == null) {
                    logger.severe("oops!document not found (wierd: this shouldn't happen");
                } else {
                    JAXBContext jc = JAXBContext.newInstance(XEvents.class);
                    // Create unmarshaller
                    Unmarshaller um = jc.createUnmarshaller();
                    // Unmarshal XML contents of res.getContent.toString() to Java Object
                    StringBuffer xmlStr = new StringBuffer(res.getContent().toString());
                    xEvents = (XEvents) um.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));

                }
            }
            return xEvents;

        } catch (Exception e) {
            logger.severe("Error Code 205-1GLI:" + e.getMessage());
            return null;
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (XMLDBException xe) {
                    xe.printStackTrace();
                }
            }
        }
    }

    public List<DsmCalendarXml> findAllScheduled(String url, String username, String password, String databaseCollection, String institutionId, String year) {

        List<DsmCalendarXml> dsmEvents = new ArrayList<>();
        DsmCalendarXml dsmEvent = null;
        Collection col = null;

        try {
                col = getDatabaseCollection(url, username, password, databaseCollection);
                String xQuery = this.constructSearchCalendarEventQuery(institutionId, databaseCollection, year);

                ResourceSet resourceSet = QueryDatabaseByQueryString(col, xQuery);

                Resource res;
                ResourceIterator i = resourceSet.getIterator();

                while (i.hasMoreResources()) {
                    res = i.nextResource();
                    if (res == null) {
                        logger.severe("oops!document not found (wierd: this shouldn't happen");
                    }
                    else {
                        JAXBContext jc = JAXBContext.newInstance(DsmCalendarXml.class);
                        // Create unmarshaller
                        Unmarshaller um = jc.createUnmarshaller();
                        // Unmarshal XML contents of res.getContent.toString() to Java Object
                        StringBuffer xmlStr = new StringBuffer(res.getContent().toString());
                        dsmEvent = (DsmCalendarXml) um.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
                        dsmEvents.add(dsmEvent);
                    }
                }
                return dsmEvents;
            } catch (Exception e) {
                    logger.severe("Error Code 205-1GLI:" + e.getMessage());
                    return null;
            } finally {
                    if (col != null) {
                        try {
                            col.close();
                        } catch (XMLDBException xe) {
                            xe.printStackTrace();
                        }
                    }
        }
    }

    public String constructSearchCalendarEventQuery(String organizationId, String DatabaseCollection, String year) {


        String xQuery = " declare namespace dsm=\"http://gunet.gr/DsmCalendarXml\"; "
                + " declare namespace dsms=\"http://gunet.gr/DsmCalendarXmls\"; "
                + " declare namespace xmldb=\"http://exist-db.org/xquery/xmldb\"; "
                + " let $recs :=  for $x in xmldb:xcollection('" + DatabaseCollection + "') "
                + " //dsm:DsmCalendarXml"
                + " [dsm:OrganizationId='" + organizationId + "']";
        if (year !=null) {
            xQuery = xQuery +" [dsm:AcademicYear='" + year + "']";
        }

        xQuery += " return $x  ";

        xQuery += " return $recs " ;

        return xQuery;
    }

    private String getEventsQueryString(String dbCollection, String institutionId) {

        StringBuilder _MainQuery = new StringBuilder();
        StringBuilder _EvalQuery = new StringBuilder();


        _MainQuery.append(" declare namespace ev=\"http://gunet.gr/Event\";");
        _MainQuery.append(" declare namespace evs=\"http://gunet.gr/xEvents\";");
        _MainQuery.append(" declare namespace vl=\"http://gunet.gr/VideoLecture\";");

        _EvalQuery.append(" for $m1 in xmldb:xcollection('").append(dbCollection).append("')//ev:Event");
        _EvalQuery.append(" [ev:Organization/vl:Identity = ('").append(institutionId).append("')]");
        _EvalQuery.append(" return $m1 ");

        _MainQuery.append(" let $recs:=util:eval(\"").append(_EvalQuery.toString()).append("\")");
        _MainQuery.append(" return <evs:xEvents>");
        _MainQuery.append(" {for $p in $recs return ($p)}");
        _MainQuery.append(" </evs:xEvents>");

        //logger.severe("Events Query:" + _MainQuery.toString());

        return _MainQuery.toString();

    }

    public ResourceSet QueryDatabaseByQueryString(Collection col, String Query) throws Exception {

        ResourceSet result;
        try {
            XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");

            service.setProperty("indent", "yes");
            result = service.query(Query);

            return result;
        } catch (XMLDBException e) {
            throw new Exception(e.getMessage());
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (XMLDBException xe) {
                    xe.printStackTrace();
                }
            }
        }
    }

    private String getVideolecturesQueryString(String dbCollection, String institutionId, int startAt, int maxResults) {

        StringBuilder _MainQuery = new StringBuilder();
        StringBuilder _EvalQuery = new StringBuilder();


        _MainQuery.append(" declare namespace vl=\"http://gunet.gr/VideoLecture\";");
        _MainQuery.append(" declare namespace tns=\"http://gunet.gr/QueryResponse\";");
        _MainQuery.append(" declare namespace ft =\"http://exist-db.org/xquery/lucene\";");
        _MainQuery.append(" declare namespace xmldb=\"http://exist-db.org/xquery/xmldb\";");
        _EvalQuery.append(" for $m1 in xmldb:xcollection('").append(dbCollection).append("')//vl:VideoLecture");
        _EvalQuery.append(" [vl:Rights/vl:Security = ('private','public')]");                       //get all but 'deleted'
        _EvalQuery.append(" [vl:Organization/vl:Identity = ('").append(institutionId).append("')]");
        _EvalQuery.append(" return $m1 ");

        _MainQuery.append(" let $recs:=util:eval(\"").append(_EvalQuery).append("\"),");
        _MainQuery.append(" $count := count($recs), ");
        _MainQuery.append(" $start := ").append(startAt).append(", ");
        _MainQuery.append(" $max := ").append(maxResults);
        _MainQuery.append(" let $end :=  if ($start + $max - 1 < $count) then $max else ($count - $start + 1) ");
        _MainQuery.append(" let $matches := subsequence($recs,$start,$end)");
        _MainQuery.append(" return <tns:QueryResponse><tns:NumofResults>{$count}</tns:NumofResults>");
        _MainQuery.append(" {for $p in $matches ");
        //TODO: Check if this append works!
        _MainQuery.append(" let $presentation_doc := fn:concat('").append(dbCollection).append("/Presentations/p_',substring-before(util:document-name($p),'.'),'.xml') ");
        _MainQuery.append(" return (<tns:Resources><tns:ResourceID>");
        _MainQuery.append(" {substring-before(util:document-name($p),'.')}</tns:ResourceID>{$p}{fn:doc($presentation_doc)}</tns:Resources>)}");
        _MainQuery.append(" </tns:QueryResponse>");

        return _MainQuery.toString();

    }

    public XDepartment getInstitutionDepartment(Institution institution, String depId)  {
        XDepartment xdepartment = new XDepartment();

        if (institution.getSchools() != null) // !Important
            for (School item : institution.getSchools().getSchool()) {
                if (item.getDepartments() != null) // !Important
                    for (Department depitem : item.getDepartments().getDepartment()) {
                        if (depitem.getId().equals(depId)) {
                            xdepartment.setInstitutionId(institution.getId());
                            xdepartment.setInstitutionName(institution.getName());
                            xdepartment.setSchoolId(item.getId());
                            xdepartment.setSchoolName(item.getName());
                            xdepartment.setDepartment(depitem);
                            break;
                        }

                    }
            }
        return xdepartment;

    }

    public XCourse getDepartmentCourse(Institution institution, String departmentId, String courseId)   {

        String institutionId = institution.getId();
        XCourse xcourse = new XCourse();
        try {

            if (institution.getSchools() != null)
                for (School item : institution.getSchools().getSchool())
                    if (item.getDepartments() != null)
                        for (Department depitem : item.getDepartments().getDepartment()) {
                            if (depitem.getId().equals(departmentId)) {
                                if (depitem.getCourses() != null)
                                    for (Course citem : depitem.getCourses().getCourse()) {

                                        if (citem.getCId().equals(courseId)) {

                                            xcourse.setCourse(citem);
                                            xcourse.setInstitutionName(institution.getName());
                                            xcourse.setInstitutionId(institutionId);
                                            xcourse.setSchoolId(item.getId());
                                            xcourse.setSchoolName(item.getName());
                                            xcourse.setDepartmentName(depitem.getName());
                                            xcourse.setDepartmentId(depitem.getId());

                                            break;
                                        }
                                    }
                                break;
                            }
                        }

            return xcourse;

        } catch (Exception e) {
            logger.severe(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + " FAILED with Error:" + e.getMessage());
        }
        return null;
    }

    public XStudies getDepartmentStudies(Institution institution, String departmentId)  {

        XStudies xstudies = new XStudies();
        xstudies.setSize(0);

        try {
            int countStudies = 0;
            if (institution.getSchools() != null) {
                for (School item : institution.getSchools().getSchool()) {
                    if (item.getDepartments() != null) {
                        for (Department depitem : item.getDepartments().getDepartment()) {
                            if (depitem.getId().equals(departmentId)) {
                                if (depitem.getStudies() != null) {
                                    for (Study cstudy : depitem.getStudies().getStudy()) {
                                        XStudy xstudy = new XStudy();
                                        xstudy.setStudy(cstudy);
                                        xstudy.setInstitutionName(institution.getName());
                                        xstudy.setInstitutionId(institution.getId());
                                        xstudy.setSchoolId(item.getId());
                                        xstudy.setSchoolName(item.getName());
                                        xstudy.setDepartmentName(depitem.getName());
                                        xstudy.setDepartmentId(depitem.getId());

                                        xstudies.getXStudy().add(xstudy);
                                        countStudies = countStudies + 1;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            xstudies.setSize(countStudies);

            return xstudies;

        } catch (Exception e) {
            logger.severe(
                    Thread.currentThread().getStackTrace()[1].getMethodName() + " FAILED with Error:" + e.getMessage());
        }
        return null;

    }
}
