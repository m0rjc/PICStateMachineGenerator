package m0rjc.ax25.generator.simulatorBuilder;

enum ActionResult
{
	/** Continue executing the Transition */
	CONTINUE_TO_NEXT_ACTION,
	/** GOTO the next transition */
	NEXT_TRANSITION,
	/** RETURN from the state engine code */
	RETURN_FROM_STATE_ENGINE
}
