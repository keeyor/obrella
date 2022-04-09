package org.opendelos.eventsapp.services.resource;

import java.util.Vector;
import java.util.concurrent.Future;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class RealEditingFutureService {

	private static final Logger logger = LoggerFactory.getLogger(RealEditingFutureService.class);
	
	private final ResourceService resourceService;
	private final RealEditingToolkit toolkit;

	private String process_status;
	private int cancelProcess = 0;
	private int failedProcess = 0;

	@Autowired
	public RealEditingFutureService(ResourceService resourceService, RealEditingToolkit toolkit) {
		this.resourceService = resourceService;
		this.toolkit = toolkit;
	}

	@Async("taskExecutor")
	public Future<String> editResource ( String rid ) {

		this.process_status = "Running";
		try {

			if ( resourceService.isFirstTimeRealEdited(rid)) {
				 resourceService.updateRealTimeEditing(rid,"DRE","Η διαδικασία δημιουργίας του νέου βίντεο, βρίσκεται σε εξέλιξη",(System.currentTimeMillis()/1000));
			}
			// STEP 1: Create FFmpeg commands which extract the proper video clips and merges into a new video file //
			Vector<String> listOfCommands = toolkit.createFFmpegCommands(rid);
			if ( listOfCommands == null ) {
				//resourceService.updateRealTimeEditing(rid,"FLRE","Η διαδικασία δημιουργίας του νέου βίντεο, απέτυχε. Μπορείτε όμως να δοκιμάσετε ξανά!",0);
				this.process_status = "FLRE";
				this.failedProcess = 1;
				return new AsyncResult<>("ERROR");
			}
			if (Thread.currentThread().isInterrupted()) {
				this.cancelProcess = 1;
				this.setProcess_status("Finished");
				return new AsyncResult<>("Canceled");
			}
			// STEP 2: Executes the above FFmpeg commands... //
			if ( toolkit.executeFFmpegCommands(listOfCommands) == -1 ) {
				//resourceService.updateRealTimeEditing(rid,"FLRE","Η διαδικασία δημιουργίας του νέου βίντεο, απέτυχε. Μπορείτε όμως να δοκιμάσετε ξανά!",0);
				this.process_status = "FLRE";
				this.failedProcess = 1;
				return new AsyncResult<>("ERROR");
			}
			if (Thread.currentThread().isInterrupted()) {
				this.cancelProcess = 1;
				this.setProcess_status("Canceled");
				return new AsyncResult<>("Canceled");
			}
			// STEP 3: Creates new versions of the XML files which describes the videolecture and its presentation //
			if ( toolkit.updateXMLFiles(rid) == -1 ) {
				//resourceService.updateRealTimeEditing(rid,"FLRE","Η διαδικασία δημιουργίας του νέου βίντεο, απέτυχε. Μπορείτε όμως να δοκιμάσετε ξανά!",0);
				this.process_status = "FLRE";
				this.failedProcess = 1;
				return new AsyncResult<>("ERROR");
			}
			if (Thread.currentThread().isInterrupted()) {
				this.cancelProcess = 1;
				this.setProcess_status("Canceled");
				return new AsyncResult<>("Canceled");
			}
			resourceService.updateRealTimeEditing(rid,"DARE","Το νέο βίντεο δημιουργήθηκε. Παρακαλώ ελέξτε αν το αποτέλεσμα σας ικανοποιεί",0);
			this.process_status = "DARE";

		} catch (Exception e) {
			logger.error("Real Editing: " + e.getMessage() );
			return new AsyncResult<>("Finished");
		}
		return new AsyncResult<>("OK");
	}

	public String getStatus() {
		return process_status;
	}

	public boolean isCancelled() {
		return cancelProcess == 1;
	}
	public boolean hasFailed() {
		return failedProcess == 1;
	}

}
	
