/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.vodapp.repository.structure.extension;


import java.util.List;

import org.opendelos.model.structure.Classroom;
import org.opendelos.model.structure.Device;


public interface ClassroomOoRepository {

     void findAndUpdate(Classroom classroom);
     void updateClassroomDevice(String id, int device_idx, Device device);
     void deleteClassroomDevice(String id, int device_idx);
     List<Device> getClassroomDevices(String id);
     String getClassroomNameById(String id);
     List<Classroom> findAllExcludingIds(List<String> ids);
     List<Classroom> findByUsage(String usage);

     List<Classroom> n_findAssignedClassrooms(List<String> departmentIds, String usage);
}
