/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.vodapp.repository.structure.extension;

import java.util.List;

import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Department;

public interface DepartmentOoRepository {

     void findAndUpdate(Department department);
     List<String> getAllClassrooms(String departmentId);
     void addClassroomToDepartment(String departmentId, String classroomId);
     void deleteClassroomFromDepartment(String departmentId, String classroomId);
     long removeClassroomAssignmentsFromAllDepartments(String classroomId);
     /* Bulk Updates */
     long updateCoursesDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateStaffMembersDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateStaffMembersCourseDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateScheduledEventsResponsibleUnitsDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateScheduledEventsResponsiblePersonDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateScheduledEventsEditorDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateResourcesDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateResourcesSupervisorDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateResourcesEditorDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateResourcesCourseDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateResourcesScheduledEventResponsiblePersonDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateResourcesScheduledEventsResponsibleUnitsDepartment(String departmentId,String departmentIdentity, String departmentTitle);
     long updateResourcesScheduledEventsEditorDepartment(String departmentId,String departmentIdentity, String departmentTitle);

     void AssignRoomsToDepartment(String departmentId, String[] roomIds);
     void UnAssignRoomFromDepartment(String departmentId, String roomId);

     /* Calendar */
     CustomPeriod getCustomPeriod(String id, String year);
     void saveCustomPeriod(String id, CustomPeriod customPeriod);
     void deleteCustomPeriod(String id, String year);


     List<CustomPeriod> getCustomPeriods(String id);
     CustomPause getCustomPause(String id, String year);
     void saveCustomPause(String id, CustomPause customPause);
     void deleteCustomPause(String id, String year);
     List<CustomPause> getCustomPauses(String id);

}
