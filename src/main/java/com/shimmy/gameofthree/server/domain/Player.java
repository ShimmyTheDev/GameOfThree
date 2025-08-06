package com.shimmy.gameofthree.server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "player")
public class Player {
    @Id
    private String id;
    private String name;
    private Boolean isLookingForGame;

    public Player(String name, Boolean isLookingForGame) {
        this.name = name;
        this.isLookingForGame = isLookingForGame;
    }

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
