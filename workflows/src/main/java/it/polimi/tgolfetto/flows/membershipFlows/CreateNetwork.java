package it.polimi.tgolfetto.flows.membershipFlows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.transactions.SignedTransaction;
import net.corda.bn.flows.CreateBusinessNetworkFlow;


@StartableByRPC
public class CreateNetwork extends FlowLogic<String> {

    /**
     * Create a new business network
     *
     * @return the new network ID
     * @throws FlowException
     */
    @Override
    @Suspendable
    public String call() throws FlowException {
        UniqueIdentifier networkId = new UniqueIdentifier();
        subFlow(new CreateBusinessNetworkFlow(networkId, null, new UniqueIdentifier(), null, null));
        return "\nA network was created with NetworkID: " + networkId;
    }
}
