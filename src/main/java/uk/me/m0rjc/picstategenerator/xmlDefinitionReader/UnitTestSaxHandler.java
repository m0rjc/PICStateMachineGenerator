package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.simulatorBuilder.Simulation;
import uk.me.m0rjc.picstategenerator.simulatorBuilder.SimulationException;
import uk.me.m0rjc.picstategenerator.simulatorBuilder.SimulatorBuilder;

/**
 * SAX handler to run unit tests defined in the state definition file.
 * 
 * Handles the state:UnitTestList element
 * 
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
class UnitTestSaxHandler extends ChainedSaxHandler
{
    private static final Logger m_log = Logger.getLogger(UnitTestSaxHandler.class.getName());

    private int m_tests;
    private int m_passed;
    private String m_currentTestName;
    private boolean m_ok;
    private StateModel m_model;
    private Simulation m_simulation;
    private boolean m_testBannerOutput;

    /**
     * Set the model that will be in use.
     * 
     * @param model
     */
    public void setModel(StateModel model)
    {
        m_model = model;
    }

    @Override
    protected void onStartElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
    {
        if ("Test".equals(localName))
        {
            m_tests++;
            m_ok = true;
            m_testBannerOutput = false;
            m_currentTestName = "Test " + m_tests;
            initialseSimulator();
        }
        else if ("Description".equals(localName))
        {
            startReadingText();
        }
        else if ("Input".equals(localName))
        {
            if (!m_testBannerOutput)
                outputBanner();
            startReadingText();
        }
        else if ("AssertEquals".equals(localName))
        {
            if (!m_testBannerOutput)
                outputBanner();
            if (m_ok)
                onAssertEquals(attributes);
        }
        else if ("AssertFlag".equals(localName))
        {
            if (!m_testBannerOutput)
                outputBanner();
            if (m_ok)
                onAssertFlag(attributes);
        }
    }

    @Override
    protected void onEndElement(String uri, String localName, String qName)
            throws SAXException
    {
        if (isHandlingOuterElement())
        {
            m_log.info(String.format(
                    "Tests run: %d tests, %d passed, %d failed", m_tests,
                    m_passed, m_tests - m_passed));
        }
        else if ("Test".equals(localName))
        {
            if (m_ok)
                m_passed++;
            m_simulation = null;
        }
        else if ("Description".equals(localName))
        {
            m_currentTestName = finishReadingText();
        }
        else if ("Input".equals(localName))
        {
            String input = finishReadingText();
            sendInput(input);
        }
    }

    /**
     * Assert that the requested flag has the required value.
     * 
     * @param attributes SAX attributes of the element
     * @throws SAXException on problem reading the XML definition file.
     */
    private void onAssertFlag(final Attributes attributes) throws SAXException
    {
        String variable = getString(attributes, "variable");
        String flag = getString(attributes, "flag");
        boolean value = getBoolean(attributes, "value");

        try
        {
            m_simulation.assertFlag(variable, flag, value);
        }
        catch (SimulationException e)
        {
            m_log.log(Level.SEVERE, String.format(
                    "Test %s: Assertion failed: %s:%s=%b: %s", m_currentTestName, variable, flag, value,
                    e.getMessage()), e);
            m_ok = false;
        }
    }

    /**
     * Assert that the given variable has the given value.
     * 
     * @param attributes SAX attributes of the AssertEquals element
     * @throws SAXException used to report error reading the XML definition.
     */
    private void onAssertEquals(final Attributes attributes) throws SAXException
    {
        String variableName = getString(attributes, "variable");

        String testedValue = "<error>";
        try
        {
            String stringValue = attributes.getValue("string");
            if (stringValue != null)
            {
                testedValue = "'" + stringValue + "'";
                m_simulation.assertChars(variableName, stringValue);
            }

            String numbers = attributes.getValue("numbers");
            if (numbers != null)
            {
                String[] tokens = numbers.split("\\s");
                testedValue = join(tokens, ",");
                byte[] values = new byte[tokens.length];
                for (int i = 0; i < tokens.length; i++)
                {
                    values[i] = (byte) InputSpecificationParser.INSTANCE
                            .parseValue(tokens[i]);
                }
                m_simulation.assertBytes(variableName, values);
            }
        }
        catch (SimulationException e)
        {
            m_log.log(Level.SEVERE, String.format(
                    "Test %s: Assertion failed: %s=%s: %s", m_currentTestName,
                    variableName, testedValue, e.getMessage()));
            m_ok = false;
        }
    }

    /**
     * PERL like join.
     * 
     * @param input array of strings to join
     * @param delimiter delimiter to insert between the elements.
     * @return elements of the array with the delimiter between each element.
     */
    private String join(final String[] input, final String delimiter)
    {
        StringBuilder sb = new StringBuilder();
        for (String value : input)
        {
            sb.append(value);
            sb.append(delimiter);
        }
        int length = sb.length();
        if (length > 0)
        {
            // Remove the extra delimiter
            sb.setLength(length - delimiter.length());
        }
        return sb.toString();
    }

    /**
     * Output the name of the test to the log.
     */
    private void outputBanner()
    {
        m_log.fine("Starting test: " + m_currentTestName);
        m_testBannerOutput = true;
    }

    /**
     * @param input Send the given input to the simulator.
     */
    private void sendInput(final String input)
    {
        m_log.finer("Sending input: " + input);
        try
        {
            m_simulation.acceptInput(input);
        }
        catch (SimulationException e)
        {
            m_log.log(Level.SEVERE,
                    "Simulator failed handling input: " + e.getMessage(), e);
            m_ok = false;
        }
    }

    /**
     * Set up the simulator for a test run.
     * 
     * @throws SAXException
     */
    private void initialseSimulator()
    {
        try
        {
            SimulatorBuilder builder = new SimulatorBuilder();
            m_model.accept(builder);
            m_simulation = builder.getSimulation();
            m_simulation.setInputVariable(m_model.getInputVariable().getName());
        }
        catch (SimulationException e)
        {
            m_log.log(Level.SEVERE,
                    "Simulator failed handling input: " + e.getMessage(), e);
            m_ok = false;
        }
    }

}
