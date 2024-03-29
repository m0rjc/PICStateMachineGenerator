package uk.me.m0rjc.picstategenerator.endToEndTest;

import java.io.InputStream;
import java.util.logging.LogManager;


import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import uk.me.m0rjc.picstategenerator.xmlDefinitionReader.XmlDefinitionLoader;

/**
 * Test model loading.
 * This class is initially to give me something to debug with so I can watch behaviour.
 * 
 * @author "Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;"
 */
@RunWith(JUnit4.class)
public class TestLoadModel
{
	@BeforeClass
	public static void systemSetup() throws Exception
	{
		// Set up logging
		InputStream in = TestLoadModel.class.getResourceAsStream("logging.properties");
		if(in != null)
		{
			LogManager.getLogManager().readConfiguration(in);
		}
	}
	
	@Test
	public void testLoadModel() throws Exception
	{
		
		InputStream in = getClass().getResourceAsStream("endToEndTestModel.xml");
		new XmlDefinitionLoader().loadAndProcessDefinition(in);
	}
}
