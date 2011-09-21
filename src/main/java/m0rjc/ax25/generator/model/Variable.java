package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.List;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * A variable used or accessed by the state machine
 */
public class Variable
{
	/** Special value for access bank on PIC18 */
	public static final int ACCESS_BANK = -1;
	
	public Variable(String name, Ownership ownership, int bank, int size)
	{
		m_name = name;
		m_bank = bank;
		m_size = size;
		m_ownership = ownership;
	}
		
	public enum Ownership
	{
		/** A variable that exists only within the generated module. */
		INTERNAL(true, false, false),
		/** A variable that we import using an EXTERN keyword */
		EXTERN(false, false, true),
		/** A variable that we declare, and export using a GLOBAL keyword */
		GLOBAL(true, true, false),
		/** 
		 * A variable that is known, but who's declaration and import is handled by literal code blocks.
		 * For example it is defined in an include file. Special Function Registers and state machines
		 * using external modules are the two examples.
		 */
		NONE(false, false, false);
		
		private Ownership(boolean mustDeclare, boolean mustExport, boolean mustImport)
		{
			m_mustDeclare = mustDeclare;
			m_mustExport = mustExport;
			m_mustImport = mustImport;
		}

		/**
		 * Must the generated code declare storate for this variable?
		 * @return
		 */
		public boolean isMustDeclareStorage()
		{
			return m_mustDeclare;
		}
		
		/**
		 * Must the generated code export this variable as GLOBAL.
		 * Must it be included in any generated .inc or .h files?
		 */
		public boolean isMustExport()
		{
			return m_mustExport;
		}
		
		/**
		 * Must the generated code declare this variable as EXTERN,
		 * so importing it.
		 */
		public boolean isMustImport()
		{
			return m_mustImport;
		}

		private boolean m_mustDeclare;
		private boolean m_mustExport;
		private boolean m_mustImport;
	}
	
	private String m_name;
	private Ownership m_ownership;
	private int m_bank;
	private int m_size;
	private List<String> m_flags = new ArrayList<String>();

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + m_bank;
		result = prime * result + ((m_flags == null) ? 0 : m_flags.hashCode());
		result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
		result = prime * result
				+ ((m_ownership == null) ? 0 : m_ownership.hashCode());
		result = prime * result + m_size;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		if (m_bank != other.m_bank)
			return false;
		if (m_flags == null) {
			if (other.m_flags != null)
				return false;
		} else if (!m_flags.equals(other.m_flags))
			return false;
		if (m_name == null) {
			if (other.m_name != null)
				return false;
		} else if (!m_name.equals(other.m_name))
			return false;
		if (m_ownership != other.m_ownership)
			return false;
		if (m_size != other.m_size)
			return false;
		return true;
	}

	/**
	 * The variable name
	 */
	public String getName()
	{
		return m_name;
	}

	/**
	 * Bytes to allocate for this variable
	 */
	public int getSize()
	{
		return m_size;
	}
	
	/**
	 * Declare a flag.
	 * The variable will grow in size for every 8 flags added.
	 * @param name
	 * @return
	 */
	public Variable addFlag(String name)
	{
		m_flags.add(name);
		while(m_flags.size() /8 >= m_size) m_size++;
		return this;
	}

	/**
	 * Return the bit for the given flag.
	 * First byte has bits 0 to 7.
	 * Second byte has bits 8 to 15.
	 * @param gpsFlagGpsNewTime
	 * @return
	 */
	public int getBit(String name)
	{
		int result = m_flags.indexOf(name);
		if(result < 0) throw new IllegalArgumentException("Flag " + name + " not defined for " + m_name);
		return result;
	}

	/**
	 * If this variable is marked EXTERN then tell the visitor.
	 * Called as part of creating the ASM, the INC and H
	 * @param visitor
	 */
	void acceptForExtern(IModelVisitor visitor)
	{
		if(m_ownership.isMustImport())
		{
			visitor.visitDeclareExternalSymbol(this);
		}
	}

	/**
	 * If this variable is marked EXTERN then tell the visitor.
	 * Called as part of generating the ASM definition.
	 */
	void acceptForGlobal(IModelVisitor visitor)
	{
		if(m_ownership.isMustExport())
		{
			visitor.visitDeclareGlobalSymbol(m_name);
		}
	}

	
	/**
	 * Define myself to the visitor.
	 * @param visitor
	 * @param currentRamPage page currently being defined, or -1 for ACCESS
	 */
	void acceptForDeclaration(IModelVisitor visitor, int currentRamPage)
	{
		if(m_ownership.isMustDeclareStorage() && isInRamPage(currentRamPage))
		{
			visitor.visitCreateVariableDefinition(getName(), getSize());
			int i = 0;
			for(String flag : m_flags)
			{
				visitor.visitCreateFlagDefinition(flag, i);
				i++;
			}
		}
	}

	/**
	 * Must this variable's storage be declared in the generated code?
	 * @return
	 */
	public boolean isMustDeclareStorage()
	{
		return m_ownership.isMustDeclareStorage();
	}
	
	/**
	 * True if the variable is in the given RAM page
	 * @param page -1 for ACCESS, 0 to 15 for RAM pages
	 * @return
	 */
	public boolean isInRamPage(int page)
	{
		return page == m_bank;
	}

	/**
	 * True if the variable is defined in access bank
	 * @return
	 */
	public boolean isAccess()
	{
		return m_bank == ACCESS_BANK;
	}

	/**
	 * Bank for this variable or -1 for ACCESS BANK.
	 * @return
	 */
	public int getBank()
	{
		return m_bank;
	}
	
}
