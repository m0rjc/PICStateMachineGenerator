package uk.me.m0rjc.picstategenerator.picAsmBuilder;

/** Methods to write assembler for PIC microcontrollers. */
class PicAssemblyWriter extends GeneratedFileWriter
{
    /** Label to output on the next command. */
    private String m_currentLabel;
    /** Indentation for lines that have no additional indent. */
    private static final int INITIAL_INDENT = 14;
    /**
     * Spaces to add to {@link #INITIAL_INDENT} per level of
     * {@link #getIndent()}.
     */
    private static final int SPACES_PER_INDENT = 2;

    /**
     * @param fileName
     *            file to write to.
     */
    public PicAssemblyWriter(final String fileName)
    {
        super(fileName);
    }

    @Override
    public void startBlockComment()
    {
        write(";------------------------------------------------------------------------------\n");
    }

    @Override
    public void writeBlockCommentLine(final String line)
    {
        write("; ");
        write(line);
        write("\n");
    }

    @Override
    public void endBlockComment()
    {
        write(";------------------------------------------------------------------------------\n");
    }

    @Override
    public void writeComment(final String string)
    {
        writeIndent();
        write("; ");
        write(string);
        write("\n");
    }

    /**
     * Output an assembler label. Will be output on next instruction.
     * 
     * @param label
     *            label to write.
     */
    public void writeLabel(final String label)
    {
        if (m_currentLabel != null)
        {
            write(m_currentLabel + ":\n");
        }
        m_currentLabel = label;
    }

    /**
     * Write a line of assembler. The parameters are separated by commas. In the
     * special case of a null parameter no comma is output.
     * 
     * @param opCode
     *            op-code to write.
     * @param args
     *            assembler arguments to write.
     */
    public void opCode(final String opCode, final String... args)
    {
        if (m_currentLabel != null)
        {
            int characterIndent = getCharacterIndent() - 1;
            String format = String.format("%%-%ds ", characterIndent);
            write(String.format(format, m_currentLabel + ":"));
        }
        else
        {
            writeIndent();
        }
        m_currentLabel = null;

        write(String.format("%-8s ", opCode));

        boolean comma = false;
        for (String arg : args)
        {
            if (arg != null)
            {
                if (comma)
                {
                    write(", ");
                }
                write(arg);
                comma = true;
            }
        }
        write("\n");
    }

    /**
     * Output a section marker.
     * 
     * @param linkerName
     *            name for use in the linker file.
     * @param sectionType
     *            assembler section type (for example UDATA_SHR).
     */
    public void writeSection(final String linkerName, final String sectionType)
    {
        write(String.format("%-12s  %s\n", linkerName, sectionType));
    }

    /**
     * Write the file END marker.
     */
    public void writeEndMarker()
    {
        writeSection("", "END");
    }

    /**
     * Encode <code>name res size</code>.
     * 
     * @param name
     *            the name of the variable in the resulting assembler.
     * @param size
     *            amount of bytes to allocate.
     */
    public void ramResourceAllocation(final String name, final int size)
    {
        write(String.format("%-20s   res .%d\n", name, size));
    }

    @Override
    protected void writeIndent()
    {
        int characterIndent = getCharacterIndent();
        for (int i = characterIndent; i > 0; i--)
        {
            write(" ");
        }
    }

    /**
     * @return The amount of characters to indent.
     * @see #getIndent()
     */
    private int getCharacterIndent()
    {
        return INITIAL_INDENT + getIndent() * SPACES_PER_INDENT;
    }
}
