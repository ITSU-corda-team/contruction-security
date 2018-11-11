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
    @GET
    @Path("CreateProject")
    @Produces(MediaType.APPLICATION_JSON)
    fun CreateProjectEndpoint(

            @QueryParam("ProjectName") ProjectName: String,
            @QueryParam("ProjectValue") ProjectValue: Int,
            @QueryParam("EstimatedProjectCost") EstimatedProjectCost: Int,
            @QueryParam("SecurityTrustee") SecurityTrustee: String,
            @QueryParam("Bank") Bank: String,
            @QueryParam("Offtaker") Offtaker: String): Response {
        println("inside createproject1")
        println("ProjectName:" + ProjectName)
        println("ProjectValue:" + ProjectValue)
        println("EstimatedProjectCost" + EstimatedProjectCost)
        val BankParty = rpcOps.partiesFromName(Bank, false).single()
        println("after BankParty" + BankParty.owningKey)
        val OfftakerParty = rpcOps.partiesFromName(Offtaker, false).single()
        println("after OfftakerParty:" + OfftakerParty.owningKey)
        val SecurityTrusteeParty = rpcOps.partiesFromName(SecurityTrustee, false).single()
        println("after SecurityTrusteeParty" + SecurityTrusteeParty.owningKey)
        rpcOps.startFlowDynamic(CreateProjectFlow::class.java, ProjectName, ProjectValue, ProjectStatus.STARTED, EstimatedProjectCost, 0, EstimatedProjectCost, SecurityTrusteeParty.owningKey, BankParty.owningKey, OfftakerParty.owningKey).returnValue.get()
        return Response.ok("Project Created Here.").build()
    }
/*
    @GET
    @Path("DeclareBankruptcy")
    @Produces(MediaType.APPLICATION_JSON)
    fun declareBankruptcyEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {
        rpcOps.startFlowDynamic(DeclareBankruptcyFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Project bankrupt.").build()
    }
*/
    @GET
    @Path("GenerateSecurityAgreement")
    @Produces(MediaType.APPLICATION_JSON)
    fun GenerateSecurityAgreementEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {
        rpcOps.startFlowDynamic(GenerateSecurityAgreementFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Security Agreement Created.").build()
    }

    @GET
    @Path("CloseProject")
    @Produces(MediaType.APPLICATION_JSON)
    fun CloseProjectEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {
        rpcOps.startFlowDynamic(CloseProjectFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Project Closed.").build()
    }

    @GET
    @Path("DeclareProjectSuccess")
    @Produces(MediaType.APPLICATION_JSON)
    fun DeclareProjectSuccessEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {
        rpcOps.startFlowDynamic(DeclareProjectSuccessFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Project Success.").build()
    }

    @GET
    @Path("DeclareProjectFailure")
    @Produces(MediaType.APPLICATION_JSON)
    fun DeclareProjectFailureEndpoint(
            @QueryParam("ProjectName") ProjectName: String): Response {
        rpcOps.startFlowDynamic(DeclareProjectFailureFlow::class.java, ProjectName).returnValue.get()
        return Response.ok("Project Failure.").build()
    }

    @GET
    @Path("getProjects")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProjectsEndpoint(): Response {
        println("inside getProjects1")
        val projects = rpcOps.vaultQueryBy<net.cordaclub.itsu.ProjectState>().states.map { it.toString() }.joinToString("\r\n")
        println("inside getProjects2")
        return Response.ok(projects).build()
    }
    /* ADDED by Andris
        fun getProjectsEndpoint(): Response {

        val gson = GsonBuilder().registerTypeAdapter(Party::class.java, PartySerializer())
                .setPrettyPrinting()
                .create()

        val projects = gson.toJson(rpcOps.vaultQueryBy<ProjectState>())

        return Response.ok(projects).build()
    }*/

}

// *********
// * Flows *
// *********



@InitiatingFlow
@StartableByRPC
class CreateProjectFlow(val ProjectName: String, val ProjectValue: Int, val ProjectStatus: ProjectStatus, val EstimatedProjectCost: Int, val ProjectCostToDate: Int, val LoanSantionedAmount: Int, val SecurityTrustee: Party, val Bank: Party, val Offtaker: Party) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        println("inside CreateProjectFlow")

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addOutputState(ProjectState(ProjectName, ProjectValue,
                        ProjectStatus, EstimatedProjectCost,ProjectCostToDate, LoanSantionedAmount,
                        SecurityTrustee,  SecurityTrustee, Bank, Offtaker), ProjectContract.ID)

/*                .addOutputState(ProjectState(ProjectName, ProjectValue,
                         ProjectStatus, EstimatedProjectCost,ProjectCostToDate, LoanSantionedAmount,
                        ourIdentity,  SecurityTrustee, Bank, Offtaker), ProjectContract.ID)
*/
//                .addOutputState(ProjectState(Project(ProjectName, ProjectValue, EstimatedProjectCost,
//                        0, EstimatedProjectCost), ProjectStatus.STARTED, SecurityAgreement(ProjectName, ProjectValue, 5 ,ourIdentity, ourIdentity), ourIdentity, Bank, Offtaker), ProjectContract.ID)
//                .addCommand(ProjectCommand.CreateProject(), ourIdentity.owningKey)
//                .addCommand(ProjectContract.command.CreateProject(), ourIdentity.owningKey)
 .addCommand(ProjectContract.Commands.CreateProject(), ourIdentity.owningKey)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class GenerateSecurityAgreementFlow(val ProjectName: String) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
        val input = projectStates.single { it.state.data.ProjectName == ProjectName }
      //  val input = projectStates.single { it.state.data.SecurityAgreement.SecurityAgreementName == ProjectName }
        val inputState = input.state.data

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)
                .addCommand(ProjectContract.Commands.GenerateSecurityAgreement(), ourIdentity.owningKey)
/* ygk temp comment
                .addOutputState(inputState.copy( SecurityAgreement().SecurityAgreementName = ProjectName), ProjectContract.ID)
                .addOutputState(inputState.copy( SecurityAgreement().SecurityValue = inputState.EstimatedProjectCost), ProjectContract.ID)
                .addOutputState(inputState.copy( SecurityAgreement().SecurityTrustee = inputState.ProjectOwner), ProjectContract.ID)
                .addOutputState(inputState.copy( SecurityAgreement().SecurityInterest = 5.0), ProjectContract.ID)
*/
//                .addOutputState(inputState.copy( SecurityAgreement().SecurityAgreementName = ProjectName), ProjectContract.ID)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
    }
}

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
                .addOutputState(inputState.copy( ProjectStatus = ProjectStatus.CLOSED), ProjectContract.ID)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
    }
}


