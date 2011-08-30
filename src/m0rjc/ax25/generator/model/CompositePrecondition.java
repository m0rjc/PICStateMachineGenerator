package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.List;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * Composite pattern applied to Precondition
 */
public class CompositePrecondition extends Precondition
{
	private List<Precondition> m_contents = new ArrayList<Precondition>();

	public CompositePrecondition(Precondition... conditions)
	{
		for(Precondition p : conditions)
		{
			m_contents.add(p);
		}
	}
	
	public void add(Precondition p)
	{
		m_contents.add(p);
	}
	
	@Override
	public void accept(IModelVisitor visitor)
	{
		for(Precondition p : m_contents)
		{
			p.accept(visitor);
		}
	}

	@Override
	public boolean accepts(Variable variable, int value)
	{
		for(Precondition p: m_contents)
		{
			if(!p.accepts(variable, value)) return false;
		}
		return true;
	}

}
