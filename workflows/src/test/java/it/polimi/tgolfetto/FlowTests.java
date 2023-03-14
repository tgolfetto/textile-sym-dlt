package it.polimi.tgolfetto;

import com.google.common.collect.ImmutableList;
import it.polimi.tgolfetto.flows.membershipFlows.*;
import net.corda.bn.states.MembershipState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode networkOperator;
    private StartedMockNode textileFirm;
    private StartedMockNode certifier;

    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withThreadPerNode(false).withCordappsForAllNodes(ImmutableList.of(
                        TestCordapp.findCordapp("it.polimi.tgolfetto.contracts"),
                        TestCordapp.findCordapp("it.polimi.tgolfetto.flows"),
                        TestCordapp.findCordapp("net.corda.bn.flows"),
                        TestCordapp.findCordapp("net.corda.bn.states")))
                .withNotarySpecs(ImmutableList.of(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=Rome,C=IT"))))
        );
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
        Future<String> future = networkOperator.startFlow(flow);
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
        System.out.println("### activateMemberTest: " + storedMembershipState);
        assert(storedMembershipState.getStatus().toString().equals("ACTIVE"));
    }


    @Test
    public void createNetworkSubGroupTest(){
        /*
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
        ActivateMember activateMemberFlow = new ActivateMember(new UniqueIdentifier(storedMembershipState.getNetworkId()));
        networkOperator.startFlow(activateMemberFlow);
        network.runNetwork();
         */
    }
}
