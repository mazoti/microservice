package microservice.models;

/**
 * Model class to format a JSON GET response:
 * 
 * { "id": "30vbllyb"}
 */
public class IDResponse {
	private final String id;

	/**
	 * Initializes the id variable to send a JSON GET response to user
	 *
	 * @param _id Crawler thread identification
	 */
	public IDResponse(final String _id) {
		this.id = _id;
	}
}
