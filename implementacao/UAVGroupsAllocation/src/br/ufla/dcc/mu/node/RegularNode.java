package br.ufla.dcc.mu.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
//import br.ufla.dcc.grubix.simulator.node.ApplicationLayer;
import br.ufla.dcc.grubix.simulator.node.Layer;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.mu.packet.AlarmPacket;
import br.ufla.dcc.mu.packet.AnswerAlarmPacket;
import br.ufla.dcc.mu.packet.HasMePacket;
import br.ufla.dcc.mu.packet.PheromonePacket;
import br.ufla.dcc.mu.utils.Alarm;
import br.ufla.dcc.mu.utils.HasMe;
import br.ufla.dcc.mu.utils.Pheromone;
import br.ufla.dcc.mu.utils.Transponder;
import br.ufla.dcc.mu.wuc.AnswerAlarmWakeUpCall;
import br.ufla.dcc.mu.wuc.PheromoneDecreaseWakeUpCall;
import br.ufla.dcc.mu.wuc.EventIdentificationWakeUpCall;
import br.ufla.dcc.mu.wuc.SelectNextSinkWakeUpCall;

import java.util.Random;

public class RegularNode extends GenericNode {
	private static final int DOING_NOTHING = 0;
	private static final int ACTING_AS_SINK = 1;
	private static final int AWAITING_ELECTION = 2;
	private static final int ALARM_ANSWER_SCHEDULED = 3;
	
	private static final int PHEROMONE_DECREASE_TIME = 600;
//	private static final int MEASURE_INTERVAL = 600;
//	private static final double TRANSPONDER_RANGE = 8.0; 
//	public static boolean isTracking = false;
	public int stage;
	private Map<NodeId, Pheromone> storedPheromones = new HashMap<NodeId, Pheromone>();
	private Random random = new Random();
	private Transponder transponder;
	private List<WakeUpCall> wucs;
	
	public RegularNode() {
		super();
		this.setStage(this.DOING_NOTHING);
		this.setWucs(new ArrayList<WakeUpCall>());
	}
	
	public List<WakeUpCall> getWucs() {
		return wucs;
	}

	public void setWucs(List<WakeUpCall> wucs) {
		this.wucs = wucs;
	}

	public void addWuc(WakeUpCall wuc){
		if (this.getWucs() != null)
			this.wucs.add(wuc);
	}
	
	public void removeWuc(WakeUpCall wuc){
		if (this.getWucs() != null)
			this.wucs.remove(wuc);
	}

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
		
		if (wuc instanceof SelectNextSinkWakeUpCall)
			this.selectNextSink();
		
		if (wuc instanceof PheromoneDecreaseWakeUpCall)
			this.decreasePheromone();
		
		if (wuc instanceof EventIdentificationWakeUpCall)
			this.eventIdentification();
		
