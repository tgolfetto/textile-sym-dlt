package it.polimi.tgolfetto;

import com.google.common.collect.ImmutableList;
import it.polimi.tgolfetto.flows.SendCertification;
import it.polimi.tgolfetto.flows.SendWasteWaterData;
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
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
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
            "  \"SMC1_cod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 17.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 10.0,\n" +
            "    \"spinningValue\": 12.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 12.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_bod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 8000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 9000.0,\n" +
            "    \"spinningValue\": 4000.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 4300.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_chlorides\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1150.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1400.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_copper\": {\n" +
            "    \"naturalFiberManufacturingValue\": 3.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 2.0,\n" +
            "    \"cuttingSewingValue\": 2.0\n" +
            "  },\n" +
            "  \"SMC1_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.1,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.12,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sodium\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2040.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1700.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_tkn\": {\n" +
            "    \"naturalFiberManufacturingValue\": 20.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 30.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 43.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sulphates\": {\n" +
            "    \"naturalFiberManufacturingValue\": 600.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 500.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_zink\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.7,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1.9,\n" +
            "    \"cuttingSewingValue\": 1.0\n" +
            "  },\n" +
            "  \"SMC2_temperature\": {\n" +
            "    \"naturalFiberManufacturingValue\": 33.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 33.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 32.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 8.5,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 7.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_oilAndGrease\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 29.0,\n" +
            "    \"weavingValue\": 38.0,\n" +
            "    \"dyeingValue\": 35.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_tds\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1900.0,\n" +
            "    \"spinningValue\": 1800.0,\n" +
            "    \"weavingValue\": 2030.0,\n" +
            "    \"dyeingValue\": 1750.0,\n" +
            "    \"cuttingSewingValue\": 1400.0\n" +
            "  }\n" +
            "}}, \"O=TextileManufacturer2, L=Prato, C=IT\": {25:{\n" +
            "  \"SMC1_cod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 17.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 10.0,\n" +
            "    \"spinningValue\": 12.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 12.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_bod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 8000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 9000.0,\n" +
            "    \"spinningValue\": 4000.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 4300.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_chlorides\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1150.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1400.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_copper\": {\n" +
            "    \"naturalFiberManufacturingValue\": 3.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 2.0,\n" +
            "    \"cuttingSewingValue\": 2.0\n" +
            "  },\n" +
            "  \"SMC1_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.1,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.12,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sodium\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2040.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1700.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_tkn\": {\n" +
            "    \"naturalFiberManufacturingValue\": 20.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 30.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 43.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sulphates\": {\n" +
            "    \"naturalFiberManufacturingValue\": 600.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 500.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_zink\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.7,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1.9,\n" +
            "    \"cuttingSewingValue\": 1.0\n" +
            "  },\n" +
            "  \"SMC2_temperature\": {\n" +
            "    \"naturalFiberManufacturingValue\": 33.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 33.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 32.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 8.5,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 7.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_oilAndGrease\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 29.0,\n" +
            "    \"weavingValue\": 38.0,\n" +
            "    \"dyeingValue\": 35.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_tds\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1900.0,\n" +
            "    \"spinningValue\": 1800.0,\n" +
            "    \"weavingValue\": 2030.0,\n" +
            "    \"dyeingValue\": 1750.0,\n" +
            "    \"cuttingSewingValue\": 1400.0\n" +
            "  }\n" +
            "}}}, \"cotton\":{\"O=TextileManufacturer2, L=Prato, C=IT\": {140:{\n" +
            "  \"SMC1_cod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 17.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 10.0,\n" +
            "    \"spinningValue\": 12.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 12.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_bod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 8000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 9000.0,\n" +
            "    \"spinningValue\": 4000.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 4300.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_chlorides\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1150.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1400.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_copper\": {\n" +
            "    \"naturalFiberManufacturingValue\": 3.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 2.0,\n" +
            "    \"cuttingSewingValue\": 2.0\n" +
            "  },\n" +
            "  \"SMC1_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.1,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.12,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sodium\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2040.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1700.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_tkn\": {\n" +
            "    \"naturalFiberManufacturingValue\": 20.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 30.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 43.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sulphates\": {\n" +
            "    \"naturalFiberManufacturingValue\": 600.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 500.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_zink\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.7,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1.9,\n" +
            "    \"cuttingSewingValue\": 1.0\n" +
            "  },\n" +
            "  \"SMC2_temperature\": {\n" +
            "    \"naturalFiberManufacturingValue\": 33.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 33.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 32.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 8.5,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 7.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_oilAndGrease\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 29.0,\n" +
            "    \"weavingValue\": 38.0,\n" +
            "    \"dyeingValue\": 35.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_tds\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1900.0,\n" +
            "    \"spinningValue\": 1800.0,\n" +
            "    \"weavingValue\": 2030.0,\n" +
            "    \"dyeingValue\": 1750.0,\n" +
            "    \"cuttingSewingValue\": 1400.0\n" +
            "  }\n" +
            "}}}}";
    private final String WASTEWATER_DATA_MOCK = "{\n" +
            "  \"SMC1_cod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 17.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 10.0,\n" +
            "    \"spinningValue\": 12.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 12.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_bod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 8000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 9000.0,\n" +
            "    \"spinningValue\": 4000.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 4300.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_chlorides\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1150.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1400.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_copper\": {\n" +
            "    \"naturalFiberManufacturingValue\": 3.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 2.0,\n" +
            "    \"cuttingSewingValue\": 2.0\n" +
            "  },\n" +
            "  \"SMC1_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.1,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.12,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.23,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sodium\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2040.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1700.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_tkn\": {\n" +
            "    \"naturalFiberManufacturingValue\": 20.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 30.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 43.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sulphates\": {\n" +
            "    \"naturalFiberManufacturingValue\": 600.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 500.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_zink\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.7,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1.9,\n" +
            "    \"cuttingSewingValue\": 1.0\n" +
            "  },\n" +
            "  \"SMC2_temperature\": {\n" +
            "    \"naturalFiberManufacturingValue\": 33.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 33.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 32.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 8.5,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 7.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_oilAndGrease\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 29.0,\n" +
            "    \"weavingValue\": 38.0,\n" +
            "    \"dyeingValue\": 35.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_tds\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1900.0,\n" +
            "    \"spinningValue\": 1800.0,\n" +
            "    \"weavingValue\": 2030.0,\n" +
            "    \"dyeingValue\": 1750.0,\n" +
            "    \"cuttingSewingValue\": 1400.0\n" +
            "  }\n" +
            "}";

    private final String CORRECT_WASTEWATER_DATA_MOCK = "{\n" +
            "  \"SMC1_cod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 17.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 10.0,\n" +
            "    \"spinningValue\": 12.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 12.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_bod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 8000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 9000.0,\n" +
            "    \"spinningValue\": 4000.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 4300.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_chlorides\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1150.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_copper\": {\n" +
            "    \"naturalFiberManufacturingValue\": 3.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 2.0,\n" +
            "    \"cuttingSewingValue\": 2.0\n" +
            "  },\n" +
            "  \"SMC1_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.1,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.12,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.13,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sodium\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2040.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1700.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_tkn\": {\n" +
            "    \"naturalFiberManufacturingValue\": 20.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 30.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 43.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sulphates\": {\n" +
            "    \"naturalFiberManufacturingValue\": 600.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 500.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_zink\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.7,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.4,\n" +
            "    \"cuttingSewingValue\": 1.0\n" +
            "  },\n" +
            "  \"SMC2_temperature\": {\n" +
            "    \"naturalFiberManufacturingValue\": 33.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 33.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 32.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 7.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 8.5,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 7.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_oilAndGrease\": {\n" +
            "    \"naturalFiberManufacturingValue\": 32.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 29.0,\n" +
            "    \"weavingValue\": 38.0,\n" +
            "    \"dyeingValue\": 35.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_tds\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1900.0,\n" +
            "    \"spinningValue\": 1800.0,\n" +
            "    \"weavingValue\": 2030.0,\n" +
            "    \"dyeingValue\": 1750.0,\n" +
            "    \"cuttingSewingValue\": 1400.0\n" +
            "  }\n" +
            "}";

    private final String CERTIFICATION_CRITERIA_MOCK = "{\n" +
            "  \"SMC1_cod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 20.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 20.0,\n" +
            "    \"spinningValue\": 20.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 20.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_bod\": {\n" +
            "    \"naturalFiberManufacturingValue\": 10000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 10000.0,\n" +
            "    \"spinningValue\": 10000.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 10000.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_chlorides\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1200.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1200.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1200.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_copper\": {\n" +
            "    \"naturalFiberManufacturingValue\": 4.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 4.0,\n" +
            "    \"cuttingSewingValue\": 4.0\n" +
            "  },\n" +
            "  \"SMC1_arsenic\": {\n" +
            "    \"naturalFiberManufacturingValue\": 0.2,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.2,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 0.2,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sodium\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2100.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 2100.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_tkn\": {\n" +
            "    \"naturalFiberManufacturingValue\": 60.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 60.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 60.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_sulphates\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1000.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 1000.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1000.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC1_zink\": {\n" +
            "    \"naturalFiberManufacturingValue\": 1.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 1.0,\n" +
            "    \"cuttingSewingValue\": 1.0\n" +
            "  },\n" +
            "  \"SMC2_temperature\": {\n" +
            "    \"naturalFiberManufacturingValue\": 35.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 35.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 35.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC2_ph\": {\n" +
            "    \"naturalFiberManufacturingValue\": 9.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 9.0,\n" +
            "    \"spinningValue\": 0.0,\n" +
            "    \"weavingValue\": 0.0,\n" +
            "    \"dyeingValue\": 9.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_oilAndGrease\": {\n" +
            "    \"naturalFiberManufacturingValue\": 40.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 0.0,\n" +
            "    \"spinningValue\": 40.0,\n" +
            "    \"weavingValue\": 40.0,\n" +
            "    \"dyeingValue\": 40.0,\n" +
            "    \"cuttingSewingValue\": 0.0\n" +
            "  },\n" +
            "  \"SMC5_tds\": {\n" +
            "    \"naturalFiberManufacturingValue\": 2100.0,\n" +
            "    \"syntheticFiberManufacturingValue\": 2100.0,\n" +
            "    \"spinningValue\": 2100.0,\n" +
            "    \"weavingValue\": 2100.0,\n" +
            "    \"dyeingValue\": 2100.0,\n" +
            "    \"cuttingSewingValue\": 2100.0\n" +
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
        SendWasteWaterData.SendTextileDataInitiator sendTextileDataInitiatorFlow = new SendWasteWaterData.SendTextileDataInitiator(networkId, textileFirmMembershipId, certifier.getInfo().identityFromX500Name(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH")), WASTEWATER_DATA_MOCK);
        textileFirm.startFlow(sendTextileDataInitiatorFlow);
        network.runNetwork();
        TextileDataState storedDataState = certifier.getServices().getVaultService()
                .queryBy(TextileDataState.class).getStates().get(0).getState().getData();
        assertEquals(storedDataState.getJsonData(), WASTEWATER_DATA_MOCK);
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
        SendWasteWaterData.SendTextileDataInitiator sendTextileDataInitiatorFlow = new SendWasteWaterData.SendTextileDataInitiator(networkId, textileFirmMembershipId, certifier.getInfo().identityFromX500Name(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH")), WASTEWATER_DATA_MOCK);
        textileFirm.startFlow(sendTextileDataInitiatorFlow);
        network.runNetwork();
        TextileDataState storedDataState = certifier.getServices().getVaultService()
                .queryBy(TextileDataState.class).getStates().get(0).getState().getData();
        assertEquals(storedDataState.getJsonData(), WASTEWATER_DATA_MOCK);
        // Send certification from Certifier to TextileFirm
        SendCertification.SendCertificationInitiator sendCertificationInitiatorFlow = new SendCertification.SendCertificationInitiator(networkId, certifierMembershipId, textileFirm.getInfo().identityFromX500Name(CordaX500Name.parse("O=TextileManufacturer1,L=Prato,C=IT")), CERTIFICATION_CRITERIA_MOCK);
        certifier.startFlow(sendCertificationInitiatorFlow);
        network.runNetwork();
        CertificationState storedCertState = textileFirm.getServices().getVaultService()
                .queryBy(CertificationState.class).getStates().get(0).getState().getData();
        assertEquals(storedCertState.getErrors(), "SMC1_arsenic - dyeingValue actual: 0.23 limit: 0.2\n" +
                "SMC1_chlorides - dyeingValue actual: 1400.0 limit: 1200.0\n" +
                "SMC1_zink - dyeingValue actual: 1.9 limit: 1.0\n");
        assertEquals(storedCertState.isCertified(), false);
        // Send more textile data from TextileFirm to Certifier
        sendTextileDataInitiatorFlow = new SendWasteWaterData.SendTextileDataInitiator(networkId, textileFirmMembershipId, certifier.getInfo().identityFromX500Name(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH")), CORRECT_WASTEWATER_DATA_MOCK);
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
        SendWasteRequest.SendWasteRequestInitiator sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 100, "wastewater", WASTEWATER_DATA_MOCK);
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        WasteRequestState storedDataState = municipality.getServices().getVaultService()
                .queryBy(WasteRequestState.class).getStates().get(0).getState().getData();
        assertEquals(storedDataState.getTextileData(), WASTEWATER_DATA_MOCK);
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
        SendWasteRequest.SendWasteRequestInitiator sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 100, "wastewater", WASTEWATER_DATA_MOCK);
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), false, 50, "wastewater", "{}");
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirmMembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), false, 40, "cotton", "{}");
        textileFirm.startFlow(sendWasteRequestInitiator);
        network.runNetwork();

        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirm2MembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 25, "wastewater", WASTEWATER_DATA_MOCK);
        textileFirm2.startFlow(sendWasteRequestInitiator);
        network.runNetwork();
        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirm2MembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 140, "cotton", WASTEWATER_DATA_MOCK);
        textileFirm2.startFlow(sendWasteRequestInitiator);
        network.runNetwork();

        sendWasteRequestInitiator = new SendWasteRequest.SendWasteRequestInitiator(networkId, textileFirm3MembershipId, municipality.getInfo().identityFromX500Name(CordaX500Name.parse("O=Municipality,L=Prato,C=IT")), true, 65, "wastewater", WASTEWATER_DATA_MOCK);
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

