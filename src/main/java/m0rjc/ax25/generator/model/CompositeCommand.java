package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.List;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * Composite pattern for Commands
 * @author Richard Corfield
 */
public class CompositeCommand extends Command
{
	private List<Command> m_commands = new ArrayList<Command>();
	
	public CompositeCommand(Command... commands)
	{
		for(Command c : commands)
		{
			m_commands.add(c);
		}
	}
	
	/**
	 * Add a command
	 * @param c
	 * @return
	 */
	public CompositeCommand add(Command c)
	{
		m_commands.add(c);
		return this;
	}
	
	@Override
	public void accept(IModelVisitor visitor)
	{
		for(Command c : m_commands) c.accept(visitor);
	}
}
