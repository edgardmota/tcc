package br.ufla.dcc.mu.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

//import com.apple.eawt.Application;

//import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
//import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.mu.packet.AlarmPacket;
import br.ufla.dcc.mu.packet.PheromonePacket;
import br.ufla.dcc.mu.utils.Alarm;
import br.ufla.dcc.mu.utils.Pheromone;
import br.ufla.dcc.mu.utils.Transponder;
import br.ufla.dcc.mu.wuc.PheromoneDecreaseWakeUpCall;
import br.ufla.dcc.mu.wuc.EventIdentificationWakeUpCall;
import java.util.Random;

public class RegularNode extends GenericNode {
	
	private static final int PHEROMONE_DECREASE_TIME = 600;
	private static final int MEASURE_INTERVAL = 600;
	private static final double TRANSPONDER_RANGE = 8.0; 
	public static boolean isTracking = false;
	private Map<NodeId, Pheromone> storedPheromones = new HashMap<NodeId, Pheromone>();
	private Random random = new Random();
	private Transponder transponder;
	
	public void processEvent(StartSimulation start){
		this.decreasePheromone();
		this.setupTransponder();
		if (this.getNode().getId().asInt() == 2){
			WakeUpCall wuc = new EventIdentificationWakeUpCall(getSender(),100000);
			this.sendEventSelf(wuc);
		}
	}
	
	private void setupTransponder() {
		this.transponder = new Transponder();
		this.transponder.setMe(this.getNode());
		this.transponder.setAllNodes(SimulationManager.getAllNodes().values());
	}
	
	public void processWakeUpCall(WakeUpCall wuc){
		super.processWakeUpCall(wuc);
		
		if (wuc instanceof PheromoneDecreaseWakeUpCall)
			this.decreasePheromone();
		
		if (wuc instanceof EventIdentificationWakeUpCall)
			this.eventIdentified();
	}
	
	private void eventIdentified() {
		//Forging needed resources and its needs in terms of levels
		Map<Integer,Float> neededResources = new HashMap<Integer,Float>();
		for (int i = 0; i <= 2; i++) {
			neededResources.put(Integer.valueOf(i),(float)new Random().nextInt(11));
			
		}
		System.out.println("Needed Resources: "+neededResources.toString());
		if (!isTracking){
			SimulationManager.logNodeState(getId(), "Event Identified", "int", "50");
			this.warnNeighborsAbout(neededResources);
		}
			
	}

	private void warnNeighborsAbout(Map<Integer,Float> neededResources) {
		NodeId uavForThisKindOfEvent = this.getUAVsAsList().get(new Random().nextInt(3));
		System.out.println("UAV: "+uavForThisKindOfEvent.toString());
		boolean containsThePheromoneFlavor = this.storedPheromones.containsKey(uavForThisKindOfEvent);
		
		if(containsThePheromoneFlavor){
			Alarm alarm = new Alarm(this.storedPheromones.get(uavForThisKindOfEvent), uavForThisKindOfEvent, this.getNode().getPosition(), SimulationManager.getInstance().getCurrentTime());
			this.forwardAlarm(alarm);
			SimulationManager.logNodeState(getId(), "Tracking", "int", "30");
			isTracking = true;
		}
	}

	private void forwardAlarm(Alarm alarm) {
		SimulationManager.logNodeState(this.getNode().getId(), "Forwarding", "int", "40");
		Packet packet = new AlarmPacket(this.getSender(), NodeId.ALLNODES, alarm);
		this.sendPacket(packet);
	}

	private List<NodeId> getUAVsAsList(){
		List<NodeId> uavs = new ArrayList<NodeId>();
		
		SortedMap<NodeId, Node> allNodes = SimulationManager.getAllNodes();
		for (NodeId id : allNodes.keySet()) {
			Node node = allNodes.get(id);
			Layer layer = node.getLayer(getLayerType());
			if (layer instanceof UAV)
				uavs.add(id);
		}
		return uavs;
	}
	
	@Override
	public void lowerSAP(Packet packet) throws LayerException {
		if (packet instanceof PheromonePacket)
			this.processPheromonePacket((PheromonePacket) packet);
		
		if (packet instanceof AlarmPacket)
			this.processAlarmPacket((AlarmPacket) packet);
	}

	private void processAlarmPacket(AlarmPacket packet) {
		Alarm receivedAlarm = packet.getAlarm();
		NodeId flavor = receivedAlarm.getFlavor();
		boolean containsThisFlavor = this.storedPheromones.containsKey(flavor);
		
		if(containsThisFlavor){
			Pheromone myPheromone = this.storedPheromones.get(flavor);
			if (myPheromone.get() > receivedAlarm.getPheromone().get()){
				this.updateAndForwardAlarm(receivedAlarm,myPheromone);
			}
		}
	}

	private void updateAndForwardAlarm(Alarm receivedAlarm, Pheromone pheromone) {
		Alarm updateAlarm = new Alarm(pheromone, receivedAlarm.getFlavor(), receivedAlarm.getPosition(), receivedAlarm.getTimeStamp());
		this.forwardAlarm(updateAlarm);
	}

	private void processPheromonePacket(PheromonePacket packet){
		NodeId uav = packet.getUavId();
		this.storePheromone(uav,packet.getResources());
	}	

	private void storePheromone(NodeId flavor, NodeId propagator, double rate,Map<Integer,Float> resources){
		Position uavPosition = SimulationManager.getAllNodes().get(propagator).getPosition();
		Position myPosition = this.getNode().getPosition();

		double distance = myPosition.getDistance(uavPosition);
		distance = distance/10;//Scales 
		distance = Math.floor(distance);
		double pheromoneAmmount = 1/(Math.pow(2, distance)); //Inverse Exponential.. calculates the value
		
		pheromoneAmmount = pheromoneAmmount * rate;

		Pheromone pheromone = this.storedPheromones.get(flavor);
		if (pheromone==null){
			pheromone = new Pheromone(flavor,resources);
			this.storedPheromones.put(flavor,pheromone);
		}  
				
		pheromone.set(pheromoneAmmount);   
		pheromone.update_view(this.getId());
	}
	
	
	private void storePheromone(NodeId flavor,Map<Integer,Float> resources){
		this.storePheromone(flavor, flavor, 1,resources);
	}
	
	private void decreasePheromone(){
		
		List<NodeId> flavorsToRemove = new ArrayList<NodeId>();
		
		Set<NodeId> flavors = this.storedPheromones.keySet();
		for (NodeId flavor : flavors) {
			Pheromone pheromone = this.storedPheromones.get(flavor);
			pheromone.update_view(this.getId());
			pheromone.evaporate();
			
			//Removes this flavor of pheromone set.. it no longer exists
			if(pheromone.get() == 0.0){
				flavorsToRemove.add(flavor);
			}
		}
		
		for (NodeId nodeId : flavorsToRemove) {
			this.storedPheromones.remove(nodeId);
		}
		
		PheromoneDecreaseWakeUpCall bw = new PheromoneDecreaseWakeUpCall(this.getSender(),PHEROMONE_DECREASE_TIME);
		this.sendEventSelf(bw);
	}
	
}
