package com.template

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction

//Added from corda-template-kotlin
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

//import java.sql.Date
import java.text.DateFormat

// *****************
// * Contract Code *
// *****************
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

// *********
// * State *
// *********

/* ygk
class IOUState(val value: Int,
               val lender: Party,
               val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}
ygk */

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

//Security Agreement signed between Lender and the Security Trustee. Attributes are
//Value
//AgreementDate
//Security Trustee Name
//SPV Name
class SecurityAgreementState(val value: Int,
                         val dateFormat: DateFormat,
                         val lender: Party,
                         val borrower: Party) : ContractState {
    override val participants get() = listOf(lender, borrower)
}

data class ProjectState(
        val name: String,
        val value: Int,
        val owner: Party,
        val securityTrustee: Party,
        val bank: Party,
        val offtaker: Party) : ContractState {
    override val participants = listOf(securityTrustee, bank, offtaker)
}

class ProjectContract : Contract {
    companion object {
        val ID = "com.template.ProjectContract"
    }

    override fun verify(tx: LedgerTransaction) {
        // TODO: Implement logic.
    }

    interface Commands: CommandData {
        class Create: Commands
        class DeclareBankruptcy: Commands
    }
}
