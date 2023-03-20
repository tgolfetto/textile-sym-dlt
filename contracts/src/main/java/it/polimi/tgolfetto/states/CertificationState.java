package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.CertificationContract;
import it.polimi.tgolfetto.model.TextileData;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(CertificationContract.class)
public class CertificationState implements LinearState {

    private final Party sender;
    private final UniqueIdentifier senderId;
    private final Party receiver;
    private final String networkId;
    private final String certification;

    @ConstructorForDeserialization
    public CertificationState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId, String certification) {
        this.sender = sender;
        this.receiver = receiver;
        this.networkId = networkId;
        this.senderId = senderId;
        this.certification = certification;
    }

    public static String evaluateScore(TextileData[] textileData, String criteria){
        return "C";
    }

    public Party getSender() {
        return sender;
    }

    public Party getReceiver() {
        return receiver;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getCertification() {
        return certification;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.senderId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(new Party[]{sender, receiver});
    }

    @Override
    public String toString() {
        return "Certification{" +
                "sender=" + sender +
                ", senderId=" + senderId +
                ", receiver=" + receiver +
                ", networkId='" + networkId + '\'' +
                ", certification='" + certification + '\'' +
                '}';
    }
}
