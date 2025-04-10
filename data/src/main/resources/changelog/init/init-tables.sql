-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE IF NOT EXISTS table_ref ( table_ref text PRIMARY KEY );
INSERT INTO table_ref (table_ref) VALUES ('CONFIGURATION'), ('PARTICIPANT'), ('EVENT_INFO'), ('REGISTRATION'), ('GUEST'), ('TRANSACTION_');

CREATE TABLE IF NOT EXISTS data_history_type ( history_type text PRIMARY KEY );
INSERT INTO data_history_type (history_type) VALUES ('INSERTED'), ('UPDATED'), ('DELETED');

CREATE TABLE IF NOT EXISTS participant_type ( participant_type text PRIMARY KEY );
INSERT INTO participant_type (participant_type) VALUES ('VENDOR'), ('VENUE'), ('ATTENDEE');

CREATE TABLE IF NOT EXISTS guest_relationship_type ( guest_relationship_type text PRIMARY KEY );
INSERT INTO guest_relationship_type (guest_relationship_type) VALUES ('ADULT'), ('CHILD'), ('PET');

CREATE TABLE IF NOT EXISTS emergency_contact_relationship_type ( emergency_contact_relationship_type text PRIMARY KEY );
INSERT INTO emergency_contact_relationship_type (emergency_contact_relationship_type) VALUES ('FRIEND'), ('FAMILY');

CREATE TABLE IF NOT EXISTS event_status ( event_status_type text PRIMARY KEY );
INSERT INTO event_status (event_status_type) VALUES ('CURRENT'), ('PAST'), ('CANCELLED');

CREATE TABLE IF NOT EXISTS transaction_instrument ( transaction_instrument_type text PRIMARY KEY );
INSERT INTO transaction_instrument (transaction_instrument_type) VALUES ('ELECTRONIC'), ('CHECK'), ('CASH');

CREATE TABLE IF NOT EXISTS transaction_type ( transaction_type text PRIMARY KEY );
INSERT INTO transaction_type (transaction_type) VALUES ('INVOICE'), ('EXPENSE'), ('INCOME');

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
   emergency_contact_relationship_type text REFERENCES emergency_contact_relationship_type(emergency_contact_relationship_type) NOT NULL,
   name_emergency character varying NOT NULL,
   phone_emergency bigint NOT NULL,
   time_recorded timestamp with time zone NOT NULL DEFAULT now(),
   PRIMARY KEY (id),
   UNIQUE (addr_email));

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

CREATE TABLE IF NOT EXISTS guest (
    id bigint GENERATED ALWAYS AS IDENTITY,
    invitee_profile_id bigint REFERENCES participant(id) NOT NULL,
    registration_id bigint REFERENCES registration(id) NOT NULL,
    raw_guest_name text NOT NULL,
    guest_profile_id bigint REFERENCES participant(id),
    relationship text REFERENCES guest_relationship_type(guest_relationship_type) NOT NULL,
    time_recorded timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );

CREATE TABLE IF NOT EXISTS transaction_ (
    id bigint GENERATED ALWAYS AS IDENTITY,
    event_info_id bigint REFERENCES event_info(id) NOT NULL,
    actor_id bigint REFERENCES participant(id) NOT NULL,
    recipient_id bigint REFERENCES participant(id) NOT NULL,
    amount decimal(16, 8) NOT NULL,
    transaction_type text REFERENCES transaction_type(transaction_type) NOT NULL,
    instrument_type text REFERENCES transaction_instrument(transaction_instrument_type) NOT NULL,
    memo text,
    time_recorded timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id) );

CREATE OR REPLACE VIEW registration_packet_with_label AS
    SELECT
        r.*,
        p.name_first AS participant_name_first,
        p.name_last AS participant_name_last,
        e.event_name,
        e.event_title
    FROM
        registration r
            JOIN
        participant p ON r.participant_id = p.id
            JOIN
        event_info e ON r.event_info_id = e.id;

CREATE OR REPLACE VIEW guest_with_labels AS
    SELECT
        g.id,
        g.registration_id,
        r.event_info_id,
        e.event_name,
        e.event_title,
        g.raw_guest_name,
        g.guest_profile_id,
        gp.name_first AS guest_name_first,
        gp.name_last AS guest_name_last,
        g.invitee_profile_id AS invitee_profile_id,
        i.name_first AS invitee_first_name,
        i.name_last AS invitee_last_name,
        g.relationship,
        g.time_recorded
    FROM
        guest g
            JOIN
        registration r ON g.registration_id = r.id
            JOIN
        event_info e ON r.event_info_id = e.id
            JOIN
        participant i ON g.invitee_profile_id = i.id
            LEFT JOIN
        participant gp ON g.guest_profile_id = gp.id;

CREATE OR REPLACE VIEW registration_with_labels AS
SELECT
    r.*,
    p.participant_type,
    p.name_first,
    p.name_last,
    p.name_nick,
    p.addr_email,
    p.addr_street_1,
    p.addr_street_2,
    p.addr_city,
    p.addr_state_abbr,
    p.addr_zip,
    p.dob,
    p.name_emergency,
    p.phone_emergency,
    p.emergency_contact_relationship_type,
    p.time_recorded AS participant_time_recorded,
    e.event_name,
    e.event_title,
    JSONB_AGG(JSONB_BUILD_OBJECT('guest_profile_id',  g.guest_profile_id, 'raw_guest_name', g.raw_guest_name, 'relationship', g.relationship, 'time_recorded', g.time_recorded)) AS guests
