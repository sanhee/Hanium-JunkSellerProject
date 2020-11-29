-- phpMyAdmin SQL Dump
-- version 4.2.9
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: 20-07-26 17:15
-- 서버 버전: 5.6.20
-- PHP 버전: 5.6.0

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- 데이터베이스: `vm_db`
--

-- --------------------------------------------------------

--
-- 테이블 구조 `device_log`
--

CREATE TABLE IF NOT EXISTS `device_log` (
`seq` int(11) NOT NULL,
  `id` varchar(40) NOT NULL,
  `date` date NOT NULL,
  `soju` int(11) NOT NULL DEFAULT '0',
  `makju` int(11) NOT NULL DEFAULT '0',
  `can` int(11) NOT NULL DEFAULT '0',
  `book` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;

--
-- 테이블의 덤프 데이터 `device_log`
--

INSERT INTO `device_log` (`seq`, `id`, `date`, `soju`, `makju`, `can`, `book`) VALUES
(1, '09O-0706041416', '2020-07-01', 8, 10, 4, 11),
(2, '09O-0706041416', '2020-07-02', 5, 11, 3, 9),
(3, '09O-0706041416', '2020-07-03', 8, 10, 4, 9),
(4, '09O-0706041416', '2020-07-04', 7, 12, 3, 10),
(5, '09O-0706041416', '2020-07-05', 5, 11, 5, 10),
(6, '09O-0706041416', '2020-07-06', 7, 10, 3, 9),
(7, '09O-0706041416', '2020-07-07', 8, 10, 5, 11),
(8, '09O-0706041416', '2020-07-08', 8, 13, 4, 10),
(9, '09O-0706041416', '2020-07-09', 5, 12, 6, 8),
(10, '09O-0706041416', '2020-07-10', 7, 13, 5, 11),
(11, '09O-0706041416', '2020-07-11', 5, 12, 6, 9),
(12, '09O-0706041416', '2020-07-12', 5, 12, 3, 9),
(13, '09O-0706041416', '2020-07-13', 6, 12, 6, 9),
(14, '09O-0706041416', '2020-07-14', 7, 11, 6, 8),
(15, '09O-0706041416', '2020-07-15', 8, 11, 4, 11),
(16, '09O-0706041416', '2020-07-16', 8, 11, 4, 11),
(17, '09O-0706041416', '2020-07-17', 8, 11, 6, 9),
(18, '09O-0706041416', '2020-07-18', 8, 12, 4, 8),
(19, '09O-0706041416', '2020-07-19', 5, 11, 3, 10),
(20, '09O-0706041416', '2020-07-20', 0, 12, 3, 9),
(21, '09O-0706041416', '2020-07-21', 1, 2, 4, 11),
(22, '09O-0706041416', '2020-07-22', 6, 0, 0, 11),
(23, '09O-0706041416', '2020-07-23', 8, 12, 16, 9),
(26, '09O-0706041416', '2020-07-24', 0, 0, 0, 1),
(27, '09O-0706041416', '2020-07-25', 1, 0, 0, 0);

-- --------------------------------------------------------

--
-- 테이블 구조 `device_state`
--

CREATE TABLE IF NOT EXISTS `device_state` (
`seq` int(11) NOT NULL,
  `id` varchar(100) NOT NULL,
  `location` longtext NOT NULL,
  `construction` tinyint(1) NOT NULL,
  `object_capacity` longtext NOT NULL,
  `have_money` longtext NOT NULL,
  `gps_latitude` float NOT NULL,
  `gps_longitude` float NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;

--
-- 테이블의 덤프 데이터 `device_state`
--

INSERT INTO `device_state` (`seq`, `id`, `location`, `construction`, `object_capacity`, `have_money`, `gps_latitude`, `gps_longitude`) VALUES
(37, 'rmi-0706040253', '대한민국 경기도 시흥시 정왕1동 2121', 1, 'Available', '20/20)20]20', 37.3412, 37.3412),
(38, 'gg7-0706041359', '대한민국 경기도 시흥시 정왕1동 2121', 1, 'Available', '20/20)20]20', 37.3413, 37.3413),
(39, '09O-0706041416', '대한민국 시흥시 정왕1동 한국산업기술대정문', 1, 'Available', '522/90)70]70', 37.3386, 37.3386),
(40, 'nBy-0708140827', '대한민국 경기도 시흥시 정왕1동 2121', 1, 'Available', '2/20)40]50', 37.3414, 37.3414),
(41, 'N0D-0715172212', '대한민국 시흥시 정왕1동 한국산업기술대정문', 1, 'Available', '10/20)20]20', 37.3389, 37.3389),
(42, '', '대한민국 경기도 시흥시 정왕1동 2121', 1, 'Available', '20/20)20]20', 37.3412, 37.3412);

-- --------------------------------------------------------

--
-- 테이블 구조 `marketprice`
--

CREATE TABLE IF NOT EXISTS `marketprice` (
`seq` int(11) NOT NULL,
  `type` varchar(30) NOT NULL,
  `price` int(11) NOT NULL,
  `weight` int(11) NOT NULL,
  `updateTime` date NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

--
-- 테이블의 덤프 데이터 `marketprice`
--

INSERT INTO `marketprice` (`seq`, `type`, `price`, `weight`, `updateTime`) VALUES
(1, 'soju', 2500, 450, '2020-07-20'),
(2, 'makju', 3000, 500, '2020-07-19'),
(3, 'book', 350, 250, '2020-07-12'),
(4, 'can', 1250, 450, '2020-07-05');

--
-- 덤프된 테이블의 인덱스
--

--
-- 테이블의 인덱스 `device_log`
--
ALTER TABLE `device_log`
 ADD PRIMARY KEY (`seq`);

--
-- 테이블의 인덱스 `device_state`
--
ALTER TABLE `device_state`
 ADD PRIMARY KEY (`seq`);

--
-- 테이블의 인덱스 `marketprice`
--
ALTER TABLE `marketprice`
 ADD PRIMARY KEY (`seq`);

--
-- 덤프된 테이블의 AUTO_INCREMENT
--

--
-- 테이블의 AUTO_INCREMENT `device_log`
--
ALTER TABLE `device_log`
MODIFY `seq` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=28;
--
-- 테이블의 AUTO_INCREMENT `device_state`
--
ALTER TABLE `device_state`
MODIFY `seq` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=43;
--
-- 테이블의 AUTO_INCREMENT `marketprice`
--
ALTER TABLE `marketprice`
MODIFY `seq` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=5;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
