CREATE SEQUENCE hibernate_sequence;

CREATE TABLE player (
  id          VARCHAR(15) PRIMARY KEY,
  created_at  TIMESTAMP     NOT NULL,
  updated_at  TIMESTAMP     NOT NULL,
  name        VARCHAR(128)  NOT NULL
);

CREATE TABLE game (
  id          VARCHAR(15) PRIMARY KEY,
  created_at  TIMESTAMP     NOT NULL,
  updated_at  TIMESTAMP     NOT NULL,
  black       VARCHAR(128)  NOT NULL references player,
  white       VARCHAR(128)  NOT NULL references player,
  moves       VARCHAR(65535) NOT NULL
);
