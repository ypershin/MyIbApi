-- Table: public.ticks

-- DROP TABLE public.ticks;

CREATE TABLE public.ticks
(
  symbol character(6),
  timestamp_ timestamp with time zone,
  isbid boolean,
  price money,
  insert_time timestamp with time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.ticks
  OWNER TO postgres;
