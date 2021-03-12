package sim.app.simps;

import java.util.ArrayList;
import java.util.Random;
import ec.util.MersenneTwisterFast;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import sim.field.network.*;


public class Person implements Steppable
{
	//public static final double MAX_FORCE = 1.0;
	MersenneTwisterFast simRandom = new MersenneTwisterFast();
	Random javaRandom = new Random();
	int id;
	double maxDistance;
	double sociability = simRandom.nextGaussian() + 2.5 * 2.5; // to avoid negative variance = 2.5
	double tolerance = simRandom.nextDouble()*0.7 + 0.1;
	double comfortZoneMin = Math.floor(sociability - sociability*tolerance); // TODO check if negative!
	double comfortZoneMax = Math.ceil(sociability + sociability*tolerance);
	double distanceFading = 1;
	double perceivedSurround = tolerance;
	boolean firstIteration = true;
	boolean socializeBehavior = true;
	double excitation = 0.0;
	double excitationPartial = 0.0;
	int step = -1;

	ArrayList<Double> acq = new ArrayList<Double>();

	public Person(int id){
		this.id = id;
	}

	//public String toString() { return "[" + this.id + "] PerceivedSurround="+ getPerceivedSurround() +
	//		" in range ["+comfortZoneMin+" - "+comfortZoneMax+"]";}
	public String toString() { return  (new Integer(this.id)).toString();}

