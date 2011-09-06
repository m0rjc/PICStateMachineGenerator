package m0rjc.ax25.generator.xmlDefinitionReader;

import m0rjc.ax25.generator.model.RomLocation;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Variable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/**
 * Handle symbol declarations within a Model.
 * Corresponds to complex element state:SymbolDeclaration
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class SymbolSaxHandler extends ChainedSaxHandler
{
	private StateModel m_model;
	private Variable m_variable;
	private String m_myName;
	private StringBuilder m_flagName;
	
	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(m_myName == null)
		{
			m_myName = localName;
			onSymbol(attributes);
		}
		else if("Flags".equals(localName))
		{
			// Nothing
		}
		else if("Flag".equals(localName))
		{
			m_flagName = new StringBuilder();
		}
		else
		{
			throw new SAXNotRecognizedException(localName);
		}
	}

	/**
	 * Define a symbol based on the attributes
	 * @param attributes
	 */
	private void onSymbol(Attributes attributes)
	{
		String name = attributes.getValue("name");
		int size = getInt(attributes, "size", 1);
		String loc = attributes.getValue("loc");
		String decl = attributes.getValue("decl");

		if(loc == null)
		{
			throw new SAXException("Symbol declaration missing 'loc' location information");
		}
		if("rom".equals(loc))
		{
			RomLocation rom = new RomLocation(name);
			if(!"none".equals(decl))
			{
				m_model.registerExternalMethod(rom, "extern".equals(decl));
			}
			return;
		}
		
		Variable v;
		if("accessram".equals(loc))
		{
			v = Variable.accessVariable(name, size);
		}
		else if(loc.startsWith("page"))
		{
			int page = Integer.parseInt(loc.substring(4));
			v = Variable.pagedVariable(page, name, size);
		}
		else
		{
			throw new SAXException("Symbol location " + loc + " not recognised");
		}
		
		if("extern".equals(decl))
		{
			m_model.registerExternalVariable(v, true);
		}
		else if("none".equals(decl))
		{
			m_model.registerExternalVariable(v, false);
		}
		else if(decl == null || "internal".equals(decl))
		{
			m_model.createVariable(v, false);
		}
		else if("global".equals(decl))
		{
			m_model.createVariable(v, true);
		}
	}

	@Override
	protected void onCharacters(char[] ch, int start, int length)
			throws SAXException
	{
		if(m_flagName != null)
		{
			m_flagName.append(ch, start, length);
		}
		else
		{
			throw new SAXException("Characters not expected here.");
		}
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
		if("Flag".equals(localName))
		{
			if(m_flagName.length() > 0)
			{
				m_variable.addFlag(m_flagName.toString());
				m_flagName = null;
			}
			else
			{
				throw new SAXException("Flag name was empty");
			}
		}
		else if(m_myName.equals(localName))
		{
			m_myName = null; // Ready for the next
			returnToParent(); // Passes the event back
		}	
	}

	public void setModel(StateModel model)
	{
		m_model = model;
	}

}
