package com.github.larissagalao.CardChallenge.controller;

import com.github.larissagalao.CardChallenge.model.entity.Deck;
import com.github.larissagalao.CardChallenge.model.repository.GameRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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

    public List<Integer> getValues(String cardsString){

        List<Integer> values = new ArrayList<Integer>();

        cardsString= cardsString.substring(cardsString.indexOf("["));
        cardsString = cardsString.substring(1, cardsString.indexOf("]"));

        String s3 = cardsString.replace("},{", "};{");
        s3 = s3.replace("ACE", "1");
        s3 = s3.replace("JACK", "11");
        s3 = s3.replace("QUEEN", "12");
        s3 = s3.replace("KING", "13");
        String[] json = s3.split(";");

        for(int i = 0; i< 20; i++){
            Object obj = StringToJsonObject(json[i]).get("value");
            values.add(Integer.parseInt(obj.toString()));
        }
        return values;
    }

    public Map<String, List<Integer>> separatedCards(List<Integer> cards){

        Map<String, List<Integer>> mapCards = new HashMap<String, List<Integer>>();


        mapCards.put("Player One", cards.subList(0,5));
        mapCards.put("Player Two", cards.subList(5,10));
        mapCards.put("Player Three", cards.subList(10,15));
        mapCards.put("Player Four", cards.subList(15,20));

        return mapCards;

    }

    public List<String> getCards(List<Integer> value){

        List<String> cards = new ArrayList<String>();

        for(int i = 0; i < value.size(); i++){
            cards.add(value.get(i).toString());
        }

        Collections.replaceAll(cards, "1", "A");
        Collections.replaceAll(cards, "11", "J");
        Collections.replaceAll(cards, "12", "Q");
        Collections.replaceAll(cards, "13", "K");

        return cards;
    }

    public JSONObject StringToJsonObject(String s){
        JSONObject jsonObject = new JSONObject(s);
        return jsonObject;
    }

    @GetMapping()
    public String Restart(){

        started = false;
        gameRepository.deleteAll();

        return "restartButton";

    }




}
