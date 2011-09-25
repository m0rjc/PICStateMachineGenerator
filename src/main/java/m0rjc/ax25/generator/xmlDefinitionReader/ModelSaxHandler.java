package m0rjc.ax25.generator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import m0rjc.ax25.generator.model.StateModel;

/**
 * SAX handler that builds the Model for a State Generator Run.
 * Corresponds to complex element state:StateModel
 * 
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class ModelSaxHandler extends ChainedSaxHandler
{
	/** Model being built by this Handler and its children */
	private StateModel m_model;
	/** Name of the input variable declared on the Model element */
	private String m_inputVariableName;
	/** Name of the root node declared on the Model element */
	private String m_rootName;
	
	private SymbolSaxHandler m_symbolHandler;
	private NodeSaxHandler m_nodeHandler;
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(isHandlingOuterElement())
		{
			onStartModel(attributes);
		}
		else if("Symbol".equals(localName))
		{
			setChild(m_symbolHandler);
			m_symbolHandler.startElement(uri, localName, qName, attributes);
		}
		else if("Node".equals(localName))
		{
			setChild(m_nodeHandler);
			m_nodeHandler.startElement(uri, localName, qName, attributes);
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
		
		m_symbolHandler = new SymbolSaxHandler(m_model);
		m_nodeHandler = new NodeSaxHandler(m_model);
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
		if(isHandlingOuterElement())
		{
			onEndModel();
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
		m_model.optimiseModel();
	}

	/**
	 * When all symbols have been read, wire up the input variable.
	 */
	private void onEndSymbols()
	{
		m_model.setInputVariable(m_model.getVariable(m_inputVariableName));
	}

	/**
	 * Return the model that has been generated.
	 * @return
	 */
	public StateModel getModel()
	{
		return m_model;
	}
}
