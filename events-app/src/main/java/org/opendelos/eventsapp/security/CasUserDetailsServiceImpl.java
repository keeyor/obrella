package org.opendelos.eventsapp.security;


import java.time.Instant;
import java.util.Map;

import org.opendelos.model.delos.OpUser;
import org.opendelos.model.users.OoUserDetails;
import org.opendelos.eventsapp.services.opUser.OpUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CasUserDetailsServiceImpl implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private final Logger log = LoggerFactory.getLogger(CasUserDetailsServiceImpl.class);

    private final OpUserService opUserService;
    private final PopulateActiveUser populateActiveUser;

    @Autowired
    public CasUserDetailsServiceImpl(OpUserService opUserService, PopulateActiveUser populateActiveUser) {
        this.opUserService = opUserService;
        this.populateActiveUser = populateActiveUser;
    }

    @Override
    public OoUserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {

        Map<String, Object> attributes = token.getAssertion().getPrincipal().getAttributes();
        String uid = (String) attributes.get("uid");                                                    //gatzonis
        String mail = (String) attributes.get("mail");                                                  //gatzonis@med.uoa.gr
        String eduPersonPrimaryAffiliation  = (String) attributes.get("eduPersonPrimaryAffiliation");   //staff

        log.debug("Database Authenticating CAS USER uid: '{}' email '{}'", uid ,mail);
        log.debug("Database Authenticating CAS attributes: '{}'", printMap(attributes));

        OpUser opdeus = opUserService.findByUid(uid);

        if (opdeus != null && opdeus.isActive()) {
            // (if changed) update user info from CAS attributes
            boolean isChanged = opUserService.updateUserInfoFromCASAttributes(opdeus,attributes);
            // DO not update references (i.e. using findAndUpdate). Let user decide in related  page (i.e. staff)
            opdeus.setLastLogin(Instant.now());
            opUserService.update(opdeus);

            return populateActiveUser.populate(opdeus);
        }
        else if (opdeus != null && !opdeus.isActive()) {
            log.debug("Database Authenticating CAS user '{}' , Locked", uid);
            throw new UsernameNotFoundException("username " + uid + " locked");
        }
        else {
            if (eduPersonPrimaryAffiliation.equalsIgnoreCase("staff") ||  eduPersonPrimaryAffiliation.equalsIgnoreCase("faculty") ||
                eduPersonPrimaryAffiliation.equalsIgnoreCase("employees") ||  eduPersonPrimaryAffiliation.equalsIgnoreCase("affiliate") ) {
                opdeus = opUserService.createNewStaffMemberFromCASAttributes(attributes);
                opdeus
                        .setLastLogin(Instant.now());
                OpUser new_opUser = opUserService.createAndReturn(opdeus);
                return populateActiveUser.populate(new_opUser);
            }
            else if (eduPersonPrimaryAffiliation.equalsIgnoreCase("student")){
                opdeus = opUserService.createInMemoryStudentAccountFromCASAttributes(attributes);
                return populateActiveUser.populate(opdeus);
            }
            else {
                log.debug("Database Authenticating CAS user '{}' , not FOUND", uid);
                throw new UsernameNotFoundException("username " + uid + " not found");
            }
        }
    }

    private static String printMap(Map<String, Object>  mp) {
        StringBuilder cas_attributes = new StringBuilder();
        for (Map.Entry<String, Object> stringObjectEntry : mp.entrySet()) {
            cas_attributes.append(stringObjectEntry.getKey()).append(" = ").append(stringObjectEntry.getValue()).append("\n");
        }
        return cas_attributes.toString();
    }
}

