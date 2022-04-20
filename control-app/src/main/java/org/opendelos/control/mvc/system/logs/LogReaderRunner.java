/* 
     Author: Michael Gatzonis - 12/3/2022 
     obrella
*/
package org.opendelos.control.mvc.system.logs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.opendelos.model.users.OoUserDetails;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LogReaderRunner {

	@RequestMapping(value = {"/admin/system/log-runner"}, method = RequestMethod.GET)
	public String adminDashboard(final Model model)   {

		OoUserDetails editor = (OoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		model.addAttribute("user",editor);

		String logFilePath;
		logFilePath = "/delos-etc/logback/OpenDelosRunner.log";

		/* Apache try */
		List<String> lines = new ArrayList<>();
		try {
			lines = this.readApacheLogFile(logFilePath);
		} catch (IOException e) {
			System.out.println("Cannot read OpenDelosRunner.log");
		}
		model.addAttribute("lines",lines);
		return "admin/system/logs/log-runner";
	}

	private List<String> readApacheLogFile(String logFile) throws IOException {

		List<String> list = new ArrayList<>();

		ReversedLinesFileReader fr = new  ReversedLinesFileReader(new File(logFile), Charset.defaultCharset());
		String ch;
		do {
			ch = fr.readLine();
			if (ch != null && !ch.contains("SCHEDULER REPORT AT") && !ch.contains("SCHEDULER RUN AT")) {
				list.add(ch);
			}
		} while (ch != null);
		fr.close();
		return list;
	}
}
