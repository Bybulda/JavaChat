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