package it.polimi.tgolfetto.flows.membershipFlows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.bn.flows.ActivateMembershipFlow;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;

@StartableByRPC
public class ActivateMember extends FlowLogic<String> {

    private final UniqueIdentifier membershipId;

    /**
     * Activate a member that requested to join the network
     * @param membershipId
     */
    public ActivateMember(UniqueIdentifier membershipId) {
        this.membershipId = membershipId;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        subFlow(new ActivateMembershipFlow(this.membershipId,notary));
        return "\nMember("+ this.membershipId.toString()+")'s network membership has been activated.";
    }
}
