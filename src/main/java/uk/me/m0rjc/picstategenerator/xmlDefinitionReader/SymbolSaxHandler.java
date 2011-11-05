package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import uk.me.m0rjc.picstategenerator.model.RomLocation;
import uk.me.m0rjc.picstategenerator.model.StateModel;
import uk.me.m0rjc.picstategenerator.model.SymbolOwnership;
import uk.me.m0rjc.picstategenerator.model.Variable;

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
	 * Define a symbol based on the attributes.
	 * @param attributes XML attributes for the Symbol element
	 * @throws SAXException to report error.
	 */
	private void onSymbol(final Attributes attributes) throws SAXException
	{
		String name = attributes.getValue("name");
		int size = getInt(attributes, "size", 1);
		String loc = attributes.getValue("loc");
		String decl = attributes.getValue("decl");

        SymbolOwnership ownership = readOwnership(decl);
		
		if(loc == null)
		{
			throw new SAXException("Symbol declaration missing 'loc' location information");
		}
		if("rom".equals(loc))
		{
			RomLocation rom = new RomLocation(name, ownership);
			if(!"none".equals(decl))
			{
				m_model.registerExternalMethod(rom);
			}
			return;
		}

		int page = readPage(loc);

		m_variable = new Variable(name, ownership, page, size);
		m_model.addVariable(m_variable);
	}

	/**
	 * Read a SymbolOwnership
	 * @param decl
	 * @return
	 * @throws SAXException
	 */
	private SymbolOwnership readOwnership(String decl) throws SAXException
	{
		if(null == decl) return SymbolOwnership.INTERNAL;
		if("internal".equals(decl)) return SymbolOwnership.INTERNAL;
		if("global".equals(decl)) return SymbolOwnership.GLOBAL;
		if("extern".equals(decl)) return SymbolOwnership.EXTERN;
		if("none".equals(decl)) return SymbolOwnership.NONE;
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
