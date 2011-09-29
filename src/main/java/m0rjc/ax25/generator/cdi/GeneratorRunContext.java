package m0rjc.ax25.generator.cdi;

import java.lang.annotation.Annotation;

import org.jboss.weld.context.AbstractUnboundContext;
import org.jboss.weld.context.beanstore.HashMapBeanStore;

public class GeneratorRunContext extends AbstractUnboundContext
{
	public GeneratorRunContext()
	{
		super(false);
	}

	@Override
	public Class<? extends Annotation> getScope()
	{
		return GeneratorRunScoped.class;
	}

	@Override
	public void activate()
	{
		super.activate();
		setBeanStore(new HashMapBeanStore());
	}
}
