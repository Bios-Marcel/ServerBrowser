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

		property.setValue("dsf");
		Assertions.assertEquals("dsf", property.getValue());

		property.setValue(" asd test ");
		Assertions.assertEquals("asd test", property.getValue());

		property.setValue("tesdsdst" + System.lineSeparator() + "tesdsdst");
		Assertions.assertEquals("tesdsdst tesdsdst", property.getValue());
	}
}
