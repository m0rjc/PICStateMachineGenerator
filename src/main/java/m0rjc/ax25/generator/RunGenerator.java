package m0rjc.ax25.generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import m0rjc.ax25.generator.xmlDefinitionReader.XmlDefinitionLoader;

/**
 * Entry point to run the generator from the command line.
 * 
 * Each argv is a StateModel.xml file
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class RunGenerator
{
	private static final Logger s_log = Logger.getLogger(RunGenerator.class.getName());
	
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			System.out.println("Pass as command line options the name(s) of the state XML files to use.");
		}
		
		for(String input : args)
		{
			try
			{
				InputStream in = new FileInputStream(input);
				new XmlDefinitionLoader().loadAndProcessDefinition(in);
			}
			catch (FileNotFoundException e)
			{
				s_log.severe(String.format("Input file '%s' not found", input));
			}
			catch (SAXException e)
			{
				s_log.severe(String.format("Error reading file '%s': %s", input, e.getMessage()));
			}
			catch (IOException e)
			{
				s_log.severe(String.format("Error reading file '%s': %s", input, e.getMessage()));
			}
		}
	}
}
