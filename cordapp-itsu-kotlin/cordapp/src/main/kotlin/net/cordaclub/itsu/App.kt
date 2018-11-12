package net.cordaclub.itsu

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.serialization.SerializationWhitelist
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import net.corda.core.contracts.requireThat
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import java.security.acl.Owner
import java.text.DateFormat
import javax.ws.rs.QueryParam

// *****************
// * API Endpoints *
// *****************
@Path("template")
class TemplateApi(val rpcOps: CordaRPCOps) {

    // 1. Create Project
    @GET
    @Path("CreateProject")
    @Produces(MediaType.APPLICATION_JSON)
    fun CreateProjectEndpoint(

        @QueryParam("ProjectName") ProjectName: String,
        @QueryParam("ProjectValue") ProjectValue: Int,
        @QueryParam("EstimatedProjectCost") EstimatedProjectCost: Int,
        @QueryParam("SPV") SPV: String,
        @QueryParam("SecurityTrustee") SecurityTrustee: String,
        @QueryParam("Bank") Bank: String,
        @QueryParam("Offtaker") Offtaker: String): Response {

        println("in CreateProject"  )

        val SPVParty = rpcOps.partiesFromName(SPV, false).single()
        println("after SPVParty" + SPVParty.name + "SPV:" + SPV )
        val BankParty = rpcOps.partiesFromName(Bank, false).single()
//        println("after BankParty" + BankParty.name + "Bank:" + Bank )
        val OfftakerParty = rpcOps.partiesFromName(Offtaker, false).single()
//        println("after OfftakerParty:" + OfftakerParty.name + "Offtaker:" + Offtaker)
        val SecurityTrusteeParty = rpcOps.partiesFromName(SecurityTrustee, false).single()
//        println("after SecurityTrusteeParty" + SecurityTrusteeParty.name + "SecurityTrustee:" + SecurityTrustee)
        rpcOps.startFlowDynamic(CreateProjectFlow::class.java, ProjectName, ProjectValue, ProjectStatus.STARTED, ProjectCompleteStatus.NULL, EstimatedProjectCost, EstimatedProjectCost, 0, 0, SPVParty, SecurityTrusteeParty, BankParty, OfftakerParty).returnValue.get()
        return Response.ok("Project Created Here.").build()
    }

