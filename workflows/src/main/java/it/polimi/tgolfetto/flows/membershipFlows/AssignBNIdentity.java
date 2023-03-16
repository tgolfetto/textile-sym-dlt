package it.polimi.tgolfetto.flows.membershipFlows;

import co.paralleluniverse.fibers.Suspendable;
import it.polimi.tgolfetto.states.CertifierIdentity;
import it.polimi.tgolfetto.states.MunicipalityIdentity;
import it.polimi.tgolfetto.states.TextileFirmIdentity;
import net.corda.bn.flows.ModifyBusinessIdentityFlow;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;

@StartableByRPC
public class AssignBNIdentity extends FlowLogic<String> {

    public enum BUSINESS_NET_IDENTITIES{
        TextileFirm,
        Certifier,
        Municipality
    }
    private String firmType;
    private UniqueIdentifier membershipId;
    private String bnIdentity;

    /**
     * Assign a firm type and a business identity to the member
     * @param firmType
     * @param membershipId
     * @param bnIdentity
     */
    public AssignBNIdentity(String firmType, UniqueIdentifier membershipId, String bnIdentity) {
        this.firmType = firmType;
        this.membershipId = membershipId;
        this.bnIdentity = bnIdentity;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        // Obtain a reference to a notary we wish to use.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // Check which BN identity we wish to apply and assign it
        if (this.firmType.equals(BUSINESS_NET_IDENTITIES.TextileFirm.toString())) {
            TextileFirmIdentity textileFirmIdentity = new TextileFirmIdentity(bnIdentity);
            if (!textileFirmIdentity.isValid()) {
                throw new IllegalArgumentException(bnIdentity + " in not a valid Textile Firm Identity");
            }
            subFlow(new ModifyBusinessIdentityFlow(membershipId, textileFirmIdentity, notary));
        } else if (this.firmType.equals(BUSINESS_NET_IDENTITIES.Certifier.toString())) {
            CertifierIdentity certifierIdentity = new CertifierIdentity(bnIdentity);
            if (!certifierIdentity.isValid()) {
                throw new IllegalArgumentException(bnIdentity + " in not a valid Certifier Identity");
            }
            subFlow(new ModifyBusinessIdentityFlow(membershipId, certifierIdentity, notary));
        } else if (this.firmType.equals(BUSINESS_NET_IDENTITIES.Municipality.toString())) {
            MunicipalityIdentity municipalityIdentity = new MunicipalityIdentity(bnIdentity);
            if (!municipalityIdentity.isValid()) {
                throw new IllegalArgumentException(bnIdentity + " in not a valid Municipality Identity");
            }
            subFlow(new ModifyBusinessIdentityFlow(membershipId, municipalityIdentity, notary));
        } else {
            throw new IllegalArgumentException(bnIdentity + " in not a valid BN Identity");
        }
        return "Issue a "+ this.firmType+" BN Identity to member("+ this.membershipId+")";
    }
}