@InitiatingFlow
@StartableByRPC
class DeclareProjectFailureFlow(val ProjectName: String) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
        val input = projectStates.single { it.state.data.ProjectName == ProjectName }
        val inputState = input.state.data

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)

                .addOutputState(inputState.copy( ProjectStatus = ProjectStatus.CLOSED_FAILURE), ProjectContract.ID)
//                .addOutputState(inputState.copy( SecurityAgreement().SecurityAgreementName = inputState.ProjectName), SecurityAgreement().SecurityValue = inputState.EstimatedProjectCost,SecurityAgreement().SecurityAgreementOwner = inputState.Bank, ProjectContract.ID)
       //         .addOutputState(inputState.copy( SecurityAgreement().SecurityValue = inputState.EstimatedProjectCost), ProjectContract.ID)
         //       .addOutputState(inputState.copy( SecurityAgreement().SecurityAgreementOwner = inputState.Bank), ProjectContract.ID)
                .addCommand(ProjectContract.Commands.DeclareProjectFailure(), ourIdentity.owningKey)
//                .addCommand(ProjectContract.Commands.DeclareBankruptcy(), ourIdentity.owningKey)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
    }
}

@InitiatingFlow
@StartableByRPC
class DeclareProjectSuccessFlow(val ProjectName: String) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val projectStates = serviceHub.vaultService.queryBy<ProjectState>().states
        val input = projectStates.single { it.state.data.ProjectName == ProjectName }
        val inputState = input.state.data

        //Build transaction
/* ygk orig  */
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)
                .addOutputState(inputState.copy(ProjectOwner = inputState.Offtaker), ProjectContract.ID)
                .addOutputState(inputState.copy(ProjectStatus = ProjectStatus.CLOSED_SUCCESS), ProjectContract.ID)

                .addCommand(ProjectContract.Commands.DeclareProjectSuccess(), ourIdentity.owningKey)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
    }
/*
        val inputProjectState = projectStates.single { it.state.data.Project.ProjectName == ProjectName }
        val inputProject = inputProjectState.state.data

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val txBuilder = TransactionBuilder(notary).apply {
            addCommand(Command(ProjectCommand.DeclareProjectSuccess(), ourIdentity.owningKey))
            addInputState(inputProjectState)
            addOutputState(inputProjectState.state.copy(data = inputProjectState.state.date.let{
                ProjectState(
                        ProjectStatus = ProjectStatus.CLOSED_SUCCESS,
                        ProjectOwner.owningKey =  Offtaker.owningKey



                )
            }
        }
  */
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
