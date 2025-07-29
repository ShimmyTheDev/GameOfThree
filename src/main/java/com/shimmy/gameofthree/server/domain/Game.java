package com.shimmy.gameofthree.server.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Game {
    @Id
    private String id;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "game_players",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private List<Player> players;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_player_id")
    private Player currentPlayer;
    private int currentNumber;
    private GameStatus status = GameStatus.WAITING_FOR_PLAYERS;
    private Instant lastUpdated;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    public enum GameStatus {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        COMPLETED
    }
}
