/* 
     Author: Michael Gatzonis - 16/2/2021 
     live
*/
package org.opendelos.live.services.resource.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadStream implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ReadStream.class);

	String name;
	InputStream is;
	Thread thread;
	public ReadStream(String name, InputStream is) {
		this.name = name;
		this.is = is;
	}
	public void start () {
		thread = new Thread (this);
		thread.start ();
	}
	public void run () {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader (isr);
			while (true) {
				String s = br.readLine ();
				if (s == null) break;
				//logger.trace ("[" + name + "] " + s);
			}
			is.close ();
		} catch (Exception ex) {
			System.out.println ("Problem reading stream " + name + "... :" + ex);
			ex.printStackTrace ();
		}
	}
}
