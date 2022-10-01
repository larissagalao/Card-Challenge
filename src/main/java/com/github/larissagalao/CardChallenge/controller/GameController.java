package com.github.larissagalao.CardChallenge.controller;

import com.github.larissagalao.CardChallenge.model.entity.Deck;
import com.github.larissagalao.CardChallenge.model.repository.GameRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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

    public String dropCards() {
        if (started == true) {

            String deckId = deck.getDeck_id();

            List<Object> list = new ArrayList<Object>();


            String url = "https://deckofcardsapi.com/api/deck/" + deckId + "/draw/?count=20";
            RestTemplate restTemplate = new RestTemplate();
            String objects = restTemplate.getForObject(url, String.class);


            String cardsString = StringToJsonObject(objects).toString();

            deck.setRemaining(StringToJsonObject(objects).get("remaining").toString());

            if(Integer.parseInt(deck.getRemaining()) <= 32) {
                url = "https://deckofcardsapi.com/api/deck/" + deckId + "/shuffle/";
                objects = restTemplate.getForObject(url, String.class);
                deck.setRemaining(StringToJsonObject(objects).get("remaining").toString());
            }

            return cardsString;

        } else {

            startTheGame();
            return dropCards();
        }
    }


    public JSONObject StringToJsonObject(String s){
        JSONObject jsonObject = new JSONObject(s);
        return jsonObject;
    }




}
