/**
 * 
 */
package microservice.models;

/**
 * Model class to parse user input.
 * Must be a JOSN POST:
 *
 * {"search": "value"}
 */
public class SearchRequest {
	/**
	 * Keyword to look for on each URL
	 */
	public String keyword;
}
