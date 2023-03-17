package it.polimi.tgolfetto;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import it.polimi.tgolfetto.flows.SendTextileData;
import it.polimi.tgolfetto.flows.membershipFlows.*;
import it.polimi.tgolfetto.states.TextileDataState;
import it.polimi.tgolfetto.states.TextileFirmIdentity;
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
    private StartedMockNode certifier;

    private List<TestCordapp> getTestCordapps() {
        return ImmutableList.of(
                TestCordapp.findCordapp("it.polimi.tgolfetto.contracts"),
                TestCordapp.findCordapp("it.polimi.tgolfetto.flows"),
                TestCordapp.findCordapp("net.corda.bn.flows"),
                TestCordapp.findCordapp("net.corda.bn.states")
        );
    }

    @Before
    public void setup() {
        List<NotaryInfo> notaryinfo = Arrays.asList();
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
                Arrays.asList(new MockNetworkNotarySpec(new CordaX500Name("Notary Service", "Zurich", "CH"), true)),
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
        certifier = network.createPartyNode(CordaX500Name.parse("O=Certifier,L=Zurich,C=CH"));
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
        System.out.println("### activateMemberTest: " + storedMembershipState.getStatus().toString());
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
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(new UniqueIdentifier[]{networkOperatorMembershipId, storedMembershipState.getLinearId()})));
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
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(new UniqueIdentifier[]{networkOperatorMembershipId, textileFirmMembershipId})));
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
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(new UniqueIdentifier[]{networkOperatorMembershipId, textileFirmMembershipId})));
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
        CreateNetworkSubGroup createNetworkSubGroupFlow = new CreateNetworkSubGroup(networkId, "GroupName", new HashSet<UniqueIdentifier>(Arrays.asList(new UniqueIdentifier[]{networkOperatorMembershipId, textileFirmMembershipId, certifierMembershipId})));
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
        assertEquals(storedDataState, "");
    }

    private String TEXTILE_DATA_MOCK = "{\n" +
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
            "    \"syntheticFiberManufacturingValue\": 10.0,\n" +
            "    \"spinningValue\": 20.0,\n" +
            "    \"weavingValue\": 13.34,\n" +
            "    \"dyeingValue\": 10.0,\n" +
            "    \"cuttingSewingValue\": 53.01\n" +
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
}

