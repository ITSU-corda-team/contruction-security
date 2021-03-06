package net.cordaclub.itsu

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class ProjectStatus {
    STARTED, CLOSED, COMPLETE, CLOSED_SUCCESS, CLOSED_FAILURE
}

@CordaSerializable
enum class ProjectCompleteStatus {
    NULL, COMPLETE_SUCCESS, COMPLETE_FAILURE
}

// *********
// * State *
// *********

data class ProjectState(
        val ProjectName: String,
        val ProjectValue: Int,
        val ProjectStatus: ProjectStatus,
        val ProjectCompleteStatus: ProjectCompleteStatus,
        val EstimatedProjectCost: Int,
        val LoanSanctionedAmount: Int,
        val ProjectCostToDate: Int,
        val ProjectCashFlow: Int,
        val ProjectOwner: Party, //SPV
        val SecurityTrustee: Party,
        val Bank: Party,
        val Offtaker: Party) : ContractState {
    override val participants = listOf(SecurityTrustee, Bank, Offtaker)
}

data class SecurityAgreementState(
        val SecurityAgreementName: String,
        val ProjectName: String,
        val SecurityValue: Int,
        val SecurityInterest: Int,
        val SecurityTrustee: Party,
        val SecurityAgreementOwner: Party

) : ContractState {
    override val participants  = listOf( SecurityTrustee)
//    override val participants: List<AbstractParty>
//    get() = listOf(Bank, SecurityTrustee, Offtaker)
}

// *****************
// * Contract Code *
// *****************

class SecurityAgreementContract : Contract {
    companion object {
        val ID: ContractClassName = SecurityAgreementContract::class.qualifiedName !!
    }
    override fun verify(tx: LedgerTransaction) {
        // TODO: Implement logic.
        /****UNCOMMENT**********
             val command = tx.commands.requireSingleCommand<SecurityAgreementContract.Commands>()
               val setOfSigners = command.signers.toSet()
               val inputSecurityAgreement = tx.inputsOfType<SecurityAgreementState>()
               val outputSecurityAgreement = tx.outputsOfType<SecurityAgreementState>().single()

               requireThat {

                   // General Constraints.
                   "No inputs should be consumed when issuing an SecurityAgreementState." using (inputSecurityAgreement.isEmpty())
                   "There should be one output state of type SecurityAgreementState." using (tx.outputsOfType<ProjectState>().size == 1)
                   "There must be two signers." using (setOfSigners.size == 2)
                   "The borrower and lender must be signers." using (command.signers.containsAll(listOf(
                           outputSecurityAgreement.SecurityAgreementOwner.owningKey, outputSecurityAgreement.SecurityTrustee.owningKey)))

               }


               when (command.value) {
                   is SecurityAgreementContract.Commands.CreateSecurityAgreement -> {
                       "Please provide ProjectName" using (inputSecurityAgreement.single().ProjectName.isEmpty())
                       "Owner and Receiver will be different" using (outputSecurityAgreement.SecurityAgreementOwner != outputSecurityAgreement.SecurityTrustee)
                   }

               }
        ****UNCOMMENT***********/
    }
    interface Commands: CommandData {
        class CreateSecurityAgreement: Commands
        // class GetSecurityAgreements: Commands
    }
}

// This is used to identify our contract when building a transaction
class ProjectContract : Contract {
    companion object {
        //        val ID = "com.template.ProjectContract"
//        val ID = "net.cordaclub.itsu.ProjectContract"
        val ID: ContractClassName = ProjectContract::class.qualifiedName !!
    }

