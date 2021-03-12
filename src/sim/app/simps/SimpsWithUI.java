package sim.app.simps;

import sim.portrayal.continuous.*;
import sim.engine.*;
import sim.display.*;
import sim.portrayal.simple.*;
import javax.swing.*;
import java.awt.Color;
import sim.portrayal.network.*;
import sim.portrayal.*;
import java.awt.*;

public class SimpsWithUI extends GUIState
{
	public Display2D display;
	public JFrame displayFrame;
	
	ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();
	NetworkPortrayal2D buddiesPortrayal = new NetworkPortrayal2D();
	
	public static void main(String[] args)
	{
		SimpsWithUI vid = new SimpsWithUI();
		Console c = new Console(vid);
		c.setVisible(true);
	}
	
	public SimpsWithUI() { super(new Persons( System.currentTimeMillis())); }

	public SimpsWithUI(SimState state) { super(state); }
	
	public static String getName() { return "SIMPS"; }
	
	public Object getSimulationInspectedObject() {return state;}
	
	public Inspector getInspector(){
		Inspector i = super.getInspector();
		i.setVolatile(true);
		return i;
	}
	
	public void start()
	{
		super.start();
		setupPortrayals();
	}
	
	public void load(SimState state)
	{
		super.load(state);
		setupPortrayals();
	}
	
	public void setupPortrayals()
	{
		Persons students = (Persons) state;
		// tell the portrayals what to portray and how to portray them
		yardPortrayal.setField( students.yard );
		yardPortrayal.setPortrayalForAll(
				new MovablePortrayal2D(
						new CircledPortrayal2D(
								new LabelledPortrayal2D(
										new OvalPortrayal2D() // to color the balls
										{
											public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
												Person student = (Person)object;
												if (student.getPerceivedSurround() > student.comfortZoneMax) 
													paint = Color.red;
												else if (student.getPerceivedSurround() >= student.comfortZoneMin && student.getPerceivedSurround() <= student.comfortZoneMax)
													paint = Color.green;
												else 
													paint = Color.blue;
												//paint = new Color(differentColor, 0, 255 - differentColor);
												super.draw(object, graphics, info);
											}
										},
										0.5, null, Color.black, false),
										0, 8.0, Color.green, true)));

		// To draw the edges of the network
		buddiesPortrayal.setField( new SpatialNetwork2D(students.yard, students.buddies));
		buddiesPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());

		// reschedule the displayer
		display.reset();
		display.setBackdrop(Color.white);
		// redraw the display
		display.repaint();
	}
	
	public void init(Controller c)
	{
		super.init(c);
		display = new Display2D(600,600,this);
		display.setClipping(false);
		displayFrame = display.createFrame();
		displayFrame.setTitle("SIMPS Display"); 
		c.registerFrame(displayFrame); // so the frame appears in the "Display" list
		displayFrame.setVisible(true);
		// display.attach(buddiesPortrayal, "Buddies"); // display edges
		display.attach( yardPortrayal, "Yard" );
	}
	
	public void quit()
	{
		super.quit();
		if (displayFrame!=null) displayFrame.dispose();
		displayFrame = null;
		display = null;
	}
}
