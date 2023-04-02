package it.polimi.tgolfetto;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import it.polimi.tgolfetto.flows.SendCertification;
import it.polimi.tgolfetto.flows.SendTextileData;
import it.polimi.tgolfetto.flows.SendWasteRequest;
import it.polimi.tgolfetto.flows.SendWasteResponse;
import it.polimi.tgolfetto.flows.membershipFlows.*;
import it.polimi.tgolfetto.states.*;
import net.corda.bn.states.MembershipState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NotaryInfo;
import net.corda.core.node.services.Vault;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode networkOperator;
    private StartedMockNode textileFirm;
    private StartedMockNode textileFirm2;
    private StartedMockNode textileFirm3;
    private StartedMockNode certifier;
    private StartedMockNode municipality;

    private List<TestCordapp> getTestCordapps() {
        return ImmutableList.of(
                TestCordapp.findCordapp("it.polimi.tgolfetto.contracts"),
                TestCordapp.findCordapp("it.polimi.tgolfetto.flows"),
                TestCordapp.findCordapp("net.corda.bn.flows"),
                TestCordapp.findCordapp("net.corda.bn.states")
        );
    }

    private final String EXPECTED_SUPPLIER_LIST_MOCK = "{\"wastewater\":{\"O=TextileManufacturer3, L=Prato, C=IT\": {65:{\n" +
            "  \"SMC1_enzymatic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 128.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 182.23,\n" +
            "    \"spinningValue\": 123.11,\n" +
            "    \"weavingValue\": 105.42,\n" +
            "    \"dyeingValue\": 100.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_antibodyBased\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 4.23,\n" +
            "    \"spinningValue\": 2.11,\n" +
            "    \"weavingValue\": 5.42,\n" +
            "    \"dyeingValue\": 1.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_aox\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.5,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 7.1,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 8.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC3_voc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.42,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.30,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.12\n" +
            "  },\n" +
            "  \"SMC3_no2\": {\n" +
            "    \"naturalFiberManufacturingValue\": 22.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 30.0,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 12.23\n" +
            "  },\n" +
            "  \"SMC3_formaldehyde\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.02,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.03,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.01\n" +
            "  },\n" +
            "  \"SMC4_renewableEnergyPerc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.10,\n" +
            "    \"spinningValue\": 0.20,\n" +
            "    \"weavingValue\": 0.13,\n" +
            "    \"dyeingValue\": 0.10,\n" +
            "    \"cuttingSewingValue\": 0.53\n" +
            "  },\n" +
            "  \"SMC5_solidFlow\": {\n" +
            "    \"naturalFiberManufacturingValue\": 21.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 30.0,\n" +
            "    \"weavingValue\": 43.13,\n" +
            "    \"dyeingValue\": 10.0,\n" +
            "    \"cuttingSewingValue\": 41.12\n" +
            "  }\n" +
            "}}, \"O=TextileManufacturer2, L=Prato, C=IT\": {25:{\n" +
            "  \"SMC1_enzymatic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 128.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 182.23,\n" +
            "    \"spinningValue\": 123.11,\n" +
            "    \"weavingValue\": 105.42,\n" +
            "    \"dyeingValue\": 100.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_antibodyBased\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 4.23,\n" +
            "    \"spinningValue\": 2.11,\n" +
            "    \"weavingValue\": 5.42,\n" +
            "    \"dyeingValue\": 1.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_aox\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.5,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 7.1,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 8.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC3_voc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.42,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.30,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.12\n" +
            "  },\n" +
            "  \"SMC3_no2\": {\n" +
            "    \"naturalFiberManufacturingValue\": 22.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 30.0,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 12.23\n" +
            "  },\n" +
            "  \"SMC3_formaldehyde\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.02,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.03,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.01\n" +
            "  },\n" +
            "  \"SMC4_renewableEnergyPerc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.10,\n" +
            "    \"spinningValue\": 0.20,\n" +
            "    \"weavingValue\": 0.13,\n" +
            "    \"dyeingValue\": 0.10,\n" +
            "    \"cuttingSewingValue\": 0.53\n" +
            "  },\n" +
            "  \"SMC5_solidFlow\": {\n" +
            "    \"naturalFiberManufacturingValue\": 21.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 30.0,\n" +
            "    \"weavingValue\": 43.13,\n" +
            "    \"dyeingValue\": 10.0,\n" +
            "    \"cuttingSewingValue\": 41.12\n" +
            "  }\n" +
            "}}}, \"cotton\":{\"O=TextileManufacturer2, L=Prato, C=IT\": {140:{\n" +
            "  \"SMC1_enzymatic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 128.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 182.23,\n" +
            "    \"spinningValue\": 123.11,\n" +
            "    \"weavingValue\": 105.42,\n" +
            "    \"dyeingValue\": 100.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_antibodyBased\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 4.23,\n" +
            "    \"spinningValue\": 2.11,\n" +
            "    \"weavingValue\": 5.42,\n" +
            "    \"dyeingValue\": 1.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_aox\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.5,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 7.1,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 8.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC3_voc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.42,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.30,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.12\n" +
            "  },\n" +
            "  \"SMC3_no2\": {\n" +
            "    \"naturalFiberManufacturingValue\": 22.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 30.0,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 12.23\n" +
            "  },\n" +
            "  \"SMC3_formaldehyde\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.02,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.03,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.01\n" +
            "  },\n" +
            "  \"SMC4_renewableEnergyPerc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.10,\n" +
            "    \"spinningValue\": 0.20,\n" +
            "    \"weavingValue\": 0.13,\n" +
            "    \"dyeingValue\": 0.10,\n" +
            "    \"cuttingSewingValue\": 0.53\n" +
            "  },\n" +
            "  \"SMC5_solidFlow\": {\n" +
            "    \"naturalFiberManufacturingValue\": 21.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 30.0,\n" +
            "    \"weavingValue\": 43.13,\n" +
            "    \"dyeingValue\": 10.0,\n" +
            "    \"cuttingSewingValue\": 41.12\n" +
            "  }\n" +
            "}}}}";
    private final String TEXTILE_DATA_MOCK = "{\n" +
            "  \"SMC1_enzymatic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 128.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 182.23,\n" +
            "    \"spinningValue\": 123.11,\n" +
            "    \"weavingValue\": 105.42,\n" +
            "    \"dyeingValue\": 100.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_antibodyBased\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 4.23,\n" +
            "    \"spinningValue\": 2.11,\n" +
            "    \"weavingValue\": 5.42,\n" +
            "    \"dyeingValue\": 1.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_aox\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.5,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 7.1,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 8.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC3_voc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.42,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.30,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.12\n" +
            "  },\n" +
            "  \"SMC3_no2\": {\n" +
            "    \"naturalFiberManufacturingValue\": 22.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 30.0,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 12.23\n" +
            "  },\n" +
            "  \"SMC3_formaldehyde\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.02,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.03,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.01\n" +
            "  },\n" +
            "  \"SMC4_renewableEnergyPerc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.10,\n" +
            "    \"spinningValue\": 0.20,\n" +
            "    \"weavingValue\": 0.13,\n" +
            "    \"dyeingValue\": 0.10,\n" +
            "    \"cuttingSewingValue\": 0.53\n" +
            "  },\n" +
            "  \"SMC5_solidFlow\": {\n" +
            "    \"naturalFiberManufacturingValue\": 21.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 30.0,\n" +
            "    \"weavingValue\": 43.13,\n" +
            "    \"dyeingValue\": 10.0,\n" +
            "    \"cuttingSewingValue\": 41.12\n" +
            "  }\n" +
            "}";

    private final String CORRECT_TEXTILE_DATA_MOCK = "{\n" +
            "  \"SMC1_enzymatic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 128.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 182.23,\n" +
            "    \"spinningValue\": 123.11,\n" +
            "    \"weavingValue\": 105.42,\n" +
            "    \"dyeingValue\": 100.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_antibodyBased\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 4.23,\n" +
            "    \"spinningValue\": 2.11,\n" +
            "    \"weavingValue\": 5.42,\n" +
            "    \"dyeingValue\": 1.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 32.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 21.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_aox\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2.99,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 2.11,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.5,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 7.1,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 8.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC3_voc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.42,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.30,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.12\n" +
            "  },\n" +
            "  \"SMC3_no2\": {\n" +
            "    \"naturalFiberManufacturingValue\": 22.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 30.0,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 12.23\n" +
            "  },\n" +
            "  \"SMC3_formaldehyde\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.02,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.03,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.01\n" +
            "  },\n" +
            "  \"SMC4_renewableEnergyPerc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.10,\n" +
            "    \"spinningValue\": 0.20,\n" +
            "    \"weavingValue\": 0.13,\n" +
            "    \"dyeingValue\": 0.10,\n" +
            "    \"cuttingSewingValue\": 0.23\n" +
            "  },\n" +
            "  \"SMC5_solidFlow\": {\n" +
            "    \"naturalFiberManufacturingValue\": 21.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 30.0,\n" +
            "    \"weavingValue\": 43.13,\n" +
            "    \"dyeingValue\": 10.0,\n" +
            "    \"cuttingSewingValue\": 41.12\n" +
            "  }\n" +
            "}";

    private final String CERTIFICATION_CRITERIA_MOCK = "{\n" +
            "  \"SMC1_enzymatic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 250.00,\n" +
            "    \"syntheticFiberManufacturingValue\": 250.00,\n" +
            "    \"spinningValue\": 250.00,\n" +
            "    \"weavingValue\": 250.00,\n" +
            "    \"dyeingValue\": 250.00,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_antibodyBased\": {\n" +
            "    \"naturalFiberManufacturingValue\": 5.00,\n" +
            "    \"syntheticFiberManufacturingValue\": 5.00,\n" +
            "    \"spinningValue\": 5.00,\n" +
            "    \"weavingValue\": 5.42,\n" +
            "    \"dyeingValue\": 4.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 50.00,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 50.00,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 50.00,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_aox\": {\n" +
            "    \"naturalFiberManufacturingValue\": 5.00,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 5.00,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 5.00,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 8.5,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 8.5,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 8.5,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC3_voc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.50,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.50,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.50\n" +
            "  },\n" +
            "  \"SMC3_no2\": {\n" +
            "    \"naturalFiberManufacturingValue\": 40.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 40.00,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 40.00\n" +
            "  },\n" +
            "  \"SMC3_formaldehyde\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.05,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.05,\n" +
            "    \"dyeingValue\": 0.0,\n" +
            "    \"cuttingSewingValue\": 0.05\n" +
            "  },\n" +
            "  \"SMC4_renewableEnergyPerc\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.30,\n" +
            "    \"spinningValue\": 0.30,\n" +
            "    \"weavingValue\": 0.30,\n" +
            "    \"dyeingValue\": 0.30,\n" +
            "    \"cuttingSewingValue\": 0.30\n" +
            "  },\n" +
            "  \"SMC5_solidFlow\": {\n" +
            "    \"naturalFiberManufacturingValue\": 50.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 50.0,\n" +
            "    \"weavingValue\": 50.00,\n" +
            "    \"dyeingValue\": 50.0,\n" +
            "    \"cuttingSewingValue\": 50.00\n" +
            "  }\n" +
            "}";

    @Before
    public void setup() {
        List<NotaryInfo> notaryinfo = Collections.emptyList();
        NetworkParameters networkParameters = new NetworkParameters(
                4,
                notaryinfo,
                10485760,
                524288000,
                java.time.Instant.now(),
                1,
                Collections.emptyMap()
        );
        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters(
                false,
                false,
                new InMemoryMessagingNetwork.ServicePeerAllocationStrategy.Random.Random(),
                Collections.singletonList(new MockNetworkNotarySpec(new CordaX500Name("Notary Service", "Zurich", "CH"), true)),
                networkParameters,
                getTestCordapps()
        );

        network = new MockNetwork(mockNetworkParameters);
        /*
        network = new MockNetwork(mockNetworkParameters.withThreadPerNode(false).withCordappsForAllNodes(ImmutableList.of(
                        TestCordapp.findCordapp("it.polimi.tgolfetto.contracts"),
                        TestCordapp.findCordapp("it.polimi.tgolfetto.flows"),
                        TestCordapp.findCordapp("net.corda.bn.flows"),
                        TestCordapp.findCordapp("net.corda.bn.states")))
                .withNotarySpecs(ImmutableList.of(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=Rome,C=IT"))))
        );*/
        networkOperator = network.createPartyNode(CordaX500Name.parse("O=NetworkOperator,L=Milan,C=IT"));
        textileFirm = network.createPartyNode(CordaX500Name.parse("O=TextileManufacturer1,L=Prato,C=IT"));
        textileFirm2 = network.createPartyNode(CordaX500Name.parse("O=TextileManufacturer2,L=Prato,C=IT"));
        textileFirm3 = network.createPartyNode(CordaX500Name.parse("O=TextileManufacturer3,L=Prato,C=IT"));
        certifier = network.createPartyNode(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH"));
        municipality = network.createPartyNode(CordaX500Name.parse("O=Municipality,L=Prato,C=IT"));
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void createNetworkTest() throws ExecutionException, InterruptedException {
        CreateNetwork flow = new CreateNetwork();
        Future<String> future = networkOperator.startFlow(flow);
        network.runNetwork();
        String resString = future.get();

        int subString = resString.indexOf("NetworkID: ");
        String networkId = resString.substring(subString + 11);

        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        System.out.println("### createNetworkTest: " + networkId);
        assert (storedMembershipState.getNetworkId().equals(networkId));

    }

    @Test
    public void requestMembershipTest() {
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        certifier.startFlow(requestMembershipFlow);
        network.runNetwork();
        List<StateAndRef<MembershipState>> storedMembershipStates = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates();
        System.out.println("### requestMembershipTest: " + storedMembershipStates);
        System.out.println("### requestMembershipTest: " + storedMembershipStates.size());
        assert (storedMembershipStates.size() == 3);
    }

    @Test
    public void queryAllMembersTest() throws ExecutionException, InterruptedException {
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        QueryAllMembers queryFlow = new QueryAllMembers();
        Future<String> future = networkOperator.startFlow(queryFlow);
        network.runNetwork();
        String resString = future.get();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        assert(resString.contains(storedMembershipState.getLinearId().toString()) && resString.contains(storedMembershipState.getStatus().toString()));
    }

    @Test
    public void activateMemberTest() throws ExecutionException, InterruptedException {
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        ActivateMember activateMemberFlow = new ActivateMember(storedMembershipState.getLinearId());
        Future<String> future= networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        System.out.println(future.get());
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        System.out.println("### activateMemberTest: " + storedMembershipState.getStatus());
        assert(storedMembershipState.getStatus().toString().equals("ACTIVE"));
    }


    @Test
    public void createNetworkSubGroupTest() throws ExecutionException, InterruptedException {
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        ActivateMember activateMemberFlow = new ActivateMember(storedMembershipState.getLinearId());
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        UniqueIdentifier networkOperatorMembershipId = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData().getLinearId();
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(networkOperatorMembershipId, storedMembershipState.getLinearId())));
        Future<String> res = networkOperator.startFlow(createNetworkSubGroupFlow);
        network.runNetwork();
        String resString = res.get();
        System.out.println("### createNetworkSubGroupTest: " + resString);
        assert (resString.contains("GroupName") && resString.contains(networkOperatorMembershipId.toString()) && resString.contains(storedMembershipState.getLinearId().toString()));
    }

    @Test
    public void assignBNIdentityTest() {
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        UniqueIdentifier textileFirmMembershipId = storedMembershipState.getLinearId();
        ActivateMember activateMemberFlow = new ActivateMember(textileFirmMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        UniqueIdentifier networkOperatorMembershipId = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData().getLinearId();
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(networkOperatorMembershipId, textileFirmMembershipId)));
        networkOperator.startFlow(createNetworkSubGroupFlow);
        network.runNetwork();
        AssignBNIdentity assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirmMembershipId, "PRATOT65LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        assert (storedMembershipState.getIdentity().getBusinessIdentity() instanceof it.polimi.tgolfetto.states.TextileFirmIdentity);
    }

    @Test
    public void assignTextileDataSharingRoleTest () {
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        UniqueIdentifier textileFirmMembershipId = storedMembershipState.getLinearId();
        ActivateMember activateMemberFlow = new ActivateMember(textileFirmMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        UniqueIdentifier networkOperatorMembershipId = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData().getLinearId();
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(networkOperatorMembershipId, textileFirmMembershipId)));
        networkOperator.startFlow(createNetworkSubGroupFlow);
        network.runNetwork();
        AssignBNIdentity assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirmMembershipId, "PRATOT65LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        AssignTextileDataSharingRole assignTextileDataSharingRoleFlow = new AssignTextileDataSharingRole(textileFirmMembershipId, networkId);
        networkOperator.startFlow(assignTextileDataSharingRoleFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        assert (storedMembershipState.getRoles().toArray()[0] instanceof TextileFirmIdentity.TextileDataSharingRole);
    }

    @Test
    public void sendTextileDataTest() throws IOException {
        // Create network
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        // TextileFirm request join
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        UniqueIdentifier textileFirmMembershipId = storedMembershipState.getLinearId();
        // Certifier request join
        requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        certifier.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(2).getState().getData();
        UniqueIdentifier certifierMembershipId = storedMembershipState.getLinearId();
        // Activate TextileFirm membership
        ActivateMember activateMemberFlow = new ActivateMember(textileFirmMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Activate Certifier membership
        activateMemberFlow = new ActivateMember(certifierMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Create network subgroup
        UniqueIdentifier networkOperatorMembershipId = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData().getLinearId();
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(networkOperatorMembershipId, textileFirmMembershipId, certifierMembershipId)));
        networkOperator.startFlow(createNetworkSubGroupFlow);
        network.runNetwork();
        // Assign business identity to TextileFirm
        AssignBNIdentity assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirmMembershipId, "PRATOT65LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign business identity to Certifier
        assignBNIdentityFlow = new AssignBNIdentity("Certifier", certifierMembershipId, "PRATOC45CRT");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign sharing permissions to TextileFirm
        AssignTextileDataSharingRole assignTextileDataSharingRoleFlow = new AssignTextileDataSharingRole(textileFirmMembershipId, networkId);
        networkOperator.startFlow(assignTextileDataSharingRoleFlow);
        network.runNetwork();
        // Send textile data from TextileFirm to Certifier
        SendTextileData.SendTextileDataInitiator sendTextileDataInitiatorFlow = new SendTextileData.SendTextileDataInitiator(networkId, textileFirmMembershipId, certifier.getInfo().identityFromX500Name(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH")), TEXTILE_DATA_MOCK);
        textileFirm.startFlow(sendTextileDataInitiatorFlow);
        network.runNetwork();
        TextileDataState storedDataState = certifier.getServices().getVaultService()
                .queryBy(TextileDataState.class).getStates().get(0).getState().getData();
        assertEquals(storedDataState.getJsonData(), TEXTILE_DATA_MOCK);
    }

    @Test
    public void sendCertificationTest() throws IOException {
        // Create network
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        // TextileFirm request join
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        UniqueIdentifier textileFirmMembershipId = storedMembershipState.getLinearId();
        // Certifier request join
        requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        certifier.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(2).getState().getData();
        UniqueIdentifier certifierMembershipId = storedMembershipState.getLinearId();
        // Activate TextileFirm membership
        ActivateMember activateMemberFlow = new ActivateMember(textileFirmMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Activate Certifier membership
        activateMemberFlow = new ActivateMember(certifierMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Create network subgroup
        UniqueIdentifier networkOperatorMembershipId = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData().getLinearId();
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(networkOperatorMembershipId, textileFirmMembershipId, certifierMembershipId)));
        networkOperator.startFlow(createNetworkSubGroupFlow);
        network.runNetwork();
        // Assign business identity to TextileFirm
        AssignBNIdentity assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirmMembershipId, "PRATOT65LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign business identity to Certifier
        assignBNIdentityFlow = new AssignBNIdentity("Certifier", certifierMembershipId, "PRATOC45CRT");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign sharing permissions to TextileFirm
        AssignTextileDataSharingRole assignTextileDataSharingRoleFlow = new AssignTextileDataSharingRole(textileFirmMembershipId, networkId);
        networkOperator.startFlow(assignTextileDataSharingRoleFlow);
        network.runNetwork();
        // Send textile data from TextileFirm to Certifier
        SendTextileData.SendTextileDataInitiator sendTextileDataInitiatorFlow = new SendTextileData.SendTextileDataInitiator(networkId, textileFirmMembershipId, certifier.getInfo().identityFromX500Name(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH")), TEXTILE_DATA_MOCK);
        textileFirm.startFlow(sendTextileDataInitiatorFlow);
        network.runNetwork();
        TextileDataState storedDataState = certifier.getServices().getVaultService()
                .queryBy(TextileDataState.class).getStates().get(0).getState().getData();
        assertEquals(storedDataState.getJsonData(), TEXTILE_DATA_MOCK);
        // Send certification from Certifier to TextileFirm
        SendCertification.SendCertificationInitiator sendCertificationInitiatorFlow = new SendCertification.SendCertificationInitiator(networkId, certifierMembershipId, textileFirm.getInfo().identityFromX500Name(CordaX500Name.parse("O=TextileManufacturer1,L=Prato,C=IT")), CERTIFICATION_CRITERIA_MOCK);
        certifier.startFlow(sendCertificationInitiatorFlow);
        network.runNetwork();
        CertificationState storedCertState = textileFirm.getServices().getVaultService()
                .queryBy(CertificationState.class).getStates().get(0).getState().getData();
        assertEquals(storedCertState.getErrors(), "SMC4_renewableEnergyPerc - cuttingSewingValue actual: 0.53 limit: 0.3\n" +
                "SMC2_aox - naturalFiberManufacturingValue actual: 32.99 limit: 5.0\n" +
                "SMC2_aox - spinningValue actual: 32.11 limit: 5.0\n" +
                "SMC2_aox - dyeingValue actual: 21.23 limit: 5.0\n");
        assertEquals(storedCertState.isCertified(), false);
        // Send more textile data from TextileFirm to Certifier
        sendTextileDataInitiatorFlow = new SendTextileData.SendTextileDataInitiator(networkId, textileFirmMembershipId, certifier.getInfo().identityFromX500Name(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH")), CORRECT_TEXTILE_DATA_MOCK);
        textileFirm.startFlow(sendTextileDataInitiatorFlow);
        network.runNetwork();
        // Issue another certification from Certifier to TextileFirm
        sendCertificationInitiatorFlow = new SendCertification.SendCertificationInitiator(networkId, certifierMembershipId, textileFirm.getInfo().identityFromX500Name(CordaX500Name.parse("O=TextileManufacturer1,L=Prato,C=IT")), CERTIFICATION_CRITERIA_MOCK);
        certifier.startFlow(sendCertificationInitiatorFlow);
        network.runNetwork();
        storedCertState = textileFirm.getServices().getVaultService()
                .queryBy(CertificationState.class).getStates().get(1).getState().getData();
        //assertEquals(storedCertState.isCertified(), true);
        assertEquals(storedCertState.getErrors(), "");
        assertEquals(storedCertState.isCertified(), true);
    }

    @Test
    public void sendWasteRequestTest() throws IOException {
        // Create network
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        // TextileFirm request join
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        UniqueIdentifier textileFirmMembershipId = storedMembershipState.getLinearId();
        // Municipality request join
        requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        municipality.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(2).getState().getData();
        UniqueIdentifier municiaplityMembershipId = storedMembershipState.getLinearId();
        // Activate TextileFirm membership
        ActivateMember activateMemberFlow = new ActivateMember(textileFirmMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Activate Municipality membership
        activateMemberFlow = new ActivateMember(municiaplityMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Create network subgroup
        UniqueIdentifier networkOperatorMembershipId = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData().getLinearId();
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(networkOperatorMembershipId, textileFirmMembershipId, municiaplityMembershipId)));
        networkOperator.startFlow(createNetworkSubGroupFlow);
        network.runNetwork();
        // Assign business identity to TextileFirm
        AssignBNIdentity assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirmMembershipId, "PRATOT65LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign business identity to Municipality
        assignBNIdentityFlow = new AssignBNIdentity("Municipality", municiaplityMembershipId, "PRATOC45MUN");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign sharing permissions to TextileFirm
        AssignTextileDataSharingRole assignTextileDataSharingRoleFlow = new AssignTextileDataSharingRole(textileFirmMembershipId, networkId);
        networkOperator.startFlow(assignTextileDataSharingRoleFlow);
        network.runNetwork();
        // Send textile data from TextileFirm to Municipality
        SendWasteRequest.SendWasteRequestInitiator sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 100, "wastewater", TEXTILE_DATA_MOCK);
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        WasteRequestState storedDataState = municipality.getServices().getVaultService()
                .queryBy(WasteRequestState.class).getStates().get(0).getState().getData();
        assertEquals(storedDataState.getTextileData(), TEXTILE_DATA_MOCK);
        assertEquals(storedDataState.isSend(), true);

    }

    @Test
    public void sendWasteResponseTest() throws IOException {
        // Create network
        CreateNetwork flow = new CreateNetwork();
        networkOperator.startFlow(flow);
        network.runNetwork();
        MembershipState storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData();
        String networkId = storedMembershipState.getNetworkId();
        // TextileFirm request join
        RequestMembership requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(1).getState().getData();
        UniqueIdentifier textileFirmMembershipId = storedMembershipState.getLinearId();

        requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm2.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(2).getState().getData();
        UniqueIdentifier textileFirm2MembershipId = storedMembershipState.getLinearId();

        requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        textileFirm3.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(3).getState().getData();
        UniqueIdentifier textileFirm3MembershipId = storedMembershipState.getLinearId();
        // Municipality request join
        requestMembershipFlow = new RequestMembership(networkOperator.getInfo().getLegalIdentities().get(0), networkId);
        municipality.startFlow(requestMembershipFlow);
        network.runNetwork();
        storedMembershipState = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(4).getState().getData();
        UniqueIdentifier municiaplityMembershipId = storedMembershipState.getLinearId();
        // Activate TextileFirm membership
        ActivateMember activateMemberFlow = new ActivateMember(textileFirmMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();

        activateMemberFlow = new ActivateMember(textileFirm2MembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();

        activateMemberFlow = new ActivateMember(textileFirm3MembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Activate Municipality membership
        activateMemberFlow = new ActivateMember(municiaplityMembershipId);
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
        // Create network subgroup
        UniqueIdentifier networkOperatorMembershipId = networkOperator.getServices().getVaultService()
                .queryBy(MembershipState.class).getStates().get(0).getState().getData().getLinearId();
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(networkOperatorMembershipId, textileFirmMembershipId, textileFirm2MembershipId, textileFirm3MembershipId, municiaplityMembershipId)));
        networkOperator.startFlow(createNetworkSubGroupFlow);
        network.runNetwork();
        // Assign business identity to TextileFirm
        AssignBNIdentity assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirmMembershipId, "PRATOT65LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();

        assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirm2MembershipId, "PRATOT67LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();

        assignBNIdentityFlow = new AssignBNIdentity("TextileFirm", textileFirm3MembershipId, "PRATOT68LWD");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign business identity to Municipality
        assignBNIdentityFlow = new AssignBNIdentity("Municipality", municiaplityMembershipId, "PRATOC45MUN");
        networkOperator.startFlow(assignBNIdentityFlow);
        network.runNetwork();
        // Assign sharing permissions to TextileFirm
        AssignTextileDataSharingRole assignTextileDataSharingRoleFlow = new AssignTextileDataSharingRole(textileFirmMembershipId, networkId);
        networkOperator.startFlow(assignTextileDataSharingRoleFlow);
        network.runNetwork();

        assignTextileDataSharingRoleFlow = new AssignTextileDataSharingRole(textileFirm2MembershipId, networkId);
        networkOperator.startFlow(assignTextileDataSharingRoleFlow);
        network.runNetwork();

        assignTextileDataSharingRoleFlow = new AssignTextileDataSharingRole(textileFirm3MembershipId, networkId);
        networkOperator.startFlow(assignTextileDataSharingRoleFlow);
        network.runNetwork();
        // Send textile data from TextileFirm to Municipality
        SendWasteRequest.SendWasteRequestInitiator sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 100, "wastewater", TEXTILE_DATA_MOCK);
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), false, 50, "wastewater", "{}");
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), false, 40, "cotton", "{}");
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();

        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirm2MembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 25, "wastewater", TEXTILE_DATA_MOCK);
        textileFirm2.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirm2MembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 140, "cotton", TEXTILE_DATA_MOCK);
        textileFirm2.startFlow(sendWasteRequestInitiator);
        network.runNetwork();

        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirm3MembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 65, "wastewater", TEXTILE_DATA_MOCK);
        textileFirm3.startFlow(sendWasteRequestInitiator);
        network.runNetwork();

        SendWasteResponse.SendWasteResponseInitiator sendWasteResponseInitiator = new SendWasteResponse.SendWasteResponseInitiator(networkId, municiaplityMembershipId, textileFirm.getInfo().identityFromX500Name(CordaX500Name.parse("O=TextileManufacturer1,L=Prato,C=IT")));
        municipality.startFlow(sendWasteResponseInitiator);
        network.runNetwork();

        WasteResponseState storedDataState = textileFirm.getServices().getVaultService()
                .queryBy(WasteResponseState.class).getStates().get(0).getState().getData();
        assertEquals(storedDataState.getJsonSuppliersList(),EXPECTED_SUPPLIER_LIST_MOCK);
    }

}

