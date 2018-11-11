package net.cordaclub.itsu
/*
package com.template

import co.paralleluniverse.fibers.Suspendable
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.corda.core.contracts.*
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
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.queryBy
import net.corda.core.serialization.serialize
import net.corda.core.transactions.SignedTransaction
import java.text.DateFormat
import javax.ws.rs.QueryParam
*/

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
            @QueryParam("Bank") Bank: String,
            @QueryParam("Offtaker") Offtaker: String): Response {
        val BankParty = rpcOps.partiesFromName(Bank, false).single()
        val OfftakerParty = rpcOps.partiesFromName(Offtaker, false).single()
        rpcOps.startFlowDynamic(CreateProjectFlow::class.java, ProjectName, ProjectValue, EstimatedProjectCost, BankParty, OfftakerParty).returnValue.get()
        return Response.ok("Project Created.").build()
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
        val projects = rpcOps.vaultQueryBy<net.cordaclub.itsu.ProjectState>().states.map { it.toString() }.joinToString("\r\n")
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

/*
data class ProjectState(
        val ProjectName: String,
        val ProjectValue: Int,
        val ProjectStatus: ProjectStatus,
        val EstimatedProjectCost: Int,
        val ProjectCostToDate: Int,
        val LoanSanctionedAmount: Int,
        val ProjectOwner: Party,
        val SecurityAgreement: SecurityAgreement,
//        val SecurityTrustee: Party,
        val Bank: Party,
        val Offtaker: Party) : ContractState {
    override val participants = listOf(Bank, Offtaker)
}
*/

@InitiatingFlow
@StartableByRPC
class CreateProjectFlow(val ProjectName: String, val ProjectValue: Int, val EstimatedProjectCost: Int, val Bank: Party, val Offtaker: Party) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addOutputState(ProjectState(Project(ProjectName, ProjectValue, EstimatedProjectCost, 0, EstimatedProjectCost), ProjectStatus.STARTED, SecurityAgreement(ProjectName, ProjectValue, 5 ,ourIdentity, ourIdentity), ourIdentity, Bank, Offtaker), ProjectContract.ID)
                .addCommand(ProjectCommand.CreateProject(), ourIdentity.owningKey)
//                .addCommand(ProjectContract.command.CreateProject(), ourIdentity.owningKey)
/* .addCommand(ProjectContract.Commands.Create(), ourIdentity.owningKey) */
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
        val input = projectStates.single { it.state.data.Project.ProjectName == ProjectName }
      //  val input = projectStates.single { it.state.data.SecurityAgreement.SecurityAgreementName == ProjectName }
        val inputState = input.state.data

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)
                .addCommand(ProjectCommand.GenerateSecurityAgreement(), ourIdentity.owningKey)
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
        val input = projectStates.single { it.state.data.Project.ProjectName == ProjectName }
        val inputState = input.state.data

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)
                .addCommand(ProjectCommand.CloseProject(), ourIdentity.owningKey)
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
        val input = projectStates.single { it.state.data.Project.ProjectName == ProjectName }
        val inputState = input.state.data

        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)

                .addOutputState(inputState.copy( ProjectStatus = ProjectStatus.CLOSED_FAILURE), ProjectContract.ID)
//                .addOutputState(inputState.copy( SecurityAgreement().SecurityAgreementName = inputState.ProjectName), SecurityAgreement().SecurityValue = inputState.EstimatedProjectCost,SecurityAgreement().SecurityAgreementOwner = inputState.Bank, ProjectContract.ID)
       //         .addOutputState(inputState.copy( SecurityAgreement().SecurityValue = inputState.EstimatedProjectCost), ProjectContract.ID)
         //       .addOutputState(inputState.copy( SecurityAgreement().SecurityAgreementOwner = inputState.Bank), ProjectContract.ID)
                .addCommand(ProjectCommand.DeclareProjectFailure(), ourIdentity.owningKey)
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
        val input = projectStates.single { it.state.data.Project.ProjectName == ProjectName }
        val inputState = input.state.data

        //Build transaction
