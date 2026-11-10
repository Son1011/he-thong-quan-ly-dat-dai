-- Login accounts after password reset:
-- admin / admin1234
-- province01 / admin123
-- central01 / admin123
-- commune01 / admin123

-- MySQL dump 10.13  Distrib 9.3.0, for Win64 (x86_64)
--
-- Host: localhost    Database: land_management_system
-- ------------------------------------------------------
-- Server version	9.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `land_management_system`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `land_management_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `land_management_system`;

--
-- Table structure for table `administrative_unit`
--

DROP TABLE IF EXISTS `administrative_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `administrative_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `unit_level` enum('CENTRAL','PROVINCE','COMMUNE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `unit_kind` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_unit_parent` (`parent_id`),
  CONSTRAINT `fk_unit_parent` FOREIGN KEY (`parent_id`) REFERENCES `administrative_unit` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `administrative_unit`
--

LOCK TABLES `administrative_unit` WRITE;
/*!40000 ALTER TABLE `administrative_unit` DISABLE KEYS */;
INSERT INTO `administrative_unit` VALUES (1,'Trung ương','CENTRAL',NULL,1,'2026-05-13 20:08:30.146','2026-05-13 20:08:30.146',NULL),(2,'Tỉnh Hà Nội','PROVINCE',1,1,'2026-05-13 20:08:30.146','2026-05-13 20:08:30.146',NULL),(3,'Xã Đông Anh','COMMUNE',2,1,'2026-05-13 20:08:30.146','2026-05-13 20:08:30.146',NULL),(4,'Xã Test 03','COMMUNE',2,1,'2026-05-22 09:10:53.935','2026-05-22 09:10:53.935','COMMUNE');
/*!40000 ALTER TABLE `administrative_unit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `approval_history`
--

DROP TABLE IF EXISTS `approval_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dossier_id` bigint NOT NULL,
  `actor_user_id` bigint NOT NULL,
  `action_type` enum('CREATE','SUBMIT','APPROVE','REJECT','RETURN','TRANSFER','COMPLETE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `action_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `actor_unit_id` bigint DEFAULT NULL,
  `from_unit_id` bigint DEFAULT NULL,
  `signature_base64_png` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `to_unit_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_approval_dossier` (`dossier_id`),
  KEY `idx_approval_actor` (`actor_user_id`),
  KEY `idx_approval_actor_unit` (`actor_unit_id`),
  KEY `idx_approval_action` (`action_code`),
  KEY `idx_approval_created` (`created_at`),
  CONSTRAINT `fk_approval_actor` FOREIGN KEY (`actor_user_id`) REFERENCES `user_account` (`id`),
  CONSTRAINT `fk_approval_dossier` FOREIGN KEY (`dossier_id`) REFERENCES `dossier` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approval_history`
--

LOCK TABLES `approval_history` WRITE;
/*!40000 ALTER TABLE `approval_history` DISABLE KEYS */;
INSERT INTO `approval_history` VALUES (1,1,2,'CREATE','Tạo hồ sơ cấp đất','2026-05-13 19:33:31.487',NULL,NULL,NULL,NULL,NULL),(2,1,3,'TRANSFER','Chuyển hồ sơ sang phòng thẩm định','2026-05-13 19:33:31.487',NULL,NULL,NULL,NULL,NULL),(3,2,2,'SUBMIT','Nộp hồ sơ chuyển nhượng','2026-05-13 19:33:31.487',NULL,NULL,NULL,NULL,NULL),(4,2,4,'CREATE',NULL,'2026-05-20 11:41:40.320','CREATE',3,NULL,NULL,2),(5,2,2,'CREATE','Tỉnh duyệt hồ sơ','2026-05-20 11:58:24.632','APPROVE',2,2,NULL,3),(6,3,4,'CREATE',NULL,'2026-05-20 12:02:23.829','CREATE',3,NULL,NULL,2),(7,3,2,'CREATE','Tỉnh chuyển hồ sơ lên Trung ương','2026-05-20 12:11:40.064','ESCALATE',2,2,NULL,1),(8,3,3,'CREATE','Trung ương duyệt hồ sơ','2026-05-20 12:20:21.974','APPROVE',1,1,NULL,3),(9,4,4,'CREATE',NULL,'2026-05-21 12:33:25.803','CREATE',3,NULL,NULL,2),(10,4,2,'CREATE','Hồ sơ thiếu giấy tờ, cần bổ sung thông tin công dân','2026-05-21 12:47:49.106','RETURN',2,2,NULL,3),(11,4,4,'CREATE','Đã bổ sung hồ sơ và gửi lại cấp tỉnh','2026-05-21 12:50:15.312','SUBMIT',3,3,NULL,2);
/*!40000 ALTER TABLE `approval_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `dossier_id` bigint DEFAULT NULL,
  `entity_id` bigint DEFAULT NULL,
  `entity_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `error_message` text COLLATE utf8mb4_unicode_ci,
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_value` text COLLATE utf8mb4_unicode_ci,
  `old_value` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_agent` text COLLATE utf8mb4_unicode_ci,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_user_id` (`user_id`),
  KEY `idx_audit_dossier_id` (`dossier_id`),
  KEY `idx_audit_timestamp` (`created_at`),
  KEY `idx_audit_action` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_log`
--

LOCK TABLES `audit_log` WRITE;
/*!40000 ALTER TABLE `audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `citizen`
--

DROP TABLE IF EXISTS `citizen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `citizen` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `identity_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `identity_number` (`identity_number`),
  KEY `idx_citizen_identity` (`identity_number`),
  KEY `idx_citizen_name` (`full_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `citizen`
--

LOCK TABLES `citizen` WRITE;
/*!40000 ALTER TABLE `citizen` DISABLE KEYS */;
INSERT INTO `citizen` VALUES (1,'Nguyễn Văn A','1995-05-20','MALE','001203456789','0988888888','nguyenvana@gmail.com','Đông Anh - Hà Nội','2026-05-13 20:08:30.157');
/*!40000 ALTER TABLE `citizen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dossier`
--

DROP TABLE IF EXISTS `dossier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dossier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dossier_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `citizen_id` bigint DEFAULT NULL,
  `dossier_type_id` bigint NOT NULL,
  `status_id` bigint DEFAULT NULL,
  `priority` enum('NORMAL','URGENT','EMERGENCY') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL',
  `current_step` enum('RECEIVE','VERIFY','APPROVE','COMPLETE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECEIVE',
  `origin_unit_id` bigint NOT NULL,
  `assigned_to_unit_id` bigint NOT NULL,
  `created_by_user_id` bigint NOT NULL,
  `received_at` datetime DEFAULT NULL,
  `due_date` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `citizen_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `citizen_identity_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `citizen_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `citizen_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_to_central` bit(1) NOT NULL,
  `status_code` enum('APPROVED','ESCALATED','PENDING','RETURNED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dossier_code` (`dossier_code`),
  UNIQUE KEY `UKtgbsr2osxysy8aeuo8jpijjjg` (`dossier_code`),
  KEY `fk_dossier_citizen` (`citizen_id`),
  KEY `fk_dossier_type` (`dossier_type_id`),
  KEY `idx_dossier_code` (`dossier_code`),
  KEY `idx_dossier_status` (`status_id`),
  KEY `idx_dossier_assigned` (`assigned_to_unit_id`),
  KEY `idx_dossier_due_date` (`due_date`),
  KEY `idx_dossier_origin_unit` (`origin_unit_id`),
  KEY `idx_dossier_assigned_unit` (`assigned_to_unit_id`),
  KEY `idx_dossier_created_by` (`created_by_user_id`),
  KEY `idx_dossier_sent_to_central` (`sent_to_central`),
  KEY `idx_dossier_created_at` (`created_at`),
  KEY `idx_dossier_status_origin` (`status_code`,`origin_unit_id`),
  CONSTRAINT `fk_dossier_assigned_unit` FOREIGN KEY (`assigned_to_unit_id`) REFERENCES `administrative_unit` (`id`),
  CONSTRAINT `fk_dossier_citizen` FOREIGN KEY (`citizen_id`) REFERENCES `citizen` (`id`),
  CONSTRAINT `fk_dossier_created_user` FOREIGN KEY (`created_by_user_id`) REFERENCES `user_account` (`id`),
  CONSTRAINT `fk_dossier_origin_unit` FOREIGN KEY (`origin_unit_id`) REFERENCES `administrative_unit` (`id`),
  CONSTRAINT `fk_dossier_status` FOREIGN KEY (`status_id`) REFERENCES `dossier_status` (`id`),
  CONSTRAINT `fk_dossier_type` FOREIGN KEY (`dossier_type_id`) REFERENCES `dossier_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dossier`
--

LOCK TABLES `dossier` WRITE;
/*!40000 ALTER TABLE `dossier` DISABLE KEYS */;
INSERT INTO `dossier` VALUES (1,'HS-2026-000001','Hồ sơ xin cấp đất',1,1,2,'NORMAL','VERIFY',3,2,1,'2026-05-13 20:08:30','2026-05-28 20:08:30',NULL,'2026-05-13 20:08:30.173','2026-05-13 20:08:30.173',NULL,NULL,'',NULL,_binary '\0','APPROVED'),(2,'HS-1779277300280','Hồ sơ cấp giấy chứng nhận quyền sử dụng đất',NULL,1,NULL,'NORMAL','RECEIVE',3,3,4,NULL,NULL,NULL,'2026-05-20 11:41:40.284','2026-05-20 11:58:24.635','Phường 1, Quận 1','012345678901','Nguyễn Văn A','0987654321',_binary '\0','APPROVED'),(3,'HS-1779278543816','Hồ sơ cần chuyển Trung ương',NULL,1,NULL,'NORMAL','RECEIVE',3,3,4,NULL,NULL,NULL,'2026-05-20 12:02:23.816','2026-05-20 12:20:21.987','Xã Đông Anh, Hà Nội','098765432109','Trần Văn B','0912345678',_binary '\0','APPROVED'),(4,'HS-1779366805777','Hồ sơ test submit',NULL,1,NULL,'NORMAL','RECEIVE',3,2,4,NULL,NULL,NULL,'2026-05-21 12:33:25.781','2026-05-21 12:50:15.312','Phường A, Quận B, Tỉnh C','012345678901','Nguyễn Văn A','0912345678',_binary '\0','PENDING');
/*!40000 ALTER TABLE `dossier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dossier_attachment`
--

DROP TABLE IF EXISTS `dossier_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dossier_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dossier_id` bigint NOT NULL,
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `storage_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `uploaded_by_user_id` bigint NOT NULL,
  `uploaded_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `fk_attachment_user` (`uploaded_by_user_id`),
  KEY `idx_attachment_dossier` (`dossier_id`),
  KEY `idx_attachment_uploaded` (`uploaded_at`),
  CONSTRAINT `fk_attachment_dossier` FOREIGN KEY (`dossier_id`) REFERENCES `dossier` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_attachment_user` FOREIGN KEY (`uploaded_by_user_id`) REFERENCES `user_account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dossier_attachment`
--

LOCK TABLES `dossier_attachment` WRITE;
/*!40000 ALTER TABLE `dossier_attachment` DISABLE KEYS */;
INSERT INTO `dossier_attachment` VALUES (1,1,'so_do.pdf','application/pdf','uploads/so_do.pdf',204800,1,'2026-05-13 20:08:30.183'),(2,4,'approval_history_full_detail.xlsx','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet','e78f5624-63d2-46e8-9585-ea4081878f9f.xlsx',NULL,4,'2026-05-21 13:23:40.517');
/*!40000 ALTER TABLE `dossier_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dossier_status`
--

DROP TABLE IF EXISTS `dossier_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dossier_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_final` tinyint(1) NOT NULL DEFAULT '0',
  `color_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `UKsekqrbcow73whn8njk928gneh` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dossier_status`
--

LOCK TABLES `dossier_status` WRITE;
/*!40000 ALTER TABLE `dossier_status` DISABLE KEYS */;
INSERT INTO `dossier_status` VALUES (1,'SUBMITTED','Đã gửi',0,NULL),(2,'UNDER_REVIEW','Đang xử lý',0,NULL),(3,'APPROVED','Đã duyệt',1,NULL),(4,'REJECTED','Từ chối',1,NULL);
/*!40000 ALTER TABLE `dossier_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dossier_transition`
--

DROP TABLE IF EXISTS `dossier_transition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dossier_transition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dossier_id` bigint NOT NULL,
  `action_type` enum('CREATE','SUBMIT','TRANSFER','APPROVE','REJECT','RETURN','COMPLETE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_status_id` bigint DEFAULT NULL,
  `to_status_id` bigint NOT NULL,
  `from_unit_id` bigint DEFAULT NULL,
  `to_unit_id` bigint DEFAULT NULL,
  `actor_user_id` bigint NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `action_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `fk_transition_from_status` (`from_status_id`),
  KEY `fk_transition_to_status` (`to_status_id`),
  KEY `fk_transition_from_unit` (`from_unit_id`),
  KEY `fk_transition_to_unit` (`to_unit_id`),
  KEY `fk_transition_actor` (`actor_user_id`),
  KEY `idx_transition_dossier` (`dossier_id`),
  CONSTRAINT `fk_transition_actor` FOREIGN KEY (`actor_user_id`) REFERENCES `user_account` (`id`),
  CONSTRAINT `fk_transition_dossier` FOREIGN KEY (`dossier_id`) REFERENCES `dossier` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transition_from_status` FOREIGN KEY (`from_status_id`) REFERENCES `dossier_status` (`id`),
  CONSTRAINT `fk_transition_from_unit` FOREIGN KEY (`from_unit_id`) REFERENCES `administrative_unit` (`id`),
  CONSTRAINT `fk_transition_to_status` FOREIGN KEY (`to_status_id`) REFERENCES `dossier_status` (`id`),
  CONSTRAINT `fk_transition_to_unit` FOREIGN KEY (`to_unit_id`) REFERENCES `administrative_unit` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dossier_transition`
--

LOCK TABLES `dossier_transition` WRITE;
/*!40000 ALTER TABLE `dossier_transition` DISABLE KEYS */;
INSERT INTO `dossier_transition` VALUES (1,1,'TRANSFER',1,2,3,2,1,'Chuyển hồ sơ lên tỉnh xử lý','2026-05-13 20:08:30.179');
/*!40000 ALTER TABLE `dossier_transition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dossier_type`
--

DROP TABLE IF EXISTS `dossier_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dossier_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `processing_days` int NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `UKk56l0d3g2wc9gu55ssodbxj94` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dossier_type`
--

LOCK TABLES `dossier_type` WRITE;
/*!40000 ALTER TABLE `dossier_type` DISABLE KEYS */;
INSERT INTO `dossier_type` VALUES (1,'LAND_ALLOCATION','Cấp đất',15,NULL),(2,'LAND_TRANSFER','Chuyển nhượng đất',10,NULL);
/*!40000 ALTER TABLE `dossier_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `UKc36say97xydpmgigg38qv5l2p` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ADMIN','Quản trị hệ thống',NULL),(2,'COMMUNE_OFFICER','Cán bộ xã',NULL),(3,'PROVINCE_OFFICER','Cán bộ tỉnh',NULL),(4,'CENTRAL_OFFICER','Cán bộ trung ương',NULL);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_account`
--

DROP TABLE IF EXISTS `user_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role_id` bigint DEFAULT NULL,
  `unit_id` bigint NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `is_first_login` tinyint(1) NOT NULL DEFAULT '1',
  `password_changed_at` datetime DEFAULT NULL,
  `password_expires_at` datetime DEFAULT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `must_change_password` bit(1) NOT NULL,
  `role_code` enum('ADMIN','CENTRAL_OFFICER','COMMUNE_OFFICER','PROVINCE_OFFICER') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `UKcastjbvpeeus0r8lbpehiu0e4` (`username`),
  UNIQUE KEY `UKhl02wv5hym99ys465woijmfib` (`email`),
  KEY `idx_user_role` (`role_id`),
  KEY `idx_user_unit` (`unit_id`),
  KEY `idx_user_active` (`is_active`),
  KEY `idx_user_username` (`username`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `fk_user_unit` FOREIGN KEY (`unit_id`) REFERENCES `administrative_unit` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_account`
--

LOCK TABLES `user_account` WRITE;
/*!40000 ALTER TABLE `user_account` DISABLE KEYS */;
INSERT INTO `user_account` VALUES (1,'admin','$2a$10$mK2yC1j9bARDpjt14V7MQ.yh1HkGXQ6j6T7Of1kmXGcmpjinNff4G','Nguyễn Văn Admin','admin@land.gov.vn','0900000001','Hà Nội',1,1,1,0,'2026-05-13 20:08:30','2026-08-11 20:08:30',NULL,'2026-05-13 20:08:30.151','2026-05-21 12:21:41.349',_binary '\0','ADMIN'),(2,'province01','$2a$10$T6ubiDRiYtvyJB.McrcIl.Qz1KLChu.WDjqQh2YNLpUefmsuwFGT6','Cán bộ tỉnh Hà Nội','province01@land.gov.vn','0900000002','Hà Nội',3,2,1,0,NULL,NULL,NULL,'2026-05-19 18:11:45.826','2026-05-20 11:56:24.799',_binary '\0','PROVINCE_OFFICER'),(3,'central01','$2a$10$T6ubiDRiYtvyJB.McrcIl.Qz1KLChu.WDjqQh2YNLpUefmsuwFGT6','Cán bộ trung ương','central01@land.gov.vn','0900000004','Trung ương',4,1,1,0,NULL,NULL,NULL,'2026-05-19 18:11:45.842','2026-05-20 12:17:20.744',_binary '\0','CENTRAL_OFFICER'),(4,'commune01','$2a$10$T6ubiDRiYtvyJB.McrcIl.Qz1KLChu.WDjqQh2YNLpUefmsuwFGT6','Cán bộ phường Hoàn Kiếm','commune01@land.gov.vn','0900000003','Phường Hoàn Kiếm, Hà Nội',2,3,1,0,NULL,NULL,NULL,'2026-05-19 18:17:18.124','2026-05-20 10:57:53.384',_binary '\0','COMMUNE_OFFICER'),(5,'testuser04','$2a$10$bwODfYc4apbPhCsBj6DntujYI8wAouJfcVzpEdF1.zuwLwDHWMEba','Nguyễn Văn Test','testuser04@example.com',NULL,NULL,NULL,3,1,1,NULL,NULL,NULL,'2026-05-21 14:07:02.283','2026-05-21 14:07:02.284',_binary '','COMMUNE_OFFICER'),(8,'testuser10','$2a$10$5DHSZTOegq/E6jJCGtiHRucj.6ni1PiEYrp8k9NoxdSZOh4rxT0rC','Nguyen Van Test','testuser10@example.com',NULL,NULL,NULL,3,1,1,NULL,NULL,NULL,'2026-05-22 09:09:59.886','2026-05-22 09:09:59.886',_binary '','COMMUNE_OFFICER');
/*!40000 ALTER TABLE `user_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'land_management_system'
--

--
-- Dumping routines for database 'land_management_system'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-22 16:24:43
