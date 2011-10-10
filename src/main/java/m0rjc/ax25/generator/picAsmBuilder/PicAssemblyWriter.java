package m0rjc.ax25.generator.picAsmBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Methods to write assembler */
class PicAssemblyWriter extends Writer
{
	private boolean m_fileStarted;
	private final Writer m_out;
	private boolean m_error;
	protected Logger m_log = Logger.getLogger(getClass().getName());

	/** Label to output on the next command */
	private String m_currentLabel;

	public PicAssemblyWriter(String fileName)
	{
		Writer out = null;
		try
		{
			out = new FileWriter(fileName);
			m_log.info(String.format("Generating file %s", new File(fileName).getAbsolutePath()));
		}
		catch (IOException e)
		{
			m_error = true;
			m_log.log(Level.SEVERE, "Unable to open output file", e);
		}
		m_out = out;
	}

	/**
	 * Output a big comment line of dashes
	 */
	public void startBlockComment()
	{
		write(";------------------------------------------------------------------------------\n");
	}
	
	/**
	 * Output a line of text as a full line comment
	 */
	public void writeBlockCommentLine(String line)
	{
		write("; ");
		write(line);
		write("\n");
	}
	
	/**
	 * Output a big comment line of dashes
	 */
	public void endBlockComment()
	{
		write(";------------------------------------------------------------------------------\n");		
	}
	
	/**
	 * Output inline comment for assembler
	 */
	public void writeComment(String string)
	{
		write("              ; ");
		write(string);
		write("\n");
	}
	
	/** Output an assembler label. Will be output on next instruction. */
	public void writeLabel(String label)
	{
		if(m_currentLabel != null)
		{
			write(m_currentLabel + ":\n");
		}
		m_currentLabel = label;
	}

	
	/** Write a line of assembler.
	 * The parameters are separated by commas.
	 * In the special case of a null parameter no comma is output.
	 */
	public void opCode(String opCode, String... args)
	{
		if(m_currentLabel != null)
		{
			write(String.format("%-13s ", m_currentLabel + ":"));
		}
		else
		{
			write("              ");
		}
		m_currentLabel = null;
		
		write(String.format("%-8s ", opCode));
		
		boolean comma = false;
		for(String arg : args)
		{
			if(arg != null)
			{
				if(comma) write(", ");
				write(arg);
				comma = true;
			}
		}
		write("\n");
	}

	/**
	 * Output a section marker
	 * @param linkerName
	 * @param sectionType
	 */
	public void writeSection(String linkerName, String sectionType)
	{
		write(String.format("%-12s  %s\n", linkerName, sectionType));
	}
	
	/**
	 * Write the END marker
	 */
	public void writeEndMarker()
	{
		writeSection("","END");
	}
	
	/** Encode <code>name res size</code> */
	public void ramResourceAllocation(String name, int size)
	{
		write(String.format("%-20s   res .%d\n", name, size));
	}

	/** Write a blank line to the output file */
	public void blankLine()
	{
		write("\n");
	}
	
	/** Write to the output, handling error if needed */
	public void write(String line)
	{
		if(!m_fileStarted)
		{
			m_fileStarted = true; // Prevent infinite recursion
			startFile();
		}
		
		if(!m_error)
		{
			try
			{
				m_out.write(line);
			}
			catch (IOException e)
			{
				m_error = true;
				m_log.log(Level.SEVERE, "Unable to write to output file", e);
				try
				{
					m_out.close();
				}
				catch (Exception e2) {}
			}
		}
	}

	/** Output the start of file header */
	private void startFile()
	{
		startBlockComment();
		writeBlockCommentLine("This file was auto-generated by PICStateGenerator. Do not edit.");
		writeBlockCommentLine("Generated: " + new Date());
		endBlockComment();
	}

	@Override
	public void close() throws IOException
	{
		m_out.close();
	}

	@Override
	public void flush() throws IOException
	{
		m_out.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		m_out.write(cbuf, off, len);
	}
}
