package logic_core.infrastructure.persistence.entity.conversation;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import logic_core.infrastructure.persistence.base.MutableEntity;
import logic_core.infrastructure.persistence.entity.conversationmember.ConversationMemberEntity;

/**
 * Spring Data JPA entity for the {@code conversations} table.
 * <p>
 * The table carries only the {@link MutableEntity} columns
 * ({@code id}, {@code created_at}, {@code updated_at}, {@code is_deleted}), so the
 * entity declares no additional business columns. Members are intentionally NOT
 * mapped as an inverse {@code @OneToMany} collection: membership is read through
 * {@link ConversationMemberEntity} queries ({@code ConversationMemberJpaRepository}),
 * which keeps model assembly free of lazy-collection surprises while preserving the
 * legacy query results.
 */
@Entity
@Table(name = "conversations")
public class ConversationEntity extends MutableEntity {
}
