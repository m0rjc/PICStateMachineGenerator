package m0rjc.ax25.generator.xmlDefinitionReader;

import m0rjc.ax25.generator.model.RomLocation;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Variable;
import m0rjc.ax25.generator.model.Variable.Ownership;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/**
 * Handle symbol declarations within a Model.
 * Corresponds to complex element state:SymbolDeclaration
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
class SymbolSaxHandler extends ChainedSaxHandler
{
	/** Model under construction */
	private final StateModel m_model;
	/** Variable under construction */
	private Variable m_variable;
	
	public SymbolSaxHandler(StateModel model)
	{
		m_model = model;
	}

	@Override
	protected void onStartElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		if(isHandlingOuterElement())
		{
			onSymbol(attributes);
		}
		else if("Flags".equals(localName))
		{
			// Nothing
		}
		else if("Flag".equals(localName))
		{
			if(m_variable == null)
			{
				throw new SAXException("FLAGS not supported in ROM symbols");
			}
			startReadingText();
		}
		else
		{
			throw new SAXNotRecognizedException(localName);
		}
	}

	/**
	 * Define a symbol based on the attributes
	 * @param attributes
	 * @throws SAXException 
	 */
	private void onSymbol(Attributes attributes) throws SAXException
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

		int page = readPage(loc);
		Variable.Ownership ownership = readOwnership(decl);

		m_variable = new Variable(name, ownership, page, size);
		m_model.addVariable(m_variable);
	}

	/**
	 * Read a SymbolOwnership
	 * @param decl
	 * @return
	 * @throws SAXException
	 */
	private Ownership readOwnership(String decl) throws SAXException
	{
		if(null == decl) return Ownership.INTERNAL;
		if("internal".equals(decl)) return Ownership.INTERNAL;
		if("global".equals(decl)) return Ownership.GLOBAL;
		if("extern".equals(decl)) return Ownership.EXTERN;
		if("none".equals(decl)) return Ownership.NONE;
		throw new SAXException("Unrecognised variable ownership: decl=" + decl);
	}

	/**
	 * Determine the numeric page from a loc declaration.
	 * ACCESS and ROM return -1.
	 * @param loc
	 * @return
	 * @throws SAXException
	 */
	private int readPage(String loc) throws SAXException
	{
		if(loc.startsWith("page"))
		{
			try
			{
				int page = Integer.parseInt(loc.substring(4));
				if(page < 0 || page > 15) throw new SAXException("RAM page " + page + " is not valid.");
				return page;
			}
			catch(NumberFormatException e)
			{
				throw new SAXException("Cannot decode RAM page " + loc + ".", e);
			}
		}
		return Variable.ACCESS_BANK;
	}

	@Override
	protected void onEndElement(String uri, String localName, String qName)
			throws SAXException
	{
		if("Flag".equals(localName))
		{
			String flagName = finishReadingText();
			if(flagName.length() > 0)
			{
				m_variable.addFlag(flagName.toString());
			}
			else
			{
				throw new SAXException("Flag name was empty");
			}
		}
		else if(isHandlingOuterElement())
		{
			m_variable = null;
		}	
	}
}
