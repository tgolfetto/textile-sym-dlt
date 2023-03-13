package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.TextileDataContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import it.polimi.tgolfetto.contracts.TextileDataContract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BelongsToContract(TextileDataContract.class)
public class TextileDataState implements LinearState {
    private Party sender;
    private Party receiver;
    private String networkId;
    private UniqueIdentifier linearId;
    private String jsonData;

    @ConstructorForDeserialization
    public TextileDataState(Party sender, Party receiver, String networkId, UniqueIdentifier linearId, String jsonData) {
        this.sender = sender;
        this.receiver = receiver;
        this.networkId = networkId;
        this.linearId = linearId;
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
        return this.linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return null;
    }
}
