package org.opendelos.liveapp.repository.delos;


import java.util.List;

import org.opendelos.liveapp.repository.delos.extension.OpUserOoRepository;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.users.UserAccess;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpUserRepository extends MongoRepository<OpUser, String>, OpUserOoRepository {

	OpUser findByIdentity(String identity);
	OpUser findByUid(String uid);
	OpUser findByEmail(String email);
	List<OpUser> findAllByDepartmentIdOrderByName(String departmentId);
	List<OpUser> findAllByAuthoritiesContains(UserAccess.UserAuthority userAuthority);
	List<OpUser> findByEduPersonPrimaryAffiliation(String primary_affiliation);
	List<OpUser> findByEduPersonPrimaryAffiliationNot(String primary_affiliation);

}
