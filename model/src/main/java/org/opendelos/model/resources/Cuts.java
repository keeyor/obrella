/* 
     Author: Michael Gatzonis - 2/25/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cuts {

    protected Clips clips;
    protected Trims trims;
    protected String version;

    @Getter
    @Setter
    public static class Clips {

        protected List<Cut> cuts;

        @Getter
        @Setter
        public static class Cut {

            protected String begin;
            protected String end;
        }
    }
    @Getter
    @Setter
    public static class Trims {

        protected Start start;
        protected Finish finish;

        @Getter
        @Setter
        public static class Start {

            protected String begin;
            protected String end;
        }
        @Getter
        @Setter
        public static class Finish {

            protected String begin;
            protected String end;
        }
    }
}
