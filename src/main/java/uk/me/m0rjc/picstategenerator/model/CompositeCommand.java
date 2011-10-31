package uk.me.m0rjc.picstategenerator.model;

import java.util.ArrayList;
import java.util.List;

import uk.me.m0rjc.picstategenerator.visitor.IModelVisitor;


/**
 * Composite pattern for Commands
 *
 * @author Richard Corfield &lt;m0rjc@m0rjc.me.uk&gt;
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
	public void accept(StateModel model, IModelVisitor visitor)
	{
		for(Command c : m_commands) c.accept(model, visitor);
	}
}
