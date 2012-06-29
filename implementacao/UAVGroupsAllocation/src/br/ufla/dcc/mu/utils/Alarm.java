package br.ufla.dcc.mu.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;

public class Alarm {
	private Position position;
	private double timeStamp;
	private int trailedUAV;
	private Map<NodeId, Pheromone> storedPheromones;
	private List<Integer> trackedUAVs;
	private Map<Integer,Float> eventNeedsList = new HashMap<Integer,Float>();
	private int root;
	
	public Alarm(Position position, double timeStamp, Map<Integer,Float> eventNeedsList, int root, int trailedUAV, Map<NodeId, Pheromone> storedPheromones, List<Integer> trackedUAVs){
		this.position = position;
		this.timeStamp = timeStamp;
		this.setPheromoneAmount(storedPheromones);
		this.setTrailedUAV(trailedUAV);
		this.setEventNeedsList(eventNeedsList);
		this.setRoot(root);
		this.setTrackedUAVs(trackedUAVs);
	}

	public Position getPosition() {
		return position;
	}
	public double getTimeStamp() {
		return timeStamp;
	}

	public int getTrailedUAV() {
		return trailedUAV;
	}

	public void setTrailedUAV(int trailedUAV) {
		this.trailedUAV = trailedUAV;
	}

	public Map<Integer, Float> getEventNeedsList() {
		return eventNeedsList;
	}

	public void setEventNeedsList(Map<Integer, Float> eventNeedsList) {
		this.eventNeedsList = eventNeedsList;
	}

	public int getRoot() {
		return root;
	}

	public void setRoot(int divisionNode) {
		this.root = divisionNode;
	}
	public void addTrackedUAV(int uav){
		this.trackedUAVs.add(uav);
	}

	public List<Integer> getTrackedUAVs() {
		return trackedUAVs;
	}

	public void setTrackedUAVs(List<Integer> trackedUAVs) {
		this.trackedUAVs = trackedUAVs;
	}

	public Map<NodeId, Pheromone> getPheromoneAmount() {
		return storedPheromones;
	}

	public void setPheromoneAmount(Map<NodeId, Pheromone> storedPheromones2) {
		this.storedPheromones = storedPheromones2;
	}
	
}
