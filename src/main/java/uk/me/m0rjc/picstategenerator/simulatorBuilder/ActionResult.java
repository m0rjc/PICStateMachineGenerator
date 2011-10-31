package uk.me.m0rjc.picstategenerator.simulatorBuilder;

enum ActionResult
{
	/** Continue executing the Transition */
	CONTINUE_TO_NEXT_ACTION,
	/** Exit the current level of the instruction block stack. */
	POP,
	/** RETURN from the state engine code */
	RETURN_FROM_STATE_ENGINE
}
