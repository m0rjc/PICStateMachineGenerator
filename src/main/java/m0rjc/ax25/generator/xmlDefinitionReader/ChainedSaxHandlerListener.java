package m0rjc.ax25.generator.xmlDefinitionReader;

interface ChainedSaxHandlerListener
{
	/** 
	 * Call to remove the child handler from the chain and process the event.
	 */
	void childReturned();
}
