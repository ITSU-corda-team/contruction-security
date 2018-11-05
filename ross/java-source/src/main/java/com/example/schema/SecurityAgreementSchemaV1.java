package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * An SecurityAgreementState schema.
 */
public class SecurityAgreementSchemaV1 extends MappedSchema {
    public SecurityAgreementSchemaV1() {
        super(SecurityAgreementSchemaV1.class, 1, ImmutableList.of(PersistentSecurityAgreement.class));
    }

    @Entity
    @Table(name = "securityagreement_states")
    public static class PersistentSecurityAgreement extends PersistentState {
        @Column(name = "lender") private final String lender;
        @Column(name = "spv") private final String spv;
        @Column(name = "value") private final int value;
        @Column(name = "linear_id") private final UUID linearId;


        public PersistentSecurityAgreement(String lender, String spv, int value, UUID linearId) {
            this.lender = lender;
            this.spv = spv;
            this.value = value;
            this.linearId = linearId;
        }

        // Default constructor required by hibernate.
        public PersistentSecurityAgreement() {
            this.lender = null;
            this.spv = null;
            this.value = 0;
            this.linearId = null;
        }

        public String getLender() {
            return lender;
        }

        public String getSPV() {
            return spv;
        }

        public int getValue() {
            return value;
        }

        public UUID getId() {
            return linearId;
        }
    }
}