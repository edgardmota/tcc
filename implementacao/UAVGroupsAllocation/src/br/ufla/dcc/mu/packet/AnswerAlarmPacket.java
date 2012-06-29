package br.ufla.dcc.mu.packet;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;
import br.ufla.dcc.mu.utils.Pheromone;

public class AnswerAlarmPacket extends ApplicationPacket {

	private Pheromone pheromone;
	private AlarmPacket alarmPacket;
	
	public AnswerAlarmPacket(Address sender, NodeId receiver, Pheromone pheromone, AlarmPacket alarmPacket) {
		super(sender, receiver);
		this.setPheromone(pheromone);
		this.setAlarmPacket(alarmPacket);
	}

	public Pheromone getPheromone() {
		return pheromone;
	}

	public void setPheromone(Pheromone pheromone) {
		this.pheromone = pheromone;
	}

	public int getSinkNode() {
		return this.getAlarmPacket().getSender().getId().asInt();
	}


	public AlarmPacket getAlarmPacket() {
		return alarmPacket;
	}

	public void setAlarmPacket(AlarmPacket alarmPacket) {
		this.alarmPacket = alarmPacket;
	}
	
	
	
}
