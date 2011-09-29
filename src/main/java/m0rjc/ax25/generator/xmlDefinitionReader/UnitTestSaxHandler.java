package m0rjc.ax25.generator.xmlDefinitionReader;

import java.util.logging.Level;

import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.simulatorBuilder.Simulation;
import m0rjc.ax25.generator.simulatorBuilder.SimulationException;
import m0rjc.ax25.generator.simulatorBuilder.SimulatorBuilder;
import m0rjc.ax25.generator.xmlDefinitionReader.framework.ChainedSaxHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX handler to run unit tests defined in the state definition file.
 * 
 * Handles the state:UnitTestList element
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class UnitTestSaxHandler extends ChainedSaxHandler
{
	private int m_tests;
	private int m_passed;
	private String m_currentTestName;
	private boolean m_ok;
	private StateModel m_model;
	private Simulation m_simulation;
	private boolean m_testBannerOutput;

	/**
	 * Set the model that will be in use.
	 * @param model
	 */
	public void setModel(StateModel model)
	{
		m_model = model;
	}
	
	@Override
	protected void onStartElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
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
			if (!m_testBannerOutput) outputBanner();
			startReadingText();
		}
		else if ("AssertEquals".equals(localName))
		{
			if (!m_testBannerOutput) outputBanner();
			if (m_ok) onAssertEquals(attributes);
		}
		else if ("AssertFlag".equals(localName))
		{
			if (!m_testBannerOutput) outputBanner();
			if (m_ok) onAssertFlag(attributes);
		}
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName) throws SAXException
	{
		if(isHandlingOuterElement())
		{
			UnitTestLog.info(String.format("Tests run: %d tests, %d passed, %d failed", m_tests, m_passed, m_tests - m_passed));
		}
		else if ("Test".equals(localName))
		{
			if (m_ok) m_passed++;
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
	 * Assert that the requested flag has the required value
	 * @param attributes
	 * @throws SAXException
	 */
	private void onAssertFlag(Attributes attributes) throws SAXException
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
			UnitTestLog.log(Level.SEVERE, String.format("Assertion failed: %s:%s=%b: %s", variable, flag, value, e.getMessage()), e);
			m_ok = false;
		}
	}

	/**
	 * Assert that the given variable has the given value
	 * @param attributes
	 * @throws SAXException
	 */
	private void onAssertEquals(Attributes attributes) throws SAXException
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
					values[i] = (byte) InputSpecificationParser.INSTANCE.parseValue(tokens[i]);
				}
				m_simulation.assertBytes(variableName, values);
			}
		}
		catch (SimulationException e)
		{
			UnitTestLog.log(Level.SEVERE, String.format("Assertion failed: %s=%s: %s", variableName, testedValue, e.getMessage()), e);
			m_ok = false;
		}
	}

	/**
	 * PERL like join
	 * @param input
	 * @param delimiter
	 * @return
	 */
	private String join(String[] input, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		for(String value : input)
		{
			sb.append(value);
			sb.append(delimiter);
		}
		int length = sb.length();
		if(length > 0)
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
		UnitTestLog.fine("Starting test: " + m_currentTestName);
		m_testBannerOutput = true;
	}

	/**
	 * Send the given input to the simulator
	 */
	public void sendInput(String input)
	{
		UnitTestLog.finer("Sending input: " + input);
		try
		{
			m_simulation.acceptInput(input);
		}
		catch (SimulationException e)
		{
			UnitTestLog.log(Level.SEVERE, "Simulator failed handling input: " + e.getMessage(), e);
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
			UnitTestLog.log(Level.SEVERE, "Simulator failed handling input: " + e.getMessage(), e);
			m_ok = false;
		}
	}

}
