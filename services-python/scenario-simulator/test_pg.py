import psycopg

conn = psycopg.connect(
    host="localhost",
    port=5432,
    dbname="pulsaride",
    user="pulsaride",
    password="pulsaride",
)

print("Connected!")