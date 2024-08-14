DROP TABLE IF EXISTS public.user_info;

CREATE TABLE IF NOT EXISTS public.user_info (
                                                id serial NOT NULL PRIMARY KEY,
                                                user_name text NOT NULL,
                                                password text NOT NULL
);