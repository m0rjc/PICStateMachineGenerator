package m0rjc.ax25.generator.cdi;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * CDI factory methods for runtime elements.
 */
public class RuntimeProducers
{
	@Produces
	public Logger getLogger(InjectionPoint injectionPoint)
	{
		String name = injectionPoint.getMember().getDeclaringClass().getName();
		return Logger.getLogger(name);
	}
}
