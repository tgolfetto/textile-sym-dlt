package it.polimi.tgolfetto.states;

import net.corda.bn.states.BNIdentity;
import net.corda.bn.states.BNPermission;
import net.corda.bn.states.BNRole;
import net.corda.core.serialization.CordaSerializable;

import java.util.Collections;
import java.util.HashSet;
/**
 * Custom Identity #3
 * Business identity specific for Municipalities
 */
@CordaSerializable
public class MunicipalityIdentity implements BNIdentity {

    private String municipalityCode;

    public MunicipalityIdentity(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }


    public boolean isValid(){
        return true;
    }

    /*
    @CordaSerializable
    public static class CertifierRole extends BNRole {
        public CertifierRole() {
            super("Certifier", new HashSet<BNPermission>(
                    Collections.singleton(CertifierPermissions.CAN_DO_SOMETHING)));
        }
    }
    @CordaSerializable
    public enum CertifierPermissions implements BNPermission {
        // Enables Business Network member to do something.
        CAN_DO_SOMETHING
    }
    */
}
