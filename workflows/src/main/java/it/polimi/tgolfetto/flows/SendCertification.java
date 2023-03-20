package it.polimi.tgolfetto.flows;

import co.paralleluniverse.fibers.Suspendable;
import it.polimi.tgolfetto.contracts.CertificationContract;
import it.polimi.tgolfetto.contracts.TextileDataContract;
import it.polimi.tgolfetto.model.TextileData;
import it.polimi.tgolfetto.states.*;
import net.corda.bn.flows.BNService;
import net.corda.bn.flows.IllegalMembershipStatusException;

import net.corda.bn.flows.MembershipNotFoundException;

import net.corda.bn.states.MembershipState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ReferencedStateAndRef;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SendCertification {
    @InitiatingFlow
    @StartableByRPC
    public static class SendCertificationInitiator extends FlowLogic<SignedTransaction> {

        private final String networkId;

        private final UniqueIdentifier senderId;
        private final Party receiver;

        private final String criteria;

        /**
         * Send a sustainability certification to a Textile Firm
         *
         * @param networkId
         * @param senderId
         * @param receiver
         * @param criteria for issuing a certification score
         */
        public SendCertificationInitiator(String networkId, UniqueIdentifier senderId, Party receiver, String criteria) {
            this.networkId = networkId;
            this.senderId = senderId;
            this.receiver = receiver;
            this.criteria = criteria;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            List<StateAndRef<TextileDataState>> states = getServiceHub().getVaultService().queryBy(TextileDataState.class).getStates();
            TextileData[] textileDatas = states.stream().filter(state -> state.getState().getData().getSender().equals(receiver)).map(state -> {
                try {
                    return TextileData.fromJson(state.getState().getData().getJsonData());
                } catch (ScriptException e) {
                    throw new RuntimeException("### SendCertificationInitiator: " + e);
                }
            }).toArray(TextileData[]::new);

            // Obtain a reference to a notary we wish to use.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            businessNetworkFullVerification(this.networkId, getOurIdentity(), this.receiver);
            CertificationState outputState = null;
            outputState = new CertificationState(getOurIdentity(), this.senderId, this.receiver, networkId);
            try {
                outputState.evaluateScore(textileDatas[0], criteria);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            BNService bnService = getServiceHub().cordaService(BNService.class);
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(outputState)
                    .addCommand(new CertificationContract.Commands.Issue(), Arrays.asList(getOurIdentity().getOwningKey(), receiver.getOwningKey()))
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
            BNService bnService = getServiceHub().cordaService(BNService.class);
            try {
                MembershipState senderMembership = bnService.getMembership(networkId, sender).getState().getData();
                if (!senderMembership.isActive()) {
                    throw new IllegalMembershipStatusException("$sender is not active member of Business Network with $networkId ID");
                }
                if (senderMembership.getIdentity().getBusinessIdentity().getClass() != CertifierIdentity.class) {
                    throw new SendCertification.IllegalMembershipBusinessIdentityException("$sender business identity should be CertifierIdentity");
                }
            } catch (Exception e){
                throw new MembershipNotFoundException("$sender is not member of Business Network with $networkId ID");
            }
            try {
                MembershipState receiverMembership = bnService.getMembership(networkId, receiver).getState().getData();
                if (!receiverMembership.isActive()) {
                    throw new IllegalMembershipStatusException("$receiver is not active member of Business Network with $networkId ID");
                }
                if (receiverMembership.getIdentity().getBusinessIdentity().getClass() != TextileFirmIdentity.class) {
                    throw new SendCertification.IllegalMembershipBusinessIdentityException("$receiver business identity should be TextileFirmIdentity");
                }
            } catch (Exception e){
                throw new MembershipNotFoundException("$receiver is not member of Business Network with $networkId ID");
            }
        }

        @Suspendable
        private SendTextileData.Memberships businessNetworkPartialVerification(String networkId, Party sender, Party receiver) throws MembershipNotFoundException {
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

            return new SendTextileData.Memberships(senderMembership, receiverMembership);
        }

    }

    @InitiatedBy(SendCertification.SendCertificationInitiator.class)
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
                    if (!(command.getValue() instanceof CertificationContract.Commands.Issue)){
                        throw new FlowException("Only CertificationContract.Commands.Issue command is allowed");
                    }

                    CertificationState certificationState = (CertificationState) stx.getTx().getOutputStates().get(0);
                    if (!(certificationState.getSender().equals(otherPartySession.getCounterparty()))){
                        throw new FlowException("Sender doesn't match sender's identity");
                    }
                    if(!(certificationState.getReceiver().equals(getOurIdentity()))){
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
