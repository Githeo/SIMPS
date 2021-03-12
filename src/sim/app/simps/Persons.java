package sim.app.simps;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;
import sim.field.network.*;

public class Persons extends SimState
{
	double yardDiscretization = 1.0;
	public static double yardWidth = 100;
	public static double yardHeight = 100;
	public Continuous2D yard; // = new Continuous2D(yardDiscretization, yardWidth, yardHeight);

	//double forceToCenter = 0.05; // fake force to the center
	// double randomMultiplier = 0.1; // Only If I want a little random movement
	double MAX_FORCE = 1.0; // Deprecated

	public Network buddies = new Network(false);
	
	public static int numOfPersons = 50;
	public static boolean planeToroidal = true; 
	public static double socialRadius = 3;
	public static double distanceFading = 1;
	public static double timeStep = 0.1;
	public static double velocity = 3; // m/s

	public double getVelocity() {return velocity;}
	public static void setVelocity(double velocity) {velocity = velocity;}
	public double getTimeStep() {return timeStep;}
	public static void setTimeStep(double step) {timeStep = step;}
	
	public double getYardDiscretization() {return yardDiscretization;}
	public void setYardDiscretization(double yardDiscretization) {this.yardDiscretization = yardDiscretization;}
	public double getYardWidth() {return yardWidth;}
	public static void setYardWidth(double yardWidthArg) {yardWidth = yardWidthArg;}
	public double getYardHeight() {return yardHeight;}
	public static void setYardHeight(double yardHeightArg) {yardHeight = yardHeightArg;}
	public boolean isPlaneToroidal() {return planeToroidal;}
	public static void setPlaneToroidal(boolean planeToroidalArg) { planeToroidal = planeToroidalArg;}
	
	public static void setNumOfPersons(int value){ if (value>0) numOfPersons = value;}
	public int getNumOfPersons(){ return numOfPersons;}

	public double getSocialRadius() {return socialRadius;}
	public static void setSocialRadius(double socialRadiusArg) {socialRadius = socialRadiusArg;}
	public double getDistanceFading() {return distanceFading;}
	public static void setDistanceFading(double distanceFadingArg) {distanceFading = distanceFadingArg;}
	
	//public double getRandomMultiplier() {return randomMultiplier;}
	//public void setRandomMultiplier(double randomMultiplier) {this.randomMultiplier = randomMultiplier;}


	public Persons(long seed){
		super(seed);
	}
	
	public Persons(long seed, String [] args){
		super(seed);
		
	}
	
	public void start()
	{
		super.start();
		
		yard = new Continuous2D(yardDiscretization, yardWidth, yardHeight); // create the new yard with given size
		yard.clear(); // clear the yard
		buddies.clear(); // clear the network
		
		
		// add some students to the yard
		for(int i = 0; i < numOfPersons; i++)
		{
			Person student = new Person(i);
			
			if(i==2) yard.setObjectLocation(student, new Double2D(yard.getHeight()-1, yard.getWidth() * 0.5));
			else if (i==3) yard.setObjectLocation(student, new Double2D(1, yard.getWidth() * 0.5));
			else if (i==0) yard.setObjectLocation(student, new Double2D(yard.getHeight() *0.5, 1));
			else if (i==1) yard.setObjectLocation(student, new Double2D(yard.getHeight() * 0.5, yard.getWidth()-1));
			else{
			// place them in a random position at the beginning
			yard.setObjectLocation(student,
					new Double2D(yard.getWidth() * 0.5 + random.nextDouble()*10 - 0.5,
							yard.getHeight() * 0.5  + random.nextDouble()*10 - 10.5 * random.nextDouble() ));
			}
			buddies.addNode(student);
			schedule.scheduleRepeating(student);
		}

		// define the acquaintances values of the graph G(V,E)
		Bag students = buddies.getAllNodes(); // Bag is like and ArrayList
		for(int i = 0; i < students.size(); i++){

			Object student = students.get(i);
			Object studentB = null;
			
			for(int j=0; j<students.size(); j++){
				studentB = students.get(j);
				if (j != i ){
					double acquaintances = random.nextDouble();
					buddies.addEdge(student, studentB, new Double(acquaintances));
					// System.out.println(i + " - " + j + " - " + acquaintances); // show the edge values
				}
			}
		}
	}

	
	public static void main(String[] args)
	{
		manageOptions(args);
		doLoop(Persons.class, args);
		System.exit(0);
	} // end main
	
