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
create table movie(
    id serial primary key,
    name varchar(100),
    producer varchar(100),
    budget bigint,
    movie_url varchar(100)
);
create table actor(
    id serial primary key,
    name varchar(100),
    surname varchar(100),
    age varchar(100)
);
create table movie_actor(
    movie_id bigint,
    actor_id bigint,
    foreign key (movie_id) references movie(id),
    foreign key (actor_id) references actor(id)
);
create table customer(
    id serial primary key,
    name varchar(100),
    surname varchar(100),
    address_id bigint references address(id)
);
create table customer_watched_movies(
    customer_id bigint,
    watched_movie_id bigint,
    foreign key (customer_id) references customer(id),
    foreign key (watched_movie_id) references movie(id)
);
create table customer_favourite_movies(
    customer_id bigint,
    favourite_movie_id bigint,
    foreign key (customer_id) references customer(id),
    foreign key (favourite_movie_id) references movie(id)
);
create table tag(
    id serial primary key,
    name varchar(100)
);
create table tag_movie(
    tag_id bigint,
    movie_id bigint,
    foreign key (tag_id) references tag(id),
    foreign key (movie_id) references movie(id)
);
create table studio(
    id serial primary key,
    name varchar(100),
    employees bigint,
    owner varchar(100)
);
create table studio_movie(
    studio_id bigint,
    movie_id bigint,
    foreign key (studio_id) references studio(id),
    foreign key (movie_id) references movie(id)
);
create table customer_address(
    customer_id bigint,
    address_id bigint,
    foreign key (customer_id) references customer(id),
    foreign key (address_id) references address(id)
)



