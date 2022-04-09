package org.opendelos.control.security.database;

import java.time.Instant;

import org.opendelos.control.security.PopulateActiveUser;
import org.opendelos.control.services.opUser.OpUserService;
import org.opendelos.model.delos.OpUser;
import org.opendelos.model.users.OoUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class DatabaseUserDetailsServiceImpl implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DatabaseUserDetailsServiceImpl.class);

    private final OpUserService opUserService;
    private final PopulateActiveUser populateActiveUser;


    @Autowired
    public DatabaseUserDetailsServiceImpl(OpUserService opUserService, PopulateActiveUser populateActiveUser) {
        this.opUserService = opUserService;
        this.populateActiveUser = populateActiveUser;
    }

    @Override
    public OoUserDetails loadUserByUsername(String username) throws UsernameNotFoundException  {

        log.debug("Database Authenticating Database User: '{}'", username);
        OpUser opdeus = opUserService.findByUid(username);
        if (opdeus != null && opdeus.isActive()) {
            opdeus.setLastLogin(Instant.now());
            opUserService.update(opdeus);
            return populateActiveUser.populate(opdeus);
        } else {
                log.info("Database Authenticating Database user '{}' , not FOUND", username);
                throw new UsernameNotFoundException("user " + username + " not found");
         }
    }

}

