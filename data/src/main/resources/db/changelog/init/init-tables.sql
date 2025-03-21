-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE IF NOT EXISTS table_ref ( table_ref text PRIMARY KEY );
INSERT INTO table_ref (table_ref) VALUES ('PARTICIPANT'), ('EVENT'), ('PAYMENT'), ('DATA');

CREATE TABLE IF NOT EXISTS data_history_type ( history_type text PRIMARY KEY );
INSERT INTO data_history_type (history_type) VALUES ('UPDATED');

CREATE TABLE IF NOT EXISTS participant_type ( participant_type text PRIMARY KEY );
INSERT INTO participant_type (participant_type) VALUES ('VENDOR'), ('VENUE'), ('ATTENDEE');

CREATE TABLE IF NOT EXISTS participant_association_type ( association_type text PRIMARY KEY );
INSERT INTO participant_association_type (association_type) VALUES ('INVITEE'), ('CHILD'), ('PET');

CREATE TABLE IF NOT EXISTS event_status ( event_status_type text PRIMARY KEY );
INSERT INTO event_status (event_status_type) VALUES ('CURRENT'), ('PAST'), ('CANCELLED');

CREATE TABLE IF NOT EXISTS payment_instrument ( payment_instrument_type text PRIMARY KEY );
INSERT INTO payment_instrument (payment_instrument_type) VALUES ('ELECTRONIC'), ('CHECK'), ('CASH');

CREATE TABLE IF NOT EXISTS payment_type ( payment_type text PRIMARY KEY );
INSERT INTO payment_type (payment_type) VALUES ('EXPENSE'), ('INCOME');

CREATE TABLE IF NOT EXISTS payment_action ( payment_action_type text PRIMARY KEY );
INSERT INTO payment_action (payment_action_type) VALUES ('SENT'), ('REFUNDED');

CREATE TABLE IF NOT EXISTS configuration (
   id bigint GENERATED ALWAYS AS IDENTITY,
   recommended_donation int NOT NULL,
   charity_name character varying NOT NULL,
   charity_url character varying NOT NULL,
   fund_processor_name character varying NOT NULL,
   fund_processor_url character varying NOT NULL,
   fund_processor_instructions json NOT NULL,
   event_guidelines json NOT NULL,
   PRIMARY KEY (id) );

CREATE TABLE IF NOT EXISTS participant (
   id bigint GENERATED ALWAYS AS IDENTITY,
   participant_type text REFERENCES participant_type(participant_type) NOT NULL,
   name_first character varying NOT NULL,
   name_last character varying NOT NULL,
   name_nick character varying,
   sponsor character varying NOT NULL,
   dob date NOT NULL,
   addr_street_1 character varying NOT NULL,
   addr_street_2 character varying,
   addr_city character varying NOT NULL,
   addr_state_abbr character varying NOT NULL,
   addr_zip int NOT NULL,
   addr_email character varying NOT NULL,
   phone_digits bigint NOT NULL,
   time_recorded timestamp with time zone NOT NULL DEFAULT now(),
   PRIMARY KEY (id),
   UNIQUE (addr_email));

CREATE TABLE IF NOT EXISTS participant_association (
    id bigint GENERATED ALWAYS AS IDENTITY,
    self bigint REFERENCES participant(id) NOT NULL,
    raw_associate_name text NOT NULL,
    associate bigint REFERENCES participant(id),
    association text REFERENCES participant_association_type(association_type) NOT NULL,
    time_recorded timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );

CREATE TABLE IF NOT EXISTS data_history (
    id bigint GENERATED ALWAYS AS IDENTITY,
    actor_id bigint REFERENCES participant(id) NOT NULL,
    action_name text REFERENCES data_history_type(history_type) NOT NULL,
    table_source text REFERENCES table_ref(table_ref) NOT NULL,
    new_data json NOT NULL,
    old_data json NOT NULL,
    time_recorded timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );

CREATE TABLE IF NOT EXISTS event_info (
    id bigint GENERATED ALWAYS AS IDENTITY,
    event_name character varying NOT NULL,
    event_title character varying NOT NULL,
    date_start date NOT NULL,
    date_end date NOT NULL,
    event_status text REFERENCES event_status(event_status_type) NOT NULL,
    time_recorded timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );

CREATE TABLE IF NOT EXISTS registration (
    id bigint GENERATED ALWAYS AS IDENTITY,
    participant_id bigint REFERENCES participant(id) NOT NULL,
    donation_pledge decimal(16, 8) NOT NULL,
    signature text NOT NULL,
    event_info_id bigint REFERENCES event_info(id) NOT NULL,
    time_recorded timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );

CREATE TABLE IF NOT EXISTS registration_participant_association (
    id bigint GENERATED ALWAYS AS IDENTITY,
    participant_association_id bigint REFERENCES participant_association(id) NOT NULL,
    registration_id bigint REFERENCES registration(id) NOT NULL,
    PRIMARY KEY (id) );

CREATE TABLE IF NOT EXISTS payment (
    id bigint GENERATED ALWAYS AS IDENTITY,
    event_info_id bigint REFERENCES event_info(id) NOT NULL,
    actor_id bigint REFERENCES participant(id) NOT NULL,
    payment_action_type text REFERENCES payment_action(payment_action_type) NOT NULL,
    recipient_id bigint REFERENCES participant(id) NOT NULL,
    amount decimal(16, 8) NOT NULL,
    payment_type text REFERENCES payment_type(payment_type) NOT NULL,
    instrument_type text REFERENCES payment_instrument(payment_instrument_type) NOT NULL,
    time_recorded timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );
