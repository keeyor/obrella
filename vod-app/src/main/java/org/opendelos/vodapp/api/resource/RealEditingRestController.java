package org.opendelos.vodapp.api.resource;

import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.opendelos.model.resources.Resource;
import org.opendelos.vodapp.services.resource.RealEditingFutureService;
import org.opendelos.vodapp.services.resource.RealEditingToolkit;
import org.opendelos.vodapp.services.resource.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RealEditingRestController {

	private static final Logger logger = LoggerFactory.getLogger(RealEditingRestController.class);

	private final RealEditingFutureService realEditingFutureService;
	private final ResourceService resourceService;
	private final RealEditingToolkit toolkit;

	private Future<String> real_editing_task;

	@Autowired
	public RealEditingRestController(RealEditingFutureService realEditingFutureService, ResourceService resourceService, RealEditingToolkit toolkit) {
		this.realEditingFutureService = realEditingFutureService;
		this.resourceService = resourceService;
		this.toolkit = toolkit;
	}


	/**
	 * Handles the command of user to fire the real editing on a resource
	 */
	@RequestMapping(value = "/api/v1/realediting/do/{rid}", method = RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody
	String doRealEditing(HttpServletRequest request, @PathVariable String rid) {

		//>>> Check Resource Status
		// IF DRE or DARE: CANCEL REAL EDITING & return "-1" to Nickorfa
		Resource videolecture;
		try {
			videolecture = resourceService.findById(rid);
		}
		catch (Exception e) {
			return "-1";
		}
		String _real_editing_status;
		if (videolecture.getRteStatus() != null) {
			_real_editing_status = videolecture.getRteStatus().getStatus();
			if (_real_editing_status.equals("DRE") || _real_editing_status.equals("DARE")) {
				return "-1";
			}
		}
		try {
			real_editing_task = realEditingFutureService.editResource(rid);
			logger.info("doRealEditing: _COMPLETED! - ResourceId=" + rid);

		}
		catch (Exception e) {
			logger.error("Real Editing: " + e.getMessage());
		}

		return "1";
	}

	@RequestMapping(value = "/api/v1/realediting/cancel/{rid}", method = RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody
	String cancelRealEditing() {
		real_editing_task.cancel(true);
		realEditingFutureService.setCancelProcess(1);
		realEditingFutureService.setProcess_status("Finished");
		return "Cancelled";
	}

	/**
	 * Handles the authorization of user to accept the real editing result
	 */
	@RequestMapping(value = "/api/v1/realediting/approve/{rid}", method = RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody
	String approveRealEditing(@PathVariable String rid) {

		try {

			if (toolkit.approveNewMedia(rid) == -1) {
				resourceService.updateRealTimeEditing(rid, "FLΑRE", "Κατά την αποδοχή του τελικού αποτελέσματος προέκυψε κάποιο σφάλμα. Παρακαλώ ξεκινήστε τη διαδικασία από την αρχή!", 0);
				return "ERROR";
			}
			resourceService.updateRealTimeEditing(rid, "FSRE", "Η διαδικασία δημιουργίας του νέου βίντεο ολοκληρώθηκε με επιτυχία!", 0);
			return "OK";

		}
		catch (Exception e) {
			logger.error("Real Editing: " + e.getMessage());
			return "ERROR";
		}
	}

	/**
	 * Handles the authorization of user to reject the real editing result
	 */
	@RequestMapping(value = "/api/v1/realediting/reject/{rid}", method = RequestMethod.POST, headers = {"Accept=text/html, text/xml, application/json"})
	public @ResponseBody
	String rejectRealEditing(@PathVariable String rid) {

		try {
			if (toolkit.rejectNewMedia(rid) == -1) {
				resourceService.updateRealTimeEditing(rid, "FLRRE", "Κατά την απόρριψη του τελικού αποτελέσματος προέκυψε κάποιο σφάλμα. Παρακαλώ ξεκινήστε τη διαδικασία από την αρχή!", 0);
				return "ERROR";
			}
			resourceService.updateRealTimeEditing(rid, "FSRE", "Το τελικό αποτέλεσμα απορρίφθηκε. Μπορείτε να δοκιμάσετε ξανά!", 0);
			return "OK";

		}
		catch (Exception e) {
			logger.error("Real Editing: " + e.getMessage());
			return "ERROR";
		}
	}

	@RequestMapping(value = "/api/v1/realediting/status")
	public String getRealEditingStatus() {
		return realEditingFutureService.getStatus();
	}

}
