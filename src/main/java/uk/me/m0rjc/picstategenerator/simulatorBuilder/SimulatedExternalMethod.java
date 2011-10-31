package uk.me.m0rjc.picstategenerator.simulatorBuilder;

/**
 * Simulate a call to an external method.
 */
class SimulatedExternalMethod
{
	private String m_name;
	private int m_callCount;
	private MockAction m_mockAction;
	
	public SimulatedExternalMethod(String name)
    {
	    super();
	    m_name = name;
    }

	public SimulatedExternalMethod(String name, MockAction mockAction)
    {
	    super();
	    m_name = name;
	    m_mockAction = mockAction;
    }

	
	public void call(Simulation s) throws SimulationException
	{
		if(m_mockAction != null) m_mockAction.run(s);
		m_callCount++;
	}

	public String getName()
	{
		return m_name;
	}
	
	public int getCallCount()
	{
		return m_callCount;
	}

	public void setMockAction(MockAction mockAction)
    {
    	m_mockAction = mockAction;
    }
	
	
}
