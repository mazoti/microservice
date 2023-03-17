package microservice;

import microservice.models.ErrorResponse;
import microservice.models.IDResponse;
import microservice.models.SearchRequest;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

import spark.Request;
import spark.Response;

/**
 * Pool of webcrawlers stored in a HashMap for faster access
 */
public class ThreadPool {
	private Map<String, String> safeKeywordID = Collections.synchronizedMap(new HashMap<>());
	private Map<String, Crawler> safeIDCrawler = Collections.synchronizedMap(new HashMap<>());

	private static final Gson gson = new Gson();

	private final Logger logger;
	private final String baseURL;
	private final int timeout;
	private final int nthreads;

	/**
	 * Initializes variables with their addresses
	 * 
	 * @param _logger   Object to log errors, warnings and informations
	 * @param _baseURL  The address of the page to start the crawling
	 * @param _timeout  The durations in seconds the crawler thread will stay
	 *                  looking for the keyword
	 * @param _nthreads The maximum number of crawlers
	 */
	public ThreadPool(Logger _logger, String _baseURL, int _timeout, int _nthreads) {
		this.logger = _logger;
		this.baseURL = _baseURL;
		this.timeout = _timeout;
		this.nthreads = _nthreads;
	}

	private String error(Request request, Response response, int code, String message, String logMessage) {
		this.logger.error(java.time.Clock.systemUTC().instant() + " - " + request.ip() + ":" + request.port() + " - "
				+ logMessage);
		response.type("application/json");
		response.status(code);
		return gson.toJson(new ErrorResponse(message));
	}

	private String idResponse(Request request, Response response, int code, String id, String logMessage) {
		this.logger.info(java.time.Clock.systemUTC().instant() + " - " + request.ip() + ":" + request.port() + " - "
				+ logMessage);
		response.type("application/json");
		response.status(code);
		return gson.toJson(new IDResponse(id));
	}

	/**
	 * Validates the POST JSON input, returns the current keyword search or creates
	 * a new one
	 * 
	 * @param request  Data sent from user
	 * @param response Data that would be send to user
	 * @return JSON string with the ID of the crawler
	 */
	public String post(Request request, Response response) {
		String keyword = gson.fromJson(request.body(), SearchRequest.class).keyword;

		// 400 - Bad request
		if (keyword == null) {
			return this.error(request, response, 400, "keyword is null",
					"POST /search - ERROR: search keyword is null");
		}

		// 400 - Bad request
		if (keyword.length() < 4 || keyword.length() > 32) {
			return this.error(request, response, 200, "keyword size must be between 4 and 32",
					"POST /search - ERROR: keyword size error must be between 4 and 32");
		}

		// 200 - Crawler already exist
		String id = this.safeKeywordID.get(keyword);
		if (id != null) {
			return this.idResponse(request, response, 200, id,
					"POST /search - Crawler for keyword \"" + keyword + "\" already exists");
		}

		// 529 - Site is overloaded
		if (this.safeKeywordID.size() == this.nthreads) {
			return this.error(request, response, 529, "too many threads", "POST /search - ERROR: too many threads");
		}

		// 201 - Created (Create a new crawler in another thread)
		id = UUID.randomUUID().toString().substring(0, 8);

		Crawler crawler = new Crawler(this.baseURL, this.logger, keyword, id, gson, this.timeout);
		crawler.start();

		this.safeKeywordID.put(keyword, id);
		this.safeIDCrawler.put(id, crawler);

		return this.idResponse(request, response, 201, id, "Crawler for the keyword \"" + keyword + "\" created");
	}

	/**
	 * Gets the crawler's id current search results
	 * 
	 * @param request  Data sent from user
	 * @param response Data that would be send to user
	 * @return JSON string with all results found by the crawler
	 */
	public String get(Request request, Response response) {
		String id = request.params("id");

		// 400 - Bad request
		if (id == null) {
			return this.error(request, response, 400, "parameter \"id\" not found",
					"GET /search/:id - ERROR: parameter \\\"id\\\" not found");
		}

		Crawler crawler = this.safeIDCrawler.get(id);

		// 404 - Not found
		if (crawler == null) {
			return this.error(request, response, 404, "crawler \"" + id + "\" not found",
					"GET /search/" + id + " - ERROR: crawler not found");
		}

		// Crawler found
		response.type("application/json");
		response.status(200);

		return crawler.toJSON();
	}
}
