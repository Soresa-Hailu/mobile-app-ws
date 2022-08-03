package com.appsdeveloperblog.ws.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UtilsTest {

	@Autowired
	Utils utils;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGenerateUserId() {
		String userId = utils.generateUserId(30);
		String userId2 = utils.generateUserId(30);

		assertNotNull(userId);
		assertTrue(userId.length() == 30);
		assertTrue(!userId.equalsIgnoreCase(userId2));

	}

	@Test
	void testHasTokenExpired() {

		String token = utils.generateEmailVerificationToken("123455432rew");
		assertNotNull(token);

		boolean tokenExpired = Utils.hasTokenExpired(token);

		assertFalse(tokenExpired);
	}

	@Test
	void testHasTokenNotExpired() {
		
		String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZWtnZXRAZ21haWwuY29tIiwiZXhwIjoxNjQxNjM4Nzc0fQ.exLPv7g1SJEpvE0j-UNCj4JaAvnJHMCF078hy4HOB6273dLcrlZ3LGJ3OUOEhu8BMD2T-DZM0OWmwcGC2yjNeA";
		assertNotNull(expiredToken);

		boolean tokenExpired = Utils.hasTokenExpired(expiredToken);

		assertTrue(tokenExpired);
	}
}
