package org.opendelos.control.security;


import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service("CasUserDetailsService")
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
        	//Choose those affiliations that should have (by default) content editing rights!!!
            if (eduPersonPrimaryAffiliation.equalsIgnoreCase("staff") ||  eduPersonPrimaryAffiliation.equalsIgnoreCase("faculty") ||        //eduPersonPrimaryAffiliation.equalsIgnoreCase("employee") ||
                eduPersonPrimaryAffiliation.equalsIgnoreCase("affiliate") ) {
                opdeus = opUserService.createNewStaffMemberFromCASAttributes(attributes);
                opdeus.setLastLogin(Instant.now());
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

    private static String printMap(Map mp) {
        String cas_attributes = "";
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            cas_attributes += pair.getKey() + " = " + pair.getValue() + "\n";
        }
        return cas_attributes;
    }
}

