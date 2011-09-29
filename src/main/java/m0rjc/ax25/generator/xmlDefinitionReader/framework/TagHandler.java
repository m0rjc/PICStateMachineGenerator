package m0rjc.ax25.generator.xmlDefinitionReader.framework;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX handler to handle a tag in an XML document.
 * A tree of chained handlers is used. When a handler sees a tag that is to
 * be handled by a child handler it performs the following calls:
 * <ol>
 *  <li>{@link #setParent(TagHandler)} to set the parent
 *  <li>{@link #startElement(String, String, String, Attributes)} to pass the starting element.
 * </ol>
 * 
 * The parent is then expected to delegate all SAX events to the child until the child calls
 * {@link #childReturned()}.
 * 
 * Tag Handlers are expected to be reusable. They may also be used for more than one element name,
 * so must store the name of the element they were started with when startElement is called.
 * 
 * The {@link ChainedSaxHandler} class provides a convenient implementation pattern.
 */
interface TagHandler
{
	/**
	 * Set the parent handler. When this handler has finished handling the
	 * tag 
	 */
	void setParent(TagHandler parent);
	
	/** 
	 * Call to remove the child handler from the chain and process the event.
	 * This is expected to be called from within the {@link #endElement(String, String, String)}
	 * handler. The parent's {@link #endElement(String, String, String)} handler will then be
	 * called.
	 */
	void childReturned();

	/**
	 * Handle a forwarded startElement event from the SAX handler.
	 * The element that result in this tag handler being called will be passed this way.
	 * 
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 * @throws SAXException
	 */
	void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException;

	/**
	 * Handle a forwarded characters event from the SAX handler.
	 *
	 * @see DefaultHandler#characters(char[], int, int)
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	void characters(char[] ch, int start, int length)
		throws SAXException;

	/**
	 * Handle a forwarded endElement from the SAX handler.
	 * It is from here that the TagHandler is expected to eventually
	 * return control by calling {@link #childReturned()} on its parent.
	 *
	 * @see DefaultHandler#endElement(String, String, String)
	 * @param uri
	 * @param localName
	 * @param qName
	 * @throws SAXException
	 */
	void endElement(String uri, String localName, String qName)
		throws SAXException;
}
