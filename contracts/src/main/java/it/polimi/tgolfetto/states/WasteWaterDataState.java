package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.WasteWaterDataContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(WasteWaterDataContract.class)
public class WasteWaterDataState implements LinearState, Serializable {
    private final Party sender;
    private final UniqueIdentifier senderId;
    private final Party receiver;
    private final String networkId;
    private final String jsonData;

    @ConstructorForDeserialization
    public WasteWaterDataState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId, String jsonData) {
        this.sender = sender;
        this.receiver = receiver;
        this.networkId = networkId;
        this.senderId = senderId;
        this.jsonData = jsonData;
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

    public String getJsonData() {
        return jsonData;
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
        return "TextileDataState{" +
                "sender=" + sender +
                ", senderId=" + senderId +
                ", receiver=" + receiver +
                ", networkId='" + networkId + '\'' +
                ", jsonData='" + jsonData + '\'' +
                '}';
    }
}
