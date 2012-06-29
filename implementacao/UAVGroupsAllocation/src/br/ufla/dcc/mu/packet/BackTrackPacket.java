package br.ufla.dcc.mu.packet;

import java.util.List;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.ApplicationPacket;
import br.ufla.dcc.mu.utils.Alarm;

public class BackTrackPacket extends ApplicationPacket {

	private List<AnswerAlarmPacket> answerList;
	
	public BackTrackPacket(Address sender, NodeId receiver, List<AnswerAlarmPacket> answerList) {
		super(sender, receiver);
		this.setAnswer(answerList);
	}

	public List<AnswerAlarmPacket> getAnswer() {
		return answerList;
	}

	public void setAnswer(List<AnswerAlarmPacket> answer) {
		this.answerList = answer;
	}
	
	
	
}
