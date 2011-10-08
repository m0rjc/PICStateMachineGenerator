package m0rjc.ax25.generator.model;

import java.util.ArrayList;
import java.util.List;

import m0rjc.ax25.generator.visitor.IModelVisitor;

/**
 * Composite pattern applied to Precondition.
 * 
 * @author Richard Corfield <m0rjc@m0rjc.me.uk>
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_contents == null) ? 0 : m_contents.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CompositePrecondition other = (CompositePrecondition) obj;
		if (m_contents == null)
		{
			if (other.m_contents != null) return false;
		}
		else if (!m_contents.equals(other.m_contents)) return false;
		return true;
	}
}
