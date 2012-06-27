package br.ufla.dcc.mu.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.Position;

public class Alarm {
	private Position position;
	private double timeStamp;
	private NodeId trailedUAV;
	private float pheromoneAmount;
	private List<NodeId> trackedUAVs;
	private Map<Integer,Float> eventNeedsList = new HashMap<Integer,Float>();
	private int divisionNode;
	
	public Alarm(Position position, double timeStamp, Map<Integer,Float> eventNeedsList, int divisionNode, NodeId trailedUAV, float pheromoneAmount, List<NodeId> trackedUAVs){
		this.position = position;
		this.timeStamp = timeStamp;
		this.setPheromoneAmount(pheromoneAmount);
		this.setTrailedUAV(trailedUAV);
		this.setEventNeedsList(eventNeedsList);
		this.setDivisionNode(divisionNode);
		this.setTrackedUAVs(trackedUAVs);
	}

	public Position getPosition() {
		return position;
	}
	public double getTimeStamp() {
		return timeStamp;
	}

	public NodeId getTrailedUAV() {
		return trailedUAV;
	}

	public void setTrailedUAV(NodeId trailedUAV) {
		this.trailedUAV = trailedUAV;
	}

	public Map<Integer, Float> getEventNeedsList() {
		return eventNeedsList;
	}

	public void setEventNeedsList(Map<Integer, Float> eventNeedsList) {
		this.eventNeedsList = eventNeedsList;
	}

	public int getDivisionNode() {
		return divisionNode;
	}

	public void setDivisionNode(int divisionNode) {
		this.divisionNode = divisionNode;
	}
	public void addTrackedUAV(NodeId uav){
		this.trackedUAVs.add(uav);
	}

	public List<NodeId> getTrackedUAVs() {
		return trackedUAVs;
	}

	public void setTrackedUAVs(List<NodeId> trackedUAVs) {
		this.trackedUAVs = trackedUAVs;
	}

	public float getPheromoneAmount() {
		return pheromoneAmount;
	}

	public void setPheromoneAmount(float pheromoneAmount) {
		this.pheromoneAmount = pheromoneAmount;
	}
	
}
