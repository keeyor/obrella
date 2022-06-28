/* 
     Author: Michael Gatzonis - 26/1/2021 
     live
*/
package org.opendelos.live.services.scheduler;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opendelos.model.properties.StreamingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class LiveUtils {

	private static final Logger logger = LoggerFactory.getLogger("LiveUtils");

	private final StreamingProperties streamingProperties;

	public LiveUtils(StreamingProperties streamingProperties) {
		this.streamingProperties = streamingProperties;
	}

	public File[] getRecordedVideoFiles(String identity) {

		String uploadPath = streamingProperties.getStorage_alt();

		final String fileTemplate = identity;

		logger.trace("Searching for recorded files in folder:" + uploadPath);
		File dir = new File(uploadPath);
		File[] files = dir.listFiles((dir1, name) -> !name.endsWith(".tmp") && name.startsWith(fileTemplate));
		if (files != null && files.length >0) {
			Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
			for (File videofile : files) {
				logger.trace("GetRecordedVideoFiles:" + videofile.getAbsolutePath());
			}
		}
		return files;
	}
}
