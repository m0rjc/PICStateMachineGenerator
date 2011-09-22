package m0rjc.ax25.generator.xmlDefinitionReader;

import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.picAsmBuilder.Pic18AsmBuilder;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Sax handler to call a PIC Assembly builder.
 * Handles the state:PicOutput element.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class PicOutputSaxHandler extends ChainedSaxHandler
{
	private final StateModel m_model;
	private Pic18AsmBuilder m_builder;
	
	public PicOutputSaxHandler(StateModel model)
	{
		m_model = model;
	}

	/** The builder instance that will be used to output the assembler */
	public Pic18AsmBuilder getBuilder()
	{
		return m_builder;
	}

	/** The builder instance that will be used to output the assembler */
	public void setBuilder(Pic18AsmBuilder builder)
	{
		m_builder = builder;
	}

	@Override
	protected void onStartElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if(isHandlingOuterElement())
		{
			onStart(attributes);
		}
		else if("Include".equals(localName) || "ReturnLine".equals(localName))
		{
			startReadingText();
		}
	}

	/**
	 * Handle the outer element. Set up the builder based on attributes.
	 * @param attributes
	 */
	private void onStart(Attributes attributes) throws SAXException
	{
		String processor = attributes.getValue("processor");
		if(processor != null) m_builder.setProcessor(processor);
		
		String outputBaseName = attributes.getValue("outputBaseName");
		if(outputBaseName != null) m_builder.setFileBaseName(outputBaseName);
		
		boolean largeModel = getBoolean(attributes, "largeRomModel", false);
		m_builder.setLargeRomModel(largeModel);		
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName) throws SAXException
	{
		if("Include".equals(localName))
		{
			m_builder.addInclude(finishReadingText());
		}
		else if("ReturnLine".equals(localName))
		{
			m_builder.addReturnLine(finishReadingText());
		}
		else if(isHandlingOuterElement())
		{
			m_model.accept(m_builder);
		}
	}
}
