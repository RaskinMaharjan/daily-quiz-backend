package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSeasonRepository extends JpaRepository<QuizSeason, Long> {
    Optional<QuizSeason> findFirstByActiveTrue();

    boolean existsByTitle(String title);

    boolean existsByTitleAndActiveTrue(String title);

    int countAllByTitleNotNull();

    List<QuizSeason> findTop10ByOrderByIdDesc();
}
