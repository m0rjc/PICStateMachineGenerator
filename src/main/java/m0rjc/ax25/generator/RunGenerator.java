package m0rjc.ax25.generator;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Provider;

import m0rjc.ax25.generator.cdi.BatchProcessor;
import m0rjc.ax25.generator.cdi.InteractiveProcessor;

import org.jboss.weld.environment.se.StartMain;
import org.jboss.weld.environment.se.events.ContainerInitialized;

/**
 * Entry point to run the generator from the command line.
 * 
 * Each argument is a StateModel.xml file. If no arguments are given then a Swing UI
 * is shown.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class RunGenerator
{
	@Inject
	private Provider<InteractiveProcessor> m_interactiveProcessorProvider;
	
	@Inject
	private Provider<BatchProcessor> m_batchProcessorProvider;
	
//	public static void main(String[] args)
//	{
//		StartMain weldMain = new StartMain(args);
//		WeldContainer weld = weldMain.go();
//	}
	
	public void run(@Observes ContainerInitialized initEvent)
	{
		String[] args = StartMain.getParameters();
		if(args.length == 0)
		{
			runSwingInterface();
		}
		
		BatchProcessor batchRunner = m_batchProcessorProvider.get();
		for(String input : args)
		{
			batchRunner.loadAndProcessDefinition(input);
		}
	}

	private void runSwingInterface()
	{
		InteractiveProcessor swingRunner = m_interactiveProcessorProvider.get();
		swingRunner.run();
	}
}
