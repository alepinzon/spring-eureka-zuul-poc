CREATE DATABASE `test` /*!40100 DEFAULT CHARACTER SET utf8 */;

CREATE TABLE `error` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_name` varchar(100) NOT NULL,
  `sequence_number` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_name` varchar(100) NOT NULL,
  `sequence_number` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_UN` (`sequence_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
