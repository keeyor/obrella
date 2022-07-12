/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.liveapp.api.system;

import org.opendelos.liveapp.services.system.SystemTextService;
import org.opendelos.model.system.SystemText;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemTextsApi {

	private final SystemTextService systemTextService;

	@Autowired
	public SystemTextsApi(SystemTextService systemTextService) {
		this.systemTextService = systemTextService;
	}

	@RequestMapping(value="/api/v1/text/{site}/code/{code}",method = RequestMethod.GET)
	public ResponseEntity<SystemText> findText(@PathVariable("site") String site,@PathVariable("code") String code) {

		SystemText systemText;
		try {
			systemText = systemTextService.findBySiteAndCode(site, code);
			return new ResponseEntity<>(systemText, HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/text/save/{site}/code/{code}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveSystemText(@PathVariable("site") String site,@PathVariable("code") String code, @RequestBody String json) {

		String _res = "ok";
		try {
			   SystemText systemText = systemTextService.findBySiteAndCode(site,code);
			   if (systemText != null) {
			   	systemText.setContent(json);
			   	systemTextService.update(systemText);
			}
			return new ResponseEntity<>(_res, HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/text/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteSystemText(@PathVariable("id") String id) {

		try {
			  systemTextService.delete(id);
			  return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
