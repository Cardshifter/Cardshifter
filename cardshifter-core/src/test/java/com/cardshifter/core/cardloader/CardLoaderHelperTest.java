
package com.cardshifter.core.cardloader;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author Frank van Heeswijk
 */
@RunWith(Parameterized.class)
public class CardLoaderHelperTest {
	@Parameters
	public static List<Object[]> data() {
		return Arrays.asList(
			new Object[] { "MAX_HEALTH", "maxhealth" },
			new Object[] { "MAXHEALTH", "maxhealth" },
			new Object[] { "maxhealth", "maxhealth" },
			new Object[] { "MaxHealth", "maxhealth" }
		);
	}
	
	@Parameter(0)
	public String inputTag;
	
	@Parameter(1)
	public String sanitizedTag;
	
	@Test
	public void testSanitizeTag() {
		assertEquals(sanitizedTag, CardLoaderHelper.sanitizeTag(inputTag));
	}
}