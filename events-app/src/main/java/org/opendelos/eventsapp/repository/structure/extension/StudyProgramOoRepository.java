/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.eventsapp.repository.structure.extension;


import java.util.List;

import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.StudyProgram;

public interface StudyProgramOoRepository {

     List<StudyProgram> findWithCriteria(String schoolId, String departmentId, String study);
     void findAndUpdate(StudyProgram studyProgram);

     /* Calendar */
     CustomPeriod getCustomPeriod(String id, String year);
     void saveCustomPeriod(String id, CustomPeriod customPeriod);
     void deleteCustomPeriod(String id, String year);
     List<CustomPeriod> getCustomPeriods(String id);
}
