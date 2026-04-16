package org.example.backend.repository;

import org.example.backend.domain.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            select m
            from Message m
            where (m.fromUser.id = :userId and m.toUser.id = :otherUserId)
               or (m.fromUser.id = :otherUserId and m.toUser.id = :userId)
            order by m.createdAt asc
            """)
    List<Message> findConversation(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
}
