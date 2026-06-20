alter table flight_files
add constraint uq_flight_files_flight_id unique (flight_id);