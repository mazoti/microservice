package microservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.google.gson.Gson;

import microservice.models.SearchResponse;

/**
 * Crawler methods to get web data, parse links, looks for the keyword and
 * converts response to JSON
 *
 */
public class Crawler extends Thread {
	private Set<String> visited = Collections.synchronizedSet(new HashSet<String>());
	private Set<String> urlSet = Collections.synchronizedSet(new HashSet<String>());

	private final Logger logger;

	private final Gson gson;

	private final String baseURL;
	private final String keyword;
	private final String id;

	private String status;

	private final long startTime;
	private final int timeout;

	/**
	 * Initializes variables, sets status to "active" and start time to calculate
	 * the time duration
	 * 
	 * @param _logger  Object to log errors, warnings and informations
	 * @param _baseURL The address of the page to start the crawling
	 * @param _keyword The keyword to searh on each page
	 * @param _id      The crawler thread identification
	 * @param _gson    Object to convert data to JSON string
	 * @param _timeout The durations in seconds the crawler thread will stay looking
	 *                 for the keyword
	 */
	public Crawler(String _baseURL, Logger _logger, String _keyword, String _id, Gson _gson, int _timeout) {
		this.logger = _logger;
		this.baseURL = _baseURL;
		this.keyword = _keyword;
		this.id = _id;
		this.gson = _gson;
		this.timeout = _timeout;

		this.status = "active";
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * Gets crawler's current data and converts to JSON String
	 * 
	 * @return JSON formatted data
	 */
	public String toJSON() {
		return this.gson.toJson(new SearchResponse(this.id, this.getStatus(), this.urlSet));
	}

	private synchronized String getStatus() {
		return this.status;
	}

	private synchronized void setStatus(String _status) {
		this.status = _status;
	}

	/**
	 * Starts the new thread and crawls URLs recursively
	 */
	@Override
	public void run() {
		this.logger.info("Thread id " + this.id + " crawling the keyword \"" + this.keyword + "\"...");
		this.crawl(this.baseURL);
		if (this.getStatus().equals("active")) {
			this.setStatus("done");
		}
	}

	private void crawl(String url) {
		if ((System.currentTimeMillis() - this.startTime) >= this.timeout) {
			this.logger.info("TIMEOUT id: " + this.id + " keyword " + this.keyword);
			this.setStatus("timeout");
			return;
		}

		if (this.visited.contains(url) == true) {
			return;
		}

		this.visited.add(url);

		this.logger.info("Crawling " + url + "...");

		String webData = getData(url, this.logger);

		if (webData == null) {
			return;
		}

		// Case INsensitive
		if (webData.toLowerCase().contains(this.keyword.toLowerCase())) {
			this.urlSet.add(url);
		}

		LinkedList<String> links = parseLinks(webData, this.baseURL);
		if (links != null) {
			for (String link : links) {
				this.crawl(link);
			}
		}
	}

	private static LinkedList<String> parseLinks(String data, String host) {

		LinkedList<String> result = new LinkedList<>();

		// Get data between quotes in href=""
		Pattern pattern = Pattern.compile("href=\"(.*?)\"");
		Matcher matcher = pattern.matcher(data);

		while (matcher.find()) {

			// Remove "href=" and the two quotes
			String address = matcher.group().substring(6, matcher.group().length() - 1);

			if (address.endsWith(".jpg") == true || address.endsWith(".gif") == true || address.endsWith(".css") == true
					|| address.endsWith(".rss") == true || address.endsWith(".js") == true) {
				continue;
			}

			if (address.startsWith("http://") || address.startsWith("https://")) {
				continue;
			}

			if (address.startsWith(host)) {
				result.add(host);
				continue;
			}
			
			if (address.startsWith("../")) {
				result.add(host + address.substring(2));
				continue;
			}

			result.add(host + "/" + address);
		}

		if (result.size() == 0) {
			return null;
		}

		return result;
	}

	private static String getData(String url, Logger logger) {
		URL address;
		URLConnection urlConnection = null;
		StringBuilder content = new StringBuilder();

		logger.info("Getting URL data from " + url + "...");

		try {
			address = new URL(url);
			urlConnection = address.openConnection();
		} catch (IOException e) {
			logger.error(e.toString());
			return null;
		}

		try (InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
			for (String line; ((line = bufferedReader.readLine()) != null);) {
				content.append(line);
			}
		} catch (IOException e) {
			logger.error(e.toString());
			return null;
		}

		return content.toString();
	}

	/**
	 * Override hashCode to use crawler object in a HashMap
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	/**
	 * Override equals to use crawler object in a HashMap
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Crawler other = (Crawler) obj;
		return Objects.equals(this.id, other.id);
	}
}
