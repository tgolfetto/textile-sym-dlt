package it.polimi.tgolfetto.contracts;

import com.google.common.collect.ImmutableList;
import it.polimi.tgolfetto.states.*;
import net.corda.bn.states.BNRole;
import net.corda.bn.states.MembershipIdentity;
import net.corda.bn.states.MembershipState;
import net.corda.bn.states.MembershipStatus;
import net.corda.core.contracts.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NotaryInfo;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
;
import javax.script.ScriptException;
import java.time.Instant;
import java.util.*;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity textileFirmIdentity = new TestIdentity(new CordaX500Name("TextileManufacturer1", "Prato", "IT"));
    private final TestIdentity certifierIdentity = new TestIdentity(new CordaX500Name("Certifier", "Zurich", "CH"));
    private final TestIdentity municipalityIdentity = new TestIdentity(new CordaX500Name("Municipality", "Prato", "IT"));

    private MockServices ledgerServices = new MockServices(
            Arrays.asList("it.polimi.tgolfetto.contracts")
    );

    @Before
    public void setup() {
    }

    @After
    public void tearDown() { }

    @Test
    public void contractTest() {
        assert(new CertificationContract() instanceof Contract);
        assert(new WasteWaterDataContract() instanceof Contract);
        assert(new WasteRequestContract() instanceof Contract);
        assert(new WasteResponseContract() instanceof Contract);
    }

    @Test
    public void failedCertifierTransactionTest() throws ScriptException {
        CertificationState certificationState = new CertificationState(certifierIdentity.getParty(), new UniqueIdentifier(), textileFirmIdentity.getParty(), "networkId", true, "");
        transaction(ledgerServices, tx -> {
            tx.output(CertificationContract.CertificationContractContract_ID, certificationState);
            tx.command(Arrays.asList(certifierIdentity.getPublicKey(), textileFirmIdentity.getPublicKey()),new CertificationContract.Commands.Claim());
            tx.fails();
            return null;
        });
    }

    @Test
    public void failedWasteWaterShareTransactionTest() throws ScriptException {
        WasteWaterDataState wasteWaterDataState = new WasteWaterDataState(textileFirmIdentity.getParty(), new UniqueIdentifier(), certifierIdentity.getParty(), "networkId", "");
        transaction(ledgerServices, tx -> {
            tx.output(CertificationContract.CertificationContractContract_ID, wasteWaterDataState);
            tx.command(Arrays.asList(textileFirmIdentity.getPublicKey(), certifierIdentity.getPublicKey()),new WasteRequestContract.Commands.Claim());
            tx.fails();
            return null;
        });
    }

    @Test
    public void failedWasteWaterRequestTransactionTest() throws ScriptException {
        WasteRequestState wasteRequestState = new WasteRequestState(textileFirmIdentity.getParty(), new UniqueIdentifier(), municipalityIdentity.getParty(), "networkId", false, 100, "wasteWater", "");
        transaction(ledgerServices, tx -> {
            tx.output(CertificationContract.CertificationContractContract_ID, wasteRequestState);
            tx.command(Arrays.asList(textileFirmIdentity.getPublicKey(), municipalityIdentity.getPublicKey()),new WasteRequestContract.Commands.Claim());
            tx.fails();
            return null;
        });
    }

    @Test
    public void failedWasteWaterResponseTransactionTest() throws ScriptException {
        WasteResponseState wasteResponseState = new WasteResponseState(municipalityIdentity.getParty(), new UniqueIdentifier(), textileFirmIdentity.getParty(), "networkId", "");
        transaction(ledgerServices, tx -> {
            tx.output(CertificationContract.CertificationContractContract_ID, wasteResponseState);
            tx.command(Arrays.asList(municipalityIdentity.getPublicKey(), textileFirmIdentity.getPublicKey()),new WasteResponseContract.Commands.Claim());
            tx.fails();
            return null;
        });
    }


}
