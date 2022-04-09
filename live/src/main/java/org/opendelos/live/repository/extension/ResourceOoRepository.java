/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.live.repository.extension;

import java.util.List;


import org.opendelos.live.repository.resource.QueryResourceResults;
import org.opendelos.live.repository.resource.ResourceQuery;
import org.opendelos.model.resources.Resource;

public interface ResourceOoRepository {

     QueryResourceResults searchPageableLectures(ResourceQuery lectureQuery);
     List<Resource> searchLecturesOnFilters(ResourceQuery lectureQuery);

     void clearCollection(String collectionName);

     void saveToCollection(Resource resource, String collectionName);
     void deleteFromCollection(Resource resource, String collectionName);
     void deleteFromCollectionById(String id, String collectionName);

     List<Resource> findByScheduleIdInCollection(String id, String collectionName);
     List<Resource> findByClassroomIdInCollection(String id, String collectionName);

     Resource findByIdInCollection(String id, String collectionName);

     List<Resource> findAllUserIdReferencesInResources(String id, int limit);

}
