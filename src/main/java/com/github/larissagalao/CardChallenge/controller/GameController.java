package com.github.larissagalao.CardChallenge.controller;

import com.github.larissagalao.CardChallenge.model.entity.Deck;
import com.github.larissagalao.CardChallenge.model.entity.Game;
import com.github.larissagalao.CardChallenge.model.repository.GameRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class GameController {

    private Deck deck;

    @Autowired
    GameRepository gameRepository;

    public Deck startTheGame(){

        deck = new Deck();

        String url = "https://deckofcardsapi.com/api/deck/new/shuffle/?deck_count=1";
        RestTemplate restTemplate = new RestTemplate();
        String obj = restTemplate.getForObject(url, String.class);

        deck.setDeck_id(StringToJsonObject(obj).get("deck_id").toString());
        deck.setRemaining(StringToJsonObject(obj).get("remaining").toString());
        deck.setStarted(true);

        return deck;
    }

    public String dropCards() {
        if (deck.getStarted() == true) {

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

        try{
            deck.setStarted(false);
            gameRepository.deleteAll();
        }catch (NullPointerException e){
            startTheGame();
        }

        return "restartButton";

    }

    public Map<String, Integer> winner(Map<String, List<Integer>> map) {

        Map<String, Integer> winnerMap = new HashMap<String, Integer>();

        String winner = null;
        Integer winnerScore = 0;
        Boolean tie = false;
        Integer score;

        for (String player : map.keySet()) {
            score = 0;
            for (int i = 0; i < map.get(player).size(); i++) {
                score += map.get(player).get(i);
            }
            if (score > winnerScore) {
                winner = player;
                winnerScore = score;
                tie = false;
            } else if (score == winnerScore) {
                tie = true;
                winnerScore = score;
            }
        }

        if(tie == true){
            winnerMap.put("Tie", winnerScore);
            return winnerMap;
        }else{
            winnerMap.put(winner, winnerScore);
            return winnerMap;
        }
    }

    public String winnerFormat(Map<String, Integer> winner){

        Integer score = 0;
        String playerName = null;

        for(String player : winner.keySet()){
            score = winner.get(player);
            playerName = player;
        }

        if(playerName != "Tie"){
            return "Winner: " + playerName + " | Score: " + score;
        }else{
            return playerName + " | Score: " + score;
        }
    }

    @GetMapping("/ui")
    public String UI(Model model){

        String s = dropCards();
        Map<String, List<Integer>> map = separatedCards(getValues(s));

        saveGame(map, winner(map));


        model.addAttribute("playerOne", getCards(map.get("Player One")));
        model.addAttribute("playerTwo", getCards(map.get("Player Two")));
        model.addAttribute("playerThree", getCards(map.get("Player Three")));
        model.addAttribute("playerFour", getCards(map.get("Player Four")));
        model.addAttribute("winnerFormat", winnerFormat(winner(map)));

        return "uiFile";
    }

    @GetMapping("/winnerTable")
    public String winnerTable(Model model){

        model.addAttribute("playerOne", mapTable().get("Player One"));
        model.addAttribute("playerTwo", mapTable().get("Player Two"));
        model.addAttribute("playerThree", mapTable().get("Player Three"));
        model.addAttribute("playerFour", mapTable().get("Player Four"));

        return  "winnerTable";

    }

    public Map<String, Integer> mapTable(){


        Map<String, Integer> map = new HashMap<String, Integer>();

        map.put("Player One", gameRepository.table("Player One"));
        map.put("Player Two", gameRepository.table("Player Two"));
        map.put("Player Three", gameRepository.table("Player Three"));
        map.put("Player Four", gameRepository.table("Player Four"));


        return map;
    }

    @PostMapping
    public Game saveGame(Map<String, List<Integer>> cards, Map<String, Integer> win){

        try{

            Game round = new Game();
            Integer score = 0;
            String playerName = null;

            for(String player : win.keySet()){
                score = win.get(player);
                playerName = player;
            }

            round.setPlayerOneCards(cards.get("Player One").toString());
            round.setPlayerTwoCards(cards.get("Player Two").toString());
            round.setPlayerThreeCards(cards.get("Player Three").toString());
            round.setPlayerFourCards(cards.get("Player Four").toString());
            round.setWinner(playerName);
            round.setWinnerScore(score);

            return gameRepository.save(round);

        }catch (Exception e){

            return null;

        }
    }


}
