package org.opendelos.live.services.scheduler;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendelos.live.repository.resource.QueryResourceResults;
import org.opendelos.live.repository.resource.ResourceQuery;
import org.opendelos.live.repository.resource.ResourceRepository;
import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.live.services.scheduledEvent.ScheduledEventService;
import org.opendelos.live.services.structure.ClassroomService;
import org.opendelos.live.services.structure.CourseService;
import org.opendelos.live.services.structure.DepartmentService;
import org.opendelos.live.services.structure.StreamingServerService;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.PlayerOptions;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ResourceStatus;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.scheduler.ScheduleQuery;
import org.opendelos.model.scheduler.TimeTableResults;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.StreamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LiveService {

    @Value("${default.institution.identity}")
    String institution_identity;
    @Value("${app.zone}")
    String app_zone;
    @Value("${app.debug}")
    boolean app_debug;

    @Autowired
    Institution defaultInstitution;
    @Autowired
    String currentAcademicYear;

    private final Logger logger = LoggerFactory.getLogger(LiveService.class);

    private final ResourceRepository resourceRepository;
    private final ScheduleService scheduleService;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final ScheduledEventService scheduledEventService;
    private final StreamingServerService streamingServerService;
    private final ResourceService resourceService;
    private final ClassroomService classroomService;

    @Autowired
    public LiveService(ResourceRepository resourceRepository, ScheduleService scheduleService, DepartmentService departmentService, CourseService courseService, ScheduledEventService scheduledEventService, StreamingServerService streamingServerService, ResourceService resourceService, ClassroomService classroomService) {
        this.resourceRepository = resourceRepository;
        this.scheduleService = scheduleService;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.scheduledEventService = scheduledEventService;
        this.streamingServerService = streamingServerService;
        this.resourceService = resourceService;
        this.classroomService = classroomService;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Europe/Athens") // Every day at 6 a.m. [Run's on appStart too: See StartUpApplicationListener]
    public void updateLiveEntries()  {
        this.UpdateTodaysSchedule();
    }

    public void UpdateTodaysSchedule() {

        logger.info("*** CREATE TODAY'S SCHEDULE *** ");

        Calendar time_start = Calendar.getInstance();
        Date startTime = time_start.getTime();

        //> Get Today's Schedule. GET FROM DB existing_todays_list
        List<Resource> existingTodaySchedule = this.getTodaysScheduleFromDatabase();
        logger.trace("LiveService: Today's Schedule (Existing):" + existingTodaySchedule.size());

        //> DELETE FROM  existingTodaySchedule future events (TO BE UPDATED) and trash events that are left from previous days (like yesterday)
        // Future events will be copied from computedTodaysSchedule
        List<Resource> delete_db_list = new ArrayList<>();
        Instant beginning_of_day = Instant.now().truncatedTo(ChronoUnit.DAYS); //in order to throw passed events
        for (Resource exist_res: existingTodaySchedule) {
                Instant broadcast_datetime = exist_res.getDate();
                if (broadcast_datetime.isAfter(Instant.now())) {
                    delete_db_list.add(exist_res);
                }
                else if (broadcast_datetime.isBefore(beginning_of_day)) {
                    delete_db_list.add(exist_res);
                }
            }
        existingTodaySchedule.removeAll(delete_db_list);
        logger.trace("LiveService: Remove Future & Obsolete events:" + delete_db_list.size());

        //# This is the list for today's schedule computed from scratch :: on application start or at 6 am
        List<ScheduleDTO> computedTodaySchedule = scheduleService.computeTodaysSchedule(currentAcademicYear);
        logger.trace("LiveService: Today's Schedule (Scratch):" + computedTodaySchedule.size());

        //merge 2 lists (updatedTodaySchedule+existingTodaySchedule) :: Keep passed and live from existingTodaySchedule
        logger.trace("LiveService: new & updated items:" + computedTodaySchedule.size());
        if (computedTodaySchedule.size() > 0) {
            for (ScheduleDTO scheduleDTO : computedTodaySchedule) {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
                    int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
                    LocalDateTime broadcast_startDateTime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
                    //Check if this should be Live or Passed
                    //Then add to today's schedule if (and only if not already there :: check existence using streamId)
                    if (broadcast_startDateTime.isBefore(LocalDateTime.now())) {
                      boolean found = false;
                      String computed_streamId = this.generateLiveStreamId(scheduleDTO);
                      for (Resource ex_resource: existingTodaySchedule) {
                          if (ex_resource.getStreamId().equals(computed_streamId)) {
                              found = true;
                              break;
                          }
                      }
                      if (!found) {
                          logger.trace("Broadcast with Id:" + computed_streamId + " should have been live or passed, but was not found in Live collection. Add it!");
                          Resource liveEntry = createLiveResourceFromScheduleDTO(scheduleDTO);
                          if (liveEntry != null) {
                              existingTodaySchedule.add(liveEntry);
                          }
                      }
                    }
                    else {
                        Resource liveEntry = createLiveResourceFromScheduleDTO(scheduleDTO);
                        if (liveEntry != null) {
                            existingTodaySchedule.add(liveEntry);
                        }
                    }
                }
        }

        //Update today list: save to db
        resourceRepository.clearCollection("Scheduler.Live");
        if (existingTodaySchedule.size() > 0) {
                for (Resource resource: existingTodaySchedule) {
                   resourceRepository.saveToCollection(resource, "Scheduler.Live");
                }
            }

        Calendar time_now = Calendar.getInstance();
        Date endTime = time_now.getTime();
        long diff = endTime.getTime() - startTime.getTime();
        logger.info("LiveService: create Today's Schedule - LoadTime: " + diff);
    }

    public void saveToCollection(Resource resource, String collection) {
        resourceRepository.saveToCollection(resource, collection);
    }

    public boolean IsOneTimeScheduleScheduledForTodayAfterNow(Schedule schedule) {
        /* return true if it scheduled for today after now! (time) or Else false */
        boolean res = true;
        if (isScheduledForTodayInTheFuture(schedule)) {
            res = true;
        }
        else {
            String classroom_id = schedule.getClassroom();
            if (classroomService.getClassroomStatus(classroom_id) != 0) {
                logger.info("ONTIME SCHEDULER: CLASSROOM DISABLED : SKIPPING UPDATE");
                res = false;
            }
        }
        return res;
    }

    public ScheduleDTO getRegularScheduleDTOScheduledForTodayAfterNow(Schedule schedule) {

        LocalDate today = LocalDate.now();
        //check classroom status
        String classroom_id = schedule.getClassroom();
        if (classroomService.getClassroomStatus(classroom_id) != 0 ) {
            logger.info("REGULAR SCHEUDLER: CLASSROOM DISABLED : SKIPPING UPDATE");
            return null;
        }
        else if (today.getDayOfWeek().equals(schedule.getDayOfWeek())) {
            /* return a scheduleDTO if it scheduled for today after now! (time) or Else null */
            TimeTableResults timeTableResults = scheduleService.calculateExactDaysOfRegularSchedule(schedule);
            if (timeTableResults != null && timeTableResults.getResults() != null && timeTableResults.getResults().size() > 0) {
                List<ScheduleDTO> exactSchedules = timeTableResults.getResults();
                for (ScheduleDTO scheduleDTO : exactSchedules) {
                    if (scheduleDTO.getArgia() != null || scheduleDTO.getCancellation() != null || scheduleDTO.getOverlapInfo() != null) {
                        continue;
                    }
                    if (isScheduledForTodayInTheFuture(scheduleDTO)) {
                        return scheduleDTO;
                    }
                }
            }
        }
        else {
            logger.info("REGULAR SCHEUDLER: NOT IN SAME DAYOFWEEK : SKIPPING UPDATE");
        }
        return null;
    }

    public void AddScheduleDTOToTodaysSchedule(ScheduleDTO scheduleDTO) {

        Map<Integer, String> ssMap = this.getStreamingServersMaps();
        //pick a server randomly
        java.util.Random random = new java.util.Random();
        int random_server_index = random.nextInt(ssMap.size()+1);
        String ss_id = ssMap.get(random_server_index);
        //add
        Resource liveEntry = this.createLiveResourceFromScheduleDTO(scheduleDTO, ss_id);
        if (liveEntry != null) {
            resourceRepository.saveToCollection(liveEntry, "Scheduler.Live");
            logger.info("new OneTime Entry:  update schedule with new onetime entry");
        }
    }

    public void AddScheduleToTodaysSchedule(Schedule schedule) {

        Map<Integer, String> ssMap = this.getStreamingServersMaps();
        //pick a server randomly
        java.util.Random random = new java.util.Random();
        int random_server_index = random.nextInt(ssMap.size()+1);
        String ss_id = ssMap.get(random_server_index);
        //add
        ScheduleDTO scheduleDTO = scheduleService.getScheduleDTO(schedule);
        Resource liveEntry = this.createLiveResourceFromScheduleDTO(scheduleDTO, ss_id);
        if (liveEntry != null) {
            resourceRepository.saveToCollection(liveEntry, "Scheduler.Live");
            logger.info("new OneTime Entry:  update schedule with new onetime entry");
        }
    }

    public QueryResourceResults getLiveResources() {

        ResourceQuery resourceQuery = new ResourceQuery();
        resourceQuery.setCollectionName("Scheduler.Live"); // defines that this query is for today only!
        resourceQuery.setSkip(0);
        resourceQuery.setLimit(50);
        resourceQuery.setSort("date");
        resourceQuery.setDirection("asc");

        //> Get Today's Schedule
        QueryResourceResults queryResourceResults;
        queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
        resourceQuery.setTotalResults(queryResourceResults.getTotalResults());
        //> Get Live List
        QueryResourceResults liveResources = scheduleService.getLiveResourceListFromTodaysSchedule(queryResourceResults.getSearchResultList());
        classroomService.setClassroomNameToResults(liveResources);

        return liveResources;
    }

    public Resource createLiveResourceFromScheduleDTO(ScheduleDTO scheduleDTO) {

        Resource liveEntry = new Resource();
        //STREAMID
        String streamId = this.generateLiveStreamId(scheduleDTO);
        liveEntry.setStreamId(streamId);

        liveEntry.setId(null);
        liveEntry.setStreamName(scheduleDTO.getClassroom().getCode());
        liveEntry.setScheduleId(scheduleDTO.getId());
        if (scheduleDTO.getType().equals("lecture")) {
            Course course = courseService.findById(scheduleDTO.getCourse().getId());
            liveEntry.setTitle(course.getTitle());
            if (scheduleDTO.getRepeat().equals("regular")) {
                liveEntry.setDescription("???????????????????????????????? ???????????????? ??????????????????");
            }
            else if (scheduleDTO.getRepeat().equals("onetime")) {
                liveEntry.setDescription("?????????????? ???????????????? ??????????????????");
            }
            Department department = departmentService.findById(scheduleDTO.getDepartment().getId());
            liveEntry.setInstitution(defaultInstitution.getId());
            liveEntry.setSchool(department.getSchoolId());
            liveEntry.setDepartment(scheduleDTO.getDepartment());
            Person supervisor = scheduleDTO.getSupervisor();
            supervisor.setDepartment(scheduleDTO.getDepartment());
            liveEntry.setSupervisor(supervisor);
            liveEntry.setCourse(course);
            liveEntry.setType("COURSE");
            liveEntry.setPeriod(scheduleDTO.getPeriod());
        }
        else if (scheduleDTO.getType().equals("event")) {
            ScheduledEvent scheduledEvent = scheduledEventService.findById(scheduleDTO.getScheduledEvent().getId());
            liveEntry.setTitle(scheduledEvent.getTitle());
            liveEntry.setDescription("???????????????????????????????? ???????????????? ??????????????????");
            liveEntry.setSchool(null);
            liveEntry.setDepartment(null);
            if (scheduledEvent.getResponsiblePerson() != null) {
                liveEntry.setSupervisor(scheduledEvent.getResponsiblePerson());
            }
            liveEntry.setEvent(scheduledEvent);
            liveEntry.setType("EVENT");
        }
        //COMMON PROPERTIES
        liveEntry.setAcademicYear(scheduleDTO.getAcademicYear());
        liveEntry.setPartNumber(0);
        liveEntry.setEditor(scheduleDTO.getEditor());
        liveEntry.setSpeakers("");
        liveEntry.setExt_speakers("");
        liveEntry.setLanguage("el");
        //>Date & Time
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
        int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
        LocalDateTime broadcast_datetime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
        //LocalDateTime broadcast_datetime = scheduleDTO.getDate().atTime(broadcast_hour,broadcast_min);
        Instant instant = broadcast_datetime.atZone(ZoneId.of(app_zone)).toInstant();
        liveEntry.setDate(instant);
        liveEntry.setDateModified(Instant.now());
        //others
        liveEntry.setTopics(null);
        liveEntry.setCategories(null);
        liveEntry.setAccessPolicy(scheduleDTO.getAccess());
        liveEntry.setLicense(defaultInstitution.getOrganizationLicense());
        liveEntry.setStatistics(0);
        liveEntry.setStatus(new ResourceStatus(-1,-1,"SCHEDULER"));
        liveEntry.setPlayerOptions(new PlayerOptions(true,false));
        //Real Duration
        DecimalFormat df = new DecimalFormat("00");
        String h = df.format(scheduleDTO.getDurationHours());
        String m = df.format(scheduleDTO.getDurationMinutes());
        liveEntry.setRealDuration(h + ":" + m); //should re-set at recording end
        liveEntry.setResourceAccess(null);
        liveEntry.setPresentation(null);
        liveEntry.setClassroom(scheduleDTO.getClassroom().getId());

        //LIVE ENTRIES
        liveEntry.setBroadcast(scheduleDTO.isBroadcast());
        liveEntry.setAccess(scheduleDTO.getAccess());
        if (scheduleDTO.getBroadcastCode() != null) {
            liveEntry.setBroadcastCode(scheduleDTO.getBroadcastCode());
        }
        liveEntry.setRecording(scheduleDTO.isRecording());
        liveEntry.setPublication(scheduleDTO.getPublication());

        return  liveEntry;
    }

    public Resource createLiveResourceFromScheduleDTO(ScheduleDTO scheduleDTO, String streamingServerId) {

        Resource liveEntry = new Resource();
        //STREAMID
        String streamId = this.generateLiveStreamId(scheduleDTO);
        liveEntry.setStreamId(streamId);

        liveEntry.setId(null);
        liveEntry.setStreamName(scheduleDTO.getClassroom().getCode());
        liveEntry.setScheduleId(scheduleDTO.getId());
        if (scheduleDTO.getType().equals("lecture")) {
            Course course = courseService.findById(scheduleDTO.getCourse().getId());
            liveEntry.setTitle(course.getTitle());
            if (scheduleDTO.getRepeat().equals("regular")) {
                liveEntry.setDescription("???????????????????????????????? ???????????????? ??????????????????");
            }
            else if (scheduleDTO.getRepeat().equals("onetime")) {
                liveEntry.setDescription("?????????????? ???????????????? ??????????????????");
            }
            Department department = departmentService.findById(scheduleDTO.getDepartment().getId());
            liveEntry.setInstitution(defaultInstitution.getId());
            liveEntry.setSchool(department.getSchoolId());
            liveEntry.setDepartment(scheduleDTO.getDepartment());
            Person supervisor = scheduleDTO.getSupervisor();
            supervisor.setDepartment(scheduleDTO.getDepartment());
            liveEntry.setSupervisor(supervisor);
            liveEntry.setCourse(course);
            liveEntry.setType("COURSE");
        }
        else if (scheduleDTO.getType().equals("event")) {
            ScheduledEvent scheduledEvent = scheduledEventService.findById(scheduleDTO.getScheduledEvent().getId());
            liveEntry.setTitle(scheduledEvent.getTitle());
            liveEntry.setDescription("???????????????????????????????? ???????????????? ??????????????????");
            liveEntry.setSchool(null);
            liveEntry.setDepartment(null);
            if (scheduledEvent.getResponsiblePerson() != null) {
                liveEntry.setSupervisor(scheduledEvent.getResponsiblePerson());
            }
            liveEntry.setEvent(scheduledEvent);
            liveEntry.setType("EVENT");
        }
        //COMMON PROPERTIES
        liveEntry.setPartNumber(0);
        liveEntry.setEditor(scheduleDTO.getEditor());
        liveEntry.setSpeakers("");
        liveEntry.setExt_speakers("");
        liveEntry.setLanguage("el");
        //>Date & Time
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
        int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
        LocalDateTime broadcast_datetime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
        //LocalDateTime broadcast_datetime = scheduleDTO.getDate().atTime(broadcast_hour,broadcast_min);
        Instant instant = broadcast_datetime.atZone(ZoneId.of(app_zone)).toInstant();
        liveEntry.setDate(instant);
        liveEntry.setDateModified(Instant.now());
        //others
        liveEntry.setTopics(null);
        liveEntry.setCategories(null);
        liveEntry.setAccessPolicy(scheduleDTO.getAccess());
        liveEntry.setLicense(defaultInstitution.getOrganizationLicense());
        liveEntry.setStatistics(0);
        liveEntry.setStatus(new ResourceStatus(-1,-1,"SCHEDULER"));
        liveEntry.setPlayerOptions(new PlayerOptions(true,false));
        //Real Duration
        DecimalFormat df = new DecimalFormat("00");
        String h = df.format(scheduleDTO.getDurationHours());
        String m = df.format(scheduleDTO.getDurationMinutes());
        liveEntry.setRealDuration(h + ":" + m); //should re-set at recording end
        liveEntry.setResourceAccess(null);
        liveEntry.setPresentation(null);
        liveEntry.setClassroom(scheduleDTO.getClassroom().getId());

        //LIVE ENTRIES
        liveEntry.setBroadcast(scheduleDTO.isBroadcast());
        liveEntry.setAccess(scheduleDTO.getAccess());
        if (scheduleDTO.getBroadcastCode() != null) {
            liveEntry.setBroadcastCode(scheduleDTO.getBroadcastCode());
        }
        liveEntry.setRecording(scheduleDTO.isRecording());
        liveEntry.setPublication(scheduleDTO.getPublication());
        liveEntry.setStreamingServerId(streamingServerId);

        StreamingServer streamingServer = streamingServerService.findById(streamingServerId);
        liveEntry.setStreamingServerInfo(streamingServer.getCode() + "/" + streamingServer.getApplication());

        return  liveEntry;
    }

    private String generateLiveStreamId(ScheduleDTO scheduleDTO) {

         StringBuilder streamIdsb = new StringBuilder();
         String classroomCode = scheduleDTO.getClassroom().getCode();

         DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
         int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
         LocalDateTime _datetime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);


         int year  = _datetime.getYear();
         int month  =_datetime.getMonthValue();
         int dayOfYear = _datetime.getDayOfYear();

          //NOTE: the second part could be encrypted
          // It must be a known algorithm so that we can publish to youtube by streamId
         streamIdsb.append(classroomCode).append("@").append(broadcast_hour).append(broadcast_min).append(dayOfYear).append(month).append(year);

         return streamIdsb.toString();
    }

    public List<Resource> getTodaysScheduleFromDatabase() {

        ResourceQuery resourceQuery = new ResourceQuery();
        resourceQuery.setCollectionName("Scheduler.Live");
        resourceQuery.setSort("date");
        resourceQuery.setDirection("asc");
        QueryResourceResults queryResourceResults;
        queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
        resourceQuery.setTotalResults(queryResourceResults.getTotalResults());

        return queryResourceResults.getSearchResultList();
    }

    private String getStreamingHostWithMinimumUtilization(Map<String,Integer> ssUtilizationMap) {

        String minHost = null;
        int utilization = 1000;
        for (Map.Entry<String, Integer> usage_ : ssUtilizationMap.entrySet()) {
            logger.trace("LiveService: Server Util:" + usage_.getKey() + " = " + usage_.getValue());
            if (usage_.getValue() < utilization) {
                minHost = usage_.getKey();
                utilization = usage_.getValue();
            }
        }
        return minHost;
    }

    private Map<String,Integer> initStreamingServersUtilization() {

        Map<String, Integer> ssUtilizationMap = new HashMap<>();
        List<StreamingServer> streamingServers = streamingServerService.getAllByStatusAndType("true","ipcamera");
        if (streamingServers != null && streamingServers.size() > 0) {
             for (StreamingServer streamingServer: streamingServers) {
                 ssUtilizationMap.put(streamingServer.getId(), 0);
             }
        }
        return ssUtilizationMap;
    }

    public Map<Integer,String> getStreamingServersMaps() {

        Map<Integer, String> ssMap = new HashMap<>();
        List<StreamingServer> streamingServers = streamingServerService.getAllByStatusAndType("true","ipcamera");
        if (streamingServers != null && streamingServers.size() > 0) {
            int index = 1;
            for (StreamingServer streamingServer: streamingServers) {
                ssMap.put(index, streamingServer.getId());
                index++;
            }
        }
        return ssMap;
    }


    public Map<String,StreamingServer> getStreamingServersHM(String enabled, String type) {

        Map<String, StreamingServer> ssMap = new HashMap<>();
        List<StreamingServer> streamingServers = streamingServerService.getAllByStatusAndType(enabled,type);
        if (streamingServers != null && streamingServers.size() > 0) {
            for (StreamingServer streamingServer: streamingServers) {
                ssMap.put(streamingServer.getId(), streamingServer);
            }
        }
        return ssMap;
    }

    public Map<String,StreamingServer> getMapOfStreamingServers() {

        Map<String, StreamingServer> ssMap = new HashMap<>();
       // List<StreamingServer> streamingServers = streamingServerService.getAllByStatus("true");
        List<StreamingServer> streamingServers = streamingServerService.getAllByStatusAndType("true","ipcamera");
        if (streamingServers != null && streamingServers.size() > 0) {
            for (StreamingServer streamingServer: streamingServers) {
                ssMap.put(streamingServer.getId(), streamingServer);
            }
        }
        return ssMap;
    }

    public StreamingServer getRecorderServer() {

        StreamingServer recorderServer = null;
        List<StreamingServer> streamingServers = streamingServerService.getAllByStatusAndType("true","recorder");
        if (streamingServers != null && streamingServers.size() > 0) {
            for (StreamingServer streamingServer: streamingServers) {
                recorderServer = streamingServer;
                break;
            }
        }
        return recorderServer;
    }

    private void updateStreamingServerUtilization(String ss_id,Map<String,Integer> ssUtilizationMap) {
        int utilization = ssUtilizationMap.get(ss_id) + 1;
        ssUtilizationMap.remove(ss_id);
        ssUtilizationMap.put(ss_id,utilization);

    }

    public boolean isScheduledForTodayInTheFuture(Schedule schedule) {
        boolean res = false;
        LocalDate today = LocalDate.now();
        LocalDate scheduleDate = schedule.getDate();
        if (!scheduleDate.isAfter(today) && !scheduleDate.isBefore(today)) {
            LocalTime schedule_start_time = LocalTime.parse(schedule.getStartTime());
            LocalTime now_time = LocalTime.now();
            if (schedule_start_time.isAfter(now_time)) {    // this excludes (correctly) live items
                res = true;
            }
        }
        return res;
    }
    public boolean isScheduledForTodayInTheFuture(ScheduleDTO scheduleDTO) {
        boolean res = false;
        LocalDate today = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate validateDate = LocalDate.parse(scheduleDTO.getDate(), f);
        if (!validateDate.isAfter(today) && !validateDate.isBefore(today)) {
            LocalTime schedule_start_time = LocalTime.parse(scheduleDTO.getStartTime());
            LocalTime now_time = LocalTime.now();
            if (schedule_start_time.isAfter(now_time)) { // this excludes (correctly) live items
                res = true;
            }
        }
        return res;
    }
}

