package microservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainTests {

	@Test
	@DisplayName("help must return -1")
	static void helpTest() {
		assertEquals(-1, Main.help());
	}
}
