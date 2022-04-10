/* 
     Author: Michael Gatzonis - 23/10/2021 
     obrella
*/
package org.opendelos.sync.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Device;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.triggers.Triggers;
import org.opendelos.model.triggers.XmlTriggerType;
import org.opendelos.sync.legacyrepo.ElegacyRepository;
import org.opendelos.sync.services.structure.ClassroomService;
import org.opendelos.sync.services.structure.InstitutionService;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service("SyncService")
public class SyncService {

	@Value("${default.institution.identity}")
	String institution_identity;
	@Value("${import.data}")
	boolean import_data;
	@Value("${import.scheduled}")
	boolean import_scheduled;
	@Value("${trigger.data}")
	boolean trigger_data;
	@Value("${import_url}")
	String import_url;
	@Value("${import_pass}")
	String import_pass;
	@Value("${match}")
	boolean found_matching;
	@Value("${restore_events}")
	boolean restore_events;
	@Value("${enable.classrooms}")
	boolean enable_classrooms;

	private final Logger logger = Logger.getLogger(SyncService.class.getName());

	private final UpdateService updateService;

	private final ElegacyRepository elegacyRepository;
	private final InstitutionService institutionService;
	private final ImportLegacyService importLegacyService;
	private final ClassroomService classroomService;

	private boolean in_process;

	@Autowired
	public SyncService(UpdateService updateService, ElegacyRepository elegacyRepository, InstitutionService institutionService, ImportLegacyService importLegacyService, ClassroomService classroomService) {
		this.updateService = updateService;
		this.elegacyRepository = elegacyRepository;
		this.institutionService = institutionService;
		this.importLegacyService = importLegacyService;
		this.classroomService = classroomService;
	}

/*	@Scheduled(cron = "0 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59 5-23 ? * SUN-SAT")*/
	public void SYNC() throws Exception {

		Institution institution = institutionService.findByIdentity(institution_identity);

		if (import_data && !in_process) {
			in_process = true;
				logger.info("Running import...");
				org.opendelos.legacydomain.institution.Institution legacyInstitution =
						elegacyRepository.getLegacyInstitution(import_url, "guest", "guest", "/db/apps/delos-uoa/institutions", "uoa");
				updateService.ReCreatedSAUser();
				boolean res = importLegacyService.ImportEverything(institution,legacyInstitution);
				if (res) { logger.info("Import Finished...");
				}
				else { logger.severe("Import failed.."); }
			in_process = false;
			import_data = false;
		}
		/* DO NOT USE THIS :: FIND RELATED RESOURCES ON THE FLY IN PLAYER CONTROLLER */
/*		if (found_matching && !in_process) {
			logger.info("RUNNING MATCH PROCESS");
			importLegacyService.matchRelations();
		}*/

		 if (restore_events && !in_process) {
			logger.info("RUNNING EVENT PROPERTIES RESTORE....");
			//#NOTE: Matvh by title and startDate
			 importLegacyService.restoreEvents();

		 }

		 if (import_scheduled && !in_process) {
		 	importLegacyService.ImportScheduled(institution_identity,"2021");
		 }

		 if (enable_classrooms) {
			 List<Classroom> classrooms = classroomService.findAll();
			 for (Classroom classroom : classrooms) {
				 List<Device> deviceList = new ArrayList<>();
			 	 if (classroom.getDevices() != null && !classroom.getDevices().isEmpty() && classroom.getDevices().get(0) != null) {
					 Device device = classroom.getDevices().get(0);
					 device.setStreamAccessUrl("rtsp://keeyor:admin123!@mgfos.hopto.org:90/videoMain");
					 deviceList.add(device);
				 }
				 else {
					Device new_device = new Device();
					new_device.setType("ipcamera");
					new_device.setDescription("Axis");
					deviceList.add(new_device);
				 }
				 classroom.setDevices(deviceList);
				 classroom.setUsage("both");
				 classroom.setCalendar("true");
				 classroomService.update(classroom);
			 }
			 logger.info("all classrooms enabled");
		 }


/* 		if (!in_process && trigger_data) {
			in_process = true;
			processTriggers(institution);
			in_process = false;
		}
		else {
			logger.info("In process. Pause triggers...");
		}*/
	}

