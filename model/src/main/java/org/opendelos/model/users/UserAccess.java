/* 
     Author: Michael Gatzonis - 11/18/2018 
     OpenDelosDAC
*/
package org.opendelos.model.users;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccess {

    @Getter
    @Setter
    public  static class UserPreferences {

        private String record;
        private String broadcast;
        private String access;
        private String publish;

    }

    @Getter
    public enum UserAuthority {
        STAFFMEMBER,
        MANAGER,
        SUPPORT,
        STUDENT,
        USER;
        public String value() {
            return name();
        }
        public static UserAuthority fromValue(String v) {
            return valueOf(v);
        }
    }

    @Getter
    @Setter
    public  static class UserRights implements Serializable {

        protected Boolean isSa;
        protected List<CoursePermission> coursePermissions;
        protected List<EventPermission> eventPermissions;
        protected List<UnitPermission> unitPermissions;

        @Getter
        @Setter
        public  static class CoursePermission implements Serializable {

            protected String staffMemberId;
            protected String courseId;
            protected boolean contentManager;
            protected boolean scheduleManager;
            //Dto usage only
            protected String staffMemberName;
            protected String courseTitle;
        }

        @Getter
        @Setter
        public  static class EventPermission implements Serializable {

            protected String staffMemberId;
            protected String eventId;
            protected boolean contentManager;
            protected boolean scheduleManager;
            //Dto usage only
            protected String staffMemberName;
            protected String eventTitle;
        }

        @Getter
        @Setter
        public static class UnitPermission implements Serializable {

            protected String unitId;
            protected UnitType unitType;
            protected boolean contentManager;
            protected boolean dataManager;
            protected boolean scheduleManager;
            //Dto usage only
            protected String unitTitle;
        }
    }
    @Getter
    public  enum UnitType {

        INSTITUTION,
        SCHOOL,
        DEPARTMENT;

        public String value() {
            return name();
        }

        public static UnitType fromValue(String v) {
            return valueOf(v);
        }
    }
}
