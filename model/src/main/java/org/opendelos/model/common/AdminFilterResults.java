/* 
     Author: Michael Gatzonis - 28/12/2020 
     live
*/
package org.opendelos.model.common;

import java.io.Serializable;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.AccessPolicy;
import org.opendelos.model.resources.ResourceTags;

@Getter
@Setter
public class AdminFilterResults implements Serializable {

	private Map<String, AccessPolicy> accessPolicyFilters;
	private Map<String, Tag> tagsFilters;
}
