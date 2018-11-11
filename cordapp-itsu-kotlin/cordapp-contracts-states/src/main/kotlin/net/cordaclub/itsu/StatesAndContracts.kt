package net.cordaclub.itsu

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

//Added from corda-template-kotlin
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import java.sql.Date

//import java.sql.Date
import java.text.DateFormat



@CordaSerializable
enum class ProjectStatus {
    STARTED, CLOSED, CLOSED_SUCCESS, CLOSED_FAILURE
}


@CordaSerializable
data class SecurityAgreement(
    val SecurityAgreementName: String,
    val SecurityValue: Int,
    val SecurityInterest: Int,
//    val date: Date,
    val SecurityTrustee: Party,
    val SecurityAgreementOwner: Party
)

/*
) : ContractState {
    override val participants  = listOf( SecurityTrustee)
//      override val participants: List<AbstractParty>
 //   get() = listOf(Bank, SecurityTrustee, Offtaker)
}

override val participants: List<AbstractParty>
    get() = listOf(treatment.hospital)

override fun toString(): String {
    return "TreatmentState(treatment=$treatment, treatmentCost=$treatmentCost)"
}
            override val participants get() = listOf(SecurityAgreementName, SecurityValue, SecurityInterest, SecurityTrustee, Owner)
*/

// *********
// * State *
// *********


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

sealed class ProjectCommand : TypeOnlyCommandData() {
    class CreateProject : ProjectCommand()
    class GenerateSecurityAgreement : ProjectCommand()
    class CloseProject : ProjectCommand()
    //    class DeclareBankruptcy : ProjectCommand()
    class DeclareProjectFailure : ProjectCommand()
    class DeclareProjectSuccess : ProjectCommand()
}

/* ygk v1
data class ProjectState(
        val name: String,
        val value: Int,
        val owner: Party,
        val securityTrustee: Party,
        val bank: Party,
        val offtaker: Party) : ContractState {
    override val participants = listOf(securityTrustee, bank, offtaker)
}
*/

/*
//Security Agreement signed between Lender and the Security Trustee. Attributes are
//Value
//AgreementDate
//Security Trustee Name
//SPV Name
class SecurityAgreementState(val value: Int,
//                             val date: Date,
                             val lender: Party,
                             val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}
 */


// *****************
// * Contract Code *
// *****************

// This is used to identify our contract when building a transaction
class ProjectContract : Contract {
    companion object {
//        val ID = "com.template.ProjectContract"
        val ID = "net.cordaclub.itsu.ProjectContract"
//        val ID: ContractClassName = ProjectContract::class.qualifiedName !!
    }

    override fun verify(tx: LedgerTransaction) {
        // TODO: Implement logic.
        val command = tx.commands.requireSingleCommand<ProjectCommand>()
        val setOfSigners = command.signers.toSet()
        val inputProjects = tx.inputsOfType<ProjectState>()
        val outputProject = tx.outputsOfType<ProjectState>().single()

        requireThat {
            inputProjects.singleOrNull()?.let {

                "The same Project" using (it.ProjectName == outputProject.ProjectName)
                "The same EstimatedProjectCost" using (it.EstimatedProjectCost == outputProject.EstimatedProjectCost)
                "The same ProjectValue" using (it.ProjectValue == outputProject.ProjectValue)
                //        "The same linearId" using (it.linearId == outputProject.linearId)
            }

//            "The patient has a correct NINO" using (isCorrectNino(outputTreatment.treatment.patient.nino))

//            "The hospital signed the transaction" using (setOfSigners.containsAll(outputTreatment.participants.map { it.owningKey } + inputTreatments.flatMap { it.participants.map { it.owningKey } }))

            when (command.value) {
                is ProjectCommand.CreateProject -> {
                    "No inputs should be consumed when creating a Project." using (inputProjects.isEmpty())
                    "The Project status should be STARTED." using (outputProject.ProjectStatus == ProjectStatus.STARTED)
                    "The Project value should be greater than ZERO." using (outputProject.ProjectValue > 0)
                }
                is ProjectCommand.GenerateSecurityAgreement -> {
                    "The Project status should be STARTED" using (inputProjects.single().ProjectStatus == ProjectStatus.STARTED)
                    "The Security Trustee should be the owner of the Security Agreement" using (outputProject.SecurityAgreement.SecurityAgreementOwner.owningKey == outputProject.SecurityAgreement.SecurityTrustee.owningKey)

//                    "The insurer signed the transaction" using (setOfSigners.contains(outputTreatment.insurerQuote!!.insurer.owningKey))
//                    "The estimated value is greater or equal than the quote" using (outputTreatment.estimatedTreatmentCost >= outputTreatment.insurerQuote!!.maxCoveredValue)

                }
                is ProjectCommand.CloseProject -> {
                    "The Project input status should be STARTED." using (inputProjects.single().ProjectStatus == ProjectStatus.STARTED)
                    "The Project output status should be CLOSED." using (outputProject.ProjectStatus == ProjectStatus.CLOSED)
                    "The Project Cost To Date should be non-ZERO" using (outputProject.ProjectCostToDate > 0)

                }
                is ProjectCommand.DeclareProjectFailure -> {
                    "The Project input status should be CLOSED." using (inputProjects.single().ProjectStatus == ProjectStatus.CLOSED)
                    "The Project output status should be CLOSED_FAILURE." using (outputProject.ProjectStatus == ProjectStatus.CLOSED_FAILURE)
                    "The Project Cost To Date will be greater than Estimated Project Cost." using (outputProject.ProjectCostToDate > outputProject.EstimatedProjectCost)
                    "The Bank should be the owner of the Security Agreement." using (outputProject.SecurityAgreement.SecurityAgreementOwner.owningKey == outputProject.Bank.owningKey)

                }
                is ProjectCommand.DeclareProjectSuccess -> {
                    "The Project input status should be CLOSED." using (inputProjects.single().ProjectStatus == ProjectStatus.CLOSED)
                    "The Project output status should be CLOSED_SUCCESS." using (outputProject.ProjectStatus == ProjectStatus.CLOSED_SUCCESS)
                    "The Project Cost To Date will be less than or equal to Estimated Project Cost." using (outputProject.ProjectCostToDate <= outputProject.EstimatedProjectCost)
                    "The Offtaker should be the owner of the Security Agreement." using (outputProject.SecurityAgreement.SecurityAgreementOwner.owningKey == outputProject.Offtaker.owningKey)

                }
            }
        }

    }

