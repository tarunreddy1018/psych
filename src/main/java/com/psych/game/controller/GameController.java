package com.psych.game.controller;

import com.psych.game.model.Game;
import com.psych.game.model.Player;
import com.psych.game.repository.GameRepository;
import com.psych.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dev")
public class GameController {
    @Autowired
    private GameRepository gameRepository;

    @GetMapping("/games")
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    @GetMapping("/games/{id}")
    public Game getGameById(@PathVariable(value="id") Long id) throws Exception {
        return gameRepository.findById(id).orElseThrow(Exception::new);
    }
}
