package br.ufla.dcc.mu.packet;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;
import br.ufla.dcc.mu.utils.Pheromone;

public class AnswerAlarmPacket extends ApplicationPacket {

	private Pheromone pheromone;
	private int sinkNode;
	
	public AnswerAlarmPacket(Address sender, NodeId receiver, Pheromone pheromone, int sinkNode) {
		super(sender, receiver);
		this.setPheromone(pheromone);
		this.setSinkNode(sinkNode);
	}

	public Pheromone getPheromone() {
		return pheromone;
	}

	public void setPheromone(Pheromone pheromone) {
		this.pheromone = pheromone;
	}

	public int getSinkNode() {
		return sinkNode;
	}

	public void setSinkNode(int sinkNode) {
		this.sinkNode = sinkNode;
	}
	
	
	
}
