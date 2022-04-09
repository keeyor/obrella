package org.opendelos.live.repository.scheduler;

import java.util.List;

import org.opendelos.model.scheduler.CalendarItem;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarItemsRepository extends MongoRepository<CalendarItem, String> {

	List<CalendarItem> findAllByAcademicYear(String academicYear);
	List<CalendarItem> findAllByAcademicYearAndAndUnitId(String academicYear, String departmentId);
}
