package it.polimi.tgolfetto.states;

import net.corda.bn.states.BNIdentity;
import net.corda.bn.states.BNPermission;
import net.corda.bn.states.BNRole;
import net.corda.core.serialization.CordaSerializable;

import java.util.Collections;
import java.util.HashSet;
/**
 * Custom Identity #2
 * Business identity specific for Certifier companies
 */
@CordaSerializable
public class CertifierIdentity implements BNIdentity {

    private final String certifierIdentityCode;
    private final String cicRegex = "^[a-zA-Z]{6}[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?$";

    public CertifierIdentity(String certifierIdentityCode) {
        this.certifierIdentityCode = certifierIdentityCode;
    }

    public String getCertifierIdentityCode() {
        return certifierIdentityCode;
    }

    public String getCicRegex() {
        return cicRegex;
    }

    public boolean isValid(){
        return this.certifierIdentityCode.matches(cicRegex);
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
