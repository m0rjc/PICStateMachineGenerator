package m0rjc.ax25.generator;

import m0rjc.ax25.generator.model.Command;
import m0rjc.ax25.generator.model.Node;
import m0rjc.ax25.generator.model.Precondition;
import m0rjc.ax25.generator.model.StateModel;
import m0rjc.ax25.generator.model.Transition;
import m0rjc.ax25.generator.model.Variable;

/**
 * Main code to generate and output the state model.
 */
public class GenerateGpsStateModel
{
	private static final int STATE_VARIABLE_PAGE = 2;
	
	/** This can only be read once per input character. It is a FIFO */
	public static final String SFR_EUSART_RECEIVE = "RCREG";
	
	public static final String VARIABLE_INPUT = "gpsInput";
	public static final String VARIABLE_GPS_TIME = "gpsTime";
	public static final String VARIABLE_GPS_QUALITY = "gpsQuality";
	public static final String VARIABLE_GPS_LONGITUDE_HUNDREDTHS = "gpsLongitudeHundredths";
	public static final String VARIABLE_GPS_LONGITUDE_DEGMIN = "gpsLongitudeDeg";
	public static final String VARIABLE_GPS_LATITUDE_HUNDREDTHS = "gpsLatitudeHundredths";
	public static final String VARIABLE_GPS_LATITUDE_DEGMIN = "gpsLatitudeDeg";

	public static final String VARIABLE_GPS_FLAGS = "gpsFlags";
	public static final String GPS_FLAG_GPS_NEW_QUALITY = "FLAG_GPS_NEW_QUALITY";
	public static final String GPS_FLAG_GPS_NEW_POSITION = "FLAG_GPS_NEW_POSITION";
	public static final String GPS_FLAG_GPS_EAST = "FLAG_GPS_EAST";
	public static final String GPS_FLAG_GPS_NORTH = "FLAG_GPS_NORTH";
		
	public static void main(String[] args) throws Exception
	{
		StateModel model = buildmodel();
	}

	public static StateModel buildmodel() throws Exception
	{
		StateModel model = new StateModel("gps");

		Variable input = model.createGlobalAccessVariable(VARIABLE_INPUT, 1);
		
		model.setInputVariable(input);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_TIME, 6);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_QUALITY, 1);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LONGITUDE_DEGMIN, 5);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LONGITUDE_HUNDREDTHS, 2);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LATITUDE_DEGMIN, 4);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_LATITUDE_HUNDREDTHS, 2);
		model.createGlobalPagedVariable(STATE_VARIABLE_PAGE, VARIABLE_GPS_FLAGS, 1)
			.addFlag(GPS_FLAG_GPS_NEW_QUALITY)
			.addFlag(GPS_FLAG_GPS_NEW_POSITION)
			.addFlag(GPS_FLAG_GPS_NORTH)
			.addFlag(GPS_FLAG_GPS_EAST);
		
		Node initial = model.getInitialState();
		Node dollar = model.createNamedNode("dollar");
		initial.addTransition(new Transition().when(Precondition.equals(input, '$')).goTo(dollar));
		
		buildGpGGA(model, dollar);
		return model;
	}

	/**
	 * Define the state model for GPGGA - System fix data
	 * @param model
	 * @param dollar
	 * @throws Exception 
	 */
	private static void buildGpGGA(StateModel model, Node dollar) throws Exception
	{
		Variable input = model.getInputVariable();
		Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);
		
		Node beforeLatLong = dollar
			.addString("GPGGA,")
			.addEntryCondition(Precondition.checkFlag(flags, GPS_FLAG_GPS_NEW_POSITION, false))
			.addNumbers(6, 6, model.getVariable(VARIABLE_GPS_TIME))
			.skipToCommaElse(new Transition().when(Precondition.equals(input, '$')).goTo(dollar));
					
		// Read Latitude and longitude
		Node afterLatLong = createMachineForLatLong(model, beforeLatLong, dollar, "GpGaa");
		
		// Read fix quality
		afterLatLong
			.addString(",")
			.addChoices(
					new Transition()
						.whenInRange(input, '0', '2')
						.doCommand(Command.storeValue(input, model.getVariable(VARIABLE_GPS_QUALITY)),
								   Command.setFlag(flags, GPS_FLAG_GPS_NEW_QUALITY, true))
						.goTo(model.getInitialState()));
	}

	/**
	 * Create a state machine to read Lat,Long
	 * @param model Model to build into
	 * @param entryNode  Node to build on
	 * @param namePrefix prefix for any names I make
	 * @param dollar if we see a $ on the way go here.
	 * @return the node at the end of the machine
	 * @throws Exception
	 */
	private static Node createMachineForLatLong(StateModel model, Node entryNode, Node dollar, final String namePrefix)
		throws Exception
	{
		String stateNameReadLongitude = namePrefix + "ReadLong";
		String stateNameComplete = namePrefix + "DoneLatLong";
		
		Variable input = model.getInputVariable();
		Variable flags = model.getVariable(VARIABLE_GPS_FLAGS);
		
		entryNode
			.addNumbers(4,4,model.getVariable(VARIABLE_GPS_LATITUDE_DEGMIN))
			.addString(".")
			.addNumbers(2,2,model.getVariable(VARIABLE_GPS_LATITUDE_HUNDREDTHS))
			.skipToCommaElse(new Transition().when(Precondition.equals(input, '$')).goTo(dollar))
			.addChoices(
				new Transition().whenEqual(input,'S')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_NORTH, false))
						.goTo(stateNameReadLongitude),
				new Transition().whenEqual(input, 'N')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_NORTH, true))
						.goTo(stateNameReadLongitude));
		
		model.createNamedNode(stateNameReadLongitude)
			.addString(",")
			.addNumbers(5,5,model.getVariable(VARIABLE_GPS_LONGITUDE_DEGMIN))
			.addString(".")
			.addNumbers(2,2,model.getVariable(VARIABLE_GPS_LONGITUDE_HUNDREDTHS))
			.skipToCommaElse(new Transition().when(Precondition.equals(input, '$')).goTo(dollar))
			.addChoices(
					new Transition().whenEqual(input,'E')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_EAST, true))
						.goTo(stateNameComplete),
					new Transition().whenEqual(input, 'W')
						.doCommand(Command.setFlag(flags, GPS_FLAG_GPS_EAST, false))
						.goTo(stateNameComplete));
		
		Node afterLatLong = model.createNamedNode(stateNameComplete);
		afterLatLong.addEntryCommand(Command.setFlag(flags, GPS_FLAG_GPS_NEW_POSITION, true));
		return afterLatLong;
	}
}
