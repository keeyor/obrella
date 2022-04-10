package org.opendelos.sync.services.scheduler;

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

import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
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
import org.opendelos.sync.properties.ChannelProperties;
import org.opendelos.sync.repository.resource.ResourceRepository;
import org.opendelos.sync.services.resource.ResourceService;
import org.opendelos.sync.services.scheduledEvent.ScheduledEventService;
import org.opendelos.sync.services.structure.ClassroomService;
import org.opendelos.sync.services.structure.CourseService;
import org.opendelos.sync.services.structure.DepartmentService;
import org.opendelos.sync.services.structure.StreamingServerService;
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
    private final ChannelProperties channelProperties;


    @Autowired
    public LiveService(ResourceRepository resourceRepository, ScheduleService scheduleService, DepartmentService departmentService, CourseService courseService, ScheduledEventService scheduledEventService, StreamingServerService streamingServerService, ResourceService resourceService, ClassroomService classroomService, ChannelProperties channelProperties) {
        this.resourceRepository = resourceRepository;
        this.scheduleService = scheduleService;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.scheduledEventService = scheduledEventService;
        this.streamingServerService = streamingServerService;
        this.resourceService = resourceService;
        this.classroomService = classroomService;
        this.channelProperties = channelProperties;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "Europe/Athens") // Every day at 6 a.m. [Run's on appStart too: See StartUpApplicationListener]
    public void updateLiveEntries() throws Exception {
        logger.info("*** UPDATE TODAY'S SCHEDULE *** ");
        this.UpdateTodaysSchedule();
    }

    public void UpdateTodaysSchedule() {

        logger.info("INITIATE SCHEDULE UPDATE: TODAY's PROGRAMME");

        Calendar time_start = Calendar.getInstance();
        Date startTime = time_start.getTime();

        logger.trace("Academic Year:" + currentAcademicYear);
         Map<String, Integer> ssUtilizationMap = this.initStreamingServersUtilization();
         if (ssUtilizationMap.size() > 0) {
          //> Calculate Today's Schedule (from scratch)
            ScheduleQuery scheduleQuery = new ScheduleQuery();
            scheduleQuery.setYear(currentAcademicYear);
            scheduleQuery.setEnabled("true");
            LocalDate now = LocalDate.now();
            scheduleQuery.setFromDate(now);
            scheduleQuery.setToDate(now);
            List<ScheduleDTO> updatedTodaySchedule = scheduleService.computeScheduleInDateRange(scheduleQuery);

            logger.info("LiveService: Today's Schedule (Scratch):" + updatedTodaySchedule.size());
            //> DELETE FROM updated_todays_list passed event ( so to leave them intact as far as streaming server )
            List<ScheduleDTO> delete_list = new ArrayList<>();
            for (ScheduleDTO liveDto: updatedTodaySchedule) {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                int broadcast_hour = Integer.parseInt(liveDto.getStartTime().substring(0,2));
                int broadcast_min = Integer.parseInt(liveDto.getStartTime().substring(3,5));
                LocalDateTime broadcast_datetime = LocalDate.parse(liveDto.getDate(), f).atTime(broadcast_hour,broadcast_min);
                long durationHours = liveDto.getDurationHours();
                long durationMinutes = liveDto.getDurationMinutes();
                //Keep Live items in case stopped and need to be restarted
                if (broadcast_datetime.plus(durationHours,ChronoUnit.HOURS).plus(durationMinutes,ChronoUnit.MINUTES).isBefore(LocalDateTime.now())) {
                    delete_list.add(liveDto);
                }
            }
            updatedTodaySchedule.removeAll(delete_list);
            logger.info("LiveService: Un-Touched Events: " + updatedTodaySchedule.size());

            //> GET FROM DB existing_todays_list
            ResourceQuery resourceQuery = new ResourceQuery();
            resourceQuery.setCollectionName("Scheduler.Live");
            resourceQuery.setSort("date");
            resourceQuery.setDirection("asc");
            //> Get Today's Schedule
            List<Resource> existingTodaySchedule = this.searchTodaysSchedule(resourceQuery);
            logger.info("LiveService: Today's Schedule (Existing):" + existingTodaySchedule.size());

            //> DELETE FROM  existing_todays_list future events and events that are left from previous call (like yesterday)
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

            logger.info("LiveService: Remove Future & Obsolete events:" + delete_db_list.size());
            //merge 2 lists (updatedTodaySchedule+existingTodaySchedule to existingTodaySchedule)
            logger.info("LiveService: new & updated items:" + updatedTodaySchedule.size());
            if (updatedTodaySchedule.size() > 0) {
                for (ScheduleDTO scheduleDTO : updatedTodaySchedule) {
                    String scheduleId = scheduleDTO.getId();
                    if (!containsScheduleId(existingTodaySchedule,scheduleId)) {
                        //Get StreamingServer with Min Usage and Assign to NEW ITEM
                        String ss_min = this.getStreamingHostWithMinimumUtilization(ssUtilizationMap);
                        updateStreamingServerUtilization(ss_min,ssUtilizationMap);
                        logger.trace("LiveService: assign to Streaming Server:" + ss_min);
                        Resource liveEntry = createLiveResourceFromScheduleDTO(scheduleDTO, ss_min);
                        if (liveEntry != null) {
                            existingTodaySchedule.add(liveEntry);
                        }
                        //FOR CHANNEL
                        if (scheduleDTO.isBroadcastToChannel()) {
                            Resource TvResource = createLiveChannelResourceFromScheduleDTO(scheduleDTO);
                            if (TvResource != null) {
                                existingTodaySchedule.add(TvResource);
                            }
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
         }
         else {
                 logger.warn("LiveService: No Streaming Servers defined or enabled");
                resourceRepository.clearCollection("Scheduler.Live");
         }

        Calendar time_now = Calendar.getInstance();
        Date endTime = time_now.getTime();
        long diff = endTime.getTime() - startTime.getTime();
        logger.trace("LiveService: Update Today's Schedule - LoadTime: " + diff);
    }

    public boolean containsScheduleId(final List<Resource> list, final String id){
        return list.stream().filter(o -> o.getScheduleId().equals(id)).findFirst().isPresent();
    }

    public boolean IsOneTimeScheduleScheduledForTodayAfterNow(Schedule schedule, boolean includeLive) {
        /* return true if it scheduled for today after now! (time) or Else false */
        boolean res = false;
        if (isScheduledForTodayInTheFuture(schedule,includeLive)) {
            res = true;
            String classroom_id = schedule.getClassroom();
            if (classroomService.getClassroomStatus(classroom_id) != 0) {
                logger.info("ONETIME SCHEDULER: CLASSROOM DISABLED : SKIPPING UPDATE");
                res = false;
            }
        }
        return res;
    }

    public ScheduleDTO getRegularScheduleDTOScheduledForTodayAfterNow(Schedule schedule, boolean includeLive) {

        LocalDate today = LocalDate.now();
        //check classroom status
        String classroom_id = schedule.getClassroom();
        boolean cancelLive = false;
        if (classroomService.getClassroomStatus(classroom_id) != 0 ) {
            logger.info("REGULAR SCHEDULER: CLASSROOM DISABLED : SKIPPING UPDATE");
            return null;
        }
        else if (today.getDayOfWeek().equals(schedule.getDayOfWeek())) {
            /* return a scheduleDTO if it scheduled for today after now! (time) or Else null */
            TimeTableResults timeTableResults = scheduleService.calculateExactDaysOfRegularSchedule(schedule,cancelLive);
            if (timeTableResults != null && timeTableResults.getResults() != null && timeTableResults.getResults().size() > 0) {
                List<ScheduleDTO> exactSchedules = timeTableResults.getResults();
                for (ScheduleDTO scheduleDTO : exactSchedules) {
                    if (scheduleDTO.getArgia() != null || scheduleDTO.getCancellation() != null || scheduleDTO.getOverlapInfo() != null) {
                        continue;
                    }
                    if (isScheduledForTodayInTheFuture(scheduleDTO, includeLive)) {
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
        int random_server_index = random.nextInt(ssMap.size());
        String ss_id = ssMap.get(random_server_index);
        //add
        Resource liveEntry = this.createLiveResourceFromScheduleDTO(scheduleDTO, ss_id);
        if (liveEntry != null) {
            resourceRepository.saveToCollection(liveEntry, "Scheduler.Live");
            logger.info("A. New OneTime Entry:  update LIVE schedule with new regular entry");
        }
        //FOR CHANNEL
        if (scheduleDTO.isBroadcastToChannel()) {
            Resource TvResource = createLiveChannelResourceFromScheduleDTO(scheduleDTO);
            if (TvResource != null) {
                resourceRepository.saveToCollection(TvResource, "Scheduler.Live");
                logger.info("A. New OneTime CHANNEL Entry:  update LIVE schedule with new onetime channel entry");
            }
        }
    }

    public void AddScheduleToTodaysSchedule(Schedule schedule) {

        Map<Integer, String> ssMap = this.getStreamingServersMaps();
        //pick a server randomly
        java.util.Random random = new java.util.Random();
        int random_server_index = random.nextInt(ssMap.size());
        String ss_id = ssMap.get(random_server_index);
        //add
        ScheduleDTO scheduleDTO = scheduleService.getScheduleDTO(schedule);
        Resource liveEntry = this.createLiveResourceFromScheduleDTO(scheduleDTO, ss_id);
        if (liveEntry != null) {
            resourceRepository.saveToCollection(liveEntry, "Scheduler.Live");
            logger.info("B. New OneTime Entry:  update LIVE schedule with new onetime entry");
        }
        //FOR CHANNEL
        if (scheduleDTO.isBroadcastToChannel()) {
            Resource TvResource = createLiveChannelResourceFromScheduleDTO(scheduleDTO);
            if (TvResource != null) {
                resourceRepository.saveToCollection(TvResource, "Scheduler.Live");
                logger.info("B. New OneTime CHANNEL Entry:  update LIVE schedule with new onetime channel entry");
            }
        }
    }

    public QueryResourceResults getLiveResources(int limit) {

        ResourceQuery resourceQuery = new ResourceQuery();
        resourceQuery.setCollectionName("Scheduler.Live"); // defines that this query is for today only!
        resourceQuery.setSkip(0);
        resourceQuery.setLimit(limit);
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

    public Long getLiveResourcesCount() {

        long liveCounter = 0;
        ResourceQuery resourceQuery = new ResourceQuery();
        resourceQuery.setCollectionName("Scheduler.Live"); // defines that this query is for today only!
        resourceQuery.setSkip(0);
        resourceQuery.setLimit(-1);
        resourceQuery.setSort("date");
        resourceQuery.setDirection("asc");

        //> Get Today's Schedule
        QueryResourceResults queryResourceResults;
        queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
        if (queryResourceResults != null && queryResourceResults.getTotalResults() >0) {
            QueryResourceResults liveResources = scheduleService.getLiveResourceListFromTodaysSchedule(queryResourceResults.getSearchResultList());
            liveCounter = liveResources.getTotalResults();
        }
        return liveCounter;
    }

    public QueryResourceResults getLiveResourcesByQuery(ResourceQuery resourceQuery) {

        //> Get Today's Schedule
        QueryResourceResults queryResourceResults;
        queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
        resourceQuery.setTotalResults(queryResourceResults.getTotalResults());
        //> Get Live List
        QueryResourceResults liveResources = scheduleService.getLiveResourceListFromTodaysSchedule(queryResourceResults.getSearchResultList());
        classroomService.setClassroomNameToResults(liveResources);

        return liveResources;
    }

    public Resource createLiveResourceFromScheduleDTO(ScheduleDTO scheduleDTO, String streamingServerId) {
        Resource liveEntry = new Resource();
        //STREAMID
        String streamId = this.generateLiveStreamId(scheduleDTO);
        if (streamId != null) {
            liveEntry.setStreamId(streamId);
        }
        else {
            logger.warn("Could not generate streamId for scheduled item:" + scheduleDTO.getId());
            return null;
        }
        liveEntry.setId(null);
        liveEntry.setBroadcastToChannel(false); //!Important : do not broadcast to channel by default
        liveEntry.setStreamName(scheduleDTO.getClassroom().getCode());
        liveEntry.setScheduleId(scheduleDTO.getId());
        if (scheduleDTO.getType().equals("lecture")) {
            Course course = courseService.findById(scheduleDTO.getCourse().getId());
            liveEntry.setTitle(course.getTitle());
            if (scheduleDTO.getRepeat().equals("regular")) {
                liveEntry.setDescription("Προγραμματισμένη Μετάδοση Μαθήματος");
            }
            else if (scheduleDTO.getRepeat().equals("onetime")) {
                liveEntry.setDescription("Έκτακτη Μετάδοση Μαθήματος");
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
            liveEntry.setDescription("Προγραμματισμένη Μετάδοση Εκδήλωσης");
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
        String[] set_atleast_one = new String[1];
        set_atleast_one[0] = "othersubsubj";
        liveEntry.setCategories(set_atleast_one);
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
        liveEntry.setRecording(scheduleDTO.isRecording());
        liveEntry.setPublication(scheduleDTO.getPublication());
        liveEntry.setStreamingServerId(streamingServerId);

        if (streamingServerId != null) { //to re-use in WeekController
            StreamingServer streamingServer = streamingServerService.findById(streamingServerId);
            liveEntry.setStreamingServerInfo(streamingServer.getCode() + "/" + streamingServer.getApplication());
        }
        return  liveEntry;
    }

    public Resource createLiveChannelResourceFromScheduleDTO(ScheduleDTO scheduleDTO) {

        StreamingServer streamingServer = streamingServerService.findByCode(channelProperties.getStreaming_server_code());
        String streamingServerId = streamingServer.getId();
        if (streamingServerId == null) {
            return null;
        }
        Resource liveEntry = new Resource();
        //STREAMID
        String streamId = channelProperties.getLive_stream_id();
        liveEntry.setStreamId(streamId);

        liveEntry.setId(null);
        liveEntry.setBroadcastToChannel(true); //!Important : broadcast to channel by default
        liveEntry.setStreamName(scheduleDTO.getClassroom().getCode());
        liveEntry.setScheduleId(scheduleDTO.getId());
        if (scheduleDTO.getType().equals("lecture")) {
            Course course = courseService.findById(scheduleDTO.getCourse().getId());
            liveEntry.setTitle(course.getTitle());
            if (scheduleDTO.getRepeat().equals("regular")) {
                liveEntry.setDescription("Προγραμματισμένη Μετάδοση Μαθήματος");
            }
            else if (scheduleDTO.getRepeat().equals("onetime")) {
                liveEntry.setDescription("Έκτακτη Μετάδοση Μαθήματος");
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
            liveEntry.setTitle("[Κανάλι] " + scheduledEvent.getTitle());
            liveEntry.setDescription("Προγραμματισμένη Μετάδοση Εκδήλωσης στο Κανάλι Εκδηλώσεων του Ιδρύματος");
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
        String[] set_atleast_one = new String[1];
        set_atleast_one[0] = "othersubsubj";
        liveEntry.setCategories(set_atleast_one);
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
        liveEntry.setAccess("open");
        liveEntry.setRecording(false);
        liveEntry.setPublication(scheduleDTO.getPublication());

        liveEntry.setStreamingServerId(streamingServerId);
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
        streamIdsb.append(classroomCode).append("@").append(dayOfYear).append(month).append(year);

        return streamIdsb.toString();


/*        String streamId;
        if (!scheduleDTO.getAccess().equals("open")) {
            if (scheduleDTO.getType().equals("lecture")) {
                streamId = scheduleDTO.getCourse().getId();
            }
            else if (scheduleDTO.getType().equals("event")) {
                streamId = scheduleDTO.getScheduledEvent().getId();
            }
            else {
                return null;
            }
            String broadcast_hour_str = scheduleDTO.getStartTime().substring(0, 2);
            String broadcast_min_str = scheduleDTO.getStartTime().substring(3, 5);
            String broadcast_datetime_str = broadcast_hour_str + broadcast_min_str;
            streamId += broadcast_datetime_str;
        }
        else {
            streamId = scheduleDTO.getClassroom().getStreamName();
        }

        return streamId;
        */
    }

    public List<Resource> searchTodaysSchedule(ResourceQuery resourceQuery) {

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
        List<StreamingServer> streamingServers = streamingServerService.getAllByStatus("true");
        if (streamingServers != null && streamingServers.size() > 0) {
             for (StreamingServer streamingServer: streamingServers) {
                 ssUtilizationMap.put(streamingServer.getId(), 0);
             }
        }
        return ssUtilizationMap;
    }

    public Map<Integer,String> getStreamingServersMaps() {

        Map<Integer, String> ssMap = new HashMap<>();
        List<StreamingServer> streamingServers = streamingServerService.getAllIdsByEnabled("true");
        if (streamingServers != null && streamingServers.size() > 0) {
            int index = 0;
            for (StreamingServer streamingServer: streamingServers) {
                ssMap.put(index, streamingServer.getId());
                index++;
            }
        }
        return ssMap;
    }

    private void updateStreamingServerUtilization(String ss_id,Map<String,Integer> ssUtilizationMap) {
        int utilization = ssUtilizationMap.get(ss_id) + 1;
        ssUtilizationMap.remove(ss_id);
        ssUtilizationMap.put(ss_id,utilization);

    }

    public boolean isScheduledForTodayInTheFuture(Schedule schedule, boolean includeLive) {
        boolean res = false;

        ZoneId zoneId = ZoneId.of(app_zone);
        LocalDate today = LocalDate.now(zoneId);
        LocalDate scheduleDate = schedule.getDate();

        if (scheduleDate.isEqual(today)) {
            if (!includeLive) {
                LocalTime schedule_start_time = LocalTime.parse(schedule.getStartTime());
                LocalTime now_time = LocalTime.now();
                if (schedule_start_time.isAfter(now_time)) {    // this excludes (correctly) live items
                    res = true;
                }
            }
            else {
                LocalTime schedule_start_time = LocalTime.parse(schedule.getStartTime());
                LocalTime schedule_end_time = schedule_start_time.plus(schedule.getDurationHours(),ChronoUnit.HOURS).plus(schedule.getDurationMinutes(), ChronoUnit.MINUTES);
                LocalTime now_time = LocalTime.now();
                if (schedule_end_time.isAfter(now_time) || schedule_start_time.isAfter(now_time)) {
                    res = true;
                }
            }
        }
        return res;
    }
    public boolean isScheduledForTodayInTheFuture(ScheduleDTO scheduleDTO, boolean includeLive) {
        boolean res = false;
        ZoneId zoneId = ZoneId.of(app_zone);
        LocalDate today = LocalDate.now(zoneId);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate validateDate = LocalDate.parse(scheduleDTO.getDate(), f);

        if (validateDate.isEqual(today)) {
            if (!includeLive) {
                LocalTime schedule_start_time = LocalTime.parse(scheduleDTO.getStartTime());
                LocalTime now_time = LocalTime.now();
                if (schedule_start_time.isAfter(now_time)) { // this excludes (correctly) passed items:: Today's but not passed items
                    res = true;
                }
            }
            else {
                LocalTime schedule_start_time = LocalTime.parse(scheduleDTO.getStartTime());
                LocalTime schedule_end_time = schedule_start_time.plus(scheduleDTO.getDurationHours(),ChronoUnit.HOURS).plus(scheduleDTO.getDurationMinutes(), ChronoUnit.MINUTES);
                LocalTime now_time = LocalTime.now();
                if (schedule_end_time.isAfter(now_time) || schedule_start_time.isAfter(now_time)) {
                    res = true;
                }
            }
        }
        return res;
    }

}

