package com.shimmy.gameofthree.server.domain.event;


import lombok.Value;

@Value
public class GameEvent<T> {
    String id;
    String gameId;
    String type;
    T data;
}
