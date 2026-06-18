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
                         id uuid primary key,
                         user_id uuid not null references app_users(id) on delete cascade,
                         file_name varchar(500) not null,
                         file_hash varchar(128) not null,
                         flight_date date,
                         pilot varchar(200),
                         glider varchar(200),
                         visibility varchar(30) not null default 'private',
                         imported_at_utc timestamp not null,
                         created_at_utc timestamp not null,
                         updated_at_utc timestamp not null,
                         deleted_at_utc timestamp,
                         unique (user_id, file_hash)
);

create table flight_files (
                              id uuid primary key,
                              flight_id uuid not null references flights(id) on delete cascade,
                              original_igc_blob_name text,
                              track_cache_blob_name text,
                              file_size_bytes bigint,
                              content_hash varchar(128),
                              created_at_utc timestamp not null
);

create table flight_shares (
                               id uuid primary key,
                               flight_id uuid not null references flights(id) on delete cascade,
                               token varchar(120) not null unique,
                               created_at_utc timestamp not null,
                               expires_at_utc timestamp,
                               revoked_at_utc timestamp
);