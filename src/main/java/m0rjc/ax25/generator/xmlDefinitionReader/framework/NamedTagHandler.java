package m0rjc.ax25.generator.xmlDefinitionReader.framework;


import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A tag handler that provides the name of the tag it is interested in.
 */
public interface NamedTagHandler extends TagHandler
{
	/** 
	 * Is this tag handler interested in handling this tag?
	 *
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 */
	boolean isInterested(String uri, String localName, String qName, Attributes attributes);
}
