package com.example.state;

import com.example.schema.SecurityAgreementSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.Arrays;
import java.util.List;

/**
 * The state object recording security agreement agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 */
public class SecurityAgreementState implements LinearState, QueryableState {
    private final Integer value;
    private final Party lender;
    private final Party spv;
    private final UniqueIdentifier linearId;

    /**
     * @param value the value of the security agreement.
     * @param lender the party issuing the security agreement.
     * @param spv the party receiving and approving the security agreement.
     */
    public SecurityAgreementState(Integer value,
                    Party lender,
                    Party spv,
                    UniqueIdentifier linearId)
    {
        this.value = value;
        this.lender = lender;
        this.spv = spv;
        this.linearId = linearId;
    }

    public Integer getValue() { return value; }
    public Party getLender() { return lender; }
    public Party getSPV() { return spv; }
    @Override public UniqueIdentifier getLinearId() { return linearId; }
    @Override public List<AbstractParty> getParticipants() {
        return Arrays.asList(lender, spv);
    }

    @Override public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof SecurityAgreementSchemaV1) {
            return new SecurityAgreementSchemaV1.PersistentSecurityAgreement(
                    this.lender.getName().toString(),
                    this.spv.getName().toString(),
                    this.value,
                    this.linearId.getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @Override public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new SecurityAgreementSchemaV1());
    }

    @Override
    public String toString() {
        return String.format("SecurityAgreementState(value=%s, lender=%s, borrower=%s, linearId=%s)", value, lender, spv, linearId);
    }
}