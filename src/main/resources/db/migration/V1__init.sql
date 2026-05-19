create table users
(
    id bigserial primary key,
    created_at timestamp with time zone not null,
    username varchar(50) not null,
    email varchar(255) not null unique,
    password varchar(100) not null,
    role varchar(20) not null check (role in ('USER', 'MANAGER', 'ADMIN'))
);

create table deactivated_tokens
(
    id uuid primary key,
    keep_until timestamp with time zone not null check (keep_until > now())
);
