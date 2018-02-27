package com.msc.serverbrowser.data.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.msc.serverbrowser.logging.Logging;

/**
 * Tests the {@link Property} class for correctness.
 *
 * @author marcel
 * @since 27.02.2018
 */
public class PropertyTest {

	/**
	 * Checks if every {@link Property} has a unique ID, this is important, because non-unique IDs
	 * will cause data loss due to equal property keys.
	 */
	@Test
	public void testForUniqueIds() {
		Logging.info("Running test: testForUniqueIds");
		final int amountOfUniqueIds = Arrays
				.stream(Property.values())
				.map(Property::getId)
				.collect(Collectors.toSet())
				.size();

		assertEquals(amountOfUniqueIds, Property.values().length, "The amount of properties has to match the amount of unique ids.");
	}
}
