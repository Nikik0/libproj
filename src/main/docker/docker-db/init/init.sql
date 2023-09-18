create table address(
    id serial primary key,
    country varchar(100),
    state varchar(100),
    city varchar(100),
    district varchar(100),
    street varchar(100),
    building bigint,
    building_literal varchar(100),
    apartment_number bigint,
    additional_info varchar(100)
);




