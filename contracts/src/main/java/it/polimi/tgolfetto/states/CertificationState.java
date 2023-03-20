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
import java.util.List;

@BelongsToContract(CertificationContract.class)
public class CertificationState implements LinearState {

    private final Party sender;
    private final UniqueIdentifier senderId;
    private final Party receiver;
    private final String networkId;
    private boolean certified;
    private ArrayList<String> errors;

    @ConstructorForDeserialization
    public CertificationState(Party sender, UniqueIdentifier senderId, Party receiver, String networkId) {
        this.sender = sender;
        this.receiver = receiver;
        this.networkId = networkId;
        this.senderId = senderId;
        this.certified = false;
        this.errors = new ArrayList<String>();
    }

    public void evaluateScore(TextileData textileData, String criteria) throws ScriptException {
        TextileData certificationCriteria = TextileData.fromJson(criteria);
        ArrayList<SMC> textileSMC = textileData.getAllSMC();
        ArrayList<SMC> criteriaSMC = certificationCriteria.getAllSMC();
        for (int i = 0; i < textileSMC.size(); i++) {
            ArrayList<Double> textileValues = textileSMC.get(i).getAllValues();
            ArrayList<Double> criteriaValues = criteriaSMC.get(i).getAllValues();
            for (int j = 0; j < textileValues.size(); j++) {
                if (textileValues.get(j).compareTo(criteriaValues.get(j)) > 0) {
                    this.errors.add("Error in value [" + j + "] of " + textileSMC.get(i).toString());
                }
            }
        }
        this.certified = this.errors.size() == 0;
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

    public ArrayList<String> getErrors() {
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