    // 2. Create Security Agreement State
    @GET
    @Path("CreateSecurityAgreement")
    @Produces(MediaType.APPLICATION_JSON)
    fun CreateSecurityAgreementEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {

        println("inside CreateSecurityAgreement 0")

        rpcOps.startFlowDynamic(CreateSecurityAgreementFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Security Agreement Created.").build()
    }

    // 3. Close Project THREE Arguments
    @GET
    @Path("CloseProject")
    @Produces(MediaType.APPLICATION_JSON)
    fun CloseProjectEndpoint(
            @QueryParam("ProjectName") ProjectName: String,
            @QueryParam("ProjectCostTillDate") ProjectCostTillDate: Int,
            @QueryParam("ProjectCashFlow") ProjectCashFlow: Int): Response {
        rpcOps.startFlowDynamic(CloseProjectFlow::class.java, ProjectName, ProjectCostTillDate, ProjectCashFlow).returnValue.get()
        return Response.ok("Project Closed.").build()
    }
    // 6. Get Projects
    @GET
    @Path("getProjects")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProjectsEndpoint(): Response {
//        println("inside getProjects1")
        val projects = rpcOps.vaultQueryBy<net.cordaclub.itsu.ProjectState>().states.map { it.toString() }.joinToString("\r\n")
        return Response.ok(projects).build()
    }

    // 7. Get Security Agreements
    @GET
    @Path("getSecurityAgreements")
    @Produces(MediaType.APPLICATION_JSON)
    fun getSecurityAgreementsEndpoint(): Response {
//        println("inside getSecurityAgreements 0")
        val SecurityAgreements = rpcOps.vaultQueryBy<net.cordaclub.itsu.SecurityAgreementState>().states.map { it.toString() }.joinToString("\r\n")
        return Response.ok(SecurityAgreements).build()
    }
/*
    // 3. Close Project ONE Argument
    @GET
    @Path("CloseProject")
    @Produces(MediaType.APPLICATION_JSON)
    fun CloseProjectEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {
        rpcOps.startFlowDynamic(CloseProjectFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Project Closed.").build()
    }

    // 3b. CloseProjectWithSuccessFailure
    @GET
    @Path("CloseProjectWithSuccessFailure")
    @Produces(MediaType.APPLICATION_JSON)
    fun CloseProjectWithSuccessFailureEndpoint(
        @QueryParam("ProjectName") ProjectName: String,
        @QueryParam("ProjectCashFlow") ProjectCashFlow: Int): Response {

        val statusProject = if ()
        {
            rpcOps.startFlowDynamic(DeclareProjectSuccessFlow::class.java, ProjectName).returnValue.get()

        } else
        {
            rpcOps.startFlowDynamic(DeclareProjectFailureFlow::class.java, ProjectName).returnValue.get()

        }

        return Response.ok("Project Closed with Success or Failure.").build()
    }


    // 4. Declare Project a Success
    @GET
    @Path("DeclareProjectSuccess")
    @Produces(MediaType.APPLICATION_JSON)
    fun DeclareProjectSuccessEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {

//          println("inside DeclareProjectSuccess")
        rpcOps.startFlowDynamic(DeclareProjectSuccessFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Project Success.").build()
    }

    // 5. Declare Project a Failure
    @GET
    @Path("DeclareProjectFailure")
    @Produces(MediaType.APPLICATION_JSON)
    fun DeclareProjectFailureEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {
        rpcOps.startFlowDynamic(DeclareProjectFailureFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Project Failure.").build()
    }
*/

}

// *********
// * Flows *
// *********

// CreateProjectFlow
@InitiatingFlow
@StartableByRPC
class CreateProjectFlow(val ProjectName: String, val ProjectValue: Int, val ProjectStatus: ProjectStatus, val ProjectCompleteStatus: ProjectCompleteStatus, val EstimatedProjectCost: Int, val LoanSanctionedAmount: Int, val ProjectCostToDate: Int, val ProjectCashFlow: Int, val SPV: Party, val SecurityTrustee: Party, val Bank: Party, val Offtaker: Party) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()
    @Suspendable
    override fun call() {

        println("inside CreateProjectFlow")
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
            .addOutputState(ProjectState(ProjectName, ProjectValue,
                    ProjectStatus, ProjectCompleteStatus, EstimatedProjectCost,LoanSanctionedAmount, ProjectCostToDate, ProjectCashFlow,
                    SPV,  SecurityTrustee, Bank, Offtaker), ProjectContract.ID)
                    //use of ourIdentity in addOutputState
            .addCommand(ProjectContract.Commands.CreateProject(), ourIdentity.owningKey)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
    }
}


