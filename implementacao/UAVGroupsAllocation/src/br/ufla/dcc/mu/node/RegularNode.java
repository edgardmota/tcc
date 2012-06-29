package br.ufla.dcc.mu.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.Collections;

import de.uni_tuebingen.sfb.macke.utilities.Combinator;

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
import br.ufla.dcc.mu.packet.BackTrackAnswerPacket;
//import br.ufla.dcc.mu.packet.HasMePacket;
import br.ufla.dcc.mu.packet.PheromonePacket;
import br.ufla.dcc.mu.packet.BackTrackPacket;
import br.ufla.dcc.mu.utils.Alarm;
//import br.ufla.dcc.mu.utils.HasMe;
import br.ufla.dcc.mu.utils.Pheromone;
import br.ufla.dcc.mu.utils.Transponder;
import br.ufla.dcc.mu.wuc.AnswerAlarmWakeUpCall;
import br.ufla.dcc.mu.wuc.PheromoneDecreaseWakeUpCall;
import br.ufla.dcc.mu.wuc.EventIdentificationWakeUpCall;
import br.ufla.dcc.mu.wuc.SelectNextSinkWakeUpCall;

//import java.util.Random;

public class RegularNode extends GenericNode {
	private static final int NONE = -1;
	private static final int PHEROMONE_DECREASE_TIME = 600;
	private Map<NodeId, Pheromone> storedPheromones = new HashMap<NodeId, Pheromone>();
	private Transponder transponder;
	private List<WakeUpCall> wucs;
	private List<AnswerAlarmPacket> answersList;
	private List<Integer> trackedUAVs;
	private int root;
	
	public RegularNode() {
		super();
		this.setWucs(new ArrayList<WakeUpCall>());
		this.setAnswersList(new ArrayList<AnswerAlarmPacket>());
		this.setTrackedUAVs(new ArrayList<Integer>());
		this.setRoot(this.NONE);
	}

	
	
	public int getRoot() {
		return this.root;
	}


	public void setRoot(int root) {
		this.root = root;
	}

	public void addTrailsFor(int uavID) {
		if (this.getTrackedUAVs()!= null)
			this.getTrackedUAVs().add(uavID);
	}

	public void setTrackedUAVs(List<Integer> trailsFor) {
		this.trackedUAVs = trailsFor;
	}

	public List<Integer> getTrackedUAVs() {
		return trackedUAVs;
	}



	public List<AnswerAlarmPacket> getAnswersList() {
		return answersList;
	}

	public void setAnswersList(List<AnswerAlarmPacket> answers) {
		this.answersList = answers;
	}

	public void addAnswer(AnswerAlarmPacket answer){
		if (this.getAnswersList() != null)
			this.answersList.add(answer);
	}
	
	public void removeAnswer(AnswerAlarmPacket answer){
		if (this.getAnswersList() != null)
			this.answersList.remove(answer);
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
			//Forging needed resources and its needs in terms of levels
			Map<Integer,Float> neededResources = new HashMap<Integer,Float>();
			for (int i = 0; i <= 2; i++) {
				neededResources.put(Integer.valueOf(i),(float)new Random().nextInt(6));
			}
			System.out.println("Needed Resources: "+neededResources.toString() + " - " + this.summarizeResources(neededResources));
			this.setRoot(this.getNode().getId().asInt());
			WakeUpCall wuc = new EventIdentificationWakeUpCall(this.getSender(),neededResources,this.getRoot(),this.storedPheromones,this.getTrackedUAVs(),100000);
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
		
		if (this.getWucs().contains(wuc)){
			if (wuc instanceof SelectNextSinkWakeUpCall){
				int nAnswers = this.getAnswersList().size();
				if(nAnswers>0){
					this.summarizeAnswers();
					this.backtrack();
					}
				else
					System.out.println(this.getNode().getId().asInt()+" says: Sem qualquer resposta :(");		
			}
			
			if (wuc instanceof PheromoneDecreaseWakeUpCall)
				this.decreasePheromone();
			
			if (wuc instanceof EventIdentificationWakeUpCall){
				Map<Integer,Float> neededResources = ((EventIdentificationWakeUpCall)wuc).getNeededResources();
				int root = ((EventIdentificationWakeUpCall)wuc).getRoot();
				Map<NodeId, Pheromone> pheromoneAmount = ((EventIdentificationWakeUpCall)wuc).getStoredPheromones();
				List<Integer> trackedUVAs = ((EventIdentificationWakeUpCall)wuc).getTrackedUAVs();
				eventIdentification(neededResources,root,pheromoneAmount,trackedUVAs);
			}
			
			if (wuc instanceof AnswerAlarmWakeUpCall)
				this.sendAlarmAnswer(((AnswerAlarmWakeUpCall) wuc).getPheromone(),((AnswerAlarmWakeUpCall) wuc).getAlarmPacket(),wuc.getDelay());
			this.removeWuc(wuc);
		}
		else{
			if (wuc instanceof AnswerAlarmWakeUpCall)				
				System.out.println(this.getNode().getId().asInt()+" says: Agendei mas responderam antes...");
		}
	}


