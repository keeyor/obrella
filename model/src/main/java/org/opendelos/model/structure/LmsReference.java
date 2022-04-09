/* 
     Author: Michael Gatzonis - 11/11/2020 
     live
*/
package org.opendelos.model.structure;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LmsReference implements Serializable {

		protected String lmsId;
		protected String lmsCode;

}
