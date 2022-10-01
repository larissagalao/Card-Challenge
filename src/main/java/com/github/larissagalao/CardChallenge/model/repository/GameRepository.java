package com.github.larissagalao.CardChallenge.model.repository;

import com.github.larissagalao.CardChallenge.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer>{

    @Query(value = "SELECT count(winner) FROM Game where winner=?1")
    Integer table(String s);

}
