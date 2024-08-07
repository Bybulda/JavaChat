DROP TABLE IF EXISTS public.cipher_info;

CREATE TABLE IF NOT EXISTS public.cipher_info (
                                                   id serial NOT NULL PRIMARY KEY,
                                                   algorithm text NOT NULL,
                                                   mode text NOT NULL,
                                                   padding text NOT NULL,
                                                   key_size_bits int NOT NULL,
                                                   block_size_bits int NOT NULL,
                                                   iv bytea NOT NULL
);

DROP TABLE IF EXISTS public.rooms_info;

CREATE TABLE IF NOT EXISTS public.rooms_info (
                                                          id serial NOT NULL PRIMARY KEY,
                                                          left_user bigint NOT NULL,
                                                          right_user bigint NOT NULL,
                                                          title_left text NOT NULL,
                                                          title_right text NOT NULL,
                                                          cipher_info_id bigint NOT NULL,
                                                          p bytea NOT NULL,
                                                          g bytea NOT NULL
);

DROP TABLE IF EXISTS public.user_info;

CREATE TABLE IF NOT EXISTS public.user_info (
                                                       id serial NOT NULL PRIMARY KEY,
                                                       user_name text NOT NULL,
                                                       password text NOT NULL,
);