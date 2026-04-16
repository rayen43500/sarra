package org.example.backend.service;

import org.example.backend.domain.entity.Rating;
import org.example.backend.domain.entity.User;
import org.example.backend.repository.RatingRepository;
import org.example.backend.web.dto.rating.RatingDto;
import org.example.backend.web.dto.rating.RatingRequest;
import org.example.backend.web.dto.rating.RatingSummaryDto;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final CurrentUserService currentUserService;
    private final BadWordsFilterService badWordsFilterService;

    public RatingService(
            RatingRepository ratingRepository,
            CurrentUserService currentUserService,
            BadWordsFilterService badWordsFilterService
    ) {
        this.ratingRepository = ratingRepository;
        this.currentUserService = currentUserService;
        this.badWordsFilterService = badWordsFilterService;
    }

    public RatingDto rate(Principal principal, RatingRequest request) {
        User user = currentUserService.require(principal);
        String normalizedType = request.targetType().toUpperCase();

        Rating rating = ratingRepository
                .findByUserIdAndTargetTypeAndTargetId(user.getId(), normalizedType, request.targetId())
                .orElseGet(Rating::new);

        rating.setUser(user);
        rating.setTargetType(normalizedType);
        rating.setTargetId(request.targetId());
        rating.setScore(request.score());
        rating.setComment(badWordsFilterService.sanitize(request.comment()));

        return toDto(ratingRepository.save(rating));
    }

    public RatingSummaryDto summary(String targetType, Long targetId) {
        String normalizedType = targetType.toUpperCase();
        double average = ratingRepository.averageByTarget(normalizedType, targetId);
        long total = ratingRepository.countByTargetTypeAndTargetId(normalizedType, targetId);
        List<RatingDto> latest = ratingRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(normalizedType, targetId)
                .stream()
                .limit(20)
                .map(this::toDto)
                .toList();

        return new RatingSummaryDto(normalizedType, targetId, average, total, latest);
    }

    private RatingDto toDto(Rating rating) {
        return new RatingDto(
                rating.getId(),
                rating.getUser().getId(),
                rating.getTargetType(),
                rating.getTargetId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }
}
