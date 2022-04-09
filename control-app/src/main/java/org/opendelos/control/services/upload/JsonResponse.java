package org.opendelos.control.services.upload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonResponse {
	
    private String status = null;
    private Object result = null;
    private String source = null;
    private String message = null;

}
