/* 
     Author: Michael Gatzonis - 22/1/2021 
     live
*/
package org.opendelos.model.delos;


import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.opendelos.model.resources.Unit;
import org.opendelos.model.users.UserAccess;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "OpUser")
@Getter
@Setter
public class OpUser implements Serializable {

	@Id
	private String id;

	@Indexed(direction = IndexDirection.ASCENDING)
	protected List<String> identity;							// compatibility to eXist-db
	@Indexed(direction = IndexDirection.ASCENDING)
	protected String uid;								// CAS 'uid' property e.g gatzonis
	@Indexed(direction = IndexDirection.ASCENDING)
	protected String email; 							// CAS 'mail' property e.g gatzonis
	@Indexed(direction = IndexDirection.ASCENDING)
	protected Unit department;  						// Ask user on first login or updated by admin

	protected String name;								// CAS 'cn;lang-el' property e.g Μιχάλης Γκατζώνης
	protected String altName;							// CAS 'cn' property e.g Michalis Gatzonis
	protected String affiliation;						// CAS 'title;lang-el' property e.g Διοικητικό Προσωπικό
	protected String eduPersonPrimaryAffiliation;		// CAS 'eduPersonPrimaryAffiliation' property e.g Staff
	protected List<String> eduPersonAffiliation;		// CAS 'eduPersonAffiliation' property e.g Staff  ? maybe array

	protected List<UserAccess.UserAuthority> authorities;     	// OpenDelos Roles(s): MANAGER, STAFFMEMBER (CAS: Faculty,Staff), STUDENT
	protected UserAccess.UserRights	rights;						// OpenDelos Rights on other's courses, services (content, data, scheduler)

	protected List<String> courses;								// STAFFMEMBERS's own Courses

	protected boolean active;									// User is active or not

	protected String password;

	protected Instant lastLogin;

	protected long resourceCounter; // how many resources refer this supervisor;
	protected long resourcePublicCounter; // how many public resources refer this supervisor;
}
