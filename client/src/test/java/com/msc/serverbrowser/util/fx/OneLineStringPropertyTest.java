package com.msc.serverbrowser.util.fx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author marcel
 * @since Sep 21, 2017
 */
@SuppressWarnings("javadoc")
public class OneLineStringPropertyTest
{

	@Test
	public void test()
	{
		final OneLineStringProperty property = new OneLineStringProperty();

		Assertions.assertEquals("", property.getValue());

		property.setValue("");
		Assertions.assertEquals("", property.getValue());

		property.setValue(null);
		Assertions.assertEquals("", property.getValue());

		property.setValue("test");
		Assertions.assertEquals("test", property.getValue());

		property.setValue(" test test ");
		Assertions.assertEquals(" test test ", property.getValue());

		property.setValue("test" + System.lineSeparator() + "test");
		Assertions.assertEquals("testtest", property.getValue());

	}
}
