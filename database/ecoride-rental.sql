-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jul 20, 2026 at 07:00 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ecoride-rental`
--

-- --------------------------------------------------------

--
-- Table structure for table `customer`
--

CREATE TABLE `customer` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `account_balance` double DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customer`
--

INSERT INTO `customer` (`id`, `name`, `email`, `phone`, `account_balance`, `password`, `role`) VALUES
(1, 'Kristijan Stefanoski', 'stefanoskii.kristijan@gmail.com', '069817202', 0, '$2a$12$VOpt4k2BhQNvnpLL9th8quGcOcY2tVbNUeSpKYu6cJfXyO8rKuUa.', 'user'),
(2, 'Admin Kiksa', 'kiksa@admin.com', '1234567890', 0, '$2a$12$VOpt4k2BhQNvnpLL9th8quGcOcY2tVbNUeSpKYu6cJfXyO8rKuUa.', 'admin'),
(3, 'Kristijan Stefanoski', 'kiksastefan@gmail.com', '077869899', 0, '$2a$12$JWWgwqeA/PN.sMDZIbCvDe5kYTCFl5y1Y87zxhGMeGAkvF10Apdau', 'user'),
(4, 'Kristijan Stefanoski', 'stefanoskii.kristijan1@gmail.com', '069817202', 0, '$2a$12$qm0TJq2U44fZ/cqCMTmqg.V1Xy.WPX/JBtPTjUoNWgyU2ljsYwAE.', 'user'),
(5, 'Kristijan Stefanoski', '1stefanoskii.kristijan@gmail.com', '069817202', 0, '$2a$12$JWWgwqeA/PN.sMDZIbCvDe5kYTCFl5y1Y87zxhGMeGAkvF10Apdau', 'user');

-- --------------------------------------------------------

--
-- Table structure for table `ebike`
--

CREATE TABLE `ebike` (
  `id` int(11) NOT NULL,
  `model` varchar(255) DEFAULT NULL,
  `battery_level` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `station_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ebike`
--

INSERT INTO `ebike` (`id`, `model`, `battery_level`, `status`, `price`, `station_id`) VALUES
(1, 'EcoStrom X1', 95, 'Available', 15, 1),
(2, 'EcoStrom X2', 70, 'Available', 35, 1),
(3, 'BMX M1', 50, 'Available', 20, 2);

-- --------------------------------------------------------

--
-- Table structure for table `payment`
--

CREATE TABLE `payment` (
  `id` int(11) NOT NULL,
  `rental_id` int(11) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `payment_status` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rental`
--

CREATE TABLE `rental` (
  `id` int(11) NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `ebike_id` int(11) DEFAULT NULL,
  `rental_start_time` datetime DEFAULT NULL,
  `rental_end_time` datetime DEFAULT NULL,
  `rental_status` varchar(255) DEFAULT NULL,
  `passport_number` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rental`
--

INSERT INTO `rental` (`id`, `customer_id`, `ebike_id`, `rental_start_time`, `rental_end_time`, `rental_status`, `passport_number`) VALUES
(1, 1, 1, '2026-07-17 01:00:00', '2026-07-17 02:00:00', 'Rejected', '123'),
(2, 2, 3, '2026-07-18 07:53:00', '2026-07-20 01:53:00', 'Pending', '123566');

-- --------------------------------------------------------

--
-- Table structure for table `station`
--

CREATE TABLE `station` (
  `id` int(11) NOT NULL,
  `location` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `station`
--

INSERT INTO `station` (`id`, `location`) VALUES
(1, 'Maribor'),
(2, 'Ljubljana');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `customer`
--
ALTER TABLE `customer`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_customer_email` (`email`);

--
-- Indexes for table `ebike`
--
ALTER TABLE `ebike`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_ebike_station` (`station_id`);

--
-- Indexes for table `payment`
--
ALTER TABLE `payment`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `rental`
--
ALTER TABLE `rental`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_rental_customer` (`customer_id`),
  ADD KEY `fk_rental_ebike` (`ebike_id`);

--
-- Indexes for table `station`
--
ALTER TABLE `station`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `customer`
--
ALTER TABLE `customer`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `ebike`
--
ALTER TABLE `ebike`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `payment`
--
ALTER TABLE `payment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `rental`
--
ALTER TABLE `rental`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `station`
--
ALTER TABLE `station`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `ebike`
--
ALTER TABLE `ebike`
  ADD CONSTRAINT `fk_ebike_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `rental`
--
ALTER TABLE `rental`
  ADD CONSTRAINT `fk_rental_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  ADD CONSTRAINT `fk_rental_ebike` FOREIGN KEY (`ebike_id`) REFERENCES `ebike` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
