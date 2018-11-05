package com.example.api;

import com.example.flow.ExampleFlow;
import com.example.schema.SecurityAgreementSchemaV1;
import com.example.state.SecurityAgreementState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.*;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

// This API is accessible from /api/example. All paths specified below are relative to it.
@Path("example")
public class ExampleApi {
    private final CordaRPCOps rpcOps;
    private final CordaX500Name myLegalName;

    private final List<String> serviceNames = ImmutableList.of("Notary");

    static private final Logger logger = LoggerFactory.getLogger(ExampleApi.class);

    public ExampleApi(CordaRPCOps rpcOps) {
        this.rpcOps = rpcOps;
        this.myLegalName = rpcOps.nodeInfo().getLegalIdentities().get(0).getName();
    }

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, CordaX500Name> whoami() {
        return ImmutableMap.of("me", myLegalName);
    }

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<CordaX500Name>> getPeers() {
        List<NodeInfo> nodeInfoSnapshot = rpcOps.networkMapSnapshot();
        return ImmutableMap.of("peers", nodeInfoSnapshot
                .stream()
                .map(node -> node.getLegalIdentities().get(0).getName())
                .filter(name -> !name.equals(myLegalName) && !serviceNames.contains(name.getOrganisation()))
                .collect(toList()));
    }

    /**
     * Displays all security agreements states that exist in the node's vault.
     */
    @GET
    @Path("securityagreements")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<SecurityAgreementState>> getSecurityAgreements() {
        return rpcOps.vaultQuery(SecurityAgreementState.class).getStates();
    }

    /**
     * Initiates a flow to agree an Security Agreement between two parties.
     *
     * Once the flow finishes it will have written the security Agreement to ledger. Both the lender and the borrower will be able to
     * see it when calling /api/example/securityagreements on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */
    @PUT
    @Path("create-securityagreement")
    public Response createSecurityAgreement(@QueryParam("securityAgreementValue") int securityAgreementValue, @QueryParam("partyName") CordaX500Name partyName) throws InterruptedException, ExecutionException {
        if (securityAgreementValue <= 0) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'securityAgreementValue' must be non-negative " + securityAgreementValue + ".\n").build();
        }
        if (partyName == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'partyName' missing or has wrong format.\n").build();
        }

        final Party otherParty = rpcOps.wellKnownPartyFromX500Name(partyName);
        if (otherParty == null) {
            return Response.status(BAD_REQUEST).entity("Party named " + partyName + "cannot be found.\n").build();
        }

        try {
            final SignedTransaction signedTx = rpcOps
                    .startTrackedFlowDynamic(ExampleFlow.Initiator.class, securityAgreementValue, otherParty)
                    .getReturnValue()
                    .get();

            final String msg = String.format("Transaction id %s committed to ledger.\n", signedTx.getId());
            return Response.status(CREATED).entity(msg).build();

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            logger.error(ex.getMessage(), ex);
            return Response.status(BAD_REQUEST).entity(msg).build();
        }
    }
	
	/**
     * Displays all security agreement states that are created by Party.
     */
    @GET
    @Path("my-securityagreements")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMySecurityAgreements() throws NoSuchFieldException {
        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Field lender = SecurityAgreementSchemaV1.PersistentSecurityAgreement.class.getDeclaredField("lender");
        CriteriaExpression lenderIndex = Builder.equal(lender, myLegalName.toString());
        QueryCriteria lenderCriteria = new QueryCriteria.VaultCustomQueryCriteria(lenderIndex);
        QueryCriteria criteria = generalCriteria.and(lenderCriteria);
        List<StateAndRef<SecurityAgreementState>> results = rpcOps.vaultQueryByCriteria(criteria,SecurityAgreementState.class).getStates();
        return Response.status(OK).entity(results).build();
    }
}
