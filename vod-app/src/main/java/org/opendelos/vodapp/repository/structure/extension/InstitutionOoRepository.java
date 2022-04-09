/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.vodapp.repository.structure.extension;

import java.util.List;

import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Institution;

public interface InstitutionOoRepository {

     void findAndUpdate(Institution institution);

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
