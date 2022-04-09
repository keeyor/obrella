/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.live.repository.delos.extension;


import java.util.List;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.users.UserAccess;

public interface OpUserOoRepository {

     void findAndUpdate(OpUser opUser);

     List<OpUser> findAllManagers();

     List<OpUser> findAllStaffMembersForCourseId(String courseId);
     List<OpUser> findAllStaffMembersTeachingInDepartment(String departmentId);
     List<OpUser> findAllStaffMembersOfDepartment(String departmentId);
     List<String> getAllStaffMemberCourseIds(String staffMemberId);

     List<UserAccess.UserRights.UnitPermission> getManagerUnitPermissions(String id);
     List<UserAccess.UserRights.CoursePermission> getManagerCoursePermissions(String id);
     void AssignUnitPermissionToManager(String id, UserAccess.UserRights.UnitPermission unitPermission);
     void UnAssignUnitPermissionFromManager(String id, String unitId);
     void AssignCoursePermissionToManager(String id, UserAccess.UserRights.CoursePermission coursePermission);
     void UnAssignCoursePermissionFromManager(String id, String staffMemberId, String courseId);
     void ChangeRoleOfManager(String id, String new_role);

     void AssignCoursesToStaffMember(String staffMemberId, String[] courseIds);
     void UnAssignCourseFromStaffMember(String staffMemberId, String courseId);

     /* BULK UPDATE */
     long updateScheduledEventsResponsiblePersonInfo(String staffMemberId, String staffMemberName, String staffMemberAffiliation);
     long updateScheduledEventsEditorInfo(String staffMemberId, String staffMemberName, String staffMemberAffiliation, Unit unit);
     long updateResourcesSupervisorInfo(String staffMemberId, String staffMemberName, String staffMemberAffiliation);
     long updateResourcesEditorInfo(String staffMemberId, String staffMemberName, String staffMemberAffiliation, Unit unit);
     long updateResourcesScheduledEventResponsiblePersonInfo(String staffMemberId, String staffMemberName, String staffMemberAffiliation);
     long updateResourcesScheduledEventEditorInfo(String staffMemberId, String staffMemberName, String staffMemberAffiliation, Unit unit);
}