// 2.  CloseProjectFlow THREE Arguments
@InitiatingFlow
@StartableByRPC
class CloseProjectFlow(val ProjectName: String, val ProjectCostTillDate: Int, val ProjectCashFlow: Int) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
        val inputProject = projectStates.single { it.state.data.ProjectName == ProjectName }
        val inputProjectState = inputProject.state.data
        val securityAgreementName = inputProjectState.ProjectName + "_SecurityAgreement"

        val securityAgreementStates = serviceHub.vaultService.queryBy<SecurityAgreementState>().states
        val inputSecurityAgreement = securityAgreementStates.single { it.state.data.SecurityAgreementName == securityAgreementName }
        val inputSecurityAgreementState = inputSecurityAgreement.state.data

        val LoanSanctionedAmount = inputProjectState.LoanSanctionedAmount

        val projectStatusInd: ProjectStatus
        val projectCompleteStatusInd: ProjectCompleteStatus
        val securityAgreementOwner: Party


        if (ProjectCashFlow > LoanSanctionedAmount)
        {
             projectStatusInd = ProjectStatus.COMPLETE
             projectCompleteStatusInd = ProjectCompleteStatus.COMPLETE_SUCCESS
             securityAgreementOwner = inputProjectState.Offtaker
        }
        else
        {
            projectStatusInd = ProjectStatus.COMPLETE
            projectCompleteStatusInd = ProjectCompleteStatus.COMPLETE_FAILURE
            securityAgreementOwner = inputProjectState.Bank
        }

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(inputProject)
                .addOutputState(inputProjectState.copy(ProjectStatus = projectStatusInd, ProjectCompleteStatus = projectCompleteStatusInd), ProjectContract.ID)

                .addInputState(inputSecurityAgreement)
                .addOutputState(inputSecurityAgreementState.copy(SecurityAgreementOwner = securityAgreementOwner), ProjectContract.ID)

                .addCommand(ProjectContract.Commands.CloseProject(), ourIdentity.owningKey)

    val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))

    }
}



// CreateSecurityAgreementFlow
@InitiatingFlow
@StartableByRPC
class CreateSecurityAgreementFlow(val ProjectName: String) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {

        println("in CreateSecurityAgreementFlow" )

        //Get the ProjectState for the ProjectName
        val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
        val inputProject = projectStates.single { it.state.data.ProjectName == ProjectName }
        val inputProjectState = inputProject.state.data

        val SecurityAgreementName = ProjectName + "_SecurityAgreement"
        val SecurityTrustee = inputProjectState.SecurityTrustee
        val SecurityAgreementOwner = inputProjectState.SPV
        val SecurityAgreementValue = inputProjectState.ProjectValue
        val SecurityInterest = 5 /*Random Number*/