    override fun verify(tx: LedgerTransaction) {

        // TODO: Implement logic.
        /****UNCOMMENT**********

          // Business Rules
            val command = tx.commands.requireSingleCommand<Commands>()
            val setOfSigners = command.signers.toSet()
            val inputProject = tx.inputsOfType<ProjectState>()
            val outputProject = tx.outputsOfType<ProjectState>().single()

            requireThat {

                // General Constraints.
                "No inputs should be consumed when issuing an ProjectState." using (inputProject.isEmpty())
                "There should be one output state of type ProjectState." using (tx.outputsOfType<ProjectState>().size == 1)
                "There must be two signers." using (setOfSigners.size == 2)

            }
          when (command.value) {
                is Commands.CreateProject -> {
                    "Create Project needs inputs." using (inputProject.single().ProjectName.isEmpty())
                    "The Project value should be greater than ZERO." using (outputProject.ProjectValue > 0)
                    "ProjectValue will be greater than EstimatedProjectCost" using (outputProject.ProjectValue >= outputProject.EstimatedProjectCost)
                }
                is Commands.CloseProject -> {
                    "Close Project needs Project Name." using (inputProject.single().ProjectStatus == ProjectStatus.COMPLETE)
                    "The Project Cost Till Date is non-ZERO" using (outputProject.ProjectCostTillDate > 0)
                }

         *********************/

/*******DELETE**********
val command = tx.commands.requireSingleCommand<ProjectCommand>()
        val setOfSigners = command.signers.toSet()
        val inputProjects = tx.inputsOfType<ProjectState>()
        val outputProject = tx.outputsOfType<ProjectState>().single()
        requireThat {
            inputProjects.singleOrNull()?.let {
//                "The same Project" using (it.Project.ProjectName == outputProject.Project.ProjectName)
//                "The same EstimatedProjectCost" using (it.Project.EstimatedProjectCost == outputProject.Project.EstimatedProjectCost)
//                "The same ProjectValue" using (it.Project.ProjectValue == outputProject.Project.ProjectValue)
                "The same Project" using (it.ProjectName == outputProject.ProjectName)
                "The same EstimatedProjectCost" using (it.EstimatedProjectCost == outputProject.EstimatedProjectCost)
                "The same ProjectValue" using (it.ProjectValue == outputProject.ProjectValue)
//                "The same linearId" using (it.linearId == outputProject.linearId)
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
                    "The Security Trustee should be the owner of the Security Agreement" using (outputProject.ProjectOwner.owningKey == outputProject.SecurityTrustee.owningKey)
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
                    "The Bank should be the owner of the Security Agreement." using (outputProject.ProjectOwner.owningKey == outputProject.Bank.owningKey)
                }
                is ProjectCommand.DeclareProjectSuccess -> {
                    "The Project input status should be CLOSED." using (inputProjects.single().ProjectStatus == ProjectStatus.CLOSED)
                    "The Project output status should be CLOSED_SUCCESS." using (outputProject.ProjectStatus == ProjectStatus.CLOSED_SUCCESS)
                    "The Project Cost To Date will be less than or equal to Estimated Project Cost." using (outputProject.ProjectCostToDate <= outputProject.EstimatedProjectCost)
                    "The Offtaker should be the owner of the Security Agreement." using (outputProject.ProjectOwner.owningKey == outputProject.Offtaker.owningKey)
                }
            }
 TODO Implement Logic*/

        /*
        when (command.value) {
            is ProjectCommand.CreateProject -> {
                "No inputs should be consumed when creating a Project." using (inputProjects.isEmpty())
                "The Project status should be STARTED." using (outputProject.ProjectStatus == ProjectStatus.STARTED)
                "The Project value should be greater than ZERO." using (outputProject.Project.ProjectValue > 0)
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
                "The Project Cost To Date should be non-ZERO" using (outputProject.Project.ProjectCostToDate > 0)
            }
            is ProjectCommand.DeclareProjectFailure -> {
                "The Project input status should be CLOSED." using (inputProjects.single().ProjectStatus == ProjectStatus.CLOSED)
                "The Project output status should be CLOSED_FAILURE." using (outputProject.ProjectStatus == ProjectStatus.CLOSED_FAILURE)
                "The Project Cost To Date will be greater than Estimated Project Cost." using (outputProject.Project.ProjectCostToDate > outputProject.Project.EstimatedProjectCost)
                "The Bank should be the owner of the Security Agreement." using (outputProject.SecurityAgreement.SecurityAgreementOwner.owningKey == outputProject.Bank.owningKey)
            }
            is ProjectCommand.DeclareProjectSuccess -> {
                "The Project input status should be CLOSED." using (inputProjects.single().ProjectStatus == ProjectStatus.CLOSED)
                "The Project output status should be CLOSED_SUCCESS." using (outputProject.ProjectStatus == ProjectStatus.CLOSED_SUCCESS)
                "The Project Cost To Date will be less than or equal to Estimated Project Cost." using (outputProject.Project.ProjectCostToDate <= outputProject.Project.EstimatedProjectCost)
                "The Offtaker should be the owner of the Security Agreement." using (outputProject.SecurityAgreement.SecurityAgreementOwner.owningKey == outputProject.Offtaker.owningKey)
            }
        }
    }
        */

    }

    interface Commands: CommandData {
        class CreateProject: Commands
        class CloseProject: Commands
        class CloseProjectAdvanced: Commands
        class DeclareProjectSuccess: Commands
        class DeclareProjectFailure: Commands
    }

}