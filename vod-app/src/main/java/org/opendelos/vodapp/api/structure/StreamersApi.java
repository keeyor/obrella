/* 
     Author: Michael Gatzonis - 1/10/2020 
     live
*/
package org.opendelos.vodapp.api.structure;

import java.util.List;

import org.opendelos.model.structure.StreamingServer;
import org.opendelos.vodapp.api.common.ApiUtils;
import org.opendelos.vodapp.services.structure.StreamingServerService;

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
public class StreamersApi {

	private final StreamingServerService streamingServerService;

	@Autowired
	public StreamersApi(StreamingServerService streamingServerService) {
		this.streamingServerService = streamingServerService;
	}

	@RequestMapping(value = "/apiw/v1/dt/streamers.web", method = RequestMethod.GET)
	public byte[] findAllForDt() {

		List<StreamingServer> streamingServers = streamingServerService.findAll();
		byte[] b;
		b = ApiUtils.TransformResultsForDataTable(streamingServers);
		return b;
	}

	@RequestMapping(value = "/apiw/v1/streamer/save", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> saveStreamingServer(@RequestBody StreamingServer streamingServer) {

		String _id;
		try {
			if (streamingServer.getId() == null || streamingServer.getId().equals("")) {
				streamingServer.setId(null);
				_id = streamingServerService.create(streamingServer);
			}
			else {
				streamingServerService.update(streamingServer);
				_id = streamingServer.getId();
			}
			return new ResponseEntity<>(_id, HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/apiw/v1/streamer/delete/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteStreamingServer(@PathVariable("id") String id) {

		try {
			streamingServerService.delete(id);
			return new ResponseEntity<>("OK", HttpStatus.ACCEPTED);
		}
		catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
