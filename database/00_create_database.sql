\set db_name edu_group

SELECT format(
  'CREATE DATABASE %I WITH ENCODING = ''UTF8'' TEMPLATE = template0',
  :'db_name'
)
WHERE NOT EXISTS (
  SELECT 1
  FROM pg_database
  WHERE datname = :'db_name'
)\gexec
