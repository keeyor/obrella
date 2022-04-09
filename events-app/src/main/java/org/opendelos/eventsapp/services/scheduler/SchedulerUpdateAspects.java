/* 
     Author: Michael Gatzonis - 6/2/2021 
     live
*/
package org.opendelos.eventsapp.services.scheduler;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.structure.Classroom;
import org.opendelos.eventsapp.repository.resource.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class SchedulerUpdateAspects {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerUpdateAspects.class);

	private final LiveService liveService;
	private final ResourceRepository resourceRepository;

	@Autowired
	public SchedulerUpdateAspects(LiveService liveService, ResourceRepository resourceRepository) {
		this.liveService = liveService;
		this.resourceRepository = resourceRepository;
	}

	/********************************* POINTCUTS *************************************************************/

	@Pointcut("execution(* org.opendelos.eventsapp.services.scheduler.ScheduleService.create(..))  && " +  "args(schedule)")
	public void AfterNewScheduleCreated(Schedule schedule){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.scheduler.ScheduleService.update(..))  && " +  "args(schedule)")
	public void AfterSchedulerUpdated(Schedule schedule){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.scheduler.ScheduleService.delete(..))  && " +  "args(id)")
	public void AfterSchedulerDeleted(String id){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.ClassroomService.findAndUpdate(..))  && " +  "args(classroom)")
	public void AfterClassroomUpdated(Classroom classroom){ }

	/* Institution CalendarUpdates */

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.InstitutionService.saveCustomPause(..))")
	public void AfterInstitutionPauseUpdated(){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.InstitutionService.saveCustomPeriod(..))")
	public void AfterInstitutionPeriodUpdated(){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.InstitutionService.deleteCustomPeriod(..))")
	public void AfterInstitutionPeriodDeleted(){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.InstitutionService.deleteCustomPause(..))")
	public void AfterInstitutionPauseDeleted(){ }

	/* Department CalendarUpdates */

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.DepartmentService.saveCustomPause(..))")
	public void AfterDepartmentPauseUpdated(){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.DepartmentService.saveCustomPeriod(..))")
	public void AfterDepartmentPeriodUpdated(){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.DepartmentService.deleteCustomPeriod(..))")
	public void AfterDepartmentPeriodDeleted(){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.DepartmentService.deleteCustomPause(..))")
	public void AfterDepartmentPauseDeleted(){ }

	/* StudyProgram CalendarUpdates */

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.StudyProgramService.saveCustomPeriod(..))")
	public void AfterStudyProgramPeriodUpdated(){ }

	@Pointcut("execution(* org.opendelos.eventsapp.services.structure.StudyProgramService.deleteCustomPeriod(..))")
	public void AfterStudyProgramPeriodDeleted(){ }


	/* ************************************************** ADVICES ********************************************/

	@AfterReturning(value = "AfterNewScheduleCreated(schedule)", argNames = "schedule")
	public void UpdateTodaysScheduleForCreateWithConstrains(Schedule schedule){

		boolean includeLive = true;

		if (schedule.getRepeat().equals("onetime")) {
			if (liveService.IsOneTimeScheduleScheduledForTodayAfterNow(schedule, includeLive)) {
				liveService.AddScheduleToTodaysSchedule(schedule);
			}
		}
		else if (schedule.getRepeat().equals("regular")) {
			 ScheduleDTO scheduleDTO = liveService.getRegularScheduleDTOScheduledForTodayAfterNow(schedule, includeLive);
			 if (scheduleDTO != null) {
			 	liveService.AddScheduleDTOToTodaysSchedule(scheduleDTO);
			 }
			 else {
			 	 logger.info("New Regular Entry:: NOT for TODAY OR HAS PASSED");
			 }
		}
		//## 05/02/2022 :: Add all future broadcasts to "Scheduler.Broadcasts" Collection
		//## disabled for now! re-think about it. it has many consequences on updates, cancellations etc
		//TimeTableResults timeTableResults = scheduleService.calculateExactDaysOfRegularSchedule(schedule,false);
		//broadcastService.AddFutureScheduleDatesToBroadcasts(timeTableResults);
	}

	@AfterReturning(value = "AfterSchedulerUpdated(schedule)", argNames = "schedule")
	public void UpdateTodaysScheduleForUpdateWithConstrains(Schedule schedule){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Schedule Updated): " + nowTime.getTime());
	}

	@AfterReturning(value = "AfterSchedulerDeleted(id)", argNames = "id")
	public void UpdateTodaysScheduleForDelete(String id){

		List<Resource> live_resources = resourceRepository.findByScheduleIdInCollection(id,"Scheduler.Live");
		//Delete from collection all future resources with same schedule_id (if any)
		if (live_resources != null && live_resources.size()>0) {
			for (Resource live_resource : live_resources) {
				if (live_resource.getDate().isAfter(Instant.now())) {
					resourceRepository.deleteFromCollection(live_resource,"Scheduler.Live");
					logger.info("Delete Entry form today's schedule");
				}
			}
		}
		//Channel Entry will get deleted since it has the same scheduleId
	}

	@AfterReturning(value = "AfterClassroomUpdated(classroom)", argNames = "classroom")
	public void UpdateTodaysScheduleForClassroomUpdate(Classroom classroom) {

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Classroom Updated): " + nowTime.getTime());
	}

	/* institution calendar updates */
	@AfterReturning("AfterInstitutionPauseUpdated()")
	public void UpdateTodaysScheduleForInstitutionPauseUpdated(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Institution Pause Update): " + nowTime.getTime());
	}
	@AfterReturning("AfterInstitutionPeriodUpdated()")
	public void UpdateTodaysScheduleForInstitutionPeriodUpdated(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Institution Period Update): " + nowTime.getTime());
	}
	@AfterReturning("AfterInstitutionPauseDeleted()")
	public void UpdateTodaysScheduleForInstitutionPauseDelete(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Institution Pause Delete): " + nowTime.getTime());
	}
	@AfterReturning("AfterInstitutionPeriodDeleted()")
	public void UpdateTodaysScheduleForInstitutionPeriodDelete(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Institution PeriodDelete): " + nowTime.getTime());
	}
	/* department calendar updates */
	@AfterReturning("AfterDepartmentPauseUpdated()")
	public void UpdateTodaysScheduleForDepartmentPauseUpdates(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Department Pause Update): " + nowTime.getTime());
	}
	@AfterReturning("AfterDepartmentPeriodUpdated()")
	public void UpdateTodaysScheduleForDepartmentPeriodUpdates(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Department Period Update): " + nowTime.getTime());
	}
	@AfterReturning("AfterDepartmentPauseDeleted()")
	public void UpdateTodaysScheduleForDepartmentPauseDeleted(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Department Pause Delete): " + nowTime.getTime());
	}
	@AfterReturning("AfterDepartmentPeriodDeleted()")
	public void UpdateTodaysScheduleForDepartmentPeriodDeleted(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Department Period Delete): " + nowTime.getTime());
	}
	/* department calendar updates */
	@AfterReturning("AfterStudyProgramPeriodUpdated()")
	public void UpdateTodaysScheduleForStudyProgramPeriodUpdated(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Study Program Period Update): " + nowTime.getTime());
	}
	@AfterReturning("AfterStudyProgramPeriodDeleted()")
	public void UpdateTodaysScheduleForStudyProgramPeriodDeleted(){

		liveService.UpdateTodaysSchedule();
		java.util.Calendar nowTime = java.util.Calendar.getInstance();
		nowTime.setTime(new Date());
		logger.info("Trigger Today's Schedule Updated (Study Program Period Delete): " + nowTime.getTime());
	}

}
