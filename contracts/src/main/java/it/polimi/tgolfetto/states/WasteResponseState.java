package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.WasteRequestContract;
import it.polimi.tgolfetto.contracts.WasteResponseContract;
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

@BelongsToContract(WasteResponseContract.class)
public class WasteResponseState implements LinearState, Serializable {
    private final Party sender;
    private final UniqueIdentifier senderId;
    private final Party receiver;
    private final String networkId;
    private final boolean send;
    private final int qty;

    private final Party supplier;


    @ConstructorForDeserialization
    public WasteResponseState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId, boolean send, int qty, Party supplier) {
        this.sender = sender;
        this.senderId = senderId;
        this.receiver = receiver;
        this.networkId = networkId;
        this.supplier = supplier;
        this.send = send;
        this.qty = qty;
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
                ", supplier='" + supplier + '\'' +
                '}';
    }

    public int getQty() {
        return qty;
    }

    public Party getSupplier() {
        return supplier;
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