	private void summarizeAnswers(){
		Iterator<AnswerAlarmPacket> iterator = this.getAnswersList().iterator();
		Map<Integer,Map<Double,AnswerAlarmPacket>> uavs = new HashMap<Integer,Map<Double,AnswerAlarmPacket>>();
		AnswerAlarmPacket answer = null;
		int uavID;
		double uavPheromoneAmmount;
		
		while (iterator.hasNext()) {
			answer = iterator.next();
			uavID = answer.getPheromone().getId().asInt();
			uavPheromoneAmmount = answer.getPheromone().get();
			if (uavs.get(uavID)==null){
				uavs.put(uavID,new HashMap<Double,AnswerAlarmPacket>());
			}
			uavs.get(uavID).put(uavPheromoneAmmount,answer);
		}
		this.setAnswersList(new ArrayList<AnswerAlarmPacket>());
		for(int key : uavs.keySet() ){
			Object[] tempArray = uavs.get(key).keySet().toArray();
			Arrays.sort(tempArray, Collections.reverseOrder());
			this.addAnswer(uavs.get(key).get(tempArray[0]));
		}	
	}
	
	
	private void backtrack() {
		if (this.getNode().getId().asInt() == this.getRoot()){
			this.selectNextSink(this.getAnswersList().get(0).getAlarmPacket().getAlarm().getEventNeedsList());
		}
		Packet backTrackPacket = new BackTrackPacket(this.getSender(), NodeId.get(this.getRoot()),this.getAnswersList());
		this.sendPacket(backTrackPacket);
	}

	private void selectNextSink(Map<Integer, Float> eventNeedsList){
		List<AnswerAlarmPacket> betterCombination;
		betterCombination = this.getBetterAnswersCombination(eventNeedsList);
		for (AnswerAlarmPacket answer: betterCombination){
			int uavID = answer.getPheromone().getId().asInt();
			System.out.println("Enviado por " + answer.getPacket(getThisLayer()).getSender().getId().asInt() + " - " + uavID + ": " + answer.getPheromone().getResources().toString());
			this.addTrailsFor(answer.getPheromone().getId().asInt());
		}
	}
		
		
//	private boolean canProceed(AnswerAlarmPacket answer) {
//		return this.backTrack(answer);
//	}
//
//	private boolean backTrack(AnswerAlarmPacket answer) {
//		if (this.getPreviousDivisor() == this.NONE)
//				return true;
//		else{
//			//
//		}
//		return false;
//	}

	private List<AnswerAlarmPacket> getBetterAnswersCombination(Map<Integer,Float> neededResources) {
		Map<Integer,Float> updatedNeededResources;
		Map<Integer,List<AnswerAlarmPacket>> combinations = new HashMap<Integer,List<AnswerAlarmPacket>>();
		int summarized;
		
		for (int i = 0;i <= this.getAnswersList().size(); i++) {
			for (AnswerAlarmPacket[] combination : new Combinator<AnswerAlarmPacket>(this.getAnswersList().toArray(new AnswerAlarmPacket[0]), i)) {
					updatedNeededResources = neededResources;
				    for(int j = 0; j < combination.length ; j++){
				    	updatedNeededResources = this.updateNeededResources(updatedNeededResources,combination[j].getPheromone().getResources());
				    }
				    summarized = this.summarizeResources(updatedNeededResources);
				    if (combinations.get(summarized) == null)
				    	combinations.put(this.summarizeResources(updatedNeededResources),Arrays.asList(combination));
			}
		}
		Object[] tempArray = combinations.keySet().toArray();
		Arrays.sort(tempArray);
		return combinations.get(tempArray[0]);
	}

