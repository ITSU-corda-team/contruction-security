package net.cordaclub.itsu

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.Requirements.using
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class ProjectStatus {
    STARTED, COMPLETE
}

enum class ProjectCompleteStatus {
    NULL, COMPLETE_SUCCESS,  COMPLETE_FAILURE
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
        val ProjectCostTillDate: Int,
        val ProjectCashFlow: Int,
        val SPV: Party,
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
        val SecurityAgreementOwner: Party,
        val SecurityTrustee: Party) : ContractState {
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
    interface Commands: CommandData {
        class CreateSecurityAgreement: Commands
    }
    override fun verify(tx: LedgerTransaction) {

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
    }
}

// This is used to identify our contract when building a transaction
class ProjectContract : Contract {
    companion object {
//        val ID = "com.template.ProjectContract"
        val ID = "net.cordaclub.itsu.ProjectContract"
//        val ID: ContractClassName = ProjectContract::class.qualifiedName !!
    }

    interface Commands: CommandData {
        class CreateProject: Commands
        class CloseProject: Commands
        class DeclareProjectSuccess: Commands
        class DeclareProjectFailure: Commands
    }

    override fun verify(tx: LedgerTransaction) {

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
            /*
            is Commands.DeclareProjectFailure -> {
            "The Project input status should be COMPLETE." using (inputProjects.single().ProjectStatus == ProjectStatus.COMPLETE)
            "The Project output status should be CLOSED_FAILURE." using (outputProject.ProjectStatus == ProjectCompleteStatus.COMPLETE_FAILURE)
            "The Project Cost To Date will be greater than Estimated Project Cost." using (outputProject.ProjectCostToDate > outputProject.EstimatedProjectCost)
            "The Bank should be the owner of the Security Agreement." using (outputProject.SPV.owningKey == outputProject.Bank.owningKey)

        }
            is Commands.DeclareProjectSuccess -> {
                "The Project input status should be CLOSED." using (inputProjects.single().ProjectStatus == ProjectStatus.COMPLETE)
                "The Project output status should be CLOSED_SUCCESS." using (outputProject.ProjectStatus == ProjectCompleteStatus.COMPLETE_SUCCESS)
                "The Project Cost To Date will be less than or equal to Estimated Project Cost." using (outputProject.ProjectCostToDate <= outputProject.EstimatedProjectCost)
                "The Offtaker should be the owner of the Security Agreement." using (outputProject.SPV.owningKey == outputProject.Offtaker.owningKey)

            }

            */
        }


    }
}
