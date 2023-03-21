package it.polimi.tgolfetto.states;

import it.polimi.tgolfetto.contracts.CertificationContract;
import it.polimi.tgolfetto.model.SMC;
import it.polimi.tgolfetto.model.TextileData;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@BelongsToContract(CertificationContract.class)
public class CertificationState implements LinearState {

    private final Party sender;
    private final UniqueIdentifier senderId;
    private final Party receiver;
    private final String networkId;
    private boolean certified;
    private String errors;

    @ConstructorForDeserialization
    public CertificationState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId, boolean certified, String errors) throws ScriptException {
        this.sender = sender;
        this.receiver = receiver;
        this.networkId = networkId;
        this.senderId = senderId;
        this.certified = certified;
        this.errors = errors;

    }

    public void evaluateScore(TextileData textileData, String criteria) throws ScriptException {
        TextileData certificationCriteria = TextileData.fromJson(criteria);
        HashMap<String, SMC> textileMap = textileData.getHashMap();
        HashMap<String, SMC> criteriaMap = certificationCriteria.getHashMap();
        for (String SMCName : criteriaMap.keySet()) {
            SMC textileSMC = textileMap.get(SMCName);
            SMC criteriaSMC = criteriaMap.get(SMCName);
            if (textileSMC != null) {
                HashMap<String, Double> textileValues = textileSMC.getHashMap();
                HashMap<String, Double> criteriaValues = criteriaSMC.getHashMap();
                for (String valueName : criteriaValues.keySet()) {
                    if (textileValues.get(valueName) != null && textileValues.get(valueName).compareTo(criteriaValues.get(valueName)) > 0) {
                        this.errors += SMCName + " - " + valueName + " actual: " + textileValues.get(valueName) + " limit: " + criteriaValues.get(valueName) + "\n";
                    }
                }
            }
        }
        this.certified = this.errors.length() == 0;
    }

    public Party getSender() {
        return sender;
    }

    public Party getReceiver() {
        return receiver;
    }

    public boolean isCertified() {
        return certified;
    }

    public String getErrors() {
        return errors;
    }

    public String getNetworkId() {
        return networkId;
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
        return "CertificationState{" +
                "sender=" + sender +
                ", senderId=" + senderId +
                ", receiver=" + receiver +
                ", networkId='" + networkId + '\'' +
                ", certified=" + certified +
                ", errors=" + errors +
                '}';
    }
}
