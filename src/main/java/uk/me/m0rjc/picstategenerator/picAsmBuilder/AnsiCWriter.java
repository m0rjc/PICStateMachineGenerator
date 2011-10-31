package uk.me.m0rjc.picstategenerator.picAsmBuilder;

/**
 * Methods to support writing an ANSI C source file.
 */
class AnsiCWriter extends GeneratedFileWriter
{
    /**
     * Write to the given filename.
     * 
     * @param fileName
     *            file to write to.
     */
    public AnsiCWriter(final String fileName)
    {
        super(fileName);
    }

    @Override
    public void writeComment(final String string)
    {
        write("// ");
        write(string);
        write("\n");
    }

    @Override
    public void endBlockComment()
    {
        writeln(" *****************************************************************************/");
    }

    @Override
    public void writeBlockCommentLine(final String line)
    {
        write(" * ");
        writeln(line);
    }

    @Override
    public void startBlockComment()
    {
        writeln("/*****************************************************************************");
    }

    @Override
    protected void writeIndent()
    {
        int indent = getIndent();
        for (int i = 0; i < indent; i++)
        {
            write("  ");
        }
    }
}
