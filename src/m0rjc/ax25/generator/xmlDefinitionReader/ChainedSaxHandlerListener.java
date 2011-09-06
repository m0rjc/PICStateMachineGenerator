package m0rjc.ax25.generator.xmlDefinitionReader;

public interface ChainedSaxHandlerListener
{
	/** 
	 * Call to remove the child handler from the chain.
	 * The child will then delegate the end element call to the parent to handle,
	 * balancing the start element call that created the child.
	 */
	void childReturned();
}
