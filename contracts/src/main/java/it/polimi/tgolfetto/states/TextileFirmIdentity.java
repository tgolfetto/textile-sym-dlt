package it.polimi.tgolfetto.states;

import net.corda.bn.states.BNIdentity;
import net.corda.bn.states.BNPermission;
import net.corda.bn.states.BNRole;
import net.corda.core.serialization.CordaSerializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

/**
 * Custom Identity #1
 * Business identity specific for Textile manufacturing Companies.
 */
@CordaSerializable
public class TextileFirmIdentity implements BNIdentity {

    private final String textileFirmIdentityCode;
    private final String iicRegex = "^[a-zA-Z]{6}[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?$";


    public TextileFirmIdentity(String textileFirmIdentityCode) {
        this.textileFirmIdentityCode = textileFirmIdentityCode;
    }

    public String getTextileFirmIdentityCode() {
        return textileFirmIdentityCode;
    }

    public String getIicRegex() {
        return iicRegex;
    }

    public boolean isValid(){
        return this.textileFirmIdentityCode.matches(iicRegex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextileFirmIdentity)) return false;
        TextileFirmIdentity that = (TextileFirmIdentity) o;
        return Objects.equals(getTextileFirmIdentityCode(), that.getTextileFirmIdentityCode()) && Objects.equals(getIicRegex(), that.getIicRegex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTextileFirmIdentityCode(), getIicRegex());
    }

    @CordaSerializable
    public static class TextileDataSharingRole extends BNRole {
        public TextileDataSharingRole() {
            super("TextileDataSharingRole", new HashSet<BNPermission>(Collections.singleton(IssuePermissions.CAN_SHARE_DATA)));
        }
    }
    @CordaSerializable
    public enum IssuePermissions implements BNPermission {
        /** Enables Business Network member to share data. **/
        CAN_SHARE_DATA
    }
}
