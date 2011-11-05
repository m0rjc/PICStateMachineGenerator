package uk.me.m0rjc.picstategenerator.model;

import java.util.ArrayList;
import java.util.List;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;

/**
 * A variable used or accessed by the state machine.
 */
public class Variable
{
    /** Assumed size of a single unit variable in bits. */
    private static final int SIZE_OF_BYTE = 8;
    /** Special value for access bank on PIC18. */
    public static final int ACCESS_BANK = -1;

    /**
     * Construct.
     * 
     * @param name
     *            name for the variable.
     * @param ownership
     *            policy for creating the variable.
     * @param bank
     *            RAM bank it lives in, or {@value #ACCESS_BANK}
     * @param size
     *            bytes to allocate.
     */
    public Variable(final String name, final SymbolOwnership ownership,
            final int bank, final int size)
    {
        m_name = name;
        m_bank = bank;
        m_size = size;
        m_ownership = ownership;
    }

    private String m_name;
    private SymbolOwnership m_ownership;
    private int m_bank;
    private int m_size;
    private List<String> m_flags = new ArrayList<String>();

    /** Eclipse generated HashCode method. */
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

    /** Eclipse generated Equals method. */
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
        if (m_flags == null)
        {
            if (other.m_flags != null)
                return false;
        }
        else if (!m_flags.equals(other.m_flags))
            return false;
        if (m_name == null)
        {
            if (other.m_name != null)
                return false;
        }
        else if (!m_name.equals(other.m_name))
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
     * Declare a flag. The variable will grow in size for every 8 flags added.
     * 
     * @param name
     *            the name of the flag to add.
     * @return this variable for fluent coding style.
     */
    public Variable addFlag(final String name)
    {
        m_flags.add(name);
        while ((m_flags.size() / SIZE_OF_BYTE) >= m_size)
        {
            m_size++;
        }
        return this;
    }

    /**
     * Return the bit for the given flag. First byte has bits 0 to 7. Second
     * byte has bits 8 to 15.
     * 
     * @param name
     *            the name to look up.
     * @return the bit index for the name.
     * @throws IllegalArgumentException
     *             if the name is not known.
     */
    public int getBit(final String name)
    {
        int result = m_flags.indexOf(name);
        if (result < 0)
        {
            throw new IllegalArgumentException("Flag " + name
                    + " not defined for " + m_name);
        }
        return result;
    }

    /**
     * If this variable is marked EXTERN then tell the visitor. Called as part
     * of creating the ASM, the INC and H
     * 
     * @param visitor
     *            visitor to call.
     */
    void acceptForExtern(final IModelVisitor visitor)
    {
        if (isMustImport())
        {
            visitor.visitDeclareExternalSymbol(this);
        }
    }

    /**
     * If this variable is marked GLOBAL then tell the visitor. Called as part
     * of generating the ASM definition.
     * 
     * @param visitor
     *            visitor to call.
     */
    void acceptForGlobal(final IModelVisitor visitor)
    {
        if (isMustExport())
        {
            visitor.visitDeclareGlobalSymbol(m_name);
        }
    }

    /**
     * Define myself to the visitor.
     * 
     * @param visitor
     *            the visitor.
     * @param currentRamPage
     *            page currently being defined, or -1 for ACCESS
     */
    void acceptForDeclaration(final IModelVisitor visitor,
            final int currentRamPage)
    {
        if (isMustDeclareStorage() && isInRamPage(currentRamPage))
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
     * 
     * @param page
     *            -1 for ACCESS, 0 to 15 for RAM pages
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
     * @return Bank for this variable or -1 ({@link #ACCESS_BANK}) for ACCESS BANK.
     */
    public int getBank()
    {
        return m_bank;
    }

    /**
     * @return true if there are flags defined for this variable.
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
     * @return true if the variable is public and must be exported from the generated module.
     */
    public boolean isMustExport()
    {
        return m_ownership.isMustExport();
    }
    
    /**
     * @return true if this variable must be imported into the generated module.
     */
    public boolean isMustImport()
    {
        return m_ownership.isMustImport();
    }

    /**
     * @return true if the variable is implicitly imported, for example by an include file.
     */
    public boolean isImplicitlyImported()
    {
        return m_ownership.isImplicitlyImported();
    }
}
