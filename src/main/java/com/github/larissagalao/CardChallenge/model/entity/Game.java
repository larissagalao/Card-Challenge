package com.github.larissagalao.CardChallenge.model.entity;

import javax.persistence.*;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String playerOneCards;

    @Column
    private String playerTwoCards;

    @Column
    private String playerThreeCards;

    @Column
    private String playerFourCards;

    @Column
    private Integer winnerScore;

    @Column String winner;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getPlayerOneCards() {
        return playerOneCards;
    }

    public void setPlayerOneCards(String playerOneCards) {
        this.playerOneCards = playerOneCards;
    }

    public String getPlayerTwoCards() {
        return playerTwoCards;
    }

    public void setPlayerTwoCards(String playerTwoCards) {
        this.playerTwoCards = playerTwoCards;
    }

    public String getPlayerThreeCards() {
        return playerThreeCards;
    }

    public void setPlayerThreeCards(String playerThreeCards) {
        this.playerThreeCards = playerThreeCards;
    }

    public String getPlayerFourCards() {
        return playerFourCards;
    }

    public void setPlayerFourCards(String playerFourCards) {
        this.playerFourCards = playerFourCards;
    }

    public Integer getWinnerScore() {
        return winnerScore;
    }

    public void setWinnerScore(Integer winnerScore) {
        this.winnerScore = winnerScore;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}
