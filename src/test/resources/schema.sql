drop table if exists orders;
drop table if exists user;
drop table if exists product;

create table user
(
    id int AUTO_INCREMENT,
    login_id varchar(25) not null,
    password varchar(50) not null,
    user_name  varchar(25) not null,
    user_role  varchar(25) not null,

    primary key (ID)
);

create table product
(
    id int AUTO_INCREMENT,
    name varchar(50) not null,
    description text not null,
    quantity int not null,
    regular_price decimal not null,
    sale_price decimal not null,
    sale_start_date_time datetime not null,
    sale_end_date_time datetime not null,
    version int default 0,

    primary key (ID)
);

create table orders
(
    id int AUTO_INCREMENT,
    product_id int not null,
    user_id int not null,

    primary key (id),
    foreign key (product_id) REFERENCES product (id),
    foreign key (user_id) REFERENCES user (id)

);
