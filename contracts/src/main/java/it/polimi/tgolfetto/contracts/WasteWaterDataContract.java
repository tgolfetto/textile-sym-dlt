package it.polimi.tgolfetto.contracts;

import it.polimi.tgolfetto.states.TextileFirmIdentity;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;
import net.corda.bn.states.MembershipState;
import net.corda.core.identity.Party;
import it.polimi.tgolfetto.states.WasteWaterDataState;
import it.polimi.tgolfetto.states.CertifierIdentity;
import it.polimi.tgolfetto.states.MunicipalityIdentity;

import java.lang.IllegalArgumentException;
import java.util.List;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class WasteWaterDataContract implements Contract {

    public static final String TextileDataContract_ID = "it.polimi.tgolfetto.textile-sym-dlt.contracts.TextileDataContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData command = tx.getCommands().get(0).getValue();
        WasteWaterDataState output = (WasteWaterDataState) tx.getOutputs().get(0).getData();
        if (command instanceof Commands.Issue){
            verifyIssue(tx,output.getNetworkId(), output.getSender(), output.getReceiver());
        }else{
            throw new IllegalArgumentException("Unsupported command "+command);
        }
    }

    public void verifyIssue(LedgerTransaction tx, String networkId, Party sender, Party receiver){
        verifyMembershipsForTextileDataTransaction(tx, networkId, sender, receiver, "Issue");

    }
    public void verifyMembershipsForTextileDataTransaction(LedgerTransaction tx, String networkId,
                                                            Party sender, Party receiver,String commandName){
        requireThat(require -> {
            //Verify number of memberships
            require.using("Textile data "+ commandName+" transaction should have 2 reference states", tx.getReferences().size() == 2);
            require.using("Textile data "+ commandName+" transaction should contain only reference MembershipStates",
                    tx.getReferenceStates().stream().allMatch(it -> it.getClass() == MembershipState.class));

            //Extract memberships
            List<MembershipState> membershipReferenceStates = tx.getReferenceStates().stream().map( it -> (MembershipState) it).collect(Collectors.toList());
            require.using("Textile data "+ commandName+
                    " transaction should contain only reference membership states from Business Network with "+networkId+" ID",
                    membershipReferenceStates.stream().allMatch(it -> it.getNetworkId().equals(networkId)));

            //Extract Membership and verify not null
            MembershipState textileFirmMembership = membershipReferenceStates.stream()
                    .filter(it -> (it.getNetworkId().equals(networkId) && it.getIdentity().getCordaIdentity().equals(sender)))
                    .collect(Collectors.toList()).get(0);
            require.using("\nTextile data "+ commandName+" transaction should have sender's reference membership state", textileFirmMembership!= null);

            MembershipState receiverMembership = membershipReferenceStates.stream()
                    .filter(it -> (it.getNetworkId().equals(networkId) && it.getIdentity().getCordaIdentity().equals(receiver)))
                    .collect(Collectors.toList()).get(0);
            require.using("\nTextile data "+ commandName+" transaction should have receiver's reference membership state", receiverMembership!= null);

            //Exam the customized Identity
            require.using("TextileFirm should be active member of Business Network with "+networkId, textileFirmMembership.isActive());
            require.using("TextileFirm should have business identity of FirmIdentity type",
                    textileFirmMembership.getIdentity().getBusinessIdentity().getClass().equals(TextileFirmIdentity.class));

            require.using("Receiver should be active member of Business Network with "+networkId, receiverMembership.isActive());
            require.using("Receiver(Certifier/Municipality) should have business identity of FirmIdentity type",
                    receiverMembership.getIdentity().getBusinessIdentity().getClass().equals(CertifierIdentity.class) || receiverMembership.getIdentity().getBusinessIdentity().getClass().equals(MunicipalityIdentity.class));
            return null;
        });
    }


    public interface Commands extends CommandData{
        class Issue implements Commands {}
        class Claim implements Commands {}

    }
}
