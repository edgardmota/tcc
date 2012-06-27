package br.ufla.dcc.mu.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.mu.packet.AlarmPacket;
import br.ufla.dcc.mu.packet.HasMePacket;
import br.ufla.dcc.mu.packet.PheromonePacket;
import br.ufla.dcc.mu.utils.Alarm;
import br.ufla.dcc.mu.utils.HasMe;
import br.ufla.dcc.mu.wuc.LayPheromoneWakeUpCall;

public class UAV extends GenericNode {
	
	private static final int LAY_PHEROMONE_DELAY = 800;
	//Attribute isWarned is false until UAV hasn't received any alarm. When its received, it becomes true
	private static boolean isWarned = false;
	private Map<Integer,Float> resources = new HashMap<Integer,Float>();
//	private NodeId hasMe; 

	public UAV(){
		super();
		//Randomically generating levels of three different kind of resources
		for (int i = 0; i <= 2; i++) {
			this.resources.put(Integer.valueOf(i),(float)new Random().nextInt(6));
		}
		System.out.println(this.resources.toString());
	}
	
	private boolean warnUAV(){
		this.isWarned = true;
		return true;
	}
	
	private boolean isWarned(){
		return this.isWarned;
	}
	
	public Map<Integer,Float> getResources(){
		return this.resources;
	}
	
	public void processEvent(StartSimulation start){
		this.layPheromone();
	}
	
	@Override
	public void lowerSAP(Packet packet) throws LayerException {
		if (packet instanceof AlarmPacket){
//			Alarm alarm = ((AlarmPacket) packet).getAlarm();
//			this.handleAlarm(alarm);
		}
//		if (packet instanceof HasMePacket){
//			HasMe hasMe = ((HasMePacket) packet).getHasMe();
//			this.handleHasMe(hasMe);
//		}
	}

//	private void handleHasMe(HasMe hasMe) {
//		this.setHasMe(hasMe.getNode());
//	}

	private void handleAlarm(Alarm alarm) {
		if (!this.isWarned()){
			boolean isMyAlarm = alarm.getTrailedUAV().equals(this.getId());
			if(isMyAlarm){
//				RegularNode.isTracking = false;
				SimulationManager.logNodeState(getId(), "Alarm Received", "int", "80");
				this.warnUAV();
				System.out.println("Alarme chegou em " + this.node.getId().toString());
			}
		}
	}

	public void processWakeUpCall(WakeUpCall wuc){
		super.processWakeUpCall(wuc);
		
		if (wuc instanceof LayPheromoneWakeUpCall)
			this.layPheromone();
	}
	
	private void layPheromone(){
		Packet pheromonePacket = new PheromonePacket(this.getSender(), NodeId.ALLNODES, this.getResources());
		this.sendPacket(pheromonePacket);
		
		WakeUpCall whenLayPheromoneAgain = new LayPheromoneWakeUpCall(this.getSender(), LAY_PHEROMONE_DELAY);
		this.sendEventSelf(whenLayPheromoneAgain);
	}

//	public NodeId getHasMe() {
//		return hasMe;
//	}
//
//	public void setHasMe(NodeId hasMe) {
//		this.hasMe = hasMe;
//		System.out.println("Node "+hasMe.toString() + " has " + this.node.getId().toString());
//	}
	
	
}
