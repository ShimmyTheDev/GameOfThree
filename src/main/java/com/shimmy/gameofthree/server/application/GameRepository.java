package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.Game;
import com.shimmy.gameofthree.server.domain.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends CrudRepository<Game, String> {

    Optional<Game> findByPlayersContaining(Player player);
}
