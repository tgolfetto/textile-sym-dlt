package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.TextileDataContract;
import it.polimi.tgolfetto.model.TextileData;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import it.polimi.tgolfetto.contracts.TextileDataContract;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(TextileDataContract.class)
public class TextileDataState implements LinearState {
    private Party sender;
    private UniqueIdentifier senderId;
    private Party receiver;
    private String networkId;
    private TextileData data;


    @ConstructorForDeserialization
    public TextileDataState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId, String jsonData) throws ScriptException {
        this.sender = sender;
        this.receiver = receiver;
        this.networkId = networkId;
        this.senderId = senderId;
        this.data = TextileData.fromJson(jsonData);
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

    public TextileData getData() {
        return data;
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
                ", data='" + data + '\'' +
                '}';
    }
}
