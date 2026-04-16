package org.example.backend.repository;

import org.example.backend.domain.entity.Rating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    List<Rating> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);

    @Query("select coalesce(avg(r.score), 0) from Rating r where r.targetType = :targetType and r.targetId = :targetId")
    Double averageByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}
