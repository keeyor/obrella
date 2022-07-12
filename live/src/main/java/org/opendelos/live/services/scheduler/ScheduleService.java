package org.opendelos.live.services.scheduler;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.opendelos.live.repository.resource.QueryResourceResults;
import org.opendelos.live.repository.resource.ResourceQuery;
import org.opendelos.live.repository.scheduler.ScheduleRepository;
import org.opendelos.live.services.opUser.OpUserService;
import org.opendelos.live.services.resource.ResourceService;
import org.opendelos.live.services.scheduledEvent.ScheduledEventService;
import org.opendelos.live.services.structure.ClassroomService;
import org.opendelos.live.services.structure.CourseService;
import org.opendelos.live.services.structure.DepartmentService;
import org.opendelos.live.services.structure.StudyProgramService;
import org.opendelos.model.calendar.Argia;
import org.opendelos.model.calendar.Period;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Person;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.resources.ScheduledEvent;
import org.opendelos.model.resources.StructureType;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.resources.dtos.ScheduledEventInfo;
import org.opendelos.model.scheduler.OverlapInfo;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.scheduler.ScheduleQuery;
import org.opendelos.model.scheduler.TimeTableResults;
import org.opendelos.model.scheduler.common.Cancellation;
import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Course;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.Institution;
import org.opendelos.model.structure.dtos.ClassroomInfo;
import org.opendelos.model.structure.dtos.CourseInfo;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    @Value("${server.servlet.context-path}")
    String app_path;
    @Value("${app.zone}")
    String app_zone;

    @Autowired
    Institution defaultInstitution;

    private final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final StudyProgramService studyProgramService;
    private final ScheduledEventService scheduledEventService;
    private final OpUserService opUserService;
    private final ClassroomService classroomService;
    private final ResourceService resourceService;


    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, DepartmentService departmentService, CourseService courseService, StudyProgramService studyProgramService, ScheduledEventService scheduledEventService, OpUserService opUserService, ClassroomService classroomService, ResourceService resourceService) {
        this.scheduleRepository = scheduleRepository;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.studyProgramService = studyProgramService;
        this.scheduledEventService = scheduledEventService;
        this.opUserService = opUserService;
        this.classroomService = classroomService;
        this.resourceService = resourceService;
    }

    public List<Schedule> findAll() {
        logger.trace("Schedule.findAll");
        return scheduleRepository.findAll();
    }

    public void deleteAll() {
        logger.trace("Schedule.deleteAll");
        try {
            scheduleRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: deleteAll:" + e.getMessage());
        }
    }

    public String create(Schedule schedule) {
        String generatedId= null;
        schedule.setId(null); // ensure that is not empty String
        try {
            Schedule nSchedule =  scheduleRepository.save(schedule);
            generatedId = nSchedule.getId();
            logger.trace(String.format("Schedule.created with id: %s:",generatedId));
        }
        catch (Exception e) {
            logger.error("error: Schedule.create:" + e.getMessage());
        }
        return generatedId;
    }

    public Schedule findById(String id) {
        logger.trace(String.format("Schedule.findById(%s)", id));
        return  scheduleRepository.findById(id).orElse(null);
    }

    public void update(Schedule schedule) {
        logger.trace(String.format("Schedule.update: %s", schedule.getId()));
        try {
            scheduleRepository.save(schedule);
        }
        catch (Exception e) {
            logger.error("error: Schedule.update:" + e.getMessage());
        }
    }

    public void delete(String id) {
        logger.trace(String.format("Schedule.delete: %s", id));
        try {
             scheduleRepository.deleteById(id);
        }
        catch (Exception e) {
            logger.error("error: Schedule.delete:" + e.getMessage());
        }
    }

    public List<Schedule> searchSchedule(ScheduleQuery scheduleQuery) {
        logger.trace("Schedule.queryTimeTable");
        return scheduleRepository.search(scheduleQuery);
    }

    public List<Schedule> searchScheduleByEditor(ScheduleQuery scheduleQuery, OoUserDetails editor, String ACCESS_TYPE) {

        List<Schedule> authorized_schedules = new ArrayList<>();
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
           // nothing more to do. Just apply the filters sent by the client
            authorized_schedules = scheduleRepository.search(scheduleQuery);
        }
        else {
            if (scheduleQuery.getType().equals("lecture")) {
                if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
                    List<String> authorized_departments = opUserService.getManagersAuthorizedUnitIdsByAccessType(editor.getId(),"scheduler");
                    ScheduleQuery manager_query = new ScheduleQuery();
                    BeanUtils.copyProperties(scheduleQuery,manager_query);
                    manager_query.setRestrictedUnitIds(authorized_departments);
                    authorized_schedules= scheduleRepository.search(manager_query);
                    //Get editor's own Schedules only if editor's department is not in authorized_departments. Otherwise they are included in authorized_schedules
                    if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER")) && !authorized_departments.contains(editor.getDepartmentId())) {
                        List<Schedule> staffMember_own_schedules= this.getStaffMemberOwnSchedules(editor.getId(),scheduleQuery);
                        if (staffMember_own_schedules != null && staffMember_own_schedules.size()>0) {
                            authorized_schedules.addAll(staffMember_own_schedules);
                        }
                    }
                }
                else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
                    ScheduleQuery supportQuery = new ScheduleQuery();
                    BeanUtils.copyProperties(scheduleQuery,supportQuery);
                    supportQuery.setSupervisorId(editor.getId());
                    List<String> supporter_authorized_personIds = opUserService.getSupporterAuthorizedPersonIdsByAccessType(editor.getId(),"scheduler");
                    scheduleQuery.setRestrictedCourseIds(supporter_authorized_personIds);
                    authorized_schedules= scheduleRepository.search(supportQuery);
                    if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
                        List<Schedule> staffMember_own_schedules= this.getStaffMemberOwnSchedules(editor.getId(),scheduleQuery);
                        if (staffMember_own_schedules != null && staffMember_own_schedules.size()>0) {
                            authorized_schedules.addAll(staffMember_own_schedules);
                        }
                    }
                    //Two lists above CANNOT contain duplicates. Self cannot be supporter of himself
                }
                else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {  //editor is StaffMember ONLY!
                    List<Schedule> staffMember_own_schedules= this.getStaffMemberOwnSchedules(editor.getId(),scheduleQuery);
                    if (staffMember_own_schedules != null && staffMember_own_schedules.size()>0) {
                        authorized_schedules.addAll(staffMember_own_schedules);
                    }
                }
            }
            else if (scheduleQuery.getType().equals("event")) {
                List<ScheduledEvent> eventList = scheduledEventService.getAuthorizedScheduledEventsByEditor(editor,ACCESS_TYPE);
                List<String> eventsIds = new ArrayList<>();
                for (ScheduledEvent scheduledEvent: eventList) {
                    eventsIds.add(scheduledEvent.getId());
                }
                scheduleQuery.setRestrictedEventIds(eventsIds);
                authorized_schedules= scheduleRepository.search(scheduleQuery);
            }
        }

        logger.info("Schedule.queryTimeTable of user: " + editor.getName() + " with access:  " + ACCESS_TYPE);
        return authorized_schedules;
    }

    public ScheduleDTO getScheduleDTO(Schedule schedule) {

        ScheduleDTO scheduleDTO = new ScheduleDTO();
        BeanUtils.copyProperties(schedule, scheduleDTO);
        if (schedule.getDepartment() != null && !schedule.getDepartment().equals("")) {
            Department department = departmentService.findById(schedule.getDepartment());
            scheduleDTO.setDepartment(new Unit(StructureType.DEPARTMENT, department.getId(),department.getTitle()));
        }
        else {
            scheduleDTO.setDepartment(new Unit(StructureType.DEPARTMENT, "",""));
        }
        if (schedule.getCourse() != null && !schedule.getCourse().equals("")) {
            Course course = courseService.findById(schedule.getCourse());
            scheduleDTO.setCourse(new CourseInfo(course.getId(),course.getTitle()));
        }
        else {
            scheduleDTO.setCourse(new CourseInfo("",""));
        }
        if (schedule.getEvent() != null && !schedule.getEvent().equals("")) {
            ScheduledEvent scheduledEvent = scheduledEventService.findById(schedule.getEvent());
            scheduleDTO.setScheduledEvent(new ScheduledEventInfo(scheduledEvent.getId(), scheduledEvent.getTitle(), scheduledEvent.getIsActive()));
        }
        else {
            scheduleDTO.setScheduledEvent(new ScheduledEventInfo("","", false));
        }
        if (schedule.getSupervisor() != null && !schedule.getSupervisor().equals("")) {
            OpUser opuser = opUserService.findById(schedule.getSupervisor());
            scheduleDTO.setSupervisor(new Person(opuser.getId(), opuser.getName(), opuser.getAffiliation()));
        }
        else {
            scheduleDTO.setSupervisor(new Person());
        }
        if (schedule.getEditor() != null && !schedule.getEditor().equals("")) {
            OpUser opuser = opUserService.findById(schedule.getEditor());
            scheduleDTO.setEditor(new Person(opuser.getId(), opuser.getName(), opuser.getAffiliation()));
        }
        else {
            scheduleDTO.setEditor(new Person());
        }
        if (schedule.getClassroom() != null) {
            Classroom classroom = classroomService.findById(schedule.getClassroom());
            if (classroom != null) {
                scheduleDTO.setClassroom(new ClassroomInfo(classroom.getId(), classroom.getName(), classroom.getCode(), classroom.getCalendar().equals("true")));
            }
            else {
                logger.error("Classroom with id:" + schedule.getClassroom() + " not found");
            }
        }
        else {
            scheduleDTO.setClassroom(new ClassroomInfo("","","", false));
        }
        if (schedule.getDate() != null) {
            LocalDate localDate = schedule.getDate();
            scheduleDTO.setDate(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate));
            //scheduleDTO.setDate(localDate);
        }
        if (schedule.getCancellations() != null && schedule.getCancellations().size() >0) {
            scheduleDTO.setCancellations(schedule.getCancellations());
        }
        return scheduleDTO;
    }

    public Schedule getScheduleFromDTO(ScheduleDTO scheduleDTO) {

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleDTO, schedule);
        if (scheduleDTO.getDepartment() != null) {
            schedule.setDepartment(scheduleDTO.getDepartment().getId());
        }
        if (scheduleDTO.getCourse() != null) {
            schedule.setCourse(scheduleDTO.getCourse().getId());
        }
        if (scheduleDTO.getScheduledEvent() != null && !scheduleDTO.getScheduledEvent().getId().equals("")) {
            schedule.setEvent(scheduleDTO.getScheduledEvent().getId());
        }
        if (scheduleDTO.getSupervisor() != null) {
            schedule.setSupervisor(scheduleDTO.getSupervisor().getId());
        }
        if (scheduleDTO.getClassroom() != null) {
            schedule.setClassroom(scheduleDTO.getClassroom().getId());
        }
        if (scheduleDTO.getDate() != null) {
            try {
                schedule.setDate(LocalDate.parse(scheduleDTO.getDate(), DateTimeFormatter.ISO_DATE_TIME));
            }
            catch (DateTimeParseException e) {
                schedule.setDate(LocalDate.parse(scheduleDTO.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        }
        if (scheduleDTO.getEditor() != null) {
            schedule.setEditor(scheduleDTO.getEditor().getId());
        }
        return schedule;
    }

    public Argia DateInPauses(LocalDate date, List<CustomPause> pauses) {

        Argia argia_date = null;
        boolean dateInPauses = false;
        for (CustomPause customPause: pauses) {
            for (Argia argia : customPause.getArgies().getArgia()) {
                LocalDate argia_startDate  = LocalDate.parse(argia.getStartDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate argia_endDate  = LocalDate.parse(argia.getEndDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (    (date.isAfter(argia_startDate) && date.isBefore(argia_endDate)) ||
                        (date.isEqual(argia_startDate) || date.isEqual(argia_endDate))
                    ) {
                        dateInPauses = true;
                        argia_date = argia;
                        break;
                }
            }
            if (dateInPauses) break;
        }
        return argia_date;
    }

    public Cancellation DateIsCanceled(LocalDate date, List<Cancellation> cancellations) {

        Cancellation canceled_date = null;
        if (cancellations == null || cancellations.size() == 0) {
            return null;
        }
        for (Cancellation cancellation: cancellations) {
              if (cancellation.getDate().isEqual(date)) {
                  canceled_date = cancellation;
                  break;
              }
        }
        return canceled_date;
    }

    public TimeTableResults calculateExactDaysOfRegularSchedule(Schedule schedule) {

        String institution_id = defaultInstitution.getId();

        TimeTableResults timeTableResults = new TimeTableResults();
        StringBuilder message_pauses = new StringBuilder();
        StringBuilder message_cancellations = new StringBuilder();
        StringBuilder message_overlaps = new StringBuilder();

        List<ScheduleDTO> scheduleDTOS = new ArrayList<>();

        //if (schedule.isEnabled()) {
            //* ONeTIME
            //* Events and onetime lectures DO NOT consider Pauses
            if (schedule.getType().equals("event") || schedule.getRepeat().equals("onetime")) {
                ScheduleDTO scheduleDTO = this.getScheduleDTO(schedule);
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate validateDate = LocalDate.parse(scheduleDTO.getDate(), f);
                //LocalDate validateDate = scheduleDTO.getDate();
                Cancellation isCanceled = this.DateIsCanceled(validateDate, schedule.getCancellations());
                if (isCanceled != null) {
                    this.setCancellationInfo(scheduleDTO,isCanceled);
                    if (message_pauses.length() == 0) {
                        message_pauses.append("Υπάρχει τουλάχιστον μια ακύρωση στις επιλεγμένες Ημερομηνίες");
                    }
                }
                scheduleDTOS.add(scheduleDTO);
            }
            else { //* REGULAR: Compute DAyS in Period
                String academicYear = schedule.getAcademicYear();
                String departmentId = schedule.getDepartment();
                Course course = courseService.findById(schedule.getCourse());
                //Find effective Period
                Period schedulePeriod;
                if (course.getStudyProgramId() != null && !course.getStudyProgramId().equals("")  && !course.getStudyProgramId().equals("program_default")) {
                    schedulePeriod = studyProgramService
                            .getStudyPeriod(course.getStudyProgramId(), departmentId, institution_id, academicYear, schedule
                                    .getPeriod());
                }
                else {
                    schedulePeriod = departmentService
                            .getDepartmentPeriod(departmentId, institution_id, academicYear, schedule.getPeriod());
                }
                if (schedulePeriod != null) {
                    /* Get Argies aka Pauses */
                    List<CustomPause> departmentPauses = departmentService
                            .getDepartmentPauses(departmentId, institution_id);

                    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate _start_date = LocalDate.parse(schedulePeriod.getStartDate(), f);
                    LocalDate _end_date = LocalDate.parse(schedulePeriod.getEndDate(), f);

                    // LocalDate now = LocalDate.now();
                    // if (_start_date.isBefore(now)) {
                    //     _start_date = now;
                    // }

                    DayOfWeek rDayOfWeek = schedule.getDayOfWeek();
                    while (!_start_date.getDayOfWeek().equals(rDayOfWeek)) {
                        _start_date = _start_date.plusDays(1);
                    }
                    LocalDate validateDate = _start_date;

                    while (validateDate.isBefore(_end_date.plus(1, ChronoUnit.DAYS))) {
                        Argia isArgia = this.DateInPauses(validateDate, departmentPauses);
                        ScheduleDTO scheduleDTO = this.getScheduleDTO(schedule);
                        if (scheduleDTO != null) {
                            scheduleDTO.setRepeat("regular");
                            scheduleDTO.setDate(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(validateDate));
                            //scheduleDTO.setDate(validateDate);
                            scheduleDTO.setFromDate(schedulePeriod.getStartDate());
                            scheduleDTO.setToDate(schedulePeriod.getEndDate());
                            if (isArgia != null) {
                                 this.setPauseInfo(scheduleDTO,isArgia);
                                if (message_cancellations.length() == 0) {
                                    message_cancellations.append("Υπάρχει τουλάχιστον μια αργία/παύση στις επιλεγμένες Ημερομηνίες");
                                }
                            }
                            else {
                                Cancellation isCanceled = this.DateIsCanceled(validateDate, schedule.getCancellations());
                                if (isCanceled != null) {
                                     this.setCancellationInfo(scheduleDTO,isCanceled);
                                    if (message_pauses.length() == 0) {
                                        message_pauses.append("Υπάρχει τουλάχιστον μια ακύρωση στις επιλεγμένες Ημερομηνίες");
                                    }
                                }
                                else {
                                   OverlapInfo overlapInfo = this.checkScheduleDateOverlapAgainstOneTimeSchedules(schedule, validateDate);
                                   if (overlapInfo != null) {
                                       this.setOverlapInfo(scheduleDTO,overlapInfo);
                                       if (message_overlaps.length() == 0) {
                                           message_overlaps.append("Υπάρχει τουλάχιστον μια απενεργοποίηση λόγω αλληλοκάλυψης στις επιλεγμένες Ημερομηνίες");
                                       }
                                   }
                                }
                            }
                            scheduleDTOS.add(scheduleDTO);
                        }
                        validateDate = validateDate.plus(7, ChronoUnit.DAYS);
                    }
                }
            }
        //}
        timeTableResults.setResults(scheduleDTOS);
        timeTableResults.setMessage_pauses(message_pauses.toString());
        timeTableResults.setMessage_cancellations(message_cancellations.toString());
        timeTableResults.setMessage_overlaps(message_overlaps.toString());

        return timeTableResults;
    }

    public OverlapInfo checkRegularScheduleOverlapsAgainstOtherRegularSchedules(Schedule schedule, boolean excludeDisabled) {

        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setClassroomId(schedule.getClassroom());
        scheduleQuery.setDayOfWeek(schedule.getDayOfWeek().toString());
        scheduleQuery.setRepeat("regular");
        if (excludeDisabled) {
            // if this is true, schedule dates are checked over enabled only
            // DEFAULT: false => do not allow scheduling on disabled schedules. Schedule occupies classroom either way
            scheduleQuery.setEnabled("true");
        }
        scheduleQuery.setYear(schedule.getAcademicYear());
        scheduleQuery.setPeriod(schedule.getPeriod());

        List<Schedule> sameDayAndDayOfWeekSchedule = this.searchSchedule(scheduleQuery);
        Schedule overlap_schedule = null;
        OverlapInfo overlapInfo = null;
        boolean  overlap = false;

        LocalTime _saved_start_time = null;
        LocalTime _saved_end_time = null;
        for (Schedule savedSchedule: sameDayAndDayOfWeekSchedule) {
            if (savedSchedule.getId().equals(schedule.getId())) {
                continue; //skip itself
            }
            _saved_start_time = LocalTime.parse(savedSchedule.getStartTime());
            _saved_end_time = _saved_start_time.plus(savedSchedule.getDurationHours(), ChronoUnit.HOURS).plus(savedSchedule.getDurationMinutes(), ChronoUnit.MINUTES);

            LocalTime validate_start_time = LocalTime.parse(schedule.getStartTime());
            LocalTime validate_end_time = validate_start_time.plus(schedule.getDurationHours(), ChronoUnit.HOURS).plus(schedule.getDurationMinutes(), ChronoUnit.MINUTES);

            if (validate_start_time.isBefore(_saved_start_time)) {
                if (validate_end_time.isBefore(_saved_start_time) || validate_end_time.equals(_saved_start_time)) {
                    overlap = false;
                }
                else {
                    overlap = true;
                    overlap_schedule = savedSchedule;
                    break;
                }
            }
            else if (validate_start_time.equals(_saved_start_time)) {
                overlap = true;
                overlap_schedule = savedSchedule;
                break;
            }
            else if (validate_start_time.isAfter(_saved_start_time) && validate_start_time.isBefore(_saved_end_time)) {
                overlap = true;
                overlap_schedule = savedSchedule;
                break;
            }
            else if (validate_start_time.equals(_saved_end_time)) {
                overlap = false;
            }
            else if (validate_start_time.isAfter(_saved_end_time)) {
                overlap = false;
            }
        }
        if (overlap) {
            Course overlap_course = courseService.findById(overlap_schedule.getCourse());
            overlapInfo = new OverlapInfo();
            overlapInfo.setMsg("Η αποθήκευση απέτυχε! Υπάρχει αλληλοκάλυψη με ΤΑΚΤΙΚΗ ΜΕΤΑΔΟΣΗ με στοιχεία");
            overlapInfo.setTitle(overlap_course.getTitle());
            overlapInfo.setType("lecture");
            overlapInfo.setRepeat("regular");
            overlapInfo.setDayOfWeek(overlap_schedule.getDayOfWeek());
            overlapInfo.setStartTime(_saved_start_time.toString());
            overlapInfo.setEndTime(_saved_end_time.toString());
        }

        return overlapInfo;
    }
    public OverlapInfo checkScheduleDateOverlapAgainstOneTimeSchedules(Schedule schedule, LocalDate validateDate) {

        // CHECK FOR OVERLAP OF Date (Regular  or OneTime) over Onetime schedules
        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setClassroomId(schedule.getClassroom());
        scheduleQuery.setDate(validateDate);
        scheduleQuery.setRepeat("onetime");

        //Allow override of disabled onetime schedules
        scheduleQuery.setEnabled("true");

        scheduleQuery.setYear(schedule.getAcademicYear());
        List<Schedule> sameDaySchedule = this.searchSchedule(scheduleQuery);
        OverlapInfo  overlapInfo = null;

        for (Schedule savedSchedule: sameDaySchedule) {
            if (savedSchedule.getId().equals(schedule.getId())) {
                continue; // skip itself
            }
            if (savedSchedule.isEnabled()) {
                LocalTime validate_start_time = LocalTime.parse(schedule.getStartTime());
                LocalTime validate_end_time = validate_start_time.plus(schedule.getDurationHours(), ChronoUnit.HOURS)
                        .plus(schedule.getDurationMinutes(), ChronoUnit.MINUTES);
                overlapInfo = this.getOverlappingInfo(validate_start_time, validate_end_time, savedSchedule);
                if (overlapInfo != null) {
                    break;
                }
            }
        }
        return overlapInfo;
    }
    public OverlapInfo checkOneTimeScheduleOverlapAgainstRegularSchedules(Schedule schedule, String iid, boolean excludeDisabled) {

        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setClassroomId(schedule.getClassroom());
        scheduleQuery.setDayOfWeek(schedule.getDayOfWeek().toString());
        scheduleQuery.setRepeat("regular");
        if (excludeDisabled) {
            // if this is true, schedule date is checked over enabled only
            // DEFAULT: false =>  do not allow onetime scheduling on disabled regular schedules. Schedule occupies classroom either way
            scheduleQuery.setEnabled("true");
        }
        scheduleQuery.setYear(schedule.getAcademicYear());

        List<Schedule> sameDaySchedule = this.searchSchedule(scheduleQuery);
        OverlapInfo overlapInfo = null;
        for (Schedule savedSchedule: sameDaySchedule) {
                List<ScheduleDTO> exactDates = this.calculateExactDaysOfRegularSchedule(savedSchedule).getResults();
                for (ScheduleDTO scheduleDTO: exactDates) {
                    LocalDate exactDate = LocalDate.parse(scheduleDTO.getDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (exactDate.isEqual(schedule.getDate())) {
                        if (scheduleDTO.getArgia() != null || scheduleDTO.getCancellation() != null) {
                            continue;   //* we can assign onetime in place of argia or cancellation. It's a matter of decision!
                        }
                        LocalTime validate_start_time = LocalTime.parse(schedule.getStartTime());
                        LocalTime validate_end_time = validate_start_time
                                .plus(schedule.getDurationHours(), ChronoUnit.HOURS)
                                .plus(schedule.getDurationMinutes(), ChronoUnit.MINUTES);

                        if (scheduleDTO.getOverlapInfo() != null) {
                            break; //DO NOT CHECK:: IT WILL FIND THE ORIGINAL THAT OCCUPIES THE ROOM LATER IN THE LOOP
                        }
                        Schedule schedule1 = this.getScheduleFromDTO(scheduleDTO);
                        overlapInfo = this.getOverlappingInfo(validate_start_time, validate_end_time, schedule1);
                        if (overlapInfo != null) {
                            break;
                        }
                    }
                }
                if (overlapInfo != null) {
                    break;
                }
        }
        return  overlapInfo;
    }

    public List<ScheduleDTO> computeTodaysSchedule(String currentAcademicYear) {

        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setYear(currentAcademicYear);
        scheduleQuery.setEnabled("true");
        LocalDate today = LocalDate.now();
        scheduleQuery.setFromDate(today);
        scheduleQuery.setToDate(today);
        return computeScheduleInDateRange(scheduleQuery);
    }

    public List<ScheduleDTO> computeScheduleInDateRange(ScheduleQuery scheduleQuery) {

        List<ScheduleDTO> inRangeResults = new ArrayList<>();

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fromDate = scheduleQuery.getFromDate();
        LocalDate toDate = scheduleQuery.getToDate();

        //> searchSchedule ignores fromDate+toDate fields. Should calculate ourselves.
        //> try using other query criteria to minimize  the size of searchSchedule query results...
        long startAt = System.currentTimeMillis();
        List<Schedule> searchResults = this.searchSchedule(scheduleQuery);

        // CODE11 : Remove From searchResults, schedules that are assigned to disabled or unknown classrooms and to de-activated ScheduledEvents
        List<Schedule> invalidList = new ArrayList<>();
        for (Schedule schedule: searchResults) {
            String classroomId = schedule.getClassroom();
            int classroom_status = classroomService.getClassroomStatus(classroomId);
            if (classroom_status == 1) {
                invalidList.add(schedule);
            }
            else if (classroom_status == 2) {
                invalidList.add(schedule);
            }
            else {
                if (schedule.getType().equals("event")) {
                    ScheduledEvent scheduledEvent = scheduledEventService.findById(schedule.getEvent());
                    if (scheduledEvent == null || !scheduledEvent.getIsActive()) {
                        invalidList.add(schedule);
                    }
                }
            }
        }
        logger.warn("Removing " + invalidList.size() + " schedules, due to invalid or de-activated rooms and events");
        searchResults.removeAll(invalidList);

        //> add schedules in date range, ignore others
        for (Schedule schedule: searchResults) {
            if (schedule.getRepeat().equals("onetime")) {
                 if (schedule.getCancellations() != null && schedule.getCancellations().size()>0) {
                     continue;
                 }
                 int startHour = Integer.parseInt(schedule.getStartTime().substring(0,2));
                 int startMinute = Integer.parseInt(schedule.getStartTime().substring(3,5));
                 LocalDateTime validateDate = schedule.getDate().atTime(startHour,startMinute);
                 if (validateDate.isBefore(fromDate.atTime(0,0)) || validateDate.isAfter(toDate.atTime(23,59))) {
                     continue;
                 }
                 else {
                     ScheduleDTO add_schedule = this.getScheduleDTO(schedule);
                     inRangeResults.add(add_schedule);
                 }
            }
            else {  //regular
                TimeTableResults timeTableResults = this.calculateExactDaysOfRegularSchedule(schedule);
                if (timeTableResults != null && timeTableResults.getResults() != null && timeTableResults.getResults().size()>0) {
                    List<ScheduleDTO> exactSchedules = timeTableResults.getResults();
                    for (ScheduleDTO scheduleDTO: exactSchedules) {
                        LocalDate validateDate = LocalDate.parse(scheduleDTO.getDate(), f);
                        if (scheduleDTO.getArgia() != null || scheduleDTO.getCancellation() != null || scheduleDTO.getOverlapInfo() != null){
                            logger.trace(validateDate + " is Argia or Canceled");
                            continue;
                        }
                        if (validateDate.isBefore(fromDate) || validateDate.isAfter(toDate)) {
                            logger.trace(validateDate + " is out of range:" + fromDate + " to " + toDate);
                            continue;
                        }
                        else {
                            logger.trace(validateDate + " is IN RANGE");
                            inRangeResults.add(scheduleDTO);
                        }
                    }
                }
            }  // < else regular
        }  //< for schedule in search results

        long endAllAt   = System.currentTimeMillis();
        logger.debug("Time:" + (endAllAt - startAt));
        return inRangeResults;
    }

    public QueryResourceResults getLiveResourceListFromTodaysSchedule(List<Resource> todaysResourceList) {

        Instant _now =  Instant.now();
        List<Resource> liveResourceList = new ArrayList<>();
        for (Resource todayResource: todaysResourceList) {
            Instant startDateTime = todayResource.getDate();
            int broadcast_hour = Integer.parseInt(todayResource.getRealDuration().substring(0,2));
            int broadcast_min = Integer.parseInt(todayResource.getRealDuration().substring(3,5));
            Instant endDateTime	  = startDateTime.plus(broadcast_hour, ChronoUnit.HOURS).plus(broadcast_min, ChronoUnit.MINUTES);
            if (startDateTime.isBefore(_now) && endDateTime.isAfter(_now)) {
                liveResourceList.add(todayResource);
            }
        }
        QueryResourceResults liveResources = new QueryResourceResults();
        liveResources.setSearchResultList(liveResourceList);
        liveResources.setLimit(100);
        liveResources.setSkip(0);
        liveResources.setSort("date");
        liveResources.setDirection("asc");
        liveResources.setTotalResults(liveResourceList.size());

        return  liveResources;
    }

    public void cancelRegularScheduleRemainingDates(String id, String reason) {

        Schedule schedule = scheduleRepository.findById(id).orElse(null);
        List<Cancellation> cancellations;

        if (schedule != null) {
            cancellations = schedule.getCancellations();
            if (cancellations == null) { cancellations = new ArrayList<>();}

            TimeTableResults timeTableResults = this
                    .calculateExactDaysOfRegularSchedule(schedule);

            for (ScheduleDTO scheduleDTO: timeTableResults.getResults()) {
                if (scheduleDTO.getArgia() == null && scheduleDTO.getCancellation() == null) {
                     LocalDate scheduleDTO_date = LocalDate.parse(scheduleDTO.getDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                     int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
                     int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
                     LocalDateTime scheduleDTO_datetime = scheduleDTO_date.atTime(broadcast_hour,broadcast_min);
                     LocalDateTime _now = LocalDateTime.now();
                     if (scheduleDTO_datetime.isAfter(_now)) {
                            //add cancellation on future date
                            Cancellation _new_cancellation = new Cancellation();
                            // ASK USER FOR REASON AND DISPLAY IT on SCHEDULE TABLE
                            _new_cancellation.setTitle(reason);
                            _new_cancellation.setDate(LocalDate.parse(scheduleDTO.getDate()));
                            cancellations.add(_new_cancellation);
                     }
                }
            }
            if (cancellations.size() > 0) {
                schedule.setCancellations(cancellations);
                schedule.setEnabled(false);
                //As of now (25-02-21) I don't have Cache on schedules. So this call does not matter. If you create cache this is unacceptable. move somewhere else or clean cache manually
                this.update(schedule);
            }
        }
    }
    public void unsetScheduleRemainingDates(String id) {

        Schedule schedule = scheduleRepository.findById(id).orElse(null);
        List<Cancellation> cancellations;

        if (schedule != null) {
            cancellations = schedule.getCancellations();
            if (!schedule.isEnabled() && cancellations != null && cancellations.size()>0) {
                List<Cancellation> remove_list = new ArrayList<>();
                for (Cancellation cancellation: schedule.getCancellations()) {
                        LocalDate cancellation_date = cancellation.getDate();
                        int broadcast_hour = Integer.parseInt(schedule.getStartTime().substring(0,2));
                        int broadcast_min = Integer.parseInt(schedule.getStartTime().substring(3,5));
                        LocalDateTime cancellation_datetime = cancellation_date.atTime(broadcast_hour,broadcast_min);
                        LocalDateTime _now = LocalDateTime.now();
                        if (cancellation_datetime.isAfter(_now)) {
                            //add cancellation to remove_list
                            remove_list.add(cancellation);
                        }
                }
                if (remove_list.size() > 0) {
                    cancellations.removeAll(remove_list);
                    schedule.setCancellations(cancellations);
                    schedule.setEnabled(true);
                    //As of now (25-02-21) I don't have Cache on schedules. So this call does not matter. If you create cache this is unacceptable. move somewhere else or clean cache manually
                    this.update(schedule);
                }
            }
        }
    }

    private List<Schedule> getStaffMemberOwnSchedules(String staffMemberId, ScheduleQuery scheduleQuery) {
        ScheduleQuery staffMember_query = new ScheduleQuery();
        BeanUtils.copyProperties(scheduleQuery,staffMember_query);
        staffMember_query.setSupervisorId(staffMemberId);
        return scheduleRepository.search(staffMember_query);
    }

    private OverlapInfo getOverlappingInfo(LocalTime validate_start_time, LocalTime validate_end_time, Schedule savedSchedule) {
        boolean  overlap = true;
        Schedule overlap_schedule = null;
        OverlapInfo  overlapInfo = null;

        LocalTime _saved_start_time = LocalTime.parse(savedSchedule.getStartTime());
        LocalTime _saved_end_time = _saved_start_time.plus(savedSchedule.getDurationHours(), ChronoUnit.HOURS).plus(savedSchedule.getDurationMinutes(), ChronoUnit.MINUTES);

        if (validate_start_time.isBefore(_saved_start_time)) {
            if (validate_end_time.isBefore(_saved_start_time) || validate_end_time.equals(_saved_start_time)) {
                overlap = false;
            }
            else {
                overlap = true;
                overlap_schedule = savedSchedule;
            }
        }
        else if (validate_start_time.equals(_saved_start_time)) {
            overlap_schedule = savedSchedule;
        }
        else if (validate_start_time.isAfter(_saved_start_time) && validate_start_time.isBefore(_saved_end_time)) {
            overlap_schedule = savedSchedule;
        }
        else if (validate_start_time.equals(_saved_end_time)) {
            overlap = false;
        }
        else if (validate_start_time.isAfter(_saved_end_time)) {
            overlap = false;
        }

        if (overlap && overlap_schedule != null) {
                overlapInfo = new OverlapInfo();
            if (overlap_schedule.getCourse() != null && !overlap_schedule.getCourse().equals("")) {
                Course overlap_course = courseService.findById(overlap_schedule.getCourse());
                if (overlap_schedule.getRepeat().equals("regular")) {
                    overlapInfo.setMsg("Υπάρχει αλληλοκάλυψη με TAKTIKH ΜΕΤΑΔΟΣΗ Διάλεξης");
                }
                else {
                    overlapInfo.setMsg("Υπάρχει αλληλοκάλυψη με EKTAKTH ΜΕΤΑΔΟΣΗ Διάλεξης");
                }
                overlapInfo.setTitle("Διάλεξη: " + overlap_course.getTitle());
                overlapInfo.setType("lecture");
                overlapInfo.setRepeat("onetime");
            }
            else if (overlap_schedule.getEvent() != null) {
                ScheduledEvent overlap_event = scheduledEventService.findById(overlap_schedule.getEvent());
                overlapInfo.setMsg("Υπάρχει αλληλοκάλυψη με ΜΕΤΑΔΟΣΗ Εκδήλωσης");
                overlapInfo.setTitle("Εκδήλωση: " + overlap_event.getTitle());
                overlapInfo.setType("lecture");
                overlapInfo.setRepeat("onetime");

            }
            overlapInfo.setDayOfWeek(overlap_schedule.getDayOfWeek());
            overlapInfo.setStartTime(_saved_start_time.toString());
            overlapInfo.setEndTime(_saved_end_time.toString());
        }

        return overlapInfo;
    }
    private void setCancellationInfo(ScheduleDTO scheduleDTO, Cancellation cancellation) {
        scheduleDTO.setCancellation(cancellation);
        scheduleDTO.setBroadcast(false);
        scheduleDTO.setAccess("canceled");
        scheduleDTO.setRecording(false);
        scheduleDTO.setPublication("canceled");
        scheduleDTO.setEnabled(false);
    }
    private void setPauseInfo(ScheduleDTO scheduleDTO, Argia argia) {
        scheduleDTO.setArgia(argia);
        scheduleDTO.setBroadcast(false);
        scheduleDTO.setAccess("pause");
        scheduleDTO.setRecording(false);
        scheduleDTO.setPublication("pause");
        scheduleDTO.setEnabled(false);
    }
    private void setOverlapInfo(ScheduleDTO scheduleDTO, OverlapInfo overlapInfo) {
        scheduleDTO.setOverlapInfo(overlapInfo);
        scheduleDTO.setBroadcast(false);
        scheduleDTO.setAccess("overlap");
        scheduleDTO.setRecording(false);
        scheduleDTO.setPublication("overlap");
        scheduleDTO.setEnabled(false);
    }
    private void removeScheduleFromTodaysProgrammeMessage(ScheduleDTO scheduleDTO, String cause) {

        if (scheduleDTO.getCourse() != null && !scheduleDTO.getCourse().getId().equals("")) {
            logger.info("Remove Lecture from broadcast schedule:" + scheduleDTO.getCourse().getTitle() + " reason: " +  cause);
        }
        if (scheduleDTO.getScheduledEvent() != null && !scheduleDTO.getScheduledEvent().getId().equals("")) {
            logger.info("Remove Event from broadcast schedule:" + scheduleDTO.getScheduledEvent().getTitle() + " Αίτία: " +  cause);
        }
    }


    public List<Resource> getTodaysProgrammeFromDatabase() {

        ResourceQuery resourceQuery = new ResourceQuery();
        resourceQuery.setCollectionName("Scheduler.Live");
        resourceQuery.setSort("date");
        resourceQuery.setDirection("asc");
        QueryResourceResults queryResourceResults;
        queryResourceResults = resourceService.searchPageableLectures(resourceQuery);
        resourceQuery.setTotalResults(queryResourceResults.getTotalResults());

        return queryResourceResults.getSearchResultList();
    }

}

