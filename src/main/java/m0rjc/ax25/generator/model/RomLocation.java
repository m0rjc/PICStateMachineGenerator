package m0rjc.ax25.generator.model;

/**
 * Representation of a location in ROM, for example a method.
 * 
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
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
