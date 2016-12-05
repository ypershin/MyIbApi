-- Table: public.hist_quotes

-- DROP TABLE public.hist_quotes;

CREATE TABLE public.hist_quotes
(
  ticker character varying(10),
  datetime_ timestamp without time zone,
  open_ numeric(8,2),
  high_ numeric(8,2),
  low_ numeric(8,2),
  close_ numeric(8,2),
  volume_ integer,
  count_ integer,
  wap numeric(10,4),
  hasgaps boolean,
  insert_date timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.hist_quotes
  OWNER TO "user";