FROM
    registration r
        JOIN
    participant p ON r.participant_id = p.id
        JOIN
    event_info e ON r.event_info_id = e.id
        LEFT JOIN
    guest g ON p.id = g.invitee_profile_id
group by r.id,
         r.participant_id,
         r.donation_pledge,
         r.signature,
         r.event_info_id,
         r.time_recorded,
         p.participant_type,
         p.name_first,
         p.name_last,
         p.name_nick,
         p.addr_email,
         p.addr_street_1,
         p.addr_street_2,
         p.addr_city,
         p.addr_state_abbr,
         p.addr_zip,
         p.dob,
         p.name_emergency,
         p.phone_emergency,
         p.emergency_contact_relationship_type,
         p.time_recorded,
         e.event_name,
         e.event_title;

CREATE OR REPLACE VIEW transaction_with_labels AS
    SELECT
        t.*,
        e.event_name,
        e.event_title,
        pa.name_first AS actor_name_first,
        pa.name_last AS actor_name_last,
        pr.name_first AS recipient_name_first,
        pr.name_last AS recipient_name_last
    FROM
        transaction_ t
            JOIN
        event_info e ON t.event_info_id = e.id
            JOIN
        participant pa ON t.actor_id = pa.id
            JOIN
        participant pr ON t.recipient_id = pr.id;

CREATE OR REPLACE VIEW data_history_with_labels AS
    SELECT
        h.*,
        a.name_first AS actor_name_first,
        a.name_last AS actor_name_last
    FROM
        data_history h
            JOIN
        participant a ON h.actor_id = a.id;

CREATE OR REPLACE VIEW event_statistics AS
    WITH
        event AS (
            SELECT
                e.id AS event_info_id,
                e.event_name,
                e.event_title,
                e.event_status,
                e.date_start,
                e.date_end
            FROM
                event_info e
        ),
        invoices AS (
            SELECT
                t.event_info_id,
                JSONB_AGG(JSON_BUILD_OBJECT('memo', t.memo, 'amount', t.amount)) AS itemization,
                COUNT(t.id) AS total_count,
                SUM(t.amount) AS total_amount
            FROM
                transaction_ t
            WHERE
                t.transaction_type = 'INVOICE'
            GROUP BY
                t.event_info_id
        ),
        expenses AS (
            SELECT
                t.event_info_id,
                COUNT(t.id) AS total_count,
                SUM(t.amount) AS total_amount
            FROM
                transaction_ t
            WHERE
                t.transaction_type = 'EXPENSES'
            GROUP BY
                t.event_info_id
        ),
        income AS (
            SELECT
                t.event_info_id,
                COUNT(t.id) AS total_count,
                SUM(t.amount) AS total_amount
            FROM
                transaction_ t
            WHERE
                t.transaction_type = 'INCOME'
            GROUP BY
                t.event_info_id
        ),
        registered_attendees AS (
            SELECT
                a.event_info_id,
                COUNT(a.participant_id) AS total_count,
                SUM(a.donation_pledge) AS total_pledged
            FROM
                registration a
            GROUP BY
                a.event_info_id
        ),
        unregistered_guests AS (
            SELECT
                r.event_info_id,
                COUNT(g.id) AS total_count
            FROM
                registration r
                    JOIN
                guest g ON g.registration_id = r.id AND g.relationship IN ('ADULT', 'CHILD') AND (g.guest_profile_id IS NULL OR g.guest_profile_id NOT IN (r.participant_id))
            GROUP BY
                r.event_info_id
        )
    SELECT
        e.event_info_id AS event_id,
        e.event_name,
        e.event_title,
        e.event_status,
        e.date_start,
        e.date_end,
        invoices.itemization AS invoices,
        ra.total_count AS registered_attendees_count,
        ug.total_count AS unregistered_guest_count,
        invoices.total_amount AS total_invoiced,
        expenses.total_amount AS total_expenses,
        ra.total_pledged,
        income.total_amount AS total_income,
        ( income.total_amount / ra.total_pledged ) * 100 AS percentage_pledged_received
    FROM
        event e
            LEFT JOIN
        invoices ON invoices.event_info_id = e.event_info_id
            LEFT JOIN
        expenses ON expenses.event_info_id = e.event_info_id
            LEFT JOIN
        income ON income.event_info_id = e.event_info_id
            LEFT JOIN
        registered_attendees ra ON ra.event_info_id = e.event_info_id
            LEFT JOIN
        unregistered_guests ug ON ug.event_info_id = e.event_info_id
    GROUP BY
        e.event_info_id,
        e.event_name,
        e.event_title,
        e.event_status,
        e.date_start,
        e.date_end,
        invoices.itemization,
        ra.total_count,
        ug.total_count,
        invoices.total_amount,
        expenses.total_amount,
        ra.total_pledged,
        income.total_amount;