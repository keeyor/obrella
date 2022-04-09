package org.opendelos.liveapp.security;

import java.util.Collection;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class PopulateActiveUser {

    public OoUserDetails populate(OpUser opdeus) {

        Collection<GrantedAuthority> authorities;
        authorities = SetUserAuthorities.createAuthorities(opdeus.getAuthorities(), opdeus.getRights());
        String username = opdeus.getUid();
        String password = "";
        if (opdeus.getPassword() != null) {
            password = opdeus.getPassword();
        }

        OoUserDetails ooUserDetails = new OoUserDetails(username, password, authorities);
        ooUserDetails.setId(opdeus.getId());
        ooUserDetails.setUid(opdeus.getUid());
        ooUserDetails.setName(opdeus.getName());
        ooUserDetails.setAltName(opdeus.getAltName());
        ooUserDetails.setEduPersonPrimaryAffiliation(opdeus.getEduPersonPrimaryAffiliation());
        ooUserDetails.setEmail(opdeus.getEmail());
        ooUserDetails.setAffiliation(opdeus.getAffiliation());
        ooUserDetails.setUserAuthorities(opdeus.getAuthorities());
        ooUserDetails.setDepartmentId(opdeus.getDepartment().getId());
        ooUserDetails.setDepartmentTitle(opdeus.getDepartment().getTitle());

        return ooUserDetails;

    }
}
