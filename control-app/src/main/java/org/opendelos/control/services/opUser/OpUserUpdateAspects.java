/* 
     Author: Michael Gatzonis - 6/2/2021 
     live
*/
package org.opendelos.control.services.opUser;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.model.structure.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
@Aspect
public class OpUserUpdateAspects {

	private static final Logger logger = LoggerFactory.getLogger(OpUserUpdateAspects.class);

	private final CourseService courseService;

	public OpUserUpdateAspects(CourseService courseService) {
		this.courseService = courseService;
	}

	@Pointcut("execution(* org.opendelos.control.services.opUser.OpUserService.assignCoursesToStaffMemberById(..))  && " +  "args(staffMemberId,courseIds)")
	public void AfterStaffMemberAssignCourses(String staffMemberId, String[] courseIds){ }

	@Pointcut("execution(* org.opendelos.control.services.opUser.OpUserService.unassignCourseFromStaffMemberById(..))  && " +  "args(staffMemberId,courseId)")
	public void AfterStaffMemberUnAssignCourse(String staffMemberId, String courseId) {}

	@AfterReturning(value = "AfterStaffMemberAssignCourses(staffMemberId,courseIds)")
	public void IncCoursesTCounter(String staffMemberId, String[] courseIds) {
		for (String courseId: courseIds) {
			Course course = courseService.findById(courseId);
 			course.setTeachingCounter(course.getTeachingCounter()+1);
 			courseService.update(course);
 			logger.info("Increment course teachingCounter by 1 : " + course.getTitle());
		}
	}

	@AfterReturning(value = "AfterStaffMemberUnAssignCourse(staffMemberId, courseId)")
	public void DecCourseTCounter(String staffMemberId, String courseId) {
		Course course = courseService.findById(courseId);
		course.setTeachingCounter(course.getTeachingCounter()-1);
		courseService.update(course);
		logger.info("Decrement course teachingCounter by 1 : " + course.getTitle());
	}
}
