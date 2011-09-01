package m0rjc.ax25.generator.simulatorBuilder;

import java.util.HashMap;
import java.util.Map;

public class SimulatedVariable
{
	private final String m_name;
	private final byte[] m_data;
	
	private final Map<String, Integer> m_namedBits = new HashMap<String, Integer>();

	public SimulatedVariable(String name, int size)
	{
		if(size <= 0) throw new IllegalArgumentException("Variable " + name + " must have positive size");
		if(size > 256) throw new IllegalArgumentException("Variable " + name + " too big to fit in a page");
		m_name = name;
		m_data = new byte[size];
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public void registerBit(String name, int index)
	{
		if(index < 0 || index >= m_data.length*8)
		{
			throw new IllegalArgumentException("Bitfield index " + index + " out of range for variable " + m_name + " of size " + m_data.length);
		}
		m_namedBits.put(name, index);
	}

	/** The value as would be seen if this symbol was accessed in assembler */
	public byte getValue()
	{
		return m_data[0];
	}

	/** The value as would be seen if this symbol was accessed in assembler */
	public void setValue(byte value)
	{
		m_data[0] = value;
	}

	public byte getValue(int offset) throws SimulationException
	{
		checkOffset(offset);
		return m_data[offset];
	}

	private void checkOffset(int offset) throws SimulationException
	{
		if(offset >= m_data.length || offset < 0) 
			throw new SimulationException(String.format("Variable index out of bounds: %s[%d]",m_name,offset));
	}
	
	public void setValue(int offset, byte value) throws SimulationException
	{
		checkOffset(offset);
		m_data[offset] = value;
	}
	
	public byte getValue(SimulatedVariable indexer) throws SimulationException
	{
		byte offset = indexer.getValue();
		if(offset >= m_data.length || offset < 0) 
			throw new SimulationException(String.format("Variable index out of bounds: %s[%s = %d]",m_name, indexer.getName(), offset));

		return m_data[offset];
	}
	
	public void setValue(SimulatedVariable indexer, byte value) throws SimulationException
	{
		byte offset = indexer.getValue();
		if(offset >= m_data.length || offset < 0) 
			throw new SimulationException(String.format("Variable index out of bounds: %s[%s = %d]",m_name, indexer.getName(), offset));

		m_data[offset] = value;
	}

	public boolean getBit(int bit) throws SimulationException
	{
		byte octet = getValue(bit/8);
		return (octet & (1 << (bit%8))) != 0;
	}

	public boolean getBit(String name) throws SimulationException
	{
		return getBit(getBitIndex(name));
	}

	
	public void setBit(int bit, boolean value) throws SimulationException
	{
		int octet = getValue(bit/8);
		
		int setMask = (1 << (bit%8));
		int andMask = ~setMask;
		
		octet &= andMask;
		if(value) octet |= setMask;
		
		setValue(bit/8, (byte)octet);
	}
	
	public void setBit(String name, boolean value) throws SimulationException
	{
		setBit(getBitIndex(name), value);
	}
	
	private int getBitIndex(String name) throws SimulationException
	{
		Integer index = m_namedBits.get(name);
		if(index == null)
		{
			throw new SimulationException(String.format("Flag %s not registered on variable %s", name, m_name));
		}
		return index;
	}

}
