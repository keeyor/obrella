/* 
     Author: Michael Gatzonis - 28/1/2021 
     live
*/
package org.opendelos.vodapp.repository.scheduler.extension;

import java.util.List;

import org.opendelos.model.scheduler.Schedule;
import org.opendelos.model.scheduler.ScheduleQuery;

public interface ScheduleOoRepository {

	List<Schedule> search(ScheduleQuery scheduleQuery);
	List<Schedule> findAllUserIdReferencesInScheduler(String id, int limit);
	List<Schedule> findAllClassroomReferencesInScheduler(String classroomId, int limit);
	List<Schedule> findAllCourseReferencesInScheduler(String courseId, int limit);
	List<Schedule> findAllScheduledEventsReferencesInScheduler(String eventId, int limit);
}
