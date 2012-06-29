package br.ufla.dcc.mu.wuc;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.mu.packet.AlarmPacket;
import br.ufla.dcc.mu.utils.Alarm;
import br.ufla.dcc.mu.utils.Pheromone;

public class AnswerAlarmWakeUpCall extends WakeUpCall {
	private Pheromone pheromone;
	private AlarmPacket alarmPacket;
	
	public AnswerAlarmWakeUpCall(Address sender, Pheromone pheromone, AlarmPacket alarmPacket, double delay) {
		super(sender, delay);
		// TODO Auto-generated constructor stub
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
