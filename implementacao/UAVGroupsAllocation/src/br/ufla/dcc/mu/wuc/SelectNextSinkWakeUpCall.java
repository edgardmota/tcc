package br.ufla.dcc.mu.wuc;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;

public class SelectNextSinkWakeUpCall extends WakeUpCall {
	public SelectNextSinkWakeUpCall(Address sender, double delay) {
		super(sender, delay);
	}
}
