package com.shimmy.gameofthree.server.application;

import com.shimmy.gameofthree.server.domain.Player;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends CrudRepository<Player, String> {
    List<Player> findByIsLookingForGameTrue();
}