	private void sendAlarmAnswer(Pheromone pheromone, AlarmPacket alarmPacket, double d) {
		SimulationManager.logNodeState(this.getNode().getId(), "Sending Alarm Answer", "int", "40");
		Packet packet = new AnswerAlarmPacket(this.getSender(), NodeId.ALLNODES, pheromone, alarmPacket);
		this.sendPacket(packet);
		System.out.println(this.getNode().getId().asInt()+" says: Eu respondi no tempo " + d + "!");
	}

	//Generates a number which estimates how much a resource is useful or how much is needed to attend the event demand
	private int summarizeResources(Map<Integer,Float> resources){
		int sum = 0;
		for (int i: resources.keySet()){
			sum += resources.get(i);
		}
		return sum;
	}
	
	private Map<Integer,Float> updateNeededResources(Map<Integer,Float> neededResources, Map<Integer,Float> offeredResources){
		Map<Integer,Float> updatedNeededResources = new HashMap<Integer,Float>();
		float level = 0;
		
		for (int key: offeredResources.keySet()) {
			level = offeredResources.get(key);
   			updatedNeededResources.put(key,Math.max(neededResources.get(key)-level,0));
        }
		return updatedNeededResources;
	}
	
	//Pseudo heuristics which estimates how valuable is selecting a specific node to propagate the alarm
	private int pseudoHeuristic(Map<Integer,Float> neededResources, Map<Integer,Float> offeredResources){
		return this.summarizeResources(updateNeededResources(neededResources,offeredResources));
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
	
	private void eventIdentification(Map<Integer,Float> neededResources, int root, Map<NodeId, Pheromone> pheromoneAmount, List<Integer> trackedUVAs) {
			this.warnNeighborsAbout(neededResources,root,pheromoneAmount,trackedUVAs);
	}

	private void warnNeighborsAbout(Map<Integer,Float> neededResources, int root, Map<NodeId, Pheromone> pheromoneAmount, List<Integer> trackedUVAs) {
			Alarm alarm = new Alarm(this.getNode().getPosition(), SimulationManager.getInstance().getCurrentTime(), neededResources, this.getNode().getId().asInt(), root, pheromoneAmount, trackedUVAs);
			this.forwardAlarm(alarm);
			SimulationManager.logNodeState(getId(), "Acting as Sink", "int", "30");
	}
	
	private void forwardAlarm(Alarm alarm) {
		Packet packet = new AlarmPacket(this.getSender(), NodeId.ALLNODES, alarm);
		this.sendPacket(packet);
		WakeUpCall wuc = new SelectNextSinkWakeUpCall(this.getSender(),alarm.getEventNeedsList(),1100);
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
		
//		if (packet instanceof BackTrackPacket)
//			this.backtrack();
			
		
		//When receives a pheromone packet, does the processment, no matter in which stage it is
		if (packet instanceof PheromonePacket)
			this.processPheromonePacket((PheromonePacket) packet);
		
		//When receives a Alarm packet, does the processment only if doing nothing
		if (packet instanceof AlarmPacket)
			if(this.isDoingNothing())
				this.processAlarmPacket((AlarmPacket) packet);
		
		//When receives a Answer Alarm packet:
		if (packet instanceof AnswerAlarmPacket){
			if (this.isSinkOfAnswerAlarm((AnswerAlarmPacket)packet)) {			
					System.out.println("Eleição: " + ((AnswerAlarmPacket) packet).getPheromone().getResources().toString());
					this.addAnswer((AnswerAlarmPacket)packet);
				}
			//Node has an alarm answer schedule to the same sink:
			else { 
					AnswerAlarmWakeUpCall answerWUC  = this.answerAlarmScheduled(((AnswerAlarmPacket) packet).getSinkNode(),((AnswerAlarmPacket) packet).getPheromone());
					if (answerWUC != null){
						this.removeWuc(answerWUC);
					}
				}
			}
	}
		
	private double delay(double pheromoneAmount){
		double time = 1100-(pheromoneAmount*1000);
		return time;
	}
	
	private void processAlarmPacket(AlarmPacket packet) {
		Pheromone uav = null;
	
		if (!this.storedPheromones.isEmpty()){
			//Individual treatment for each distinc pheromone
			for (NodeId key: this.storedPheromones.keySet()){
					uav = this.storedPheromones.get(key);
					//When pheromone is from the same UAV being trailed by the alarm
					if (uav.getId().asInt() == packet.getAlarm().getTrailedUAV()){
//						if(pheromone.get() > packet.getAlarm().getPheromoneAmount()){
//							WakeUpCall wuc = new AnswerAlarmWakeUpCall(getSender(),pheromone,packet,this.delay(pheromone.get()));
//							this.sendEventSelf(wuc);
//						}
					}
					//When pheromone is from a UAV being trailed elsewhere
					else if (packet.getAlarm().getTrackedUAVs().contains(uav.getId())){
						
					}
					//When pheromone is from a UAV not being trailed at all
					else{
						if (this.haveUsefulTrail(uav.getResources())){
							//WakeUpCall has been scheduled
							WakeUpCall wuc = new AnswerAlarmWakeUpCall(getSender(),uav,packet,this.delay(uav.get()));
							this.sendEventSelf(wuc);
						}
					}
			}
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
		//distance = Math.floor(distance);
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
			if(pheromone.get() <= 0.0){
				flavorsToRemove.add(flavor);
			}
		}
		
		for (NodeId nodeId : flavorsToRemove) {
			this.storedPheromones.remove(nodeId);
		}
		
		PheromoneDecreaseWakeUpCall bw = new PheromoneDecreaseWakeUpCall(this.getSender(),PHEROMONE_DECREASE_TIME);
		this.sendEventSelf(bw);
	}

	private boolean isDoingNothing(){
		Iterator<WakeUpCall> iterator = this.getWucs().iterator();
		WakeUpCall wuc = null;
		
		//Looking for an scheduled WUC in answer to the same alarm
		while ((iterator.hasNext()) && (wuc == null)) {
			wuc = iterator.next();
			if (wuc instanceof PheromoneDecreaseWakeUpCall)
				wuc = null;
		}
		return wuc == null;
	}
	
	
	
	private boolean isSinkOfAnswerAlarm(AnswerAlarmPacket answerAlarm){
		if (this.getNode().getId().asInt() == answerAlarm.getSinkNode())
			return this.hasSelectNextSinkScheduled();
		else
			return false;
	}
	
	private boolean hasSelectNextSinkScheduled(){
		Iterator<WakeUpCall> iterator = this.getWucs().iterator();
		WakeUpCall wuc = null;
		
		//Looking for an scheduled WUC in answer to the same alarm
		while ((iterator.hasNext()) && (wuc == null)) {
			wuc = iterator.next();
			if (!(wuc instanceof SelectNextSinkWakeUpCall))
				wuc = null;
		}
		return wuc != null;
	}
	
	private AnswerAlarmWakeUpCall answerAlarmScheduled(int sinkNode, Pheromone pheromone){
		Iterator<WakeUpCall> iterator = this.getWucs().iterator();
		WakeUpCall wuc = null;
		
		//Looking for an scheduled WUC in answer to the same alarm
		while ((iterator.hasNext()) && (wuc == null)) {
			wuc = iterator.next();
			if (!((wuc instanceof AnswerAlarmWakeUpCall) && (((AnswerAlarmWakeUpCall)wuc).getSinkNode() == sinkNode) && (((AnswerAlarmWakeUpCall)wuc).getPheromone().getId().asInt() == pheromone.getId().asInt())))
				wuc = null;
		}
		return (AnswerAlarmWakeUpCall) wuc;
	}
}

