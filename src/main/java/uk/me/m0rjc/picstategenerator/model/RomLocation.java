package uk.me.m0rjc.picstategenerator.model;

/**
 * Representation of a location in ROM, for example a method.
 * 
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
 */
public class RomLocation
{
	private String m_name;

	public RomLocation(String name)
	{
		m_name = name;
	}

	public String getName()
    {
    	return m_name;
    }
}