    /* YGK Commented for now
    interface Commands: CommandData {
        class Create: Commands
        class DeclareBankruptcy: Commands
        class DeclareProjectSuccess: Commands
        class DeclareProjectFailure: Commands
    }
    */
}

/*
// This is used to identify our contract when building a transaction
val IOU_CONTRACT_ID = "com.template.IOUContract"

class IOUContract : Contract {
    // Our Create command.
    class Create : CommandData

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Create>()

        requireThat {
            // Constraints on the shape of the transaction.
            "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
            "There should be one output state of type IOUState." using (tx.outputs.size == 1)

            // IOU-specific constraints.
            val out = tx.outputsOfType<SecurityAgreementState>().single()
            "The IOU's value must be non-negative." using (out.value > 0)
            "The lender and the borrower cannot be the same entity." using (out.lender != out.borrower)

            // Constraints on the signers.
            "There must be two signers." using (command.signers.toSet().size == 2)
            "The borrower and lender must be signers." using (command.signers.containsAll(listOf(
                    out.borrower.owningKey, out.lender.owningKey)))
        }
    }
}

*/



/* ygk
class IOUState(val value: Int,
               val lender: Party,
               val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}
ygk */

/*
//Shareholder Agreement signed between Shareholder and the SPV. Attributes are
//Value
//AgreementDate
//Shareholder Name
//SPV Name
class ShareholderAgreementState(val value: Int,
                                val dateFormat: DateFormat,
               val lender: Party,
               val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

//Construction Contract signed between Contractor and the SPV. Attributes are
//Value
//ContractDate
//SPV Name
//Contractor Name
class ConstructionContractState(val value: Int,
                                val dateFormat: DateFormat,
                                val lender: Party,
                                val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

//Offtaker Agreement signed between Offtaker and the SPV. Attributes are
//Value
//AgreementDate
//SPV Name
//Offtaker Name
class OfftakerAgreementState(val value: Int,
                                val dateFormat: DateFormat,
                                val lender: Party,
                                val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

//Contractor Direct Agreement signed between Contractor and the SPV. Attributes are
//Value
//AgreementDate
//Security Trustee Name
//Contractor Name
class ContractorDirectAgreementState(val value: Int,
                                val dateFormat: DateFormat,
                                val lender: Party,
                                val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

//Offtaker Direct Agreement signed between Offtaker and the SPV. Attributes are
//Value
//AgreementDate
//Security Trustee Name
//Offtaker Name
class OfftakerDirectAgreementState(val value: Int,
                                val dateFormat: DateFormat,
                                val lender: Party,
                                val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

//Loan Agreement signed between Lender and the Security Trustee. Attributes are
//Value
//AgreementDate
//Lender Name
//Security Trustee Name
class LoanAgreementState(val value: Int,
                                val dateFormat: DateFormat,
                                val lender: Party,
                                val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

*/