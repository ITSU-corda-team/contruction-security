package com.example.contract;

import com.example.state.SecurityAgreementState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/**
 * A implementation of a basic smart contract in Corda.
 *
 * This contract enforces rules regarding the creation of a valid [SecurityAgreementState], which in turn encapsulates an [SecurityAgreement].
 *
 * For a new [SecurityAgreement] to be issued onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [SecurityAgreement].
 * - An Create() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
public class SecurityAgreementContract implements Contract {
    public static final String SECURITY_AGREEMENT_CONTRACT_ID = "com.example.contract.SecurityAgreementContract";

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands.Create> command = requireSingleCommand(tx.getCommands(), Commands.Create.class);
        requireThat(require -> {
            require.using("No inputs should be consumed when issuing an security agreement.",
                    tx.getInputs().isEmpty());
            require.using("Only one output state should be created.",
                    tx.getOutputs().size() == 1);
            final SecurityAgreementState out = tx.outputsOfType(SecurityAgreementState.class).get(0);
            require.using("The lender and the spv cannot be the same entity.",
                    out.getLender() != out.getSPV());
            require.using("All of the participants must be signers.",
                    command.getSigners().containsAll(out.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())));

            // IOU-specific constraints.
            require.using("The IOU's value must be non-negative.",
                    out.getValue() > 0);

            return null;
        });
    }

    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}