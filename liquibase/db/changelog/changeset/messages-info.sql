DROP TABLE IF EXISTS public.messages_info;

CREATE TABLE IF NOT EXISTS public.messages_info (
                                                    id serial NOT NULL PRIMARY KEY,
                                                    chat_id bigint NOT NULL,
                                                    sender_id bigint NOT NULL,
                                                    component_id bigint NOT NULL,
                                                    message_type text NOT NULL,
                                                    message bytea NOT NULL,
                                                    timestamp timestamp NOT NULL
);