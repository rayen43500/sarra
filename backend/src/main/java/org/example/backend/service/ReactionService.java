package org.example.backend.service;

import org.example.backend.domain.entity.Reaction;
import org.example.backend.domain.entity.User;
import org.example.backend.repository.ReactionRepository;
import org.example.backend.web.dto.reaction.ReactionRequest;
import org.example.backend.web.dto.reaction.ReactionSummaryDto;
import java.security.Principal;
import org.springframework.stereotype.Service;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final CurrentUserService currentUserService;

    public ReactionService(ReactionRepository reactionRepository, CurrentUserService currentUserService) {
        this.reactionRepository = reactionRepository;
        this.currentUserService = currentUserService;
    }

    public ReactionSummaryDto react(Principal principal, ReactionRequest request) {
        User user = currentUserService.require(principal);

        Reaction reaction = reactionRepository
                .findByUserIdAndTargetTypeAndTargetId(user.getId(), request.targetType(), request.targetId())
                .orElseGet(Reaction::new);

        reaction.setUser(user);
        reaction.setTargetType(request.targetType().toUpperCase());
        reaction.setTargetId(request.targetId());
        reaction.setLiked(request.liked());
        reactionRepository.save(reaction);

        return summary(principal, request.targetType(), request.targetId());
    }

    public ReactionSummaryDto summary(Principal principal, String targetType, Long targetId) {
        User user = currentUserService.require(principal);
        String normalizedType = targetType.toUpperCase();

        long likes = reactionRepository.countByTargetTypeAndTargetIdAndLikedIsTrue(normalizedType, targetId);
        long dislikes = reactionRepository.countByTargetTypeAndTargetIdAndLikedIsFalse(normalizedType, targetId);

        Boolean myReaction = reactionRepository
                .findByUserIdAndTargetTypeAndTargetId(user.getId(), normalizedType, targetId)
                .map(Reaction::getLiked)
                .orElse(null);

        return new ReactionSummaryDto(normalizedType, targetId, likes, dislikes, myReaction);
    }
}
