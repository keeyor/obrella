/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.vodapp.repository.resource.extension;

import java.util.List;

import org.opendelos.model.repo.QueryResourceResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.Resource;

public interface ResourceOoRepository {

     QueryResourceResults searchPageableLectures(ResourceQuery lectureQuery);
     List<Resource> searchLecturesOnFilters(ResourceQuery lectureQuery);
     QueryResourceResults searchLMSLectures(ResourceQuery resourceQuery);

     void clearCollection(String collectionName);

     void saveToCollection(Resource resource, String collectionName);
     void deleteFromCollection(Resource resource, String collectionName);
     void deleteFromCollectionById(String id, String collectionName);

     List<Resource> findByScheduleIdInCollection(String id, String collectionName);
     List<Resource> findByClassroomIdInCollection(String id, String collectionName);

     List<Resource> findAllUserIdReferencesInResources(String id, int limit);

     Resource findByIdInCollection(String id, String collectionName);
     List<Resource> findByStreamNameInCollection(String streamName, String collectionName);
     Resource findByStreamIdInCollection(String streamId, String collectionName);
     //NOTE : Should return one or Multiple if same classroom is used during the day.
     //Extract he live one in Service
     List<Resource> findLiveStreamByIdOrNameInCollection(String idOrName, String collectionName);

     List<Resource> findRelatedCourseResources(Resource resource, String accessPolicy);

     long CountPublicResourcesByType(String type);
     long CountCollectionDocuments(String collectionName);
     long CountResourcesByStaffMemberAsSupervisor(String staffId);
     long CountScheduledByStaffMemberAsSupervisor(String staffId, String collectionName);
     long CountResourcesByManagerAsEditor(String userId);
     long CountScheduledByManagerAsEditor(String userId, String collectionName);

     long CountPublicResourcesByCourseId(String courseId);
     long CountPublicResourcesByStaffMemberAsSupervisor(String staffId);
}
