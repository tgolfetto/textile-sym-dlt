package it.polimi.tgolfetto.contracts;

import it.polimi.tgolfetto.states.CertificationState;
import net.corda.core.contracts.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
;
import javax.script.ScriptException;
import java.util.*;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity textileFirm = new TestIdentity(new CordaX500Name("TextileManufacturer1", "Prato", "IT"));
    private final TestIdentity certifier = new TestIdentity(new CordaX500Name("Certifier", "Zurich", "CH"));
    private final TestIdentity municipality = new TestIdentity(new CordaX500Name("Municipality", "Prato", "IT"));

    private MockServices ledgerServices = new MockServices(
            Arrays.asList("it.polimi.tgolfetto.contracts")
    );

    @Before
    public void setup() { }

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
    public void failedTransactionTest() throws ScriptException {
        CertificationState certificationState = new CertificationState(certifier.getParty(), new UniqueIdentifier(), textileFirm.getParty(), "networkId", true, "");

        transaction(ledgerServices, tx -> {
            tx.output(CertificationContract.CertificationContractContract_ID, certificationState);
            tx.command(Arrays.asList(certifier.getPublicKey(), textileFirm.getPublicKey()),new CertificationContract.Commands.Issue());
            tx.fails();
            return null;
        });

    }


}
