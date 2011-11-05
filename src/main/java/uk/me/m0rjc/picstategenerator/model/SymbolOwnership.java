package uk.me.m0rjc.picstategenerator.model;

/**
 * Information about which code owns a variable, therefore how it must be
 * declared in code.
 * 
 * @author "Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;"
 * 
 */
public enum SymbolOwnership
{
    /** A variable that exists only within the generated module. */
    INTERNAL(true, false, false),
    /** A variable that we import using an EXTERN keyword. */
    EXTERN(false, false, true),
    /** A variable that we declare, and export using a GLOBAL keyword. */
    GLOBAL(true, true, false),
    /**
     * A variable that is known, but who's declaration and import is handled
     * by literal code blocks. For example it is defined in an include file.
     * Special Function Registers and state machines using external modules
     * are the two examples.
     */
    NONE(false, false, false);

    /**
     * @param mustDeclare
     *            true if the variable must be declared in the ASM module.
     * @param mustExport
     *            true if the variable is public, so must be exported by the
     *            module.
     * @param mustImport
     *            true if the variable is declared outside the module, so
     *            must be imported.
     */
    private SymbolOwnership(final boolean mustDeclare, final boolean mustExport,
            final boolean mustImport)
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
     * Must the generated code export this variable as GLOBAL. Must it be
     * included in any generated .inc or .h files?
     * 
     * @return true if the variable is public, so must be exported.
     */
    public boolean isMustExport()
    {
        return m_mustExport;
    }

    /**
     * Must the generated code declare this variable as EXTERN, so importing
     * it.
     * 
     * @return true if the variable is defined outside the generated module
     *         so must be imported.
     */
    public boolean isMustImport()
    {
        return m_mustImport;
    }

    /**
     * @return true if the variable is implicitly imported, for example by an include file.
     */
    public boolean isImplicitlyImported()
    {
        return !(m_mustDeclare || m_mustImport);
    }

    private boolean m_mustDeclare;
    private boolean m_mustExport;
    private boolean m_mustImport;
    
}