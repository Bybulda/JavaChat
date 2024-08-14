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