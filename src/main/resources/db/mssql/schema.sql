--Michael Ashby, added to create tables for SQL Database.
declare @sql nvarchar(2000)
while(exists(select 1 from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_TYPE='FOREIGN KEY'))
begin

 SELECT TOP 1 @sql=('ALTER TABLE ' + TABLE_SCHEMA + '.[' + TABLE_NAME
 + '] DROP CONSTRAINT [' + CONSTRAINT_NAME + ']')
 FROM information_schema.table_constraints
 WHERE CONSTRAINT_TYPE = 'FOREIGN KEY'
exec (@sql)
 PRINT @sql
end
while(exists(select 1 from INFORMATION_SCHEMA.TABLES))
begin

 SELECT TOP 1 @sql=('DROP TABLE ' + TABLE_SCHEMA + '.[' + TABLE_NAME
 + ']')
 FROM INFORMATION_SCHEMA.TABLES
exec (@sql)
PRINT @sql
end

  /* DROP TABLE IF EXISTS vets;
  DROP TABLE IF EXISTS specialties;
  DROP TABLE IF EXISTS vet_specialties;
  DROP TABLE IF EXISTS types;
  DROP TABLE IF EXISTS holders;
  DROP TABLE IF EXISTS pets;
  DROP TABLE IF EXISTS visits; */

  CREATE TABLE vets (
    id INTEGER NOT NULL IDENTITY PRIMARY KEY,
    first_name VARCHAR(30),
    last_name VARCHAR(30)
  );
  CREATE INDEX vets_last_name ON vets(last_name);



  CREATE TABLE specialties (
    id INTEGER NOT NULL IDENTITY PRIMARY KEY,
    name VARCHAR(80)
  );
  CREATE INDEX specialties_name ON specialties(name);

  CREATE TABLE vet_specialties (
    vet_id INTEGER NOT NULL  ,
    specialty_id INTEGER NOT NULL
  );
  CREATE CLUSTERED INDEX vet_specialties_PK ON vet_specialties(vet_id, specialty_id);

  alter table vet_specialties add constraint fk_vet_specialties_vets foreign key (vet_id) references vets(id);
  alter table vet_specialties add constraint fk_vet_specialties_specialties foreign key (specialty_id) references specialties(id);

  CREATE TABLE types (
    id INTEGER NOT NULL IDENTITY PRIMARY KEY,
    name VARCHAR(80)
  );
  CREATE INDEX types_name ON types(name);

  CREATE TABLE holders (
    id INTEGER NOT NULL IDENTITY PRIMARY KEY,
    first_name VARCHAR(30),
    last_name VARCHAR(30),
    address VARCHAR(255),
    city VARCHAR(80),
    telephone VARCHAR(20)
  );
  CREATE INDEX holders_last_name ON holders(last_name);

  CREATE TABLE pets (
    id INTEGER NOT NULL IDENTITY PRIMARY KEY,
    name VARCHAR(30),
    birth_date DATE,
    type_id INTEGER NOT NULL,
    holder_id INTEGER NOT NULL
  );
  alter table pets add constraint fk_pets_holders foreign key (holder_id) references holders(id);
  alter table pets add constraint fk_pets_types foreign key (type_id) references types(id);
  CREATE INDEX pets_name ON pets(name);

  CREATE TABLE visits (
    id INTEGER NOT NULL IDENTITY PRIMARY KEY,
    pet_id INTEGER NOT NULL,
    visit_date DATE,
    description VARCHAR(255)
  );
  alter table visits add constraint fk_visits_pets foreign key (pet_id) references pets(id);
  CREATE INDEX visits_pet_id ON visits(pet_id);
