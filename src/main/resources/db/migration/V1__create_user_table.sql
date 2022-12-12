CREATE TABLE users
(
    id   INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    login VARCHAR NOT NULL UNIQUE,
    password  VARCHAR NOT NULL
);

insert into public.users ( login, password) values ( 'admin', '7OI8P68Gyz7Ywh0enKfpeg==');

