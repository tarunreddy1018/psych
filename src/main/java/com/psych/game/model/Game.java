package com.psych.game.model;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.psych.game.Utils;
import com.psych.game.exceptions.InvalidActionForGameStateException;
import com.psych.game.exceptions.InvalidInputException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="games")
public class Game extends Auditable {
    @Getter
    @Setter
    @NotNull
    private int numRounds;

    @Getter
    @Setter
    private boolean hasEllen = false;

    // Table Name: games_players_stats
    // Column Names: game_id, player_id, stats_id
    @ManyToMany(cascade = CascadeType.ALL)
    @Getter
    @Setter
    private Map<Player, Stats> playerStats = new HashMap<>();

    @ManyToMany
    @JsonIdentityReference
    @Getter
    @Setter
    private List<Player> players = new ArrayList<>();

    @NotNull
    @Getter
    @Setter
    private GameMode gameMode;

    @Getter
    @Setter
    private GameStatus gameStatus = GameStatus.JOINING;

    @ManyToOne
    @NotNull
    @Getter
    @Setter
    @JsonIdentityReference
    private Player leader;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @JsonManagedReference
    @Getter
    @Setter
    private List<Round> rounds = new ArrayList<>();

    public Game() {
    }

    private Game(Builder builder) {
        setNumRounds(builder.numRounds);
        setHasEllen(builder.hasEllen);
        setGameMode(builder.gameMode);
        setLeader(builder.leader);
        try {
            addPlayer(leader);
        }
        catch(InvalidActionForGameStateException ignored) {}
    }

    public boolean hasPlayer(Player player) {
        return playerStats.containsKey(player);
    }

    public void addPlayer(Player player) throws InvalidActionForGameStateException {
        if(!gameStatus.equals(GameStatus.JOINING)) {
            throw new InvalidActionForGameStateException("Cannot join game because it has already started");
        }
        if(playerStats.containsKey(player)) {
            return;
        }
        players.add(player);
        playerStats.put(player, new Stats());
    }

    private void startNewRound() {
        Round round = new Round(this, Utils.getRandomQuestion(), 1);
        rounds.add(round);
        gameStatus = GameStatus.SUBMITTING_ANSWERS;
    }

    public void start() throws InvalidActionForGameStateException {
        if(!gameStatus.equals(GameStatus.JOINING)) {
            throw new InvalidActionForGameStateException("Game has already started");
        }
        startNewRound();
    }

    public void submitAnswer(Player player, String answer) throws InvalidActionForGameStateException {
        if(!gameStatus.equals(GameStatus.SUBMITTING_ANSWERS)) {
            throw new InvalidActionForGameStateException("Not Accepting any answers at the moment");
        }
        Round round = currentRound();
        round.submitAnswer(player, answer);
        if(round.getSubmittedAnswers().size() == players.size()) {
            gameStatus = GameStatus.SELECTING_ANSWERS;
        }
    }

    public Round currentRound() {
        return rounds.get(rounds.size()-1);
    }

    public void selectAnswer(Player player, PlayerAnswer playerAnswer) throws InvalidActionForGameStateException, InvalidInputException {
        if(!gameStatus.equals(GameStatus.SELECTING_ANSWERS)) {
            throw new InvalidActionForGameStateException("Not Selecting any answers at the moment");
        }
        Round round = currentRound();
        round.selectAnswer(player, playerAnswer);
        if(round.getSelectedAnswers().size() == players.size()) {
            if(rounds.size() < numRounds) {
                gameStatus = GameStatus.GETTING_READY;
            }
            else {
                gameStatus = GameStatus.OVER;
                // todo: update the stats of ellen
            }
        }
    }

    public void getReady(Player player) {
        Round round = currentRound();
        round.getReady(player);
        if(round.getReadyPlayers().size() == players.size()) {
            startNewRound();
        }
    }

    public String getState() {
    }

    public static final class Builder {
        @NotNull
        private int numRounds;

        private boolean hasEllen;

        @NotNull
        private GameMode gameMode;

        @NotNull
        private Player leader;

        public Builder() {

        }

        public Builder numRounds(@NotNull int val) {
            numRounds = val;
            return this;
        }

        public Builder hasEllen(boolean val) {
            hasEllen = val;
            return this;
        }

        public Builder gameMode(@NotNull GameMode val) {
            gameMode = val;
            return this;
        }

        public Builder leader(@NotNull Player val) {
            leader = val;
            return this;
        }

        public Game build() {
            return new Game(this);
        }
    }
}
