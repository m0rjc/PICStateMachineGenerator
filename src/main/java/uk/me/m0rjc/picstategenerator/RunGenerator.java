package uk.me.m0rjc.picstategenerator;

import javax.swing.WindowConstants;

import uk.me.m0rjc.picstategenerator.swingui.SwingRunner;
import uk.me.m0rjc.picstategenerator.xmlDefinitionReader.XmlDefinitionLoader;

/**
 * Entry point to run the generator from the command line.
 * 
 * Each argument is a StateModel.xml file. If no arguments are given then a
 * Swing UI is shown.
 * 
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public final class RunGenerator
{
    /** Inhibit construction as per checkstyle. */
    private RunGenerator()
    {
    }

    /**
     * Run the generator from the command line.
     * 
     * @param args
     *            command line arguments.
     */
    public static void main(final String[] args)
    {
        if (args.length == 0)
        {
            runSwingInterface();
        }

        XmlDefinitionLoader xmlDefinitionLoader = new XmlDefinitionLoader();
        for (String input : args)
        {
            xmlDefinitionLoader.loadAndProcessDefinition(input);
        }
    }

    /** Run the interactive Swing user interface. */
    private static void runSwingInterface()
    {
        SwingRunner swingRunner = new SwingRunner();
        swingRunner.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        swingRunner.setVisible(true);
    }
}
