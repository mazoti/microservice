package microservice.models;

import java.util.LinkedList;
import java.util.Set;

/**
 * Model class to format a JSON response:
 * 
 * { "id": "30vbllyb", "status": "active", "urls":
 * ["http://example.com/index.html", "http://example.com/html/index.html"] }
 */
public class SearchResponse {
	private final String id;
	private final String status;
	private final LinkedList<String> urls;

	/**
	 * Initializes variables and copy URLs to local list
	 * @param _id Crawler thread identification
	 * @param _status Crawler thread status: active, done or timeout
	 * @param _urls All urls where the keyword was found
	 */
	public SearchResponse(final String _id, final String _status, final Set<String> _urls) {
		this.urls = new LinkedList<>(_urls);
		this.id = _id;
		this.status = _status;
	}
}