		if (wuc instanceof AnswerAlarmWakeUpCall)
			this.sendAlarmAnswer(((AnswerAlarmWakeUpCall) wuc).getPheromone(),((AnswerAlarmWakeUpCall) wuc).getSinkNode());
	}
	
	private void selectNextSink() {
		// TODO Auto-generated method stub
		
	}

	private void sendAlarmAnswer(Pheromone pheromone, int sinkNode) {
				if (this.getStage() == this.ALARM_ANSWER_SCHEDULED){
					SimulationManager.logNodeState(this.getNode().getId(), "Sending Alarm Answer", "int", "40");
					Packet packet = new AnswerAlarmPacket(this.getSender(), NodeId.ALLNODES, pheromone, sinkNode);
					this.sendPacket(packet);
					this.setStage(this.AWAITING_ELECTION);
				}
	}

	//Generates a number which estimates how much a resource is useful or how much is needed to attend the event demand
	private int summarizeResources(Map<Integer,Float> resources){
		int sum = 0;
		for (int i: resources.keySet()){
			sum += resources.get(i);
		}
		return sum;
	}
	
	//Pseudo heuristics which estimates how valuable is selecting a specific node to propagate the alarm
	private int pseudoHeuristic(Map<Integer,Float> neededResources, Map<Integer,Float> offeredResources){
		Map<Integer,Float> updatedNeededResources = new HashMap<Integer,Float>();
		float level = 0;
		
		for (int key: offeredResources.keySet()) {
			level = offeredResources.get(key);
   			updatedNeededResources.put(key,Math.max(neededResources.get(key)-level,0));
        }
		return this.summarizeResources(updatedNeededResources);
	}
	
	//Combine two UAV resources as one
	private Map<Integer,Float> combineUAVResources(Map<Integer,Float> UAVResources1, Map<Integer,Float> UAVResources2){
		Map<Integer,Float> combinedResources = new HashMap<Integer,Float>();
		float level = 0;
		
		for (int key: UAVResources1.keySet()) {
			level = UAVResources1.get(key);
			combinedResources.put(key,UAVResources2.get(key)+level);
        }
		return combinedResources;
	}
	
	//Returns true if the node have useful trail for the needed resources and false otherwise
	private boolean haveUsefulTrail(Map<Integer,Float> neededResources){
		int sum = this.summarizeResources(neededResources);
		int newSum = 0;
		boolean useful = false;
		Pheromone pheromone;
		
		for (NodeId key: this.storedPheromones.keySet()) {
			if (!useful) {
				pheromone = this.storedPheromones.get(key);
				newSum = this.pseudoHeuristic(neededResources, pheromone.getResources());
				if (newSum < sum)
	       			useful = true;
			}
		}
		return useful;
	}
	
	private void eventIdentification() {
		Map<Integer,Float> offeredResources;
		
		//Forging needed resources and its needs in terms of levels
		Map<Integer,Float> neededResources = new HashMap<Integer,Float>();
		for (int i = 0; i <= 2; i++) {
			neededResources.put(Integer.valueOf(i),(float)new Random().nextInt(6));
			
		}
		System.out.println("Needed Resources: "+neededResources.toString() + " - " + this.summarizeResources(neededResources));
		if (this.haveUsefulTrail(neededResources)){
			SimulationManager.logNodeState(getId(), "Event Identified", "int", "50");
			System.out.println("Useful: ");
			for (NodeId key: this.storedPheromones.keySet()){
				offeredResources = this.storedPheromones.get(key).getResources();
				System.out.println(offeredResources.toString() + " - " + this.pseudoHeuristic(neededResources, offeredResources));
				this.warnNeighborsAbout(neededResources);
			}
		}
		else {
			System.out.println("OH man.... =/");
		}
			
	}

//	private void warnNeighborsAbout(Map<Integer,Float> neededResources) {
//		NodeId uavForThisKindOfEvent = this.getUAVsAsList().get(new Random().nextInt(3));
//		Sy(int) (1100-(pheromoneAmount*1000));stem.out.println("UAV: "+uavForThisKindOfEvent.toString());
//		boolean containsThePheromoneFlavor = this.storedPheromones.containsKey(uavForThisKindOfEvent);
//		
//		if(containsThePheromoneFlavor){
//			Alarm alarm = new Alarm(this.storedPheromones.get(uavForThisKindOfEvent), uavForThisKindOfEvent, this.getNode().getPosition(), SimulationManager.getInstance().getCurrentTime());
//			this.forwardAlarm(alarm);
//			SimulationManager.logNodeState(getId(), "Tracking", "int", "30");
//			isTracking = true;
//		}
//	}

	private void warnNeighborsAbout(Map<Integer,Float> neededResources) {
			Alarm alarm = new Alarm(this.getNode().getPosition(), SimulationManager.getInstance().getCurrentTime(), neededResources, this.getNode().getId().asInt(), null, 0, new ArrayList<NodeId>());
			this.forwardAlarm(alarm);
			SimulationManager.logNodeState(getId(), "Acting as Sink", "int", "30");
			this.setStage(this.ACTING_AS_SINK);
	}
	
	private void forwardAlarm(Alarm alarm) {
		Packet packet = new AlarmPacket(this.getSender(), NodeId.ALLNODES, alarm);
		this.sendPacket(packet);
		WakeUpCall wuc = new SelectNextSinkWakeUpCall(this.getSender(),1000);
		this.sendEventSelf(wuc);
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
		//When receives a pheromone packet, does the processment, no matter in which stage it is
		if (packet instanceof PheromonePacket)
			this.processPheromonePacket((PheromonePacket) packet);
		
		//When receives a Alarm packet, does the processment only if doing nothing
		if (packet instanceof AlarmPacket)
			if(this.getStage() == this.DOING_NOTHING)
				this.processAlarmPacket((AlarmPacket) packet);
		
		//When receives a Answer Alarm packet:
		if (packet instanceof AnswerAlarmPacket){
			if (this.getStage() == this.ACTING_AS_SINK) {
				//Node is acting a sink of this answer
				if (this.getNode().getId().asInt() == ((AnswerAlarmPacket) packet).getSinkNode()){
					System.out.println("Eleição: " + ((AnswerAlarmPacket) packet).getPheromone().getResources().toString());
				}
			}
			//Node has an alarm answer schedule to the same sink:
			else if (this.getStage() == this.ALARM_ANSWER_SCHEDULED){
				//Some other node answered faster!
				Iterator<WakeUpCall> iterator = this.getWucs().iterator();
				WakeUpCall answerScheduled = null;
				//Looking for an scheduled WUC in answer to the same alarm
				while ((iterator.hasNext()) && (answerScheduled == null)) {
					answerScheduled = iterator.next();
					if (!(answerScheduled instanceof AnswerAlarmWakeUpCall))
						answerScheduled = null;
				}
				if (!(answerScheduled == null) && (((AnswerAlarmWakeUpCall)answerScheduled).getSinkNode() == ((AnswerAlarmPacket) packet).getSinkNode()))
					//Canceling my answer
					this.setStage(this.DOING_NOTHING);
				}
			}
		}

