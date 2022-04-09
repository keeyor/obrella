/* 
     Author: Michael Gatzonis - 28/1/2021 
     live
*/
package org.opendelos.control.repository.scheduledEvent.extension;

import java.util.List;

import org.opendelos.model.repo.QueryScheduledEventsResults;
import org.opendelos.model.repo.ResourceQuery;
import org.opendelos.model.resources.ScheduledEvent;

public interface ScheduledEventOoRepository {

	List<ScheduledEvent> findAllByEditorId(String editorId,int limit);
	List<ScheduledEvent> findAllByResponsiblePersonId(String editorId,int limit);
	List<ScheduledEvent> findAllUserIdReferencesInScheduledEvents(String id, int limit);
	List<ScheduledEvent> findAllWhereResponsibleUnitInIdList(List<String> unitIdList);
	List<ScheduledEvent> findAllWhereResponsiblePersonInIdList(List<String> personIdList);
	List<ScheduledEvent> findAllWhereResponsiblePersonIsInDepartmentId(List<String> unitIdList);
	long updateResourcesScheduledEvent(ScheduledEvent scheduledEvent);
	QueryScheduledEventsResults searchPageableScheduledEvents(ResourceQuery lectureQuery);
	long CountEventsByStaffMemberAsSupervisor(String staffId);
	long CountEventsByManagerAsEditor(String userId);
	public List<ScheduledEvent> searchScheduledEventsOnFilters(ResourceQuery resourceQuery);
}
