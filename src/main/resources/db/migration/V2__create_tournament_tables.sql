CREATE TABLE tournament
(
    id   INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL UNIQUE
);


CREATE TABLE player
(
    id   INTEGER PRIMARY KEY,
    account VARCHAR NOT NULL UNIQUE
);

CREATE TABLE tournament_player
(
    tournament_id   INT REFERENCES tournament (id),
    player_id INT REFERENCES player (id),
    cumulated_point INT

);
