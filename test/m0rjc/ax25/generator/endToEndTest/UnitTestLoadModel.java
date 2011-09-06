package m0rjc.ax25.generator.endToEndTest;

import java.io.InputStream;

import m0rjc.ax25.generator.xmlDefinitionReader.XmlDefinitionLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test model loading.
 * This class is initially to give me something to debug with so I can watch behaviour.
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
@RunWith(JUnit4.class)
public class UnitTestLoadModel
{
	@Test
	public void testLoadModel() throws Exception
	{
		
		InputStream in = getClass().getResourceAsStream("endToEndTestModel.xml");
		new XmlDefinitionLoader().loadDefinition(in);
	}
}
