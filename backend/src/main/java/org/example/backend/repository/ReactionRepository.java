package org.example.backend.repository;

import org.example.backend.domain.entity.Reaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long countByTargetTypeAndTargetIdAndLikedIsTrue(String targetType, Long targetId);
    long countByTargetTypeAndTargetIdAndLikedIsFalse(String targetType, Long targetId);
}
