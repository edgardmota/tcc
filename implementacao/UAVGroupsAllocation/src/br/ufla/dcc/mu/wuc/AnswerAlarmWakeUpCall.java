package br.ufla.dcc.mu.wuc;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.mu.utils.Pheromone;

public class AnswerAlarmWakeUpCall extends WakeUpCall {
	private Pheromone pheromone;
	private int sinkNode;
	
	public AnswerAlarmWakeUpCall(Address sender, Pheromone pheromone, int sinkNode, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
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
