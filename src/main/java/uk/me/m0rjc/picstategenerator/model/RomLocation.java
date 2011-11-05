package uk.me.m0rjc.picstategenerator.model;


/**
 * Representation of a location in ROM, for example a method.
 * 
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public class RomLocation
{
	private String m_name;
	private SymbolOwnership m_ownership;

	/**
	 * @param name name of the symbol.
	 * @param ownership responsibility for declaring the symbol.
	 */
	public RomLocation(final String name, final SymbolOwnership ownership)
	{
		m_name = name;
		m_ownership = ownership;
	}

	/** @return the symbol name. */
	public final String getName()
    {
    	return m_name;
    }
	
	/** @return the symbol ownership. */
	public final SymbolOwnership getOwnership()
	{
	    return m_ownership;
	}

	/**
	 * A symbol defined in the generated module. 
	 * @return Must storage be declared in the generated module?
	 */
    public final boolean isMustDeclareStorage()
    {
        return m_ownership.isMustDeclareStorage();
    }

    /**
     * @return Must the generate module export this symbol?
     */
    public final boolean isMustExport()
    {
        return m_ownership.isMustExport();
    }

    /**
     * @return Must the generated module import this symbol?
     *  Note that some symbols are implicitly imported, so will not have this set to true.
     */
    public final boolean isMustImport()
    {
        return m_ownership.isMustImport();
    }
}
