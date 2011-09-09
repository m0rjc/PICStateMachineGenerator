package m0rjc.ax25.generator.xmlDefinitionReader;

import m0rjc.ax25.generator.model.Precondition;

import org.xml.sax.SAXException;

/**
 * Callback interface for Condition creation.
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public interface ConditionCreationCallback
{
	/** Callback to create a new Precondition and attach it to wherever it needs to go. */
	void onNewCondition(Precondition condition) throws SAXException;
}