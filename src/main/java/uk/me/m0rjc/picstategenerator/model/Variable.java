package uk.me.m0rjc.picstategenerator.model;

import java.util.ArrayList;
import java.util.List;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;


/**
 * A variable used or accessed by the state machine.
 */
public class Variable
{
	/** Special value for access bank on PIC18. */
	public static final int ACCESS_BANK = -1;
	
	/**
	 * Construct.
	 * @param name name for the variable.
	 * @param ownership policy for creating the variable.
	 * @param bank RAM bank it lives in, or {@value #ACCESS_BANK}
	 * @param size bytes to allocate.
	 */
	public Variable(final String name, final Ownership ownership, final int bank, final int size)
	{
		m_name = name;
		m_bank = bank;
		m_size = size;
		m_ownership = ownership;
	}
		
	/**
	 * Information about which code owns a variable, therefore how it must be declared in code.
	 * @author "Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;"
	 *
	 */
	public enum Ownership
	{
		/** A variable that exists only within the generated module. */
		INTERNAL(true, false, false),
		/** A variable that we import using an EXTERN keyword. */
		EXTERN(false, false, true),
		/** A variable that we declare, and export using a GLOBAL keyword. */
		GLOBAL(true, true, false),
		/** 
		 * A variable that is known, but who's declaration and import is handled by literal code blocks.
		 * For example it is defined in an include file. Special Function Registers and state machines
		 * using external modules are the two examples.
		 */
		NONE(false, false, false);
		
		/**
		 * @param mustDeclare true if the variable must be declared in the ASM module.
		 * @param mustExport true if the variable is public, so must be exported by the module.
		 * @param mustImport true if the variable is declared outside the module, so must be imported.
		 */
		private Ownership(final boolean mustDeclare, final boolean mustExport, final boolean mustImport)
		{
			m_mustDeclare = mustDeclare;
			m_mustExport = mustExport;
			m_mustImport = mustImport;
		}

		/**
		 * @return the generated code must declare storage for this variable.
		 */
		public boolean isMustDeclareStorage()
		{
			return m_mustDeclare;
		}
		
		/**
		 * Must the generated code export this variable as GLOBAL.
		 * Must it be included in any generated .inc or .h files?
		 * @return true if the variable is public, so must be exported.
		 */
		public boolean isMustExport()
		{
			return m_mustExport;
		}
		
		/**
		 * Must the generated code declare this variable as EXTERN,
		 * so importing it.
		 * @return true if the variable is defined outside the generated module so must be imported.
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
	 * @return the variable name.
	 */
	public String getName()
	{
		return m_name;
	}

	/**
	 * @return number of bytes to allocate for this variable.
	 */
	public int getSize()
	{
		return m_size;
	}
	
	/**
	 * Declare a flag.
	 * The variable will grow in size for every 8 flags added.
	 * @param name the name of the flag to add.
	 * @return this variable for fluent coding style.
	 */
	public Variable addFlag(final String name)
	{
		m_flags.add(name);
		while( (m_flags.size() /8) >= m_size)
		{
		    m_size++;
		}
		return this;
	}

	/**
	 * Return the bit for the given flag.
	 * First byte has bits 0 to 7.
	 * Second byte has bits 8 to 15.
	 * @param name the name to look up.
	 * @return the bit index for the name.
	 * @throws IllegalArgumentException if the name is not known.
	 */
	public int getBit(final String name)
	{
		int result = m_flags.indexOf(name);
		if(result < 0) 
		{
		    throw new IllegalArgumentException("Flag " + name + " not defined for " + m_name);
		}
		return result;
	}

	/**
	 * If this variable is marked EXTERN then tell the visitor.
	 * Called as part of creating the ASM, the INC and H
     * @param visitor visitor to call.
	 */
	void acceptForExtern(final IModelVisitor visitor)
	{
		if(m_ownership.isMustImport())
		{
			visitor.visitDeclareExternalSymbol(this);
		}
	}

	/**
	 * If this variable is marked EXTERN then tell the visitor.
	 * Called as part of generating the ASM definition.
	 * 
	 * @param visitor visitor to call.
	 */
	void acceptForGlobal(final IModelVisitor visitor)
	{
		if(m_ownership.isMustExport())
		{
			visitor.visitDeclareGlobalSymbol(m_name);
		}
	}

	
	/**
	 * Define myself to the visitor.
	 * @param visitor the visitor.
	 * @param currentRamPage page currently being defined, or -1 for ACCESS
	 */
	void acceptForDeclaration(final IModelVisitor visitor, final int currentRamPage)
	{
		if(m_ownership.isMustDeclareStorage() && isInRamPage(currentRamPage))
		{
			visitor.visitCreateVariableDefinition(this);
		}
	}

	/**
	 * @return Must this variable's storage be declared in the generated code?
	 */
	public boolean isMustDeclareStorage()
	{
		return m_ownership.isMustDeclareStorage();
	}
	
	/**
	 * True if the variable is in the given RAM page.
	 * @param page -1 for ACCESS, 0 to 15 for RAM pages
	 * @return true if in the given page.
	 */
	public boolean isInRamPage(final int page)
	{
		return page == m_bank;
	}

	/**
	 * @return True if the variable is defined in access bank
	 */
	public boolean isAccess()
	{
		return m_bank == ACCESS_BANK;
	}

	/**
	 * @return Bank for this variable or -1 for ACCESS BANK.
	 */
	public int getBank()
	{
		return m_bank;
	}

	/**
	 * Has the variable flags?
	 * @return true if there are flags.
	 */
    public boolean hasFlags()
    {
        return !m_flags.isEmpty();
    }

    /**
     * @return the names of the flags in ascending bit order.
     */
    public String[] getFlagNames()
    {
        return m_flags.toArray(new String[m_flags.size()]);
    }

    /**
     * @return true if the variable is public and must be exported.
     */
    public boolean isMustExport()
    {
        return m_ownership.isMustExport();
    }
}
