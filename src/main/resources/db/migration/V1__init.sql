create table app_users (
                           id uuid primary key,
                           email varchar(320) not null unique,
                           display_name varchar(200),
                           avatar_url text,
                           created_at_utc timestamp not null,
                           last_login_at_utc timestamp
);

create table user_identities (
                                 id uuid primary key,
                                 user_id uuid not null references app_users(id) on delete cascade,
                                 provider varchar(50) not null,
                                 provider_user_id varchar(255) not null,
                                 email varchar(320),
                                 created_at_utc timestamp not null,
                                 unique (provider, provider_user_id)
);

create table flights (
                         id varchar(64) primary key,
                         user_id uuid not null references app_users(id) on delete cascade,

                         file_name varchar(500) not null,
                         flight_date date,
                         pilot varchar(200),
                         glider varchar(200),

                         visibility varchar(20) not null default 'PRIVATE',

                         imported_at_utc timestamp not null,
                         created_at_utc timestamp not null,
                         updated_at_utc timestamp not null,
                         deleted_at_utc timestamp,

                         constraint chk_flights_id_sha256
                             check (id ~ '^[a-f0-9]{64}$'),

    constraint chk_flights_visibility
        check (visibility in ('PRIVATE', 'UNLISTED', 'PUBLIC'))
);

create index idx_flights_user_id
    on flights(user_id);

create index idx_flights_user_imported
    on flights(user_id, imported_at_utc desc);

create index idx_flights_deleted_at
    on flights(deleted_at_utc);

create table flight_stats (
                              flight_id varchar(64) primary key references flights(id) on delete cascade,

                              start_index integer not null,
                              end_index integer not null,
                              fix_count integer not null,

                              start_time_sec integer not null,
                              end_time_sec integer not null,
                              duration_sec integer not null,

                              distance_m double precision not null,

                              min_alt_gps_m double precision not null,
                              max_alt_gps_m double precision not null,
                              gain_gps_m double precision not null,

                              min_alt_baro_m double precision not null,
                              max_alt_baro_m double precision not null,
                              gain_baro_m double precision not null,

                              created_at_utc timestamp not null,
                              updated_at_utc timestamp not null,

                              constraint chk_flight_stats_indices
                                  check (start_index >= 0 and end_index >= start_index),

                              constraint chk_flight_stats_fix_count
                                  check (fix_count >= 0),

                              constraint chk_flight_stats_duration
                                  check (duration_sec >= 0),

                              constraint chk_flight_stats_distance
                                  check (distance_m >= 0)
);

create table flight_tracks (
                               flight_id varchar(64) primary key references flights(id) on delete cascade,

                               track_blob_name text not null,
                               format_version integer not null,
                               point_count integer not null,

                               created_at_utc timestamp not null,
                               updated_at_utc timestamp not null,

                               constraint chk_flight_tracks_format_version
                                   check (format_version >= 1),

                               constraint chk_flight_tracks_point_count
                                   check (point_count >= 0)
);

create table flight_files (
                              flight_id varchar(64) primary key references flights(id) on delete cascade,

                              igc_blob_name text not null,
                              file_name varchar(500) not null,
                              file_size_bytes bigint not null,
                              content_hash varchar(64) not null,

                              created_at_utc timestamp not null,
                              updated_at_utc timestamp not null,

                              constraint chk_flight_files_content_hash_sha256
                                  check (content_hash ~ '^[a-f0-9]{64}$'),

    constraint chk_flight_files_size
        check (file_size_bytes >= 0)
);

create table flight_shares (
                               id uuid primary key,
                               flight_id varchar(64) not null references flights(id) on delete cascade,

                               share_token varchar(100) not null unique,
                               created_at_utc timestamp not null,
                               expires_at_utc timestamp
);

create index idx_flight_shares_flight_id
    on flight_shares(flight_id);