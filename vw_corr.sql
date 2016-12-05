-- View: public.vw_corr

-- DROP VIEW public.vw_corr;

CREATE OR REPLACE VIEW public.vw_corr AS 

with z as (
 SELECT a.ticker ticker_a, b.ticker ticker_b, a.datetime_::date AS date_,
    a.datetime_::time without time zone AS time_,
    a.close_ AS p1,
    b.close_ AS p2
   FROM hist_quotes a,
    hist_quotes b
  WHERE b.datetime_ = a.datetime_ AND a.ticker < b.ticker::text
)
select concat(ticker_a, ' - ', ticker_b) pair_, date_, corr(p1,p2) corr_ from z group by ticker_a, ticker_b, date_
order by ticker_a, ticker_b, date_;




ALTER TABLE public.vw_corr
  OWNER TO "user";
