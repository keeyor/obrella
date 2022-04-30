/* 
     Author: Michael Gatzonis - 6/2/2021 
     live
*/
package org.opendelos.control.services.resource;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.control.services.structure.CourseService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Resource;
import org.opendelos.model.structure.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
@Aspect
public class ResourceUpdateAspects {

	private static final Logger logger = LoggerFactory.getLogger(ResourceUpdateAspects.class);

	private final ResourceService resourceService;
	private final CourseService courseService;
	private final OpUserService opUserService;

	public ResourceUpdateAspects(ResourceService resourceService, CourseService courseService, OpUserService opUserService) {
		this.resourceService = resourceService;
		this.courseService = courseService;
		this.opUserService = opUserService;
	}

	@Pointcut("execution(* org.opendelos.control.services.resource.ResourceService.updateAccessPolicy(..))  && " +  "args(resourceId,status)")
	public void AfterResourceUpdateAccessPolicy(String resourceId, String status){ }


	@AfterReturning(value = "AfterResourceUpdateAccessPolicy(resourceId,status)")
	public void UpdateCourseAndStaffMemberPublicCounters(String resourceId, String status) {

		Resource resource = resourceService.findById(resourceId);
		if (resource.getType().equals("COURSE")) {
			if (resource.getCourse() != null && resource.getCourse().getId() != null) {
				Course course = courseService.findById(resource.getCourse().getId());
				if (status.equals("public")) {
					course.setResourcePublicCounter(course.getResourcePublicCounter() + 1);
				}
				else if (status.equals("private") && course.getResourcePublicCounter() > 0) {
					course.setResourcePublicCounter(course.getResourcePublicCounter() - 1);
				}
				courseService.update(course);
			}
			if (resource.getSupervisor() != null && resource.getSupervisor().getId() != null) {
				OpUser supervisor = opUserService.findById(resource.getSupervisor().getId());
				if (status.equals("public")) {
					supervisor.setResourcePublicCounter(supervisor.getResourcePublicCounter() + 1);
				}
				else if (status.equals("private") && supervisor.getResourcePublicCounter()  > 0) {
					supervisor.setResourcePublicCounter(supervisor.getResourcePublicCounter() - 1);
				}
			}
		}
	}
}
