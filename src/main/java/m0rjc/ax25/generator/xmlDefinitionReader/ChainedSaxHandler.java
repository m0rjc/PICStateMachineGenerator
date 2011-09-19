package m0rjc.ax25.generator.xmlDefinitionReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Support for chaining SAX handlers.
 * SAX handlers are effectively nested allowing details to be handled
 * by handlers that specialise in them.
 * 
 * The handlers exist in a parent/child hierarchy. Each handler remembers
 * the name of the element that enters it. It continues until it sees
 * that element end.
 * 
 * <strong>In this rudimentary version that means that if an element name
 * is used recursively then the handlers must also recurse.</strong>
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class ChainedSaxHandler extends DefaultHandler
{
	private ChainedSaxHandlerListener m_parent;
	private DefaultHandler m_child;
	private String m_outerElementName;
	private boolean m_isOuterElement;
	private boolean m_wantsText;
	private StringBuilder m_textBuffer;
	
	/**
	 * Set future requests to go to the given child.
	 * Will set the parent link on the child so it can return.
	 * @param child
	 */
	protected void setChild(ChainedSaxHandler child)
	{
		m_child = child;
		child.setParent(new ChainedSaxHandlerListener() {
			@Override
			public void childReturned()
			{
				m_child = null;
			}
		});
	}
	
	/**
	 * Tell the parent that this child is returning.
	 * The parent will handle the ending event.
	 */
	private void returnToParent()
	{
		if(m_parent != null)
		{
			m_parent.childReturned();
		}
	}
	
	/**
	 * Set the parent link
	 * @param parent
	 */
	private void setParent(ChainedSaxHandlerListener parent)
	{
		m_parent = parent;
	}
	
	@Override
	public final void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(m_outerElementName == null)
		{
			m_outerElementName = localName;
			m_isOuterElement = true;
		}
		
		if(m_child != null)
		{
			m_child.startElement(uri, localName, qName, attributes);
		}

		if(m_child == null)
		{
			onStartElement(uri, localName, qName, attributes);
		}
		m_isOuterElement = false;
	}

	/**
	 * Handle the start element event from DefaultHandler
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 * @throws SAXException 
	 */
	protected void onStartElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
	}

	@Override
	public final void characters(char[] ch, int start, int length)
			throws SAXException
	{
		if(m_child != null)
		{
			m_child.characters(ch, start, length);
		}
		if(m_child == null)
		{
			onCharacters(ch, start, length);
		}
	}

	/**
	 * Handle the Characters event from DefaultHandler.
	 * The default implementation captures text if {@link #startReadingText()} has been called.
	 * Alternative it throws an exception to say that text is unexpected.
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	protected void onCharacters(char[] ch, int start, int length) throws SAXException
	{	
		if(m_wantsText)
		{
			m_textBuffer.append(ch, start, length);
		}
		else
		{
			throw new SAXException("Characters not expected here: " + new String(ch, start, length));
		}
	}
	
	/**
	 * Provide end element, returning to parent if needed.
	 */
	@Override
	public final void endElement(String uri, String localName, String qName)
			throws SAXException
	{
		if(m_child != null)
		{
			m_child.endElement(uri, localName, qName);
		}
		else if(localName.equals(m_outerElementName))
		{
			returnToParent();
			m_isOuterElement = true;
		}

		if(m_child == null)
		{
			onEndElement(uri, localName, qName);
		}
		
		if(m_isOuterElement) m_outerElementName = null;
		m_isOuterElement = false;
	}

	/**
	 * Handle the endElement event from DefaultHandler
	 */
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
	}
	
	/**
	 * True in onStartElement or onEndElement if we are handling the outer element
	 * @return
	 */
	protected final boolean isHandlingOuterElement()
	{
		return m_isOuterElement;
	}
	
	/**
	 * The name of the element that was used to enter this handler
	 * @return
	 */
	protected final String getOuterElementName()
	{
		return m_outerElementName;
	}
	
	protected void startReadingText()
	{
		if(m_textBuffer != null)
		{
			m_textBuffer.setLength(0);
		}
		else
		{
			m_textBuffer = new StringBuilder();
		}
		m_wantsText = true;
	}
	
	/**
	 * Stop reading text and return what was read.
	 * Clear the buffer.
	 */
	protected String finishReadingText()
	{
		m_wantsText = false;
		String result = m_textBuffer.toString();
		m_textBuffer.setLength(0);
		return result;
	}

	/**
	 * Is text currently being read?
	 * @return
	 */
	protected final boolean isReadingText()
	{
		return m_wantsText;
	}
	
	/**
	 * Return an integer if provided
	 * @param attr
	 * @param qName
	 * @return
	 * @throws SAXException 
	 */
	protected final int getInt(Attributes attr, String qName, int defaultValue) throws SAXException
	{
		String value = attr.getValue(qName);
		if(value == null) return defaultValue;
		try
		{
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e)
		{
			throw new SAXException("Cannot parse number: " + qName + "=" + value);
		}
	}

	/**
	 * Return an integer - mandatory
	 * @param attr
	 * @param qName
	 * @return
	 * @throws SAXException 
	 */
	protected final int getInt(Attributes attr, String qName) throws SAXException
	{
		String value = attr.getValue(qName);
		if(value == null)
		{
			throw new SAXException("Value missing for attribute: " + qName);
		}
		
		try
		{
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e)
		{
			throw new SAXException("Cannot parse number: " + qName + "=" + value);
		}
	}

	
	/**
	 * Read a boolean. If not provided then throw an exception
	 * @param attr
	 * @param qName
	 * @return
	 * @throws SAXException
	 */
	protected final boolean getBoolean(Attributes attr, String qName) throws SAXException
	{
		String value = attr.getValue(qName);
		if(value == null)
		{
			throw new SAXException("Value missing for attribute: " + qName);
		}
		
		value = value.toLowerCase();
		if("true".equals(value) || "yes".equals(value) || "1".equals(value)) return true;
		if("false".equals(value) || "no".equals(value) || "0".equals(value)) return false;
		throw new SAXException("Value " + qName + "=" + value + " was not recognised as an XML boolean");
	}

	/**
	 * Read a mandatory string.
	 * @param attr
	 * @param qName
	 * @return
	 * @throws SAXException if the attribute is missing
	 */
	protected final String getString(Attributes attr, String qName) throws SAXException
	{
		String value = attr.getValue(qName);
		if(value == null)
		{
			throw new SAXException("Value missing for attribute: " + qName);
		}
		return value;
	}
}
