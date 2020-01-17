package com.psych.game.controller;

import com.psych.game.Utils;
import com.psych.game.exceptions.IllegalGameException;
import com.psych.game.exceptions.InsufficientPlayersException;
import com.psych.game.exceptions.InvalidActionForGameStateException;
import com.psych.game.exceptions.InvalidInputException;
import com.psych.game.model.*;
import com.psych.game.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/play")
public class PlayEndpoint {
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private RoundRepository roundRepository;
    @Autowired
    private PlayerAnswerRepository playerAnswerRepository;

    @GetMapping("/create-game/{pid}/{gm}/{nr}")
    public String createGame(@PathVariable(value = "pid") Long playerId,
                             @PathVariable(value = "gm") int gameMode,
                             @PathVariable(value = "nr") int numRounds,
                             @PathVariable(value = "ellen") int hasEllen) {
        Player player = playerRepository.findById(playerId).orElseThrow();
        GameMode mode = GameMode.fromValue(gameMode);

        Game game = new Game.Builder()
                .hasEllen(hasEllen == 1)
                .numRounds(numRounds)
                .gameMode(mode)
                .leader(player)
                .build();

        gameRepository.save(game);

        return "Created game: " + game.getId() + "- Code: " + Utils.getSecretCodeFromId(game.getId());
    }

    @GetMapping("/join-game/{pid}/{gc}")
    public String joinGame(@PathVariable(value = "pid") Long playerId,
                           @PathVariable(value = "gc") String gameCode) {
        Game game = gameRepository.findById(Utils.getGameIdFromSecretCode(gameCode)).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();

        try {
            game.addPlayer(player);
        }
        catch(InvalidActionForGameStateException ignore) {}

        gameRepository.save(game);
        return "Joined Game";
    }

    @GetMapping("/start-game/{pid}/{gid}")
    public String startGame(@PathVariable(value = "pid") Long playerId,
                           @PathVariable(value = "gid") Long gameId) throws InvalidActionForGameStateException, IllegalGameException, InsufficientPlayersException {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();

        if(!game.getLeader().equals(player)) {
            throw new IllegalGameException("Player hasn't joined any such game");
        }
        if(game.getPlayers().size() < 2) {
            throw new InsufficientPlayersException("Cannot start a game without any friends");
        }
        game.start();
        gameRepository.save(game);
        return "Game Started";
    }

    @GetMapping("/submit-answer/{pid}/{gid}/{answer}")
    public String submitAnswer(@PathVariable(value = "pid") Long playerId,
                               @PathVariable(value = "gid") Long gameId,
                               @PathVariable(value = "answer") String answer) throws InvalidActionForGameStateException, IllegalGameException, InsufficientPlayersException {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();
        if(!game.hasPlayer(player)) {
            throw new IllegalGameException("Player has not joined the game yet");
        }
        game.submitAnswer(player, answer);
        gameRepository.save(game);
        return "Submitted Answer";
    }

    @GetMapping("/select-answer/{pid}/{gid}/{paid}")
    public String selectAnswer(@PathVariable(value = "pid") Long playerId,
                               @PathVariable(value = "gid") Long gameId,
                               @PathVariable(value = "paid") Long playerAnswerId) throws InvalidActionForGameStateException, IllegalGameException, InsufficientPlayersException, InvalidInputException {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();
        PlayerAnswer playerAnswer = playerAnswerRepository.findById(playerAnswerId).orElseThrow();
        if(!game.hasPlayer(player)) {
            throw new IllegalGameException("Player has not joined the game yet");
        }
        game.selectAnswer(player, playerAnswer);
        gameRepository.save(game);
        return "Selected Answer";
    }

    @GetMapping("/get-ready/{pid}/{gid}")
    public String getReady(@PathVariable(value = "pid") Long playerId,
                           @PathVariable(value = "gid") Long gameId) throws InvalidActionForGameStateException, IllegalGameException, InsufficientPlayersException, InvalidInputException {
        Game game = gameRepository.findById(gameId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();
        if(!game.hasPlayer(player)) {
            throw new IllegalGameException("Player has not joined the game yet");
        }
        game.getReady(player);
        gameRepository.save(game);
        return "Get Ready";
    }

    @GetMapping("/game-state/{gid}")
    public String getGameState(@PathVariable(value = "gid") Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        return game.getState();
    }

    // end game --> pid, gid
    // make sure your that your the leader of the game

    // leave game --> pid, gid
    // update player's stats
}