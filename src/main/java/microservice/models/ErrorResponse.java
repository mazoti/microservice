package microservice.models;

/**
 * Model class to format a JSON error response:
 * 
 * { "error": "message"}
 */
public class ErrorResponse {
	private final String error;

	/**
	 * Initializes the message variable to send JSON error response to user
	 *
	 * @param message Message error to send to user
	 */
	public ErrorResponse(final String message) {
		this.error = message;
	}
}
