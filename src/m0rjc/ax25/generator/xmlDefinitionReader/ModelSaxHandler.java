package m0rjc.ax25.generator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import m0rjc.ax25.generator.model.StateModel;

/**
 * SAX handler that builds the Model
 */
public class ModelSaxHandler extends ChainedSaxHandler
{
	private StateModel m_model;
	private String m_rootName;
	private String m_inputVariableName;
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes)
	{
		if("Model".equals(localName))
		{
			onStartModel(attributes);
		}
		else if("Symbol".equals(localName))
		{
			onSymbol(attributes);
		}
		else if("Node".equals(localName))
		{
			onNode(attributes);
		}
	}
		
	/**
	 * Start of the model
	 * @param attributes
	 */
	private void onStartModel(Attributes attributes)
	{
		String modelName = attributes.getValue("name");
		m_rootName = attributes.getValue("root");
		m_inputVariableName = attributes.getValue("inputVariable");
		
		m_model = new StateModel(modelName);
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
		if("Model".equals(localName))
		{
			onEndModel();
			returnToParent();
		}
		if("Symbols".equals(localName))
		{
			onEndSymbols();
		}
	}

	/**
	 * When the model has been read, wire up the root state
	 */
	private void onEndModel()
	{
		m_model.setRoot(m_model.getNode(m_rootName));
	}

	/**
	 * When all symbols have been read, wire up the input variable.
	 */
	private void onEndSymbols()
	{
		m_model.setInputVariable(m_model.getVariable(m_inputVariableName));
	}
	
	
}
