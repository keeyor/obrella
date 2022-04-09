package org.opendelos.model.users;

import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
public class OoUserDetails extends org.springframework.security.core.userdetails.User {


    public OoUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    private String id;
    private String uid;
    private String name;
    private String altName;
    private String email;
    private String eduPersonPrimaryAffiliation;

    private String affiliation;
    private String title;
    private String departmentId;
    private String departmentTitle;

    private List<UserAccess.UserAuthority> userAuthorities;

}
