package it.polimi.tgolfetto.flows.membershipFlows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.bn.flows.RequestMembershipFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;

@StartableByRPC
public class RequestMembership extends FlowLogic<String>{

    private final Party authorisedParty;
    private final String networkId;

    /**
     * Request to a Network Operator to join his network
     * @param authorisedParty
     * @param networkId
     */
    public RequestMembership(Party authorisedParty, String networkId) {
        this.authorisedParty = authorisedParty;
        this.networkId = networkId;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        subFlow(new RequestMembershipFlow(this.authorisedParty,this.networkId,null,null));
        return "\nRequest membership sent from "+ this.getOurIdentity().getName().getOrganisation()+" nodes (ourself) to an authorized network member "+this.authorisedParty.getName().getOrganisation();
    }
}
