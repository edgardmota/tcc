package br.ufla.dcc.mu.packet;

import java.util.HashMap;
import java.util.Map;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Packet;

public class PheromonePacket extends Packet {

	private NodeId uavId;
	private Map<Integer,Float> resources = new HashMap<Integer,Float>();
	
	public PheromonePacket(Address sender, NodeId receiver, Map<Integer,Float> resources) {
		super(sender, receiver);
		this.uavId = sender.getId();
		this.setResources(resources);
	}

	public boolean setResources(Map<Integer,Float> resources){
		this.resources = resources;
		return true;
	}
	
	public Map<Integer,Float> getResources(){
		return this.resources;
	}
	
	public NodeId getUavId(){
		return this.uavId;
	}
}
