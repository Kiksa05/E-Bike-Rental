-- Fake demo data, loaded by Docker the first time the database volume is created.
-- Both accounts log in with the password: demo1234

CREATE TABLE `customer` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `account_balance` double DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `station` (
  `id` int NOT NULL AUTO_INCREMENT,
  `location` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ebike` (
  `id` int NOT NULL AUTO_INCREMENT,
  `model` varchar(255) DEFAULT NULL,
  `battery_level` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `station_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_ebike_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `rental` (
  `id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int DEFAULT NULL,
  `ebike_id` int DEFAULT NULL,
  `rental_start_time` datetime DEFAULT NULL,
  `rental_end_time` datetime DEFAULT NULL,
  `rental_status` varchar(255) DEFAULT NULL,
  `passport_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_rental_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `fk_rental_ebike` FOREIGN KEY (`ebike_id`) REFERENCES `ebike` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `payment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `rental_id` int DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `payment_status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- kiksa@admin.com is seeded because CustomerController makes whoever registers that email an admin.
INSERT INTO `customer` (`name`, `email`, `phone`, `account_balance`, `password`, `role`) VALUES
('Demo Admin', 'kiksa@admin.com', '000000000', 0, '$2a$12$5mONSnOP6yxHdIf26hgxO.6ZcJt446t2oSFn435NvwmVDcrExAiTG', 'admin'),
('Demo User', 'user@demo.local', '000000001', 0, '$2a$12$5mONSnOP6yxHdIf26hgxO.6ZcJt446t2oSFn435NvwmVDcrExAiTG', 'user');

INSERT INTO `station` (`location`) VALUES
('Maribor'),
('Ljubljana');

INSERT INTO `ebike` (`model`, `battery_level`, `status`, `price`, `station_id`) VALUES
('EcoStrom X1', 95, 'Available', 15, 1),
('EcoStrom X2', 70, 'Available', 35, 1),
('BMX M1', 50, 'Available', 20, 2);

INSERT INTO `rental` (`customer_id`, `ebike_id`, `rental_start_time`, `rental_end_time`, `rental_status`, `passport_number`) VALUES
(2, 3, '2026-07-18 08:00:00', '2026-07-20 18:00:00', 'Pending', 'DEMO123');
