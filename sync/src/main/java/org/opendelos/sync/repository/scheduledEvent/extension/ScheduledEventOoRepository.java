/* 
     Author: Michael Gatzonis - 28/1/2021 
     live
*/
package org.opendelos.sync.repository.scheduledEvent.extension;

import java.time.Instant;
import java.util.List;

import org.opendelos.model.resources.ScheduledEvent;

public interface ScheduledEventOoRepository {

	List<ScheduledEvent> findAllByEditorId(String editorId,int limit);
	List<ScheduledEvent> findAllByResponsiblePersonId(String editorId,int limit);
	List<ScheduledEvent> findAllUserIdReferencesInScheduledEvents(String id, int limit);
	List<ScheduledEvent> findAllWhereResponsibleUnitInIdList(List<String> unitIdList);
	List<ScheduledEvent> findAllWhereResponsiblePersonInIdList(List<String> personIdList);
	List<ScheduledEvent> findAllWhereResponsiblePersonIsInDepartmentId(List<String> unitIdList);
	long updateResourcesEvents(ScheduledEvent scheduledEvent);

	List<ScheduledEvent> findAllInCollection(String collection);
	public ScheduledEvent findMatchInCollection(String title, Instant date, String collection);
}
