package microservice;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Spark;
import static spark.Spark.after;

/**
 * Initializes tools, parses command line configurations and starts service
 *
 */
public class Main {

	static final Logger logger = LoggerFactory.getLogger(Main.class);

	static int help() {
		logger.error("Usage:" + System.lineSeparator()
				+ "\t java -jar crawler-microservice.jar [Bind IP (IPV4 format)] [Port] [BaseURL] [Timeout in seconds] [Max number of threads]"
				+ System.lineSeparator()
				+ "\t java -jar crawler-microservice.jar 127.0.0.1 4567 http://domain.com/index.html 5 128");
		return -1;
	}

	static boolean validateIPV4(final String address) {
		String[] numbers = address.split("\\.", 4);

		if (numbers.length != 4) {
			return false;
		}

		for (String n : numbers) {
			try {
				int bytenum = Integer.parseInt(n);
				if ((bytenum < 0) || (bytenum > 255)) {
					return false;
				}
			} catch (java.lang.NumberFormatException e) {
				logger.error(e.toString());
				return false;
			}
		}

		return true;
	}

	/**
	 * Validates all arguments, creates the thread pool, binds service to ip:port
	 * and maps routes
	 * 
	 * @param args Command line arguments on this order: [Bind IP (IPV4 format)]
	 *             [Port] [BaseURL] [Timeout in seconds] [Max number of threads]
	 */
	public static void main(final String[] args) {
		if (args.length != 5) {
			System.exit(help());
		}

		try {
			// Validates IP address
			if (validateIPV4(args[0]) == false) {
				logger.error("Invalid IP address: " + args[0]);
				System.exit(1);
			}

			// Validates Port
			int port = Integer.parseInt(args[1]);
			if ((port < 1) || (port > 65535)) {
				logger.error("Port must be between 1 and 65535");
				System.exit(2);
			}

			// Validates BaseURL
			URL baseURL = new URL(args[2]);

			// Validates Timeout
			int timeout = Integer.parseInt(args[3]);
			if (timeout < 1) {
				logger.error("Timeout must be greater than 0");
				System.exit(3);
			}

			// Validates the number of threads
			int nthreads = Integer.parseInt(args[4]);
			if (nthreads < 1) {
				logger.error("The number of threads must be greater than 0");
				System.exit(4);
			}

			Spark.ipAddress(args[0]);
			Spark.port(port);

			ThreadPool threadPool = new ThreadPool(logger, baseURL.toString(), timeout * 1000, nthreads);
			Spark.post("/search", (request, response) -> threadPool.post(request, response));
			Spark.get("/search/:id", (request, response) -> threadPool.get(request, response));

		} catch (MalformedURLException e) {
			logger.error(e.toString());
			System.exit(5);
		} catch (java.lang.NumberFormatException e) {
			logger.error(e.toString());
			System.exit(6);
		}

		// Compress all responses
		after((request, response) -> {
			response.header("Content-Encoding", "gzip");
		});

		// User entered CRTL + C
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("CTRL + C, shutting down...");
				spark.Spark.stop();
				System.exit(7);
			}
		});
	}
}
