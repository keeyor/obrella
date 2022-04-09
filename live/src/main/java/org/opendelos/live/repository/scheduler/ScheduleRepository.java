package org.opendelos.live.repository.scheduler;

import java.time.DayOfWeek;
import java.util.List;

import org.opendelos.model.scheduler.Schedule;
import org.opendelos.live.repository.scheduler.extension.ScheduleOoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String>, ScheduleOoRepository {

	List<Schedule> findAllByAcademicYear(String academicYear);
	List<Schedule> findAllByType(String type);
	List<Schedule> findAllByDayOfWeek(DayOfWeek dayOfWeek);
	List<Schedule> findAllByAcademicYearAndType(String academicYear, String type);

}
