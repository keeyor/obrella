/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.control.api.system;

import java.time.Instant;
import java.util.List;

import org.opendelos.control.api.common.ApiUtils;
import org.opendelos.control.services.system.SystemMessageService;
import org.opendelos.model.system.SystemMessage;

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
public class SystemMessagesApi {

	private final SystemMessageService systemMessageService;

	@Autowired
	public SystemMessagesApi(SystemMessageService systemMessageService) {
		this.systemMessageService = systemMessageService;
	}

	@RequestMapping(value="/api/v1/dt/messages.web",method = RequestMethod.GET)
	public byte[] findAllForDt() {

		List<SystemMessage> streamingServers = systemMessageService.findAll();
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(streamingServers);
		return b;
	}

	@RequestMapping(value = "/api/v1/message/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveSystemMessage(@RequestBody SystemMessage systemMessage) {

		String _id;
		try {
			if (systemMessage.getId() == null || systemMessage.getId().equals("")) {
				systemMessage.setId(null);
				systemMessage.setStartDate(Instant.now());
				_id = systemMessageService.create(systemMessage);
			}
			else {
				systemMessage.setStartDate(Instant.now());
				systemMessageService.update(systemMessage);
				_id = systemMessage.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/api/v1/message/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteSystemMessage(@PathVariable("id") String id) {

		try {
			  systemMessageService.delete(id);
			  return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