	public void step(SimState state)
	{
		Persons students = (Persons) state;
		Continuous2D yard = students.yard;
		Double2D me = students.yard.getObjectLocation(this);
		MutableDouble2D sumForces = new MutableDouble2D();
		Bag out = students.buddies.getEdges(this, null);
		Bag nodes = students.buddies.allNodes;
		int numberOfNodes = nodes.size();
		int len = out.size();
		step++;

		// ---------- I define the acq at the very first step ------------------ //
		if (step==0){
			for(int i=0; i<numberOfNodes; i++){
				acq.add(simRandom.nextDouble());
				//System.out.println("INDEX="+i +" ACQ="+acq.get(i));
			}
		}
		// ---------------------------------------------------------------------- //

		// -------------------- check the parameters ---------------------------- //
		//System.out.println("WIDTH=" + students.getYardWidth() + " LENGTH="+students.getYardHeight() +
		//		" PEOPLE=" +students.getNumOfPersons() + " TOROIDAL="+ students.isPlaneToroidal() + " RADIUS=" +students.getSocialRadius() +
		//		" FADING=" + students.getDistanceFading() + " TIMESTEP=" + students.getTimeStep() + " VELOCITY= "+students.getVelocity());
		// ---------------------------------------------------------------------- //

		// ---------- Check my next behavior ------------------------------------ //
		/*
		int peopleAroundMe = 0;
		//Bag nodes = students.buddies.allNodes;
		int i;
		for (int index=0; index<numberOfNodes; index++){

			if (step%2 == 0) // odd cycles start from the end
				i = index;
			else
				i = numberOfNodes-1-index;

			if (me.distance(students.yard.getObjectLocation(nodes.get(i))) < students.getSocialRadius())
					peopleAroundMe++;
		}

		perceivedSurround = peopleAroundMe ; // NOTE: there is no point I take the perceivedSurrount of the previous step, less me
		socializeBehavior = perceivedSurround > comfortZoneMax ? false : true;
		excitationPartial = Math.abs((perceivedSurround - sociability)/(sociability*tolerance));
		excitation = excitationPartial > 1 ? excitationPartial : 1;  
		 */
		perceivedSurround = students.yard.getObjectsExactlyWithinDistance(me, 
				students.getSocialRadius(), students.isPlaneToroidal()).size();
		socializeBehavior = perceivedSurround > comfortZoneMax ? false : true;
		excitationPartial = Math.abs((perceivedSurround - sociability)/(sociability*tolerance));
		excitation = excitationPartial > 1 ? excitationPartial : 1;  

		System.out.printf("%10d%4d%7s%12.10s%10.2f%10.2f ", step, this.id, socializeBehavior, excitation, me.x, me.y);
		//System.out.printf("%10d%4d%7s%12.10s%20.18s%20.18s ", step, this.id, socializeBehavior, excitation, me.x, me.y);
		// ------------------------------------------------------------------------ //


		MutableDouble2D temp2 = new MutableDouble2D();
		for (int index=0; index<numberOfNodes; index++){

			Double2D him = students.yard.getObjectLocation(nodes.get(index));
			double acquaintance = acq.get(index);
			double dist = Math.sqrt((me.x -him.x)*(me.x -him.x) + (me.y - him.y)*(me.y - him.y));

			double toroidDist = Math.sqrt(students.yard.tds(me, him));
			if (toroidDist<=0) toroidDist=1; // the distance from myself is 0 so I get a NaN!

			if (dist<=0) dist=1; // the distance from myself is 0 so I get a NaN

			if (!students.isPlaneToroidal()){ // not toroidal
				if(socializeBehavior){
					temp2.setTo((him.x - me.x)*acquaintance/Math.pow(dist, students.getDistanceFading())*excitation, 
							(him.y - me.y)*acquaintance/Math.pow(dist, students.getDistanceFading())*excitation); 
					//if (temp2.length() > students.getMAX_FORCE())
					//	temp2.resize(students.getMAX_FORCE());
				}
				else{
					temp2.setTo((him.x - me.x)*-(1-acquaintance)/Math.pow(dist, students.getDistanceFading())*excitation, 
							(him.y - me.y)*-(1-acquaintance)/Math.pow(dist, students.getDistanceFading())*excitation); 
					//if (temp2.length() > students.getMAX_FORCE())
					//	temp2.resize(0.0);
					//else if (temp2.length() > 0)
					//	temp2.resize(students.getMAX_FORCE() - temp2.length());
				}
				//sumForces.addIn(new Double2D((him.x - me.x) * javaRandom.nextDouble(), (him.y - me.y) * javaRandom.nextDouble()));
				//System.out.print("\nDIST= "+ dist +" PLANE FORCE = " + temp2.length());
				sumForces.addIn(temp2);
			}

			else{  // toroidal plane
				//double toroidDist = Math.sqrt(tds(me, him, students.yard));

				if(toroidDist != dist){
				//if(toroidDist < dist){ // use toroidal force
					if(socializeBehavior ){
						temp2.setTo((him.x - me.x)*-acquaintance/Math.pow(toroidDist, students.getDistanceFading())*excitation, 
								(him.y - me.y)*-acquaintance/Math.pow(toroidDist, students.getDistanceFading())*excitation); 
					} else {
						temp2.setTo((him.x - me.x)*(1-acquaintance)/Math.pow(toroidDist, students.getDistanceFading())*excitation, 
								(him.y - me.y)*(1-acquaintance)/Math.pow(toroidDist, students.getDistanceFading())*excitation); 
					}
					//System.out.println(" TDIST=" + toroidDist + " TOROIDAL FORCE = " + temp2.length());
					sumForces.addIn(temp2);
				//}
				//else{ // use in plane force
					if(socializeBehavior){
						temp2.setTo((him.x - me.x)*acquaintance/Math.pow(dist, students.getDistanceFading())*excitation, 
								(him.y - me.y)*acquaintance/Math.pow(dist, students.getDistanceFading())*excitation); 
					}
					else{
						temp2.setTo((him.x - me.x)*-(1-acquaintance)/Math.pow(dist, students.getDistanceFading())*excitation, 
								(him.y - me.y)*-(1-acquaintance)/Math.pow(dist, students.getDistanceFading())*excitation); 
					}
					//sumForces.addIn(new Double2D((him.x - me.x) * javaRandom.nextDouble(), (him.y - me.y) * javaRandom.nextDouble()));
					//System.out.print("\nDIST= "+ dist +" PLANE FORCE = " + temp2.length());
					sumForces.addIn(temp2);
				} else{
					if(socializeBehavior){
						temp2.setTo((him.x - me.x)*acquaintance/Math.pow(dist, students.getDistanceFading())*excitation, 
								(him.y - me.y)*acquaintance/Math.pow(dist, students.getDistanceFading())*excitation); 
					}
					else{
						temp2.setTo((him.x - me.x)*-(1-acquaintance)/Math.pow(dist, students.getDistanceFading())*excitation, 
								(him.y - me.y)*-(1-acquaintance)/Math.pow(dist, students.getDistanceFading())*excitation); 
					}
					sumForces.addIn(temp2);
				}
			}

		}
		//System.out.println("FINAL, ANGLE="+sumForces.angle() + " LENGTH="+sumForces.length());

		// ---------------- Rescale to the maxDistance in according to human velocity ------ //
		maxDistance = students.getTimeStep()*students.getVelocity();
		if (sumForces.length() > maxDistance ) 	sumForces.resize(maxDistance);


		// ------------------ add a bit of randomness --------------------- //		
		//sumForces.addIn(new Double2D(students.getRandomMultiplier() * (students.random.nextDouble() * 1.0 - 0.5),
		//		students.getRandomMultiplier() * (students.random.nextDouble() * 1.0 - 0.5)));
		// ---------------------------------------------------------------- //


		sumForces.addIn(me); // IMP!!!


		if (!students.isPlaneToroidal()){ // not toroidal
			if(sumForces.x > yard.width) sumForces.x = yard.width;
			if(sumForces.x < 0) sumForces.x = 0;
			if(sumForces.y > yard.height) sumForces.y = yard.height;
			if(sumForces.y < 0) sumForces.y = 0;

			// Move the ball at the end!
			students.yard.setObjectLocation(this, new Double2D(sumForces));
			System.out.printf("-> %12.10s%10.2f%10.2f\n", sumForces.length(),  sumForces.x, sumForces.y);
			//System.out.printf("-> %12.10s%20.18s%20.18s\n", sumForces.length(),  sumForces.x, sumForces.y);

		} else { // toroidal plane

			// NOTE the functions stx and sty are fast but can be used only in the range (-width ... width * 2) not inclusive
			students.yard.setObjectLocation(this, new Double2D(students.yard.stx(sumForces.x), students.yard.sty(sumForces.y)));

			/*
			double newXPosition = sumForces.x;
			double newYPosition = sumForces.y;

			if (sumForces.x > yard.width){
				do{
					sumForces.x -= yard.width;
					newXPosition = sumForces.x;
				} while (sumForces.x > yard.width);
			}
			if (sumForces.x < 0){
				do{
					sumForces.x += yard.width;
					newXPosition = sumForces.x;
				} while (sumForces.x < 0);
			}
			if (sumForces.y > yard.height){
				do {
					sumForces.y -= yard.height;
					newYPosition = sumForces.y;
				} while (sumForces.y > yard.height);
			}
			if (sumForces.y < 0){
				do {
					sumForces.y += yard.height;
					newYPosition = sumForces.y;
				} while (sumForces.y < 0);
			}
			// Move the ball at the end!
			students.yard.setObjectLocation(this, new Double2D(newXPosition, newYPosition));
			 */

			System.out.printf("-> %10.2f%10.2f\n", students.yard.stx(sumForces.x), students.yard.sty(sumForces.y));
			//System.out.printf("-> %20.18s%20.18s\n", students.yard.stx(sumForces.x), students.yard.sty(sumForces.y));
		}

		// Move the ball at the end!
		//students.yard.setObjectLocation(this, new Double2D(sumForces));

		//System.out.print("-> " + students.yard.getObjectLocation(this) + "\n");
		//System.out.printf("-> %12.10s%20.18s%20.18s\n", sumForces.length(),  me.x, me.y);
		//System.out.println("--------------------------------------------------------------");
	}

	// ------------------ Java Bean read only properties ----------------- //
	public double getComfortZoneMin(){ return comfortZoneMin; } 
	public double getComfortZoneMax(){ return comfortZoneMax; }
	public double getPerceivedSurround() { return perceivedSurround; }
	public boolean getSocializeBehavior() { return socializeBehavior; }
	public double getExcitation() {return excitation;}
	public void setExcitation(double excitation) { this.excitation = excitation;}
	public void setComfortZoneMax(double v){ if (v > comfortZoneMin) comfortZoneMax = v;} 
	// ------------------------------------------------------------------- //

}