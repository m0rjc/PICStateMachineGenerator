package uk.me.m0rjc.picstategenerator.xmlDefinitionReader;

interface ChainedSaxHandlerListener
{
	/** 
	 * Call to remove the child handler from the chain and process the event.
	 */
	void childReturned();
}