/* ygk orig  */
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities[0])
                .addInputState(input)
                .addOutputState(inputState.copy(ProjectOwner = inputState.Offtaker), ProjectContract.ID)
                .addOutputState(inputState.copy(ProjectStatus = ProjectStatus.CLOSED_SUCCESS), ProjectContract.ID)

                .addCommand(ProjectCommand.DeclareProjectSuccess(), ourIdentity.owningKey)
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

/*


   class PatientTreatmentPaymentResponseFlow(private val session: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val hospital = session.counterparty

            val treatmentState = subFlow(ReceiveStateAndRefFlow<TreatmentState>(session)).single()

            val hospitalAccount = session.receive<AccountAddress>().unwrap { it }

            val treatment = treatmentState.state.data
            val toPay = treatment.treatmentCost!! - treatment.amountPayed!!

            //Build transaction
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val txb = TransactionBuilder(notary).apply {
                addCommand(Command(TreatmentCommand.FullyPayTreatment(), listOf(hospital.owningKey, ourIdentity.owningKey)))
                addInputState(treatmentState)
                addOutputState(treatmentState.state.copy(data = treatmentState.state.data.let {
                    TreatmentState(
                            treatment = it.treatment,
                            estimatedTreatmentCost = it.estimatedTreatmentCost,
                            treatmentCost = it.treatmentCost,
                            amountPayed = it.treatmentCost,
                            insurerQuote = it.insurerQuote,
                            treatmentStatus = TreatmentStatus.FULLY_PAID,
                            linearId = it.linearId
                    )
                }))
            }

@InitiatingFlow
@StartableByRPC

class IOUFlow(val iouValue: Int,
              val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = IOUState(iouValue, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID)
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define IOUFlowResponder:
@InitiatedBy(IOUFlow::class)
class IOUFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction." using (output is IOUState)
                val iou = output as IOUState
                "The IOU's value can't be too high." using (iou.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}
*/
/*
// Flow#1
// Shareholder Agreement To SPV Flow
@InitiatingFlow
@StartableByRPC

class ShareholderAgreementToSPVFlow(val iouValue: Int, val dateFormat: DateFormat,
              val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = ShareholderAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define ShareholderAgreementToSPVFlow Responder:
@InitiatedBy(ShareholderAgreementToSPVFlow::class)
class ShareholderAgreementToSPVFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Shareholder Agreement." using (output is ShareholderAgreementState)
                val ShareholderAgreement = output as ShareholderAgreementState
                "The Agreement value can't be too high." using (ShareholderAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}

// Flow#2
//Construction Contract To SPV Flow
@InitiatingFlow
@StartableByRPC

class ConstructionContractToSPVFlow(val iouValue: Int, val dateFormat: DateFormat,
                                    val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = ConstructionContractState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define ConstructionContractToSPVFlow Responder:
@InitiatedBy(ConstructionContractToSPVFlow::class)
class ConstructionContractToSPVFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Construction Contract." using (output is ConstructionContractState)
                val ConstructionContract = output as ConstructionContractState
                "The Contract value can't be too high." using (ConstructionContract.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}

// Flow#3
//Offtaker Agreement To SPV Flow
@InitiatingFlow
@StartableByRPC

class OfftakerAgreementToSPVFlow(val iouValue: Int, val dateFormat: DateFormat,
                                    val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = OfftakerAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define OfftakerAgreementToSPVFlow Responder:
@InitiatedBy(OfftakerAgreementToSPVFlow::class)
class OfftakerAgreementToSPVFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Offtaker Agreement." using (output is OfftakerAgreementState)
                val OfftakerAgreement = output as OfftakerAgreementState
                "The Agreement value can't be too high." using (OfftakerAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}

// Flow#4
//Contractor Direct Agreement To SecTrustee Flow
@InitiatingFlow
@StartableByRPC

class ContractorDirectAgreementToSecTrusteeFlow(val iouValue: Int, val dateFormat: DateFormat,
                                 val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = ContractorDirectAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define ContractorDirectAgreementToSecTrusteeFlow Responder:
@InitiatedBy(ContractorDirectAgreementToSecTrusteeFlow::class)
class ContractorDirectAgreementToSecTrusteeFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Contractor Direct Agreement." using (output is ContractorDirectAgreementState)
                val ContractorDirectAgreement = output as ContractorDirectAgreementState
                "The Agreement value can't be too high." using (ContractorDirectAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}

// Flow#5
//Offtaker Direct Agreement To SecTrustee Flow
@InitiatingFlow
@StartableByRPC

class OfftakerDirectAgreementToSecTrusteeFlow(val iouValue: Int, val dateFormat: DateFormat,
                                 val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = OfftakerDirectAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define OfftakerDirectAgreementToSecTrusteeFlow Responder:
@InitiatedBy(OfftakerDirectAgreementToSecTrusteeFlow::class)
class OfftakerDirectAgreementToSecTrusteeFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Direct Agreement." using (output is OfftakerDirectAgreementState)
                val OfftakerDirectAgreement = output as OfftakerDirectAgreementState
                "The Agreement value can't be too high." using (OfftakerDirectAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}


// Flow#6
//Security Agreement SPV To SecTrustee Flow
@InitiatingFlow
@StartableByRPC

class SecurityAgreementSPVToSecTrusteeFlow(val iouValue: Int, val dateFormat: DateFormat,
                                       val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = SecurityAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define SecurityAgreementSPVToSecTrusteeFlow Responder:
@InitiatedBy(SecurityAgreementSPVToSecTrusteeFlow::class)
class SecurityAgreementSPVToSecTrusteeFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Security Agreement." using (output is SecurityAgreementState)
                val SecurityAgreement = output as OfftakerDirectAgreementState
                "The Agreement value can't be too high." using (SecurityAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}


// Flow#7
//Loan Agreement To SecTrustee Flow
@InitiatingFlow
@StartableByRPC

class LoanAgreementToSecTrusteeFlow(val iouValue: Int, val dateFormat: DateFormat,
                                       val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = LoanAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define LoanAgreementToSecTrusteeFlow Responder:
@InitiatedBy(LoanAgreementToSecTrusteeFlow::class)
class LoanAgreementToSecTrusteeFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Loan Agreement." using (output is LoanAgreementState)
                val LoanAgreement = output as LoanAgreementState
                "The Agreement value can't be too high." using (LoanAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}


// Flow#8
//Security Agreement SecTrustee To Bank Flow
@InitiatingFlow
@StartableByRPC

class SecurityAgreementSecTrusteeToBankFlow(val iouValue: Int, val dateFormat: DateFormat,
                                            val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = SecurityAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define SecurityAgreementSecTrusteeToBankFlow Responder:
@InitiatedBy(SecurityAgreementSecTrusteeToBankFlow::class)
class SecurityAgreementSecTrusteeToBankResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Security Agreement." using (output is SecurityAgreementState)
                val SecurityAgreement = output as SecurityAgreementState
                "The Agreement value can't be too high." using (SecurityAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}

// Flow#8
//Security Agreement SecTrustee To NewCompany Flow
@InitiatingFlow
@StartableByRPC

class SecurityAgreementSecTrusteeToNewCompanyFlow(val iouValue: Int, val dateFormat: DateFormat,
                                            val otherParty: Party) : FlowLogic<Unit>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call() {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = SecurityAgreementState(iouValue, dateFormat, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID) /*ygk what is this*/
        val cmd = Command(IOUContract.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        subFlow(FinalityFlow(fullySignedTx))
    }
}

// Define SecurityAgreementSecTrusteeToNewCompanyFlow Responder:
@InitiatedBy(SecurityAgreementSecTrusteeToNewCompanyFlow::class)
class SecurityAgreementSecTrusteeToNewCompanyResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Security Agreement." using (output is SecurityAgreementState)
                val SecurityAgreement = output as SecurityAgreementState
                "The Agreement value can't be too high." using (SecurityAgreement.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}
*/



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
