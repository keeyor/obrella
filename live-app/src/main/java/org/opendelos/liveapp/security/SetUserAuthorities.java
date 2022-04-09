package org.opendelos.liveapp.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendelos.model.users.UserAccess;

import org.springframework.security.core.GrantedAuthority;


public final class SetUserAuthorities {

    private static final Collection<GrantedAuthority> SA_AUTHORITIES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_SA");
    private static final Collection<GrantedAuthority> MA_AUTHORITIES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_MANAGER");
    private static final Collection<GrantedAuthority> SP_AUTHORITIES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_SUPPORT");
    private static final Collection<GrantedAuthority> SM_AUTHORITIES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_STAFFMEMBER");
    private static final Collection<GrantedAuthority> SU_AUTHORITIES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_STUDENT");
    private static final Collection<GrantedAuthority> USER_AUTHORITIES = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_USER");


    public static Collection<GrantedAuthority> createAuthorities(List<UserAccess.UserAuthority> roles, UserAccess.UserRights userRights) {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (userRights != null && userRights.getIsSa()) {
            authorities.addAll(SA_AUTHORITIES);
        }
        if (roles.contains(UserAccess.UserAuthority.MANAGER)) {
            authorities.addAll(MA_AUTHORITIES);
        }
        else if (roles.contains(UserAccess.UserAuthority.SUPPORT)) {
            authorities.addAll(SP_AUTHORITIES);
        }
        if (roles.contains(UserAccess.UserAuthority.STAFFMEMBER)) {
            authorities.addAll(SM_AUTHORITIES);
        }
        if (roles.contains(UserAccess.UserAuthority.STUDENT)) {
            authorities.addAll(SU_AUTHORITIES);
        }
        if (authorities.isEmpty()) {
            authorities.addAll(USER_AUTHORITIES);
        }
        return authorities;
    }


}


