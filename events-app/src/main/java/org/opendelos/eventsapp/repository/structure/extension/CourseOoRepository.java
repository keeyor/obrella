/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.eventsapp.repository.structure.extension;


import java.util.List;

import org.opendelos.model.structure.Course;


public interface CourseOoRepository {

     List<Course> findAllByDepartmentId(String departmentId);
     List<Course> findCoursesAssignedToStaffMember(String staffMemberId);
     List<Course> findWithCriteria(String schoolId, String departmentId, String study, String programId);
     List<Course> findFromIds(List<String> ids);
     List<Course> findExcludingIds(List<String> ids, String departmentId);
     void findAndUpdate(Course course);
     long updateResourcesCourse(Course course);
}
