/* 
     Author: Michael Gatzonis - 14/11/2020 
     live
*/
package org.opendelos.model.common;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.springframework.web.util.UriComponentsBuilder;

public class SearchUtils {

public static String prepareSearchLinksForParam(String urlString, String param, String value) throws UnsupportedEncodingException {

	URI skipUrlString;
	 if (urlString.contains("skip")) {
		skipUrlString = replaceQueryParam(urlString, param, value);
	}
	else {
		skipUrlString = addQueryParam(urlString,param, value);
	}

	String url= skipUrlString.toString();
	url = URLDecoder.decode(url, "UTF-8");

	return url;
}

private static URI replaceQueryParam(String urlString, String param, String value) {

	return UriComponentsBuilder.fromHttpUrl(urlString)
			.replaceQueryParam(param, value)
			.build().toUri();
}
private static URI addQueryParam(String urlString, String param, String value) {

	return UriComponentsBuilder.fromHttpUrl(urlString)
			.queryParam(param,value)
			.build().toUri();
}
}
