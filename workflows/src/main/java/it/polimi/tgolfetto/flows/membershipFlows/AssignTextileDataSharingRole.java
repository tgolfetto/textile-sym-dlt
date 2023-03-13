package it.polimi.tgolfetto.flows.membershipFlows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.bn.flows.BNService;
import net.corda.bn.flows.ModifyRolesFlow;
import net.corda.bn.states.BNRole;
import net.corda.bn.states.MembershipState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import it.polimi.tgolfetto.states.TextileFirmIdentity;

import java.util.HashSet;
import java.util.Set;

@StartableByRPC
public class AssignTextileDataSharingRole extends FlowLogic<SignedTransaction> {

    private UniqueIdentifier membershipId;
    private String networkId;

    /**
     * Assing to the member of a network the role of textile data sharing
     * @param membershipId
     * @param networkId
     */
    public AssignTextileDataSharingRole(UniqueIdentifier membershipId, String networkId) {
        this.membershipId = membershipId;
        this.networkId = networkId;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // Obtain a reference to a notary we wish to use.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // Get the current roles and add the TextileDataSharingRole
        BNService bnService = getServiceHub().cordaService(BNService.class);
        MembershipState membershipState = bnService.getMembership(this.membershipId).getState().getData();
        Set<BNRole> roles = new HashSet<>();
        for(BNRole br : membershipState.getRoles()){
            roles.add(br);
        }
        roles.add(new TextileFirmIdentity.TextileDataSharingRole());
        return subFlow(new ModifyRolesFlow(membershipId, roles, notary));
    }
}
