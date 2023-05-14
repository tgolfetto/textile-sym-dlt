package it.polimi.tgolfetto.contracts;

import it.polimi.tgolfetto.states.*;
import net.corda.bn.states.MembershipState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CertificationContract implements Contract {

    public static final String CertificationContractContract_ID = "it.polimi.tgolfetto.contracts.CertificationContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandData command = tx.getCommands().get(0).getValue();
        CertificationState output = (CertificationState) tx.getOutputs().get(0).getData();
        if (command instanceof CertificationContract.Commands.Issue){
            verifyIssue(tx,output.getNetworkId(), output.getSender(), output.getReceiver());
        }else{
            throw new IllegalArgumentException("Unsupported command "+command);
        }
    }

    public void verifyIssue(LedgerTransaction tx, String networkId, Party sender, Party receiver){
        verifyMembershipsForCertificationTransaction(tx, networkId, sender, receiver, "Issue");

    }
    public void verifyMembershipsForCertificationTransaction(LedgerTransaction tx, String networkId,
                                                           Party sender, Party receiver,String commandName){
        requireThat(require -> {
            //Verify number of memberships
            require.using("Certification "+ commandName+" transaction should have 2 reference states", tx.getReferences().size() == 2);
            require.using("Certification "+ commandName+" transaction should contain only reference MembershipStates",
                    tx.getReferenceStates().stream().allMatch(it -> it.getClass() == MembershipState.class));

            //Extract memberships
            List<MembershipState> membershipReferenceStates = tx.getReferenceStates().stream().map(it -> (MembershipState) it).collect(Collectors.toList());
            require.using("Certification "+ commandName+
                            " transaction should contain only reference membership states from Business Network with "+networkId+" ID",
                    membershipReferenceStates.stream().allMatch(it -> it.getNetworkId().equals(networkId)));

            //Extract Membership and verify not null
            MembershipState textileFirmMembership = membershipReferenceStates.stream()
                    .filter(it -> (it.getNetworkId().equals(networkId) && it.getIdentity().getCordaIdentity().equals(receiver)))
                    .collect(Collectors.toList()).get(0);
            require.using("\nCertification "+ commandName+" transaction should have receiver's reference membership state", textileFirmMembership!= null);

            MembershipState certifierMembership = membershipReferenceStates.stream()
                    .filter(it -> (it.getNetworkId().equals(networkId) && it.getIdentity().getCordaIdentity().equals(sender)))
                    .collect(Collectors.toList()).get(0);
            require.using("\nCertification "+ commandName+" transaction should have sender's reference membership state", certifierMembership!= null);

            //Exam the customized Identity
            require.using("TextileFirm should be active member of Business Network with "+networkId, textileFirmMembership.isActive());
            require.using("TextileFirm should have business identity of FirmIdentity type",
                    textileFirmMembership.getIdentity().getBusinessIdentity().getClass().equals(TextileFirmIdentity.class));

            require.using("Certifier should be active member of Business Network with "+networkId, certifierMembership.isActive());
            require.using("Certifier should have business identity of FirmIdentity type",
                    certifierMembership.getIdentity().getBusinessIdentity().getClass().equals(CertifierIdentity.class));
            return null;
        });
    }


    public interface Commands extends CommandData{
        class Issue implements CertificationContract.Commands {}
        class Claim implements CertificationContract.Commands {}
    }
}
