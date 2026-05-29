CREATE TABLE `department` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(50),
  `active` boolean DEFAULT true
);

CREATE TABLE `province` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(50),
  `active` boolean DEFAULT true,
  `department_id` integer
);

CREATE TABLE `district` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `active` boolean NOT NULL DEFAULT true,
  `province_id` integer NOT NULL
);

CREATE TABLE `document_type` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `description` varchar(10)
);

CREATE TABLE `business_type` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `description` varchar(50) NOT NULL
);

CREATE TABLE `client_group` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `description` varchar(3)
);

CREATE TABLE `client` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `code` varchar(6) NOT NULL,
  `name` varchar(50) NOT NULL,
  `document_type_id` int NOT NULL,
  `document_number` varchar(11) UNIQUE NOT NULL,
  `address` text NOT NULL,
  `district_id` int NOT NULL,
  `business_type_id` int NOT NULL,
  `client_group_id` int NOT NULL,
  `cellphone` varchar(9),
  `telephone` varchar(9),
  `active` boolean DEFAULT true,
  `user_id` int NOT NULL,
  `latitud` double,
  `longitud` double,
  `observation` text NULL
);

CREATE TABLE `user` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `code` varchar(6) UNIQUE NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `second_name` varchar(50) NOT NULL,
  `first_surname` varchar(50) NOT NULL,
  `second_surname` varchar(50) NOT NULL,
  `document_type_id` int NOT NULL,
  `document_number` varchar(11) UNIQUE NOT NULL,
  `cellphone` varchar(9) NOT NULL,
  `email` varchar(30) NOT NULL,
  `password` varchar(255) NOT NULL
);

CREATE TABLE `route` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `name` varchar(50) NOT NULL,
  `scheduled_date` date NOT NULL,
  `user_id` int NOT NULL,
  `active` boolean DEFAULT true
);

CREATE TABLE `waypoint` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `route_id` int NOT NULL,
  `address` text NOT NULL,
  `latitud` double,
  `longitud` double,
  `order_sequence` int NOT NULL,
  `client_id` int NOT NULL,
  `status` varchar(10) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE, VISITA, CANCELADA',
  `visited_at` timestamp,
  `comment` text
);

CREATE TABLE `client_schedule` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `client_id` int NOT NULL,
  `user_id` int NOT NULL,
  `day` date NOT NULL,
  `start_time` time NOT NULL,
  `observation` text NULL,
  `active` boolean NOT NULL DEFAULT true
);

CREATE TABLE `role_user` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `role_id` int NOT NULL
);

CREATE TABLE `role` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `role` varchar(20)
);

CREATE UNIQUE INDEX `uq_client_code` ON `client` (`code`);

ALTER TABLE `province` ADD CONSTRAINT `department_province_fk` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`);

ALTER TABLE `district` ADD CONSTRAINT `province_district_fk` FOREIGN KEY (`province_id`) REFERENCES `province` (`id`);

ALTER TABLE `client` ADD CONSTRAINT `client_business_type_fk` FOREIGN KEY (`business_type_id`) REFERENCES `business_type` (`id`);

ALTER TABLE `client` ADD CONSTRAINT `client_document_type_fk` FOREIGN KEY (`document_type_id`) REFERENCES `document_type` (`id`);

ALTER TABLE `client` ADD CONSTRAINT `client_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `client` ADD CONSTRAINT `client_group_client_fk` FOREIGN KEY (`client_group_id`) REFERENCES `client_group` (`id`);

ALTER TABLE `client` ADD CONSTRAINT `client_district_fk` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`);

ALTER TABLE `user` ADD CONSTRAINT `user_document_type_fk` FOREIGN KEY (`document_type_id`) REFERENCES `document_type` (`id`);

ALTER TABLE `role_user` ADD CONSTRAINT `user_role_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `role_user` ADD CONSTRAINT `role_role_user_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`);

ALTER TABLE `route` ADD CONSTRAINT `route_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `waypoint` ADD CONSTRAINT `route_waypoint_fk` FOREIGN KEY (`route_id`) REFERENCES `route` (`id`);

ALTER TABLE `waypoint` ADD CONSTRAINT `waypoint_client_fk` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`);

ALTER TABLE `client_schedule` ADD CONSTRAINT `client_schedule_client_fk` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`);

ALTER TABLE `client_schedule` ADD CONSTRAINT `client_schedule_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
