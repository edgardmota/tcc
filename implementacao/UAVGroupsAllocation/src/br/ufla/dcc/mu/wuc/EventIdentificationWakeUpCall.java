package br.ufla.dcc.mu.wuc;

import java.util.List;
import java.util.Map;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.mu.utils.Pheromone;

public class EventIdentificationWakeUpCall extends WakeUpCall {
	private int root;
	private Map<NodeId, Pheromone> storedPheromones;
	private List<Integer> trackedUAVs;
	private Map<Integer,Float> neededResources;
	private int teste;

	public EventIdentificationWakeUpCall(Address sender,Map<Integer,Float> neededResources, int root, Map<NodeId, Pheromone> storedPheromones, List<Integer> trackedUAVs, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
		this.setRoot(root);
		this.setPheromoneAmount(storedPheromones);
		this.setTrackedUAVs(trackedUAVs);
		this.setNeededResources(neededResources);
	}

	
	
	public Map<Integer, Float> getNeededResources() {
		return neededResources;
	}



	public void setNeededResources(Map<Integer, Float> neededResources) {
		this.neededResources = neededResources;
	}



	public int getRoot() {
		return root;
	}

	public void setRoot(int divisionNode) {
		this.root = divisionNode;
	}

	public Map<NodeId, Pheromone> getStoredPheromones() {
		return storedPheromones;
	}

	public void setPheromoneAmount(Map<NodeId, Pheromone> storedPheromones) {
		this.storedPheromones = storedPheromones;
	}

	public List<Integer> getTrackedUAVs() {
		return trackedUAVs;
	}

	public void setTrackedUAVs(List<Integer> trackedUAVs) {
		this.trackedUAVs = trackedUAVs;
	}
	

}
