-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE IF NOT EXISTS table_ref ( table_ref text UNIQUE NOT NULL );
INSERT INTO table_ref (table_ref) VALUES ('PARTICIPANT'), ('EVENT'), ('PAYMENT'), ('DATA');

CREATE TABLE IF NOT EXISTS data_history_type ( history_type text UNIQUE NOT NULL );
INSERT INTO data_history_type (history_type) VALUES ('UPDATED');

CREATE TABLE IF NOT EXISTS participant_type ( participant_type text UNIQUE NOT NULL );
INSERT INTO participant_type (participant_type) VALUES ('VENDOR'), ('VENUE'), ('ATTENDEE');

CREATE TABLE IF NOT EXISTS participant_association_type ( association_type text UNIQUE NOT NULL );
INSERT INTO participant_association_type (association_type) VALUES ('INVITEE'), ('CHILD'), ('PET');

CREATE TABLE IF NOT EXISTS event_record_action ( event_action_type text UNIQUE NOT NULL );
INSERT INTO event_record_action (event_action_type) VALUES ('REGISTERED');

CREATE TABLE IF NOT EXISTS event_status ( event_status_type text UNIQUE NOT NULL );
INSERT INTO event_status (event_status_type) VALUES ('CURRENT'), ('PAST'), ('CANCELLED');

CREATE TABLE IF NOT EXISTS payment_instrument ( payment_instrument_type text UNIQUE NOT NULL );
INSERT INTO payment_instrument (payment_instrument_type) VALUES ('ELECTRONIC'), ('CHECK'), ('CASH');

CREATE TABLE IF NOT EXISTS payment_type ( payment_type text UNIQUE NOT NULL );
INSERT INTO payment_type (payment_type) VALUES ('EXPENSE'), ('INCOME');

CREATE TABLE IF NOT EXISTS payment_record_action ( payment_action_type text UNIQUE NOT NULL );
INSERT INTO payment_record_action (payment_action_type) VALUES ('SENT'), ('REFUNDED');

CREATE TABLE IF NOT EXISTS participant (
   id bigint GENERATED ALWAYS AS IDENTITY,
   participant_type text NOT NULL,
   name_first character varying NOT NULL,
   name_last character varying NOT NULL,
   dob date NOT NULL,
   addr_street_1 character varying NOT NULL,
   addr_street_2 character varying,
   addr_city character varying NOT NULL,
   addr_state_abbr character varying NOT NULL,
   addr_zip int NOT NULL,
   addr_email character varying NOT NULL,
   phone_digits int NOT NULL,
   time_recorded timestamp with time zone NOT NULL,
   PRIMARY KEY (id),
   CONSTRAINT fk_participant_type FOREIGN KEY (participant_type) REFERENCES participant_type(participant_type) );

CREATE TABLE IF NOT EXISTS participant_association_record (
    id bigint GENERATED ALWAYS AS IDENTITY,
    self int NOT NULL,
    associate int NOT NULL,
    association text NOT NULL,
    time_recorded timestamp with time zone NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_participant_association_record_self FOREIGN KEY (self) REFERENCES participant(id),
    CONSTRAINT fk_participant_association_record_associate FOREIGN KEY (associate) REFERENCES participant(id) );

CREATE TABLE IF NOT EXISTS data_history_record (
    id bigint GENERATED ALWAYS AS IDENTITY,
    actor_id int NOT NULL,
    action_name text NOT NULL,
    table_source text NOT NULL,
    new_data json NOT NULL,
    old_data json NOT NULL,
    time_recorded timestamp with time zone NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_data_history_record_action_name FOREIGN KEY (action_name) REFERENCES data_history_type(history_type),
    CONSTRAINT fk_data_history_record_table_source FOREIGN KEY (table_source) REFERENCES table_ref(table_ref),
    CONSTRAINT fk_data_history_record_actor_id FOREIGN KEY (actor_id) REFERENCES participant(id) );

CREATE TABLE IF NOT EXISTS event_info (
    id bigint GENERATED ALWAYS AS IDENTITY,
    event_name character varying NOT NULL,
    event_title character varying NOT NULL,
    date_start date NOT NULL,
    date_end date NOT NULL,
    event_status text NOT NULL,
    time_recorded timestamp with time zone NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_event_info_event_status FOREIGN KEY (event_status) REFERENCES event_status(event_status_type) );

CREATE TABLE IF NOT EXISTS event_record (
    id bigint GENERATED ALWAYS AS IDENTITY,
    participant_id int NOT NULL,
    action_type text NOT NULL,
    event_id int NOT NULL,
    time_recorded timestamp with time zone NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_event_record_action_type FOREIGN KEY (action_type) REFERENCES event_record_action(event_action_type),
    CONSTRAINT fk_event_record_event_id FOREIGN KEY (event_id) REFERENCES event_info(id),
    CONSTRAINT fk_event_record_participant_id FOREIGN KEY (participant_id) REFERENCES participant(id) );

CREATE TABLE IF NOT EXISTS payment_info (
    id bigint GENERATED ALWAYS AS IDENTITY,
    amount numeric NOT NULL,
    payment_type text NOT NULL,
    instrument_type text NOT NULL,
    time_recorded timestamp with time zone NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_info_action_type FOREIGN KEY (payment_type) REFERENCES payment_type(payment_type),
    CONSTRAINT fk_payment_info_instrument_type FOREIGN KEY (instrument_type) REFERENCES payment_instrument(payment_instrument_type) );

CREATE TABLE IF NOT EXISTS payment_record (
    id bigint GENERATED ALWAYS AS IDENTITY,
    event_id int NOT NULL,
    actor_id int NOT NULL,
    payment_action_type text NOT NULL,
    recipient_id int NOT NULL,
    payment_id int NOT NULL,
    time_recorded timestamp with time zone NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_event_id FOREIGN KEY (event_id) REFERENCES event_info(id),
    CONSTRAINT fk_payment_actor_id FOREIGN KEY (actor_id) REFERENCES participant(id),
    CONSTRAINT fk_payment_recipient_id FOREIGN KEY (recipient_id) REFERENCES participant(id),
    CONSTRAINT fk_payment_record_action_type FOREIGN KEY (payment_action_type) REFERENCES payment_record_action(payment_action_type),
    CONSTRAINT fk_payment_id FOREIGN KEY (payment_id) REFERENCES payment_info(id) );
