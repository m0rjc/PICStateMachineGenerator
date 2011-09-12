package m0rjc.ax25.generator.simulatorBuilder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import m0rjc.ax25.generator.model.RomLocation;

/**
 * Simulation of the state engine with methods to check the internal state.
 */
public class Simulation
{
	private SimulatedNode m_currentState;
	private SimulatedVariable m_inputVariable;
	
	private Set<String> m_assemblerSymbols = new HashSet<String>();
	private Map<String, SimulatedNode> m_states = new HashMap<String, SimulatedNode>();
	private Map<String, SimulatedVariable> m_variables = new HashMap<String, SimulatedVariable>();
	private Map<String, SimulatedExternalMethod> m_externalMethods = new HashMap<String, SimulatedExternalMethod>();
	
	public void addNode(SimulatedNode node)
	{
		assertNameUnique(node.getName());
		m_states.put(node.getName(), node);
	}

	/**
	 * Set the inpt variable. It must have been declared.
	 * @param name
	 * @throws SimulationException
	 */
	public void setInputVariable(String name) throws SimulationException
	{
		m_inputVariable = getVariable(name);
	}
	
	public void addVariable(SimulatedVariable variable)
	{
		assertNameUnique(variable.getName());
		m_variables.put(variable.getName(), variable);
	}

	private void assertNameUnique(String name)
	{
		if(!m_assemblerSymbols.add(name))
		{
			throw new IllegalArgumentException("Duplicate assembler label: " + name);			
		}
	}
	
	public void setCurrentState(String name) throws SimulationException
	{
		Log.finest("Simulation entering state: " + name);
		m_currentState = m_states.get(name);
		if(m_currentState == null)
		{
			throw new SimulationException("No state with name " + name);
		}
	}
	
	/** Feed the given string as UTF8 into the state engine */
	public void acceptInput(String string) throws SimulationException
	{
		try {
			byte[] bytes = string.getBytes("UTF8");
			acceptInput(bytes);
		} catch (UnsupportedEncodingException e) {
			throw new SimulationException("Cannot convert input string to byte array.", e);
		}
	}

	/** Feed the given bytes into the state engine */
	private void acceptInput(byte[] bytes) throws SimulationException
	{
		for(byte b : bytes) acceptInput(b);
	}

	/** Feed the given byte into the state engine */
	private void acceptInput(byte b) throws SimulationException
	{
		Log.fine("Simulation setting input: " + Log.formatByte(b));
		m_inputVariable.setValue(b);
		m_currentState.step();
	}

	/**
	 * Assert that the given variable contains the expected text as UTF8
	 * @param variableName
	 * @param expected
	 * @throws SimulationException 
	 */
	public void assertChars(String variableName, String expected) throws SimulationException
	{
		try {
			byte[] bytes = expected.getBytes("UTF8");
			assertBytes(variableName, bytes);
		} catch (UnsupportedEncodingException e) {
			throw new SimulationException("Cannot convert input string to byte array.", e);
		}		
	}


	public void assertBytes(String variableName, byte[] bytes) throws SimulationException
	{
		SimulatedVariable v = getVariable(variableName);
		for(int i = 0; i < bytes.length; i++)
		{
			if(v.getValue(i) != bytes[i])
			{
				throw new SimulationException(String.format("Variable %s, unexpected data at index %d. Expected 0x%x got 0x%x", variableName, i, bytes[i], v.getValue(i)));
			}
		}
	}

	public void assertFlag(String variable, String flagName, boolean expectedResult) throws SimulationException
	{
		SimulatedVariable v = getVariable(variable);
		boolean value = v.getBit(flagName);
		if(value != expectedResult)
		{
			throw new SimulationException(String.format("Unexpected flag %s (variable %s). Expected %s got %s",
														flagName, variable, Boolean.toString(expectedResult), Boolean.toString(value)));			
		}
	}

	SimulatedVariable getVariable(String variableName) throws SimulationException
	{
		SimulatedVariable v = m_variables.get(variableName);
		if(v == null)
		{
			throw new SimulationException("Variable " + variableName + " was not created.");
		}
		return v;
	}

	public void setCurrentState(SimulatedNode node)
	{
		m_currentState = node;
	}

	public int getStateCount()
	{
		return m_states.size();
	}

	/**
	 * Set an action to perform when the method is called.
	 * @param methodName
	 * @param action
	 * @throws SimulationException
	 */
	public void setMockAction(String methodName, MockAction action) throws SimulationException
	{
		SimulatedExternalMethod impl = m_externalMethods.get(methodName);
		if(impl == null) throw new SimulationException("Method " + methodName + " not found (setup exception)");
		impl.setMockAction(action);
	}
	
	/**
	 * Simulate a method call
	 */
	void call(RomLocation method) throws SimulationException
    {
		SimulatedExternalMethod impl = m_externalMethods.get(method.getName());
		if(impl == null) throw new SimulationException("Method " + method.getName() + " not found (runtime exception)");
		impl.call(this);
    }

	/**
	 * Register an externally defined rom location.
	 * It may be a method, so create a stub for it.
	 * @param romLocation
	 */
	void registerExternalSymbol(RomLocation romLocation)
    {
		String name = romLocation.getName();
		assertNameUnique(name);
		m_externalMethods.put(name, new SimulatedExternalMethod(name));
    }

	public void assertMethodCalled(String name) throws SimulationException
	{
		SimulatedExternalMethod impl = m_externalMethods.get(name);
		if(impl == null) throw new SimulationException("Method " + name + " not found (test assertion exception)");
		if(impl.getCallCount() == 0) throw new SimulationException("Method " + name + " was not called.");
	}
}