        println("SecurityTrustee: " + SecurityTrustee.name)

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])

        .addOutputState(SecurityAgreementState(SecurityAgreementName, ProjectName, SecurityAgreementValue,
                SecurityInterest, SecurityAgreementOwner, SecurityTrustee), SecurityAgreementContract.ID)
        .addCommand(SecurityAgreementContract.Commands.CreateSecurityAgreement(), ourIdentity.owningKey)

        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))

    }

    /*
    // 2.  CloseProjectFlow ONE Argument
    @InitiatingFlow
    @StartableByRPC
    class CloseProjectFlow(val ProjectName: String) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
            val input = projectStates.single { it.state.data.ProjectName == ProjectName }
            val inputState = input.state.data

            val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)
                .addCommand(ProjectContract.Commands.CloseProject(), ourIdentity.owningKey)
                .addOutputState(inputState.copy(ProjectOwner = inputState.Offtaker, ProjectStatus = ProjectStatus.COMPLETE ), ProjectContract.ID)
               // .addOutputState(inputState.copy(ProjectStatus = ProjectStatus.COMPLETE), ProjectContract.ID)
            val signedTx = serviceHub.signInitialTransaction(txBuilder)
            subFlow(FinalityFlow(signedTx))
        }
    }

    // CloseProjectWithSuccessFailureFlow
    @InitiatingFlow
    @StartableByRPC
    class CloseProjectWithSuccessFailureFlow(val ProjectName: String) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
            val input = projectStates.single { it.state.data.ProjectName == ProjectName }
            val inputState = input.state.data
            val ProjectCashFlow = inputState.ProjectCashFlow
            val LoanSanctionedAmount = inputState.LoanSanctionedAmount

            val max = if (a > b) a else b


            val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                    .addInputState(input)
                    .addCommand(ProjectContract.Commands.CloseProject(), ourIdentity.owningKey)
                    .addOutputState(inputState.copy(ProjectOwner = inputState.Offtaker, ProjectStatus = ProjectStatus.COMPLETE ), ProjectContract.ID)
            val signedTx = serviceHub.signInitialTransaction(txBuilder)
            subFlow(FinalityFlow(signedTx))
        }
    }


    // DeclareProjectSuccessFlow
    @InitiatingFlow
    @StartableByRPC
    class DeclareProjectSuccessFlow(val ProjectName: String) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
    //        println("Inside DeclareProjectSuccessFlow: " +ProjectName )

            val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
            val inputProject = projectStates.single { it.state.data.ProjectName == ProjectName }
            val inputProjectState = inputProject.state.data

    //        println("inputProjectState.ProjectName: " + inputProjectState.ProjectName )

            val SecurityAgreementName = inputProjectState.ProjectName + "_SecurityAgreement"
            val securityAgreementStates = serviceHub.vaultService.queryBy<SecurityAgreementState>().states
            val inputSecurityAgreement = securityAgreementStates.single { it.state.data.SecurityAgreementName == SecurityAgreementName }
            val inputSecurityAgreementState = inputSecurityAgreement.state.data

    //        println("inputProjectState.Offtaker: " + inputProjectState.Offtaker.name)

            //Build transaction
            val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(inputProject)
                .addOutputState(inputProjectState.copy(ProjectCompleteStatus = ProjectCompleteStatus.COMPLETE_SUCCESS), ProjectContract.ID)

                .addInputState(inputSecurityAgreement)
                .addOutputState(inputSecurityAgreementState.copy(SecurityAgreementOwner = inputProjectState.Offtaker), ProjectContract.ID)

                .addCommand(ProjectContract.Commands.DeclareProjectSuccess(), ourIdentity.owningKey)
            val signedTx = serviceHub.signInitialTransaction(txBuilder)
            subFlow(FinalityFlow(signedTx))
        }

    }

    // DeclareProjectFailureFlow
    @InitiatingFlow
    @StartableByRPC
    class DeclareProjectFailureFlow(val ProjectName: String) : FlowLogic<Unit>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call() {
            val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
            val inputProject = projectStates.single { it.state.data.ProjectName == ProjectName }
            val inputProjectState = inputProject.state.data

            val SecurityAgreementName = inputProjectState.ProjectName + "_SecurityAgreement"
            val securityAgreementStates = serviceHub.vaultService.queryBy<SecurityAgreementState>().states
            val inputSecurityAgreeemnt = securityAgreementStates.single { it.state.data.SecurityAgreementName == SecurityAgreementName }
            val inputSecurityAgreeemntState = inputSecurityAgreeemnt.state.data

            println("inputProjectState.Bank: " + inputProjectState.Bank.name)

            //Build transaction
            val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(inputProject)
                .addOutputState(inputProjectState.copy(ProjectCompleteStatus = ProjectCompleteStatus.COMPLETE_FAILURE), ProjectContract.ID)

                .addInputState(inputSecurityAgreeemnt)
                .addOutputState(inputSecurityAgreeemntState.copy(SecurityAgreementOwner = inputProjectState.Bank), ProjectContract.ID)

                .addCommand(ProjectContract.Commands.DeclareProjectFailure(), ourIdentity.owningKey)
            val signedTx = serviceHub.signInitialTransaction(txBuilder)
            subFlow(FinalityFlow(signedTx))
        }
    }*/

}



// ***********
// * Plugins *
// ***********
class TemplateWebPlugin : WebServerPluginRegistry {
    // A list of classes that expose web JAX-RS REST APIs.
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::TemplateApi))
    //A list of directories in the resources directory that will be served by Jetty under /web.
    // This template's web frontend is accessible at /web/template.
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the templateWeb directory in resources to /web/template
            "template" to javaClass.classLoader.getResource("templateWeb").toExternalForm()
    )
}

// Serialization whitelist.
class TemplateSerializationWhitelist : SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf(TemplateData::class.java)
}

// This class is not annotated with @CordaSerializable, so it must be added to the serialization whitelist, above, if
// we want to send it to other nodes within a flow.
data class TemplateData(val payload: String)