//	private void processAlarmPacket(AlarmPacket packet) {
//		Alarm receivedAlarm = packet.getAlarm();
//		NodeId flavor = receivedAlarm.getFlavorstatus();
//		boolean containsThisFlavor = this.storedPheromones.containsKey(flavor);
//		
//		if(containsThisFlavor){
//			Pheromone myPheromone = this.storedPheromones.get(flavor);
//			if (myPheromone.get() > receivedAlarm.getPheromone().get()){
//				this.updateAndForwardAlarm(receivedAlarm,myPheromone);
//			}
//		}
//	}
	private int delay(double pheromoneAmount){
		int temp = (int) (1100-(pheromoneAmount*1000));
		return ((int) (temp/100))*100;
	}
	
	private void processAlarmPacket(AlarmPacket packet) {
		Pheromone pheromone = null;
		int useful = 0;
	
		if (!this.storedPheromones.isEmpty()){
			//Individual treatment for each distinc pheromone
			for (NodeId key: this.storedPheromones.keySet()){
					pheromone = this.storedPheromones.get(key);
					//When pheromone is from the same UAV being trailed by the alarm
					if (pheromone.getId() == packet.getAlarm().getTrailedUAV()){
						if(pheromone.get() > packet.getAlarm().getPheromoneAmount()){
							this.setStage(this.ALARM_ANSWER_SCHEDULED);
							WakeUpCall wuc = new AnswerAlarmWakeUpCall(getSender(),pheromone,packet.getSender().getId().asInt(),this.delay(pheromone.get()));
							this.sendEventSelf(wuc);
						}
					}
					//When pheromone is from a UAV being trailed elsewhere
					else if (packet.getAlarm().getTrackedUAVs().contains(pheromone.getId())){
						
					}
					//When pheromone is from a UAV not being trailed at all
					else{
						if (this.haveUsefulTrail(pheromone.getResources())){
							//WakeUpCall has been scheduled
							this.setStage(this.ALARM_ANSWER_SCHEDULED);
							WakeUpCall wuc = new AnswerAlarmWakeUpCall(getSender(),pheromone,packet.getSender().getId().asInt(),this.delay(pheromone.get()));
							this.sendEventSelf(wuc);
							useful += 1;
						}
					}
			}
			System.out.println("Uteis em " + this.getNode().getId().asInt() + ":" + useful );
		}else
			System.out.println("Sem cheiro!");
	}

	
	
	@Override
	public void sendEventSelf(ToLayer event) {
		// TODO Auto-generated method stub
		super.sendEventSelf(event);
		this.addWuc((WakeUpCall)event);
	}



	private void updateAndForwardAlarm(Alarm receivedAlarm, Pheromone pheromone) {
//
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
//		if (Math.abs(pheromoneAmmount - 1) <= 0.2)
//			this.sendPacket(new HasMePacket(this.getSender(),propagator,new HasMe(this.getNode().getId())));
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

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	
	
}
