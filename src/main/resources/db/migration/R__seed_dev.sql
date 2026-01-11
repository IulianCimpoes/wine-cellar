-- Dev-only seed data (repeatable migration).
-- Controlled by Flyway placeholder: ${seed.enabled}
-- In non-dev environments, set it to false and this becomes a no-op.

-- -------------------------
-- Seed wineries
-- -------------------------
INSERT INTO winery (name, country, version)
SELECT 'Fautor', 'Moldova', 0
WHERE ${seed.enabled}
  AND NOT EXISTS (
    SELECT 1 FROM winery WHERE name = 'Fautor' AND country = 'Moldova'
);

INSERT INTO winery (name, country, version)
SELECT 'Purcari', 'Moldova', 0
WHERE ${seed.enabled}
  AND NOT EXISTS (
    SELECT 1 FROM winery WHERE name = 'Purcari' AND country = 'Moldova'
);

INSERT INTO winery (name, country, version)
SELECT 'Cricova', 'Moldova', 0
WHERE ${seed.enabled}
  AND NOT EXISTS (
    SELECT 1 FROM winery WHERE name = 'Cricova' AND country = 'Moldova'
);

-- -------------------------
-- Seed wines (resolve winery_id via name+country)
-- -------------------------
INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT
    'Feteasca Neagra', w.id, 'Moldova', 2021, 120.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Fautor' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1
    FROM wine x
    WHERE x.name = 'Feteasca Neagra'
      AND x.wine_year = 2021
      AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT
    'Negru de Purcari', w.id, 'Moldova', 2020, 250.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Purcari' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1
    FROM wine x
    WHERE x.name = 'Negru de Purcari'
      AND x.wine_year = 2020
      AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT
    'Crisecco', w.id, 'Moldova', 2022, 90.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Cricova' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1
    FROM wine x
    WHERE x.name = 'Crisecco'
      AND x.wine_year = 2022
      AND x.winery_id = w.id
);
-- -------------------------
-- Extra wines for pagination demo (9 more)
-- -------------------------

-- FAUTOR (3)
INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Rara Neagra', w.id, 'Moldova', 2020, 110.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Fautor' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Rara Neagra' AND x.wine_year = 2020 AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Viorica', w.id, 'Moldova', 2023, 95.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Fautor' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Viorica' AND x.wine_year = 2023 AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Pinot Noir', w.id, 'Moldova', 2019, 130.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Fautor' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Pinot Noir' AND x.wine_year = 2019 AND x.winery_id = w.id
);

-- PURCARI (4)
INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Alb de Purcari', w.id, 'Moldova', 2021, 180.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Purcari' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Alb de Purcari' AND x.wine_year = 2021 AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Rosu de Purcari', w.id, 'Moldova', 2019, 160.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Purcari' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Rosu de Purcari' AND x.wine_year = 2019 AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Rose de Purcari', w.id, 'Moldova', 2022, 140.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Purcari' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Rose de Purcari' AND x.wine_year = 2022 AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Cabernet Sauvignon de Purcari', w.id, 'Moldova', 2018, 170.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Purcari' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Cabernet Sauvignon de Purcari' AND x.wine_year = 2018 AND x.winery_id = w.id
);

-- CRICOVA (2)
INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Cricova Brut', w.id, 'Moldova', 2021, 85.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Cricova' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Cricova Brut' AND x.wine_year = 2021 AND x.winery_id = w.id
);

INSERT INTO wine (name, winery_id, country, wine_year, price, version)
SELECT 'Cricova Chardonnay', w.id, 'Moldova', 2020, 100.00, 0
FROM winery w
WHERE ${seed.enabled}
  AND w.name = 'Cricova' AND w.country = 'Moldova'
  AND NOT EXISTS (
    SELECT 1 FROM wine x
    WHERE x.name = 'Cricova Chardonnay' AND x.wine_year = 2020 AND x.winery_id = w.id
);