	private static void printSimpsHelp() {
		System.err.println(
				"SIMPS specific options: ----------------------------------------------------------------------------\n\n" + 
				"Format:			[-width W] [-height H] [-toroidal true/false] [-people N]\n"+
				"			[-radius R] [-fading F] [-timestep T] [-velocity V]\n\n" +
				"-width 			plane width in meters, double > 0, default value = 100\n" +
				"-height			plane height in meters, double > 0, default value = 100\n" +
				"-toroidal		'true' in case of toroidal plane (default value), 'false' otherwise\n" +
				"-people			number of people involved in the simulation, integer value >0, default value = 50\n" +
				"-radius			people's social radius, double > 0, default value = 3\n" +
				"-fading			distance fading value, double > 0, default value = 1\n" + 
				"-timestep		seconds per step, double value > 0, default value = 0.1\n" +
				"-velocity		people's walking velocity, double value > 0, default value = 3 m/s\n" +
                "-----------------------------------------------------------------------------------------------------\n\n" +
				"General options:\n");
	}
	
	static String argumentForKey(String key, String[] args, int startingAt) {
		for(int x=0;x<args.length-1;x++)  // key can't be the last string
			if (args[x].equalsIgnoreCase(key))
				return args[x + 1];
		return null;
	}
	
	static boolean keyExists(String key, String[] args, int startingAt) {
		for(int x=0;x<args.length;x++)  // key can't be the last string
			if (args[x].equalsIgnoreCase(key))
				return true;
		return false;
	}
	
	private static void manageOptions(String [] args){
		
		String width_s = argumentForKey("-width", args, 0);
		if (width_s != null)
			try {
				Persons.setYardWidth(Double.parseDouble(width_s));
				if (Double.parseDouble(width_s) <= 0 ) throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'width' value: must be a greater than zero double");}
        
		String height_s = argumentForKey("-height", args, 0);
		if (height_s != null)
			try {
				Persons.setYardHeight(Double.parseDouble(height_s));
				if (Double.parseDouble(height_s) <= 0 ) throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'height' value: must be a greater than zero double");}

		String people_s = argumentForKey("-people", args, 0);
		if (people_s != null)
			try {
				Persons.setNumOfPersons(Integer.parseInt(people_s));
				if (Integer.parseInt(people_s) <= 0 ) throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'people' value: must be an integer greater than zero");}
    
		String toroidal_s = argumentForKey("-toroidal", args, 0);
		if (toroidal_s != null)
			try {
				if (toroidal_s.equals("true"))
					Persons.setPlaneToroidal(true);
				else if (toroidal_s.equals("false"))
					Persons.setPlaneToroidal(false);
				else throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'toroidal' value: must be 'true' (default case) or 'false'");}
    	
		String radius_s = argumentForKey("-radius", args, 0);
		if (radius_s != null)
			try {
				Persons.setSocialRadius(Double.parseDouble(radius_s));
				if (Double.parseDouble(radius_s) <= 0 ) throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'radius' value: must be a double greater than zero");}
    	
		String fading_s = argumentForKey("-fading", args, 0);
		if (fading_s != null)
			try {
				Persons.setDistanceFading(Double.parseDouble(fading_s));
				if (Double.parseDouble(fading_s) <= 0 ) throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'fading' value: must be a double greater than zero");}
    	
		String timeStep_s = argumentForKey("-timestep", args, 0);
		if (timeStep_s != null)
			try {
				Persons.setTimeStep(Double.parseDouble(timeStep_s));
				if (Double.parseDouble(timeStep_s) <= 0 ) throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'timeStep' value: must be a double greater than zero");}
    	
		String velocity_s = argumentForKey("-velocity", args, 0);
		if (velocity_s != null)
			try {
				Persons.setDistanceFading(Double.parseDouble(velocity_s));
				if (Double.parseDouble(velocity_s) <= 0 ) throw new Exception();
		} catch (Exception e) { throw new RuntimeException("Invalid 'velocity' value: must be a double greater than zero");}
    	
		 if (keyExists("-help", args, 0))
			 printSimpsHelp();
		
	}
}