	public void processTriggers(Institution institution) throws Exception {

		org.opendelos.legacydomain.institution.Institution legacyInstitution = updateService.getLegacyInstitution();

		Triggers triggers = null;
		try {
			Collection col = elegacyRepository.getDatabaseCollection(import_url, "guest", "guest", "/db/");
			triggers = (Triggers) elegacyRepository.GetDataBaseObject(col, "triggers-log.xml", Triggers.class);
		}
		catch (Exception e) {
			logger.severe("Could not Read triggers-log.xml");
		}
		if (triggers != null && triggers.getTrigger().size() > 0) {
			logger.info("Running triggers..");

			//#Merge triggers with same event and uri. Execute only One!
			logger.info("Number of Triggers:" + triggers.getTrigger().size());
			List<XmlTriggerType> processUniqueTriggers = new ArrayList<>();
			for (XmlTriggerType xmlTriggerType: triggers.getTrigger()) {
				if (!processUniqueTriggers.contains(xmlTriggerType)) {
					processUniqueTriggers.add(xmlTriggerType);
				}
			}
			logger.info("Number of Unique Triggers:" + processUniqueTriggers.size());
			for (XmlTriggerType xmlTriggerType: processUniqueTriggers) {
				String triggerEvent = xmlTriggerType.getEvent();
				String triggerUri = xmlTriggerType.getUri();

				switch (triggerEvent) {
					case  "after-assign-course-to-staffMember":
							logger.info("assign course trigger");
							try {
								updateService.AssignCourses2StaffMember(triggerUri);
							}
							catch (Exception e) {
								logger.severe("Failed AssignCourses2StaffMember:" + e.getMessage());
							}
							break;
					case  "after-unassign-course-from-staffMember":
							logger.info("unassign course trigger");
							try {
								updateService.UnAssignCourseFromStaffMember(triggerUri);
							}
							catch (Exception e) {
								logger.severe("Failed UnAssignCourseFromStaffMember:" + e.getMessage());
							}
							break;
					case  "after-assign-classroom-to-department":
							logger.info("assign classroom trigger");
							try {
								updateService.AssignClassroom2Department(triggerUri);
							}
							catch (Exception e) {
								logger.severe("Failed AssignClassroom2Department:" + e.getMessage());
							}
							break;
					case  "after-unassign-classroom-from-department":
							logger.info("unassign classroom trigger");
							try {
								updateService.UnAssignClassroomFromDepartment(triggerUri);
							}
							catch (Exception e) {
								logger.severe("Failed UnAssignClassroomFromDepartment:" + e.getMessage());
							}
							break;
					case  "after-create-school":
							logger.info("create school trigger");
							try {
								updateService.CreateSchool(triggerUri,legacyInstitution);
							}
							catch (Exception e){
								logger.severe("Failed CreateSchool:" + e.getMessage());
							}
							break;
					case  "after-update-school":
							logger.info("update school trigger");
							try {
								updateService.UpdateSchool(triggerUri,legacyInstitution);
							}
							catch (Exception e){
								logger.severe("Failed UpdateSchool:" + e.getMessage());
							}
							break;
					case  "after-create-department":
							logger.info("create department trigger");
							try {
								updateService.CreateDepartment(triggerUri,institution.getId(),legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed CreateDepartment:" + e.getMessage());
							}
							break;
					case  "after-update-department":
							logger.info("update department trigger");
							try {
								updateService.UpdateDepartment(triggerUri,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed UpdateDepartment:" + e.getMessage());
							}
							break;
					case  "after-create-study":
							logger.info("create study trigger");
							try {
								updateService.CreateStudyProgram(triggerUri,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed CreateStudyProgram:" + e.getMessage());
							}
							break;
					case  "after-update-study":
							logger.info("update study trigger");
							try {
								updateService.UpdateStudyProgram(triggerUri,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed UpdateStudyProgram:" + e.getMessage());
							}
							break;
					case  "after-delete-study":
							logger.info("delete study trigger");
							try {
								updateService.DeleteStudyProgram(triggerUri);
							}
							catch (Exception e) {
								logger.severe("Failed DeleteStudyProgram:" + e.getMessage());
							}
							break;
					case  "after-create-staff":
							logger.info("create staff trigger");
							try {
								updateService.CreateUpdateStaffMember(triggerUri, null,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed CreateStaffMember:" + e.getMessage());
							}
							break;
					case  "after-update-staff":
							logger.info("update staff trigger");
							try {
								updateService.CreateUpdateStaffMember(triggerUri, "UPDATE",legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed UpdateStaffMember:" + e.getMessage());
							}
							break;
					case  "after-delete-staff":
							logger.info("delete staff trigger");
							try {
								updateService.DeleteStaffMember(triggerUri,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed DeleteStaffMember:" + e.getMessage());
							}
							break;
					case  "after-create-course":
							logger.info("create course trigger");
							try {
								updateService.CreateCourse(triggerUri,institution.getId(),legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed CreateCourse:" + e.getMessage());
							}
							break;
					case  "after-update-course":
							logger.info("update course trigger");
							try {
								updateService.UpdateCourse(triggerUri,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed UpdateCourse:" + e.getMessage());
							}
							break;
					case  "after-delete-course":
							logger.info("delete course trigger");
							try {
								updateService.DeleteCourse(triggerUri);
							}
							catch (Exception e) {
								logger.severe("Failed DeleteCourse:" + e.getMessage());
							}
							break;
					case  "after-create-classroom" :
							logger.info("create classroom trigger");
							try {
								updateService.CreateClassroom(triggerUri,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed CreateClassroom:" + e.getMessage());
							}
							break;
					case "after-update-classroom":
							logger.info("update classroom trigger");
							try {
								updateService.UpdateClassroom(triggerUri,legacyInstitution);
							}
							catch (Exception e) {
								logger.severe("Failed UpdateClassroom:" + e.getMessage());
							}
							break;
					case  "after-delete-classroom":
							logger.info("delete classroom trigger");
							try {
								updateService.DeleteClassroom(triggerUri);
							}
							catch (Exception e) {
								logger.severe("Failed DeleteClassroom:" + e.getMessage());
							}
							break;
					default:
							String triggerCollection = triggerUri.substring(0,triggerUri.lastIndexOf("/"));
							String triggerDocument = triggerUri.substring(triggerUri.lastIndexOf("/")+1);

							String updateType="";
							if (triggerCollection.endsWith("Live")) {
								updateType = "Live";
							}
							else if (triggerCollection.endsWith("Config")) {
								updateType = "Calendar";
							}
							else if (triggerCollection.endsWith("Events")) {
								updateType = "Event";
							}
							else if (triggerCollection.endsWith("Videolectures")) {
								updateType = "Resource";
							}
							else if (triggerCollection.endsWith("Users")) {
								updateType = "User";
							}
							else if (triggerCollection.endsWith("Presentations")) {
								updateType = "Presentation";
							}
							switch (updateType) {
								case "Calendar" :
									if (triggerEvent.equals("after-update-document")) {
										logger.info("UPDATING 2021 Calendar");
										importLegacyService.ImportCalendar(triggerCollection,triggerDocument,institution);
									}
									break;
								case "Live" :
									logger.info("Live Event Trigger");
									if (triggerEvent.equals("after-create-document") || triggerEvent.equals("after-update-document")) {
										updateService.CreateLiveEntry(triggerCollection,triggerDocument,legacyInstitution, institution);
									}
									else {
										updateService.DeleteLiveEntry(triggerDocument);
									}
									break;
								case "Event" :
									logger.info("Scheduled Event Trigger");
									if (triggerEvent.equals("after-create-document") || triggerEvent.equals("after-update-document")) {
										updateService.UpdateCreateScheduledEvent(triggerCollection,triggerDocument,triggerEvent,legacyInstitution,institution.getId());
									}
									else {
										updateService.DeleteScheduledEvent(triggerDocument);
									}
									break;
								case "Resource" :
									if (triggerEvent.equals("after-create-document") || triggerEvent.equals("after-update-document")) {
										try {
											updateService.CreateUpdateResource(triggerCollection,triggerDocument,triggerEvent,institution.getId());
										}
										catch (Exception e) {
											logger.info("Failed to update:" + triggerDocument + " deleted?");
										}
									}
									else {
										updateService.DeleteResource(triggerDocument);
									}
									break;
								case "User" :
									if (triggerEvent.equals("after-create-document") || triggerEvent.equals("after-update-document")) {
										updateService.CreateUpdateUser(triggerCollection,triggerDocument,triggerEvent,legacyInstitution,institution);
									}
									else {
										updateService.DeleteUser(triggerDocument);
									}
									break;
								case "Presentation" :
									if (triggerEvent.equals("after-create-document") || triggerEvent.equals("after-update-document")) {
										updateService.CreateUpdatePresentation(triggerCollection,triggerDocument);
									}
									else {
										logger.fine("NO need to handle Presentation delete. Nothing to delete!!!");
									}
									break;
								default:
										logger.info("Unknown eXist-db Trigger");
							}
				}
			}   //#For Triggers
 			try {
				this.deleteProcessedTriggers(triggers);
			}
			catch (XMLDBException e) {
				logger.severe("deleteProcessedTriggers Error: " + e.getMessage());
			}
		}
		else {
			logger.info("NO New Triggers");
		}

	}

	void deleteProcessedTriggers(Triggers triggers) throws XMLDBException {
		Collection col = elegacyRepository.getDatabaseCollection(import_url, "admin", import_pass, "/db/");
		XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
		service.setProperty("indent", "yes");
		StringBuilder xQuery = new StringBuilder();
		xQuery.append(" let $triggers := doc('/db/triggers-log.xml')//triggers/trigger[");
		int num = 0;
		for (XmlTriggerType xmlTriggerType: triggers.getTrigger()) {
				if (num > 0) {
					xQuery.append(" or (").append("timestamp").append("=").append("'")
							.append(xmlTriggerType.getTimestamp()).append("')");
				}
				else {
					xQuery.append("(").append("timestamp").append("=").append("'").append(xmlTriggerType.getTimestamp())
							.append("')");
				}
				num++;
		}
		xQuery.append("] ");
		xQuery.append("return (").append(" update delete $triggers )");
		logger.fine("DELETE TRIGGERS QUERY: " + xQuery);
		service.query(xQuery.toString());
	}


}
