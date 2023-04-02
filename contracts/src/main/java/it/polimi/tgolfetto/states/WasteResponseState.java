package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.WasteResponseContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@BelongsToContract(WasteResponseContract.class)
public class WasteResponseState implements LinearState, Serializable {
    private final Party sender;
    private final UniqueIdentifier senderId;
    private final Party receiver;
    private final String networkId;

    private String jsonSuppliersList;


    @ConstructorForDeserialization
    public WasteResponseState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId, String jsonSuppliersList) {
        this.sender = sender;
        this.senderId = senderId;
        this.receiver = receiver;
        this.networkId = networkId;
        this.jsonSuppliersList = jsonSuppliersList;
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



    @Override
    public String toString() {
        return "WasteResponseState{" +
                "sender=" + sender +
                ", senderId=" + senderId +
                ", receiver=" + receiver +
                ", networkId='" + networkId + '\'' +
                ", suppliers='" + jsonSuppliersList + '\'' +
                '}';
    }

    public String getJsonSuppliersList() {
        return jsonSuppliersList;
    }

    public void suppliersListFromMap(HashMap<String, HashMap<String, Integer>> map) {
        String json = "{" + map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":{" + e.getValue().entrySet().stream().map(v -> "\"" + v.getKey() + "\":" + v.getValue()).collect(Collectors.joining(", ")) + "}")
                .collect(Collectors.joining(", ")) + "}";
        this.jsonSuppliersList = json;
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
