# textile-sym-dlt
Digital Ledger Technology to foster Industrial Symbiosis in textile manufacturing

### Usage

#### Running the CorDapp

Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean build deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```
#### Interacting with the CorDapp

**Step 1:** Create the network in NetworkOperator's terminal
```
flow start CreateNetwork
```
Sample output:
```
Mon Apr 12 10:35:47 EDT 2021>>> flow start CreateNetwork

 ✅   Starting
➡️   Done
Flow completed with result: 
A network was created with NetworkID: 121577cf-30bf-4e20-9c7d-97f0b4628b06  <- This is what you need in Step 2
```
**Step 2:** 2 non-member makes the request to join the network. Fill in the networkId with what was return from Step1
```
flow start RequestMembership authorisedParty: NetworkOperator, networkId: <xxxx-xxxx-NETWORK-ID-xxxxx>
```
**Step 3:** go back to the admin node, and query all the membership requests.
```
flow start QueryAllMembers
```
**Step 4:** In this step, Network Operator will activate the pending memberships
Insurance: fill in the Insurance node MembershipId that is display in the previous query
```
flow start ActiveMembers membershipId: <xxxx-xxxx-INSURANCE-ID-xxxxx>
```
CarePro: fill in the CarePro node MembershipId that is display in the previous query
```
flow start ActiveMembers membershipId: <xxxx-xxxx-CAREPRO-ID-xxxxx>
```

**Step 5:** Admin create subgroup and add group members.
```
flow start CreateNetworkSubGroup networkId: <xxxx-FROM-STEP-ONE-xxxxx>, groupName: APAC_Insurance_Alliance, groupParticipants: [<xxxx-NETWORKOPERATOR-ID-xxxxx>, <xxxx-xxxx-INSURANCE-ID-xxxxx>, <xxxx-xxxx-CAREPRO-ID-xxxxx>]
```
**Step 6:** Admin assign business identity to a member.
```
flow start AssignBNIdentity firmType: InsuranceFirm, membershipId: <xxxx-xxxx-INSURANCE-ID-xxxxx>, bnIdentity: APACIN76CZX
```
**Step 7:** Admin assign business identity to the second member
```
flow start AssignBNIdentity firmType: CareProvider, membershipId: <xxxx-xxxx-CAREPRO-ID-xxxxx>, bnIdentity: APACCP44OJS
```
**Step 8:** Admin assign business identity related ROLE to the member.
```
flow start AssignPolicyIssuerRole membershipId: <xxxx-xxxx-INSURANCE-ID-xxxxx>, networkId: <xxxx-xxxx-NETWORK-ID-xxxxx>
```
Now to see our membership states, we can run these vault queries.
```
run vaultQuery contractStateType: net.corda.core.contracts.ContractState
run vaultQuery contractStateType: net.corda.bn.states.MembershipState
```
-------------------Network setup is done, and business flow begins--------------------------

**Step 9:** The insurance Company will issue a policy to insuree. The flow initiator (the insurance company) has to be a member of the Business network, has to have a insuranceIdentity, and has to have issuer Role, and has to have issuance permission.
```
flow start IssuePolicyInitiator networkId: <xxxx-xxxx-NETWORK-ID-xxxxx>, careProvider: CarePro, insuree: PeterLi
```
**Step 10:** Query the state from the CarePro node.
```
run vaultQuery contractStateType: net.corda.samples.businessmembership.states.InsuranceState
```
