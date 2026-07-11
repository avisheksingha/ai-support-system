package com.aisupport.orchestration.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aisupport.orchestration.infrastructure.persistence.entity.ConversationEventEntity;

@Repository
public interface ConversationEventRepository extends JpaRepository<ConversationEventEntity, String> {
    List<ConversationEventEntity> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}
