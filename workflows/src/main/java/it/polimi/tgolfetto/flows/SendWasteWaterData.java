package it.polimi.tgolfetto.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.bn.flows.BNService;
import net.corda.bn.flows.IllegalMembershipStatusException;
import net.corda.bn.flows.MembershipAuthorisationException;
import net.corda.bn.flows.MembershipNotFoundException;
import net.corda.bn.states.BNRole;
import net.corda.bn.states.MembershipState;
import net.corda.core.contracts.*;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import it.polimi.tgolfetto.contracts.WasteWaterDataContract;
import it.polimi.tgolfetto.states.CertifierIdentity;
import it.polimi.tgolfetto.states.MunicipalityIdentity;
import it.polimi.tgolfetto.states.WasteWaterDataState;
import it.polimi.tgolfetto.states.TextileFirmIdentity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class SendWasteWaterData {

    @InitiatingFlow
    @StartableByRPC
    public static class SendWasteWaterDataInitiator extends FlowLogic<SignedTransaction> {

        private final String networkId;

        private final UniqueIdentifier senderId;
        private final Party receiver;

        private final String jsonData;

        /**
         * Send a json collection of data to a Certifier or Municipality
         *
         * @param networkId
         * @param senderId
         * @param receiver
         * @param jsonData
         */
        public SendWasteWaterDataInitiator(String networkId, UniqueIdentifier senderId, Party receiver, String jsonData) {
            this.networkId = networkId;
            this.senderId = senderId;
            this.receiver = receiver;
            this.jsonData = jsonData;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            // Obtain a reference to a notary we wish to use.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            businessNetworkFullVerification(this.networkId, getOurIdentity(), this.receiver);
            WasteWaterDataState outputState = null;
            outputState = new WasteWaterDataState(getOurIdentity(), this.senderId, this.receiver, networkId, this.jsonData);
            BNService bnService = getServiceHub().cordaService(BNService.class);
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(outputState)
                    .addCommand(new WasteWaterDataContract.Commands.Issue(), Arrays.asList(getOurIdentity().getOwningKey(), receiver.getOwningKey()))
                    .addReferenceState(new ReferencedStateAndRef<>(Objects.requireNonNull(bnService.getMembership(networkId, getOurIdentity()))))
                    .addReferenceState(new ReferencedStateAndRef<>(Objects.requireNonNull(bnService.getMembership(networkId, receiver))
                    ));
            txBuilder.verify(getServiceHub());

            SignedTransaction ptx = getServiceHub().signInitialTransaction(txBuilder);
            FlowSession session = initiateFlow(receiver);
            SignedTransaction ftx = subFlow(new CollectSignaturesFlow(ptx, Collections.singletonList(session)));
            return subFlow(new FinalityFlow(ftx, Collections.singletonList(session)));
        }


        /**
         * Verifies that [sender] and [receiver] are members of Business Network with [networkId] ID, their memberships are active, contain
         * business identity of specific type and that sender is authorised to share the data.
         */
        @Suspendable
        protected void businessNetworkFullVerification(String networkId, Party sender, Party receiver) throws MembershipNotFoundException {
           Memberships memberships = businessNetworkPartialVerification(networkId, sender, receiver);
            try {
                MembershipState senderMembership = memberships.getMembershipA().getState().getData();
                if (!senderMembership.isActive()) {
                    throw new IllegalMembershipStatusException("$sender is not active member of Business Network with $networkId ID");
                }
                if (senderMembership.getIdentity().getBusinessIdentity().getClass() != TextileFirmIdentity.class) {
                    throw new IllegalMembershipBusinessIdentityException("$sender business identity should be TextileFirmIdentity");
                }
                Set<BNRole> setRoles = senderMembership.getRoles();
                for (BNRole role : setRoles) {
                    if (!role.getPermissions().contains(TextileFirmIdentity.IssuePermissions.CAN_SHARE_DATA)) {
                        throw new MembershipAuthorisationException("$ender is not authorised to share data in Business Network with $networkId ID");
                    }
                }
            } catch (Exception e){
                throw new MembershipNotFoundException("$sender is not member of Business Network with $networkId ID");
            }
            try {
                MembershipState receiverMembership = memberships.getMembershipB().getState().getData();
                if (!receiverMembership.isActive()) {
                    throw new IllegalMembershipStatusException("$receiver is not active member of Business Network with $networkId ID");
                }
                if (receiverMembership.getIdentity().getBusinessIdentity().getClass() != MunicipalityIdentity.class && receiverMembership.getIdentity().getBusinessIdentity().getClass() != CertifierIdentity.class) {
                    throw new IllegalMembershipBusinessIdentityException("$receiver business identity should be MunicipalityIdentity or CertifierIdentity");
                }
            } catch (Exception e){
                throw new MembershipNotFoundException("$receiver is not member of Business Network with $networkId ID");
            }
        }

        @Suspendable
        private Memberships businessNetworkPartialVerification(String networkId, Party sender, Party receiver) throws MembershipNotFoundException {
            BNService bnService = getServiceHub().cordaService(BNService.class);
            StateAndRef<MembershipState> senderMembership = null;
            try {
                senderMembership = bnService.getMembership(networkId, sender);
            } catch (Exception e) {
                throw new MembershipNotFoundException("Sender is not part of Business Network with $networkId ID");
            }
            StateAndRef<MembershipState> receiverMembership = null;
            try {
                receiverMembership = bnService.getMembership(networkId, receiver);
            }catch(Exception e){
                throw new MembershipNotFoundException("Receiver is not part of Business Network with $networkId ID");
            }

            return new Memberships(senderMembership, receiverMembership);
        }

    }

    @InitiatedBy(SendWasteWaterDataInitiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartySession;

        public Acceptor(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    Command command = stx.getTx().getCommands().get(0);
                    if (!(command.getValue() instanceof WasteWaterDataContract.Commands.Issue)){
                        throw new FlowException("Only TextileDataContract.Commands.Issue command is allowed");
                    }

                    WasteWaterDataState wasteWaterDataState = (WasteWaterDataState) stx.getTx().getOutputStates().get(0);
                    if (!(wasteWaterDataState.getSender().equals(otherPartySession.getCounterparty()))){
                        throw new FlowException("Sender doesn't match sender's identity");
                    }
                    if(!(wasteWaterDataState.getReceiver().equals(getOurIdentity()))){
                        throw new FlowException("Receiver doesn't match receiver's identity");
                    }
                }
            }
            final SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            final SecureHash txId = subFlow(signTxFlow).getId();
            return subFlow(new ReceiveFinalityFlow(otherPartySession, txId));
        }
    }

    static class Memberships{
        private final StateAndRef<MembershipState> MembershipA;
        private final StateAndRef<MembershipState> MembershipB;

        public Memberships(StateAndRef<MembershipState> membershipA, StateAndRef<MembershipState> membershipB) {
            MembershipA = membershipA;
            MembershipB = membershipB;
        }

        public StateAndRef<MembershipState> getMembershipA() {
            return MembershipA;
        }

        public StateAndRef<MembershipState> getMembershipB() {
            return MembershipB;
        }
    }

    static class IllegalMembershipBusinessIdentityException extends FlowException{
        public IllegalMembershipBusinessIdentityException(@Nullable String message) {
            super(message);
        }
    }

}
