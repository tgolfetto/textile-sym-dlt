package it.polimi.tgolfetto.flows;

import co.paralleluniverse.fibers.Suspendable;
import it.polimi.tgolfetto.states.WasteRequestState;
import it.polimi.tgolfetto.states.WasteResponseState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.utilities.UntrustworthyData;


public class SendWasteResponse extends FlowLogic<Void> {
    private final FlowSession counterpartySession;

    public SendWasteResponse(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;

    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        UntrustworthyData<WasteRequestState> counterpartyData = counterpartySession.receive(WasteRequestState.class);
        WasteRequestState wasteRequestState = counterpartyData.unwrap(msg -> {
            return msg;
        });
        // TODO search supplier and send it in response
        WasteResponseState wasteResponseState = new WasteResponseState(wasteRequestState.getSender(),wasteRequestState.getLinearId(), wasteRequestState.getReceiver(), wasteRequestState.getNetworkId(), false, 100, wasteRequestState.getSender());
        counterpartySession.send(wasteResponseState);
        return null;
    }
}
