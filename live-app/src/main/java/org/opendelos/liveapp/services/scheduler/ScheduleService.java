package org.opendelos.liveapp.services.scheduler;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.opendelos.liveapp.repository.scheduler.ScheduleRepository;
import org.opendelos.liveapp.services.opUser.OpUserService;
import org.opendelos.liveapp.services.scheduledEvent.ScheduledEventService;
import org.opendelos.liveapp.services.structure.ClassroomService;
import org.opendelos.liveapp.services.structure.CourseService;
import org.opendelos.liveapp.services.structure.DepartmentService;
import org.opendelos.liveapp.services.structure.StudyProgramService;
import org.opendelos.model.calendar.Argia;
import org.opendelos.model.calendar.Period;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.repo.QueryResourceResults;
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
import org.opendelos.model.users.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    @Autowired
    Institution defaultInstitution;

    @Autowired
    String currentAcademicYear;

    private final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final StudyProgramService studyProgramService;
    private final ScheduledEventService scheduledEventService;
    private final OpUserService opUserService;
    private final ClassroomService classroomService;


    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, DepartmentService departmentService, CourseService courseService, StudyProgramService studyProgramService, ScheduledEventService scheduledEventService, OpUserService opUserService, ClassroomService classroomService) {
        this.scheduleRepository = scheduleRepository;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.studyProgramService = studyProgramService;
        this.scheduledEventService = scheduledEventService;
        this.opUserService = opUserService;
        this.classroomService = classroomService;
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
            logger.info(String.format("Schedule.created with id: %s:",generatedId));
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
        logger.info(String.format("Schedule.update: %s", schedule.getId()));
        try {
            scheduleRepository.save(schedule);
        }
        catch (Exception e) {
            logger.error("error: Schedule.update:" + e.getMessage());
        }
    }

    public void updateWithNoChangeTrigger(Schedule schedule) {
        logger.trace(String.format("Schedule.update: %s", schedule.getId()));
        try {
            scheduleRepository.save(schedule);
        }
        catch (Exception e) {
            logger.error("error: Schedule.update:" + e.getMessage());
        }
    }

    public void delete(String id) {
        logger.info(String.format("Schedule.delete: %s", id));
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

        scheduleQuery.setManagerId(editor.getId());
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFFMEMBER"))) {
            scheduleQuery.setStaffMember(true);
        }
        if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SA"))) {
            scheduleQuery.setSA(true);
        }
        else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            scheduleQuery.setManager(true);
            List<String> authorizedUnits = opUserService.getManagersAuthorizedDepartmentIdsByAccessType(editor.getId(),"scheduler");
            scheduleQuery.setAuthorizedUnitIds(authorizedUnits);
        }
        else if (editor.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPPORT"))) {
            scheduleQuery.setSupport(true);
            List<UserAccess.UserRights.CoursePermission> editors_course_support;
            List<UserAccess.UserRights.EventPermission> editors_event_support;

            editors_course_support = opUserService.getManagersCoursePermissionsByAccessType(editor.getId(),"scheduler");
            editors_event_support = opUserService.getManagersEventPermissionsByAccessType(editor.getId(),"scheduler");
            scheduleQuery.setAuthorized_courses(editors_course_support);
            scheduleQuery.setAuthorized_events(editors_event_support);
        }
        try {
            authorized_schedules = scheduleRepository.search(scheduleQuery);
        }
        catch (Exception ignored) {}   // on empty query this might throw error


        logger.trace("Schedule.queryTimeTable of user: " + editor.getName() + " with access:  " + ACCESS_TYPE);
        return authorized_schedules;
    }

    public boolean ApproveScheduledItemEdit(OoUserDetails editor, String resourceId) {

        if (resourceId == null || resourceId.trim().equals("")) {
            return true;
        }
        /* Authorize Edit */
        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setType("_all");
        List<Schedule> scheduleList = this.searchScheduleByEditor(scheduleQuery, editor, "scheduler");

        boolean authorize = false;
        for (Schedule schedule: scheduleList) {
            if (schedule.getId().equals(resourceId)) {
                authorize = true;
                break;
            }
        }
        return authorize;
    }

    public boolean ApproveTodaysScheduledItemEdit(OoUserDetails editor, String resourceId) {

        /* Authorize Edit */
        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setDayOfWeek(LocalDate.now().getDayOfWeek().toString());
        scheduleQuery.setYear(currentAcademicYear);
        scheduleQuery.setType("_all");
        List<Schedule> scheduleList = this.searchScheduleByEditor(scheduleQuery, editor, "scheduler");

        boolean authorize = false;
        for (Schedule schedule: scheduleList) {
            if (schedule.getId().equals(resourceId)) {
                authorize = true;
                break;
            }
        }
        return authorize;
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
            if (scheduledEvent != null) {
                if (scheduledEvent.getResponsiblePerson() != null && scheduledEvent.getResponsiblePerson().getName() != null) {
                    scheduleDTO.setScheduledEvent(new ScheduledEventInfo(scheduledEvent.getId(), scheduledEvent.getTitle(), scheduledEvent.getResponsiblePerson().getName()));
                }
                else {
                    scheduleDTO.setScheduledEvent(new ScheduledEventInfo(scheduledEvent.getId(), scheduledEvent.getTitle()));
                }
            }
            else {
                scheduleDTO.setScheduledEvent(new ScheduledEventInfo("-1","Άγνωστη Εκδήλωση"));
            }
        }
        else {
            scheduleDTO.setScheduledEvent(new ScheduledEventInfo("",""));
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
            if (opuser == null) {
                return null;
            }
            scheduleDTO.setEditor(new Person(opuser.getId(), opuser.getName(), opuser.getAffiliation()));
        }
        else {
            scheduleDTO.setEditor(new Person());
        }
        if (schedule.getClassroom() != null) {
            Classroom classroom = classroomService.findById(schedule.getClassroom());
            if (classroom != null) {
                scheduleDTO.setClassroom(new ClassroomInfo(classroom.getId(), classroom.getName(), classroom.getCode()));
            }
            else {
                logger.error("Classroom with id:" + schedule.getClassroom() + " not found");
            }
        }
        else {
            scheduleDTO.setClassroom(new ClassroomInfo("","",""));
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
                LocalDate localDate = LocalDate.parse(scheduleDTO.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                //System.out.println("Saved DateTime:" + localDate);
                schedule.setDate(localDate);
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

    public TimeTableResults calculateExactDaysOfRegularSchedule(Schedule schedule, boolean cancelLive) {

        String institution_id = defaultInstitution.getId();

        TimeTableResults timeTableResults = new TimeTableResults();
        StringBuilder message_pauses = new StringBuilder();
        StringBuilder message_cancellations = new StringBuilder();
        StringBuilder message_overlaps = new StringBuilder();

        List<ScheduleDTO> scheduleDTOS = new ArrayList<>();

        if (schedule.getType().equals("event") || schedule.getRepeat().equals("onetime")) {
                ScheduleDTO scheduleDTO = this.getScheduleDTO(schedule);
                if (scheduleDTO != null) {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate validateDate = LocalDate.parse(scheduleDTO.getDate(), f);

                    Cancellation isCanceled = this.DateIsCanceled(validateDate, schedule.getCancellations());
                    if (isCanceled != null) {
                        this.setCancellationInfo(scheduleDTO, isCanceled);
                        if (message_pauses.length() == 0) {
                            message_pauses.append("Υπάρχει τουλάχιστον μια ακύρωση στις επιλεγμένες Ημερομηνίες");
                        }
                    }
                    if (isCanceled == null && cancelLive) {
                        //check if this should be live right now! and set as cancelled
                        if (this.isScheduledForNow(scheduleDTO)) {
                            Cancellation cancellation_due_to_live = new Cancellation();
                            cancellation_due_to_live.setDate(validateDate);
                            cancellation_due_to_live.setTitle("...στο παρελθόν κατά τη δημιουργία");
                            this.setCancellationInfo(scheduleDTO, cancellation_due_to_live);
                            if (message_pauses.length() == 0) {
                                message_pauses.append("Υπάρχει τουλάχιστον μια ακύρωση στις επιλεγμένες Ημερομηνίες");
                            }
                        }
                    }
                    scheduleDTOS.add(scheduleDTO);
                }
            }
        else { //* REGULAR: Compute DAyS in Period
                String academicYear = schedule.getAcademicYear();
                String departmentId = schedule.getDepartment();
                Course course = courseService.findById(schedule.getCourse());
                if (course != null) {
                    //Find effective Period
                    Period schedulePeriod;
                    if (course.getStudyProgramId() != null && !course.getStudyProgramId()
                            .equals("") && !course.getStudyProgramId().equals("program_default")) {
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
                                scheduleDTO.setFromDate(schedulePeriod.getStartDate());
                                scheduleDTO.setToDate(schedulePeriod.getEndDate());
                                if (isArgia != null) {
                                    this.setPauseInfo(scheduleDTO, isArgia);
                                    if (message_cancellations.length() == 0) {
                                        message_cancellations.append("Υπάρχει τουλάχιστον μια αργία/παύση στις επιλεγμένες Ημερομηνίες");
                                    }
                                }
                                else {
                                    OverlapInfo overlapInfo = null;
                                    Cancellation isCanceled = this.DateIsCanceled(validateDate, schedule.getCancellations());
                                    if (isCanceled != null) {
                                        this.setCancellationInfo(scheduleDTO, isCanceled);
                                        if (message_pauses.length() == 0) {
                                            message_pauses.append("Υπάρχει τουλάχιστον μια ακύρωση στις επιλεγμένες Ημερομηνίες");
                                        }
                                    }
                                    else {
                                        overlapInfo = this.checkScheduleDateOverlapAgainstOneTimeSchedules(schedule, validateDate);
                                        if (overlapInfo != null) {
                                            this.setOverlapInfo(scheduleDTO, overlapInfo);
                                            if (message_overlaps.length() == 0) {
                                                message_overlaps.append("Υπάρχει τουλάχιστον μια απενεργοποίηση λόγω αλληλοκάλυψης στις επιλεγμένες Ημερομηνίες");
                                            }
                                        }
                                    }
                                    if (isCanceled == null && overlapInfo == null && cancelLive) {
                                        //check if this should be live right now! and set as cancelled
                                        if (this.isScheduledForNow(scheduleDTO)) {
                                            Cancellation cancellation_due_to_live = new Cancellation();
                                            cancellation_due_to_live.setDate(validateDate);
                                            cancellation_due_to_live.setTitle("...στο παρελθόν κατά τη δημιουργία");
                                            this.setCancellationInfo(scheduleDTO, cancellation_due_to_live);
                                            if (message_pauses.length() == 0) {
                                                message_pauses.append("Υπάρχει τουλάχιστον μια ακύρωση στις επιλεγμένες Ημερομηνίες");
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
                else {
                    logger.warn("Course with id:" + schedule.getCourse() + " not found. Skipping");
                }
        }

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
        boolean cancelLive = false;
        for (Schedule savedSchedule: sameDaySchedule) {
                List<ScheduleDTO> exactDates = this.calculateExactDaysOfRegularSchedule(savedSchedule,cancelLive).getResults();
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

    public List<ScheduleDTO> computeScheduleInDateRange(ScheduleQuery scheduleQuery) {

        List<ScheduleDTO> inRangeResults = new ArrayList<>();

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fromDate = scheduleQuery.getFromDate();
        LocalDate toDate = scheduleQuery.getToDate();

        //> searchSchedule ignores fromDate+toDate fields. Should calculate ourselves.
        //> try using other query criteria to minimize  the size of searchSchedule query results...
        long startAt = System.currentTimeMillis();
        List<Schedule> searchResults = this.searchSchedule(scheduleQuery);
        //> add schedules in date range, ignore others
        boolean cancelLive = false;
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
                TimeTableResults timeTableResults = this.calculateExactDaysOfRegularSchedule(schedule,cancelLive);
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

       //Remove From inRangeResults, scheduleDTOs that are assigned to disabled room
       List<ScheduleDTO> disabledClassroomList = new ArrayList<>();
       for (ScheduleDTO scheduleDTO: inRangeResults) {
           String classroomId = scheduleDTO.getClassroom().getId();
           int classroom_status = classroomService.getClassroomStatus(classroomId);
           if (classroom_status == 1) {
               disabledClassroomList.add(scheduleDTO);
               removeScheduleFromTodaysProgrammeMessage(scheduleDTO,"Ανύπαρκτη αίθουσα:"  + classroomId);
           }
           else if (classroom_status == 2) {
               disabledClassroomList.add(scheduleDTO);
               removeScheduleFromTodaysProgrammeMessage(scheduleDTO,"Απενεργοποιημένη αίθουσα:"  + classroomId);
           }
       }
       inRangeResults.removeAll(disabledClassroomList);

        long endAllAt   = System.currentTimeMillis();
        logger.debug("Time:" + (endAllAt - startAt));
        return inRangeResults;
    }

    public List<ScheduleDTO> computeScheduleInDateRangeByEditor(ScheduleQuery scheduleQuery, OoUserDetails editor, String access) {

        List<ScheduleDTO> inRangeResults = new ArrayList<>();

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fromDate = scheduleQuery.getFromDate();
        LocalDate toDate = scheduleQuery.getToDate();

        //> searchSchedule ignores fromDate+toDate fields. Should calculate ourselves.
        //> try using other query criteria to minimize  the size of searchSchedule query results...
        long startAt = System.currentTimeMillis();
        List<Schedule> searchResults = this.searchScheduleByEditor(scheduleQuery, editor,access);
        //> add schedules in date range, ignore others
        boolean cancelLive = false;
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
                TimeTableResults timeTableResults = this.calculateExactDaysOfRegularSchedule(schedule,cancelLive);
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

        //Remove From inRangeResults, scheduleDTOs that are assigned to disabled room
        List<ScheduleDTO> disabledClassroomList = new ArrayList<>();
        for (ScheduleDTO scheduleDTO: inRangeResults) {
            String classroomId = scheduleDTO.getClassroom().getId();
            int classroom_status = classroomService.getClassroomStatus(classroomId);
            if (classroom_status == 1) {
                disabledClassroomList.add(scheduleDTO);
                removeScheduleFromTodaysProgrammeMessage(scheduleDTO,"Ανύπαρκτη αίθουσα:"  + classroomId);
            }
            else if (classroom_status == 2) {
                disabledClassroomList.add(scheduleDTO);
                removeScheduleFromTodaysProgrammeMessage(scheduleDTO,"Απενεργοποιημένη αίθουσα:"  + classroomId);
            }
        }
        inRangeResults.removeAll(disabledClassroomList);

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
        boolean cancelLive = false;
        if (schedule != null) {
            cancellations = schedule.getCancellations();
            if (cancellations == null) { cancellations = new ArrayList<>();}

            TimeTableResults timeTableResults = this
                    .calculateExactDaysOfRegularSchedule(schedule,cancelLive);

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

    public List<ScheduleDTO> getNextLiveBroadcastsFromClassroomById(String classroomId) {

        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setYear(currentAcademicYear);
        scheduleQuery.setType("event");
        scheduleQuery.setRepeat("onetime");
        scheduleQuery.setClassroomId(classroomId);
        scheduleQuery.setEnabled("true");
        scheduleQuery.setFromDate(LocalDate.now());
        scheduleQuery.setToDate(LocalDate.now().plus(10,ChronoUnit.DAYS));
        scheduleQuery.setSortBy("date");
        scheduleQuery.setLimit(0); // do not set limit here

        List<ScheduleDTO> scheduleDTOList = new ArrayList<>(this.computeScheduleInDateRange(scheduleQuery));

        LocalDateTime localDateTime = LocalDateTime.now();
        List<ScheduleDTO> remove_passed = new ArrayList<>();
        for (ScheduleDTO scheduleDTO: scheduleDTOList) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
            int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
            LocalDateTime broadcast_datetime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
            if (broadcast_datetime.isBefore(localDateTime)) {
                remove_passed.add(scheduleDTO);
            }
            else {
                break;
            }
        }
        scheduleDTOList.removeAll(remove_passed);

        return scheduleDTOList;
    }

    public List<ScheduleDTO> getNextLiveBroadcastToChannel(int days_ahead, boolean futureOnly) {

        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setYear(currentAcademicYear);
        scheduleQuery.setType("event");
        scheduleQuery.setRepeat("onetime");
        scheduleQuery.setBroadcastToChannel(true);
        scheduleQuery.setEnabled("true");
        scheduleQuery.setFromDate(LocalDate.now());
        scheduleQuery.setToDate(LocalDate.now().plus(days_ahead,ChronoUnit.DAYS));
        scheduleQuery.setSortBy("dateTime");
        scheduleQuery.setSortDirection("asc");
        scheduleQuery.setLimit(0); // do not set limit here

      List<ScheduleDTO> scheduleDTOList = new ArrayList<>(this.computeScheduleInDateRange(scheduleQuery));

      if (futureOnly) {
          LocalDateTime localDateTime = LocalDateTime.now();
          List<ScheduleDTO> remove_passed = new ArrayList<>();
          for (ScheduleDTO scheduleDTO : scheduleDTOList) {
              DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
              int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0, 2));
              int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3, 5));
              LocalDateTime broadcast_datetime = LocalDate.parse(scheduleDTO.getDate(), f)
                      .atTime(broadcast_hour, broadcast_min);
              //get the live ones too
              int duration_hours = scheduleDTO.getDurationHours();
              int duration_minutes = scheduleDTO.getDurationMinutes();
              LocalDateTime broadcast_end_datetime = broadcast_datetime.plus(duration_hours, ChronoUnit.HOURS)
                      .plus(duration_minutes, ChronoUnit.MINUTES);
              if (broadcast_datetime.isBefore(localDateTime) && broadcast_end_datetime.isBefore(localDateTime)) {
                  remove_passed.add(scheduleDTO);
              }
          }
          scheduleDTOList.removeAll(remove_passed);
      }
      return scheduleDTOList;
    }

    public List<ScheduleDTO> getPassedLiveBroadcastToChannel(int days_before, boolean futureOnly) {

        ScheduleQuery scheduleQuery = new ScheduleQuery();
        scheduleQuery.setYear(currentAcademicYear);
        scheduleQuery.setType("event");
        scheduleQuery.setRepeat("onetime");
        scheduleQuery.setBroadcastToChannel(true);
        scheduleQuery.setEnabled("true");
        scheduleQuery.setFromDate(LocalDate.now().minus(days_before,ChronoUnit.DAYS));
        scheduleQuery.setToDate(LocalDate.now().minus(1,ChronoUnit.DAYS));
        scheduleQuery.setSortBy("dateTime");
        scheduleQuery.setSortDirection("asc");
        scheduleQuery.setLimit(0); // do not set limit here

        List<ScheduleDTO> scheduleDTOList = new ArrayList<>(this.computeScheduleInDateRange(scheduleQuery));
        return scheduleDTOList;
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
            logger.trace("Αφαίρεση Διάλεξης από το πρόγραμμα μεταδόσεων:" + scheduleDTO.getCourse().getTitle() + " Αίτία: " +  cause);
        }
        if (scheduleDTO.getScheduledEvent() != null && !scheduleDTO.getScheduledEvent().getId().equals("")) {
            logger.trace("Αφαίρεση Εκδήλωσης από το πρόγραμμα μεταδόσεων:" + scheduleDTO.getScheduledEvent().getTitle() + " Αίτία: " +  cause);
        }
    }


    public boolean isScheduledForNow(ScheduleDTO scheduleDTO) {
        boolean res = false;
        ZoneId zoneId = ZoneId.systemDefault();//.of(app_zone);
        LocalDate today = LocalDate.now(zoneId);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate validateDate = LocalDate.parse(scheduleDTO.getDate(), f);

        if (validateDate.isEqual(today)) {
            LocalTime schedule_start_time = LocalTime.parse(scheduleDTO.getStartTime());
            LocalTime schedule_end_time = schedule_start_time.plus(scheduleDTO.getDurationHours(),ChronoUnit.HOURS).plus(scheduleDTO.getDurationMinutes(), ChronoUnit.MINUTES);
            LocalTime now_time = LocalTime.now();
            if (schedule_start_time.isBefore(now_time) && schedule_end_time.isAfter(now_time)) {
                res = true;
            }
        }
        return res;
    }

    public LocalDateTime getDateTimeOfSchedule(ScheduleDTO scheduleDTO) {

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
        int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
        LocalDateTime broadcast_datetime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
        return broadcast_datetime;
    }

    public List<ScheduleDTO> getNextScheduledEvents() {

        ScheduleQuery scheduleQuery = new ScheduleQuery();
        LocalDate fromDate = LocalDate.now();
        LocalDate endDate = fromDate.plus(180,ChronoUnit.DAYS);
        scheduleQuery.setFromDate(fromDate);
        scheduleQuery.setToDate(endDate);
        scheduleQuery.setType("event");
        scheduleQuery.setSortBy("date");
        scheduleQuery.setSortDirection("asc");
        scheduleQuery.setBroadcasting(true);
        scheduleQuery.setLimit(50);

        List<ScheduleDTO> scheduleDTOList = new ArrayList<>();
        if (scheduleQuery.getFromDate() != null && scheduleQuery.getToDate() != null) {
            scheduleDTOList.addAll(this.computeScheduleInDateRange(scheduleQuery));
        }

        return scheduleDTOList;
    }
}

