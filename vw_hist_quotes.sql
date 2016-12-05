-- View: public.vw_hist_quotes

-- DROP VIEW public.vw_hist_quotes;

CREATE OR REPLACE VIEW public.vw_hist_quotes AS 
 SELECT hist_quotes.ticker,
    hist_quotes.datetime_::date AS date_,
    hist_quotes.datetime_::time without time zone AS time_,
    hist_quotes.close_
   FROM hist_quotes
  ORDER BY hist_quotes.ticker, hist_quotes.datetime_;

ALTER TABLE public.vw_hist_quotes
  OWNER TO "user";
