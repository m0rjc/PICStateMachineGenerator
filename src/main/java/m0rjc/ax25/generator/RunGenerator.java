package m0rjc.ax25.generator;

import javax.swing.WindowConstants;

import m0rjc.ax25.generator.swingui.SwingRunner;
import m0rjc.ax25.generator.xmlDefinitionReader.XmlDefinitionLoader;

/**
 * Entry point to run the generator from the command line.
 * 
 * Each argument is a StateModel.xml file. If no arguments are given then a Swing UI
 * is shown.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class RunGenerator
{
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			runSwingInterface();
		}
		
		XmlDefinitionLoader xmlDefinitionLoader = new XmlDefinitionLoader();
		for(String input : args)
		{
			xmlDefinitionLoader.loadAndProcessDefinition(input);
		}
	}

	private static void runSwingInterface()
	{
		SwingRunner swingRunner = new SwingRunner();
		swingRunner.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		swingRunner.setVisible(true);
	}
}
