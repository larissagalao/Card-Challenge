package com.github.larissagalao.CardChallenge.controller;

import com.github.larissagalao.CardChallenge.model.entity.Deck;
import com.github.larissagalao.CardChallenge.model.repository.GameRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

@Controller
public class GameController {

    private Deck deck;

    private Boolean started = false;

    @Autowired
    GameRepository gameRepository;

    public Deck startTheGame(){

        deck = new Deck();

        String url = "https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=1";
        RestTemplate restTemplate = new RestTemplate();
        String obj = restTemplate.getForObject(url, String.class);

        deck.setDeck_id(StringToJsonObject(obj).get("deck_id").toString());
        deck.setRemaining(StringToJsonObject(obj).get("remaining").toString());

        started = true;

        return deck;
    }

    public JSONObject StringToJsonObject(String s){
        JSONObject jsonObject = new JSONObject(s);
        return jsonObject;
    }




}
