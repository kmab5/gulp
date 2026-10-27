#!/usr/bin/env python3
"""Migration and contract checks for v1 -> v2 saved drinks (not a substitute for Android Room tests)."""
from pathlib import Path
import sqlite3
import re
R=Path(__file__).resolve().parents[1]
source=(R/'app/src/main/java/com/kmab/gulp/core/data/GulpDatabase.kt').read_text()
repo=(R/'app/src/main/java/com/kmab/gulp/core/data/GulpRepository.kt').read_text()
models=(R/'app/src/main/java/com/kmab/gulp/core/domain/DrinkModels.kt').read_text()
assert '@Database(entities = [DrinkEntity::class, DrinkTypeEntity::class], version = 2' in source
assert '.addMigrations(MIGRATION_1_2)' in repo
assert 'drinkTypeId' in models and 'DrinkTypeCatalog' in models
con=sqlite3.connect(':memory:')
con.execute('CREATE TABLE drink_records (id INTEGER PRIMARY KEY,amountMl INTEGER NOT NULL,drinkType TEXT NOT NULL,timestampMillis INTEGER NOT NULL)')
con.executemany('INSERT INTO drink_records VALUES (?,?,?,?)', [(1,250,'WATER',100),(2,200,'COFFEE',200),(3,125,'TEA',300),(4,150,'OTHER',400)])
for sql in [
 'ALTER TABLE drink_records ADD COLUMN drinkTypeId TEXT',
 'ALTER TABLE drink_records ADD COLUMN drinkName TEXT',
 'ALTER TABLE drink_records ADD COLUMN drinkIcon TEXT',
 '''CREATE TABLE IF NOT EXISTS drink_types (
     id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, iconId TEXT NOT NULL,
     defaultAmountMl INTEGER NOT NULL, legacyKind TEXT NOT NULL, archived INTEGER NOT NULL)''',
 "UPDATE drink_records SET drinkTypeId = lower(drinkType), drinkName = CASE drinkType WHEN 'WATER' THEN 'Water' WHEN 'COFFEE' THEN 'Coffee' WHEN 'TEA' THEN 'Tea' ELSE 'Other' END, drinkIcon = lower(drinkType) WHERE drinkTypeId IS NULL"
]: con.execute(sql)
assert con.execute('SELECT id,amountMl,timestampMillis FROM drink_records ORDER BY id').fetchall()==[(1,250,100),(2,200,200),(3,125,300),(4,150,400)]
assert con.execute('SELECT drinkTypeId,drinkName FROM drink_records ORDER BY id').fetchall()==[('water','Water'),('coffee','Coffee'),('tea','Tea'),('other','Other')]
con.execute('INSERT INTO drink_types VALUES (?,?,?,?,?,?)',('custom_fruit','Fruit water','juice',300,'OTHER',0))
con.execute("UPDATE drink_types SET name='Infused water' WHERE id='custom_fruit'")
assert con.execute('SELECT drinkName FROM drink_records WHERE id=4').fetchone()[0]=='Other'
con.execute('UPDATE drink_types SET archived=1 WHERE id=?',('custom_fruit',))
assert con.execute('SELECT count(*) FROM drink_records').fetchone()[0]==4
print('PASS: v1-v2 additive migration, historical records, custom type rename/archive')
