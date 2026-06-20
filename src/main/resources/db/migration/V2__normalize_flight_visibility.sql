update flights
set visibility = upper(visibility)
where visibility is not null;

alter table flights
alter column visibility set default 'PRIVATE';

alter table flights
add constraint chk_flights_visibility
check (visibility in ('PRIVATE', 'UNLISTED', 'PUBLIC'));