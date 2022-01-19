CREATE TABLE IF NOT EXISTS users (
  id char(36) PRIMARY KEY NOT NULL,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  password VARCHAR(100) NOT NULL,
  create_time timestamp NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS index_user_name on users (name);
CREATE INDEX IF NOT EXISTS index_user_email ON users (email);

CREATE TABLE IF NOT EXISTS comment(
  id INTEGER AUTO_INCREMENT NOT NULL,
  content VARCHAR(300) NOT NULL,
  user_id char(36)  NOT NULL,
  create_time timestamp NOT NULL,
  update_time timestamp NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tree_path(
  parent INTEGER NOT NULL,
  child INTEGER NOT NULL,
  PRIMARY KEY (parent, child)
);
