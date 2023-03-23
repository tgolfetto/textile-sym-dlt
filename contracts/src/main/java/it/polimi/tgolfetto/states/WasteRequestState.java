package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.WasteRequestContract;
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

@BelongsToContract(WasteRequestContract.class)
public class WasteRequestState implements LinearState, Serializable {
    private final Party sender;
    private final UniqueIdentifier senderId;
    private final Party receiver;
    private final String networkId;
    private final boolean send;
    private final int qty;
    private final String wasteName;
    private final String textileData;

    @ConstructorForDeserialization
    public WasteRequestState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId, boolean send, int qty, String wasteName, String textileData) {
        this.sender = sender;
        this.senderId = senderId;
        this.receiver = receiver;
        this.networkId = networkId;
        this.textileData = textileData;
        this.send = send;
        this.qty = qty;
        this.wasteName = wasteName;
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

    public boolean isSend() {
        return send;
    }

    @Override
    public String toString() {
        return "WasteRequestState{" +
                "sender=" + sender +
                ", senderId=" + senderId +
                ", receiver=" + receiver +
                ", networkId='" + networkId + '\'' +
                ", send=" + send +
                ", qty=" + qty +
                ", wasteName='" + wasteName + '\'' +
                ", textileData='" + textileData + '\'' +
                '}';
    }

    public int getQty() {
        return qty;
    }

    public String getWasteName() {
        return wasteName;
    }

    public String getTextileData() {
        return textileData;
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
}
