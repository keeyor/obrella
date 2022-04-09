/* 
     Author: Michael Gatzonis - 19/2/2021 
     live
*/
package org.opendelos.model.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitRightDto {

	protected String unitId;
	protected String unitType;
	protected String unitTitle;
	protected boolean contentManager;
	protected boolean dataManager;
	protected boolean scheduleManager;
}
