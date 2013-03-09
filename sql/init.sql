CREATE TABLE IF NOT EXISTS `forbidden` (
  `id_resource` int(10) NOT NULL AUTO_INCREMENT, 
  `website` varchar(30) CHARACTER SET utf8 NOT NULL DEFAULT '', 
  PRIMARY KEY (`id_resource`)
) ENGINE=MyISAM;
INSERT INTO `forbidden` (`id_resource`, `website`) VALUES (1, 'localhost') ON DUPLICATE KEY UPDATE website = 'localhost';
INSERT INTO `forbidden` (`id_resource`, `website`) VALUES (2, '127.0.0.1') ON DUPLICATE KEY UPDATE website = '127.0.0.1';

DROP TABLE IF EXISTS `logs`;
CREATE TABLE IF NOT EXISTS `logs` (`added` datetime NOT NULL DEFAULT '0000-00-00 00:00:00', `type` varchar(30) CHARACTER SET utf8 NOT NULL DEFAULT '', `info` text CHARACTER SET utf8 ) ENGINE=MyISAM;
INSERT INTO `logs` (`added`, `type`, `info`) VALUES (now(), 'INFO', 'Installation');

CREATE TABLE IF NOT EXISTS `resource` (
  `id_resource` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `website` varchar(100) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `port` int(11) NOT NULL DEFAULT '80',
  `pingtime` int(8) NOT NULL DEFAULT '0',
  `added` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `last_access` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `info` text CHARACTER SET utf8,
  `userid` varchar(10) CHARACTER SET utf8 NOT NULL DEFAULT '0',
  `ison` tinyint(1) NOT NULL DEFAULT '1',
  `sucess` int(20) NOT NULL DEFAULT '1',
  `fail` int(20) NOT NULL DEFAULT '0',
  `sms_send` enum('yes','no') CHARACTER SET utf8 NOT NULL DEFAULT 'no',
  `sms_out` int(10) NOT NULL DEFAULT '0',
  `die` int(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_resource`),
  UNIQUE KEY `website` (`website`,`port`),
  KEY `pingtime` (`pingtime`),
  KEY `die` (`die`)
) ENGINE=MyISAM;
INSERT INTO `resource` (`website`, `port`, `added`, `userid`) VALUES ('google.ru', '80', now(), '1') ON DUPLICATE KEY UPDATE website = 'google.ru', port = '80', userid = 1, added = now();
INSERT INTO `resource` (`website`, `port`, `added`, `userid`) VALUES ('ya.ru', '80', now(), '1') ON DUPLICATE KEY UPDATE website = 'ya.ru', port = '80', userid = 1, added = now();
INSERT INTO `resource` (`website`, `port`, `added`, `userid`) VALUES ('mail.ru', '80', now(), '1') ON DUPLICATE KEY UPDATE website = 'mail.ru', port = '80', userid = 1, added = now();


CREATE TABLE IF NOT EXISTS `users` (
  `id_user` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(40) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `class` varchar(3) CHARACTER SET utf8 NOT NULL DEFAULT '0',
  `secret` varchar(20) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `passhash` varchar(32) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `editsecret` varchar(20) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `added` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `last_login` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ip` varchar(15) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `icq` varchar(15) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `sms` int(10) NOT NULL DEFAULT '0',
  `sms_send` enum('yes','no') CHARACTER SET utf8 NOT NULL DEFAULT 'no',
  `phonemy` varchar(15) CHARACTER SET utf8 NOT NULL,
  `email` varchar(80) CHARACTER SET utf8 NOT NULL DEFAULT '',
  `warned` enum('yes','no') CHARACTER SET utf8 NOT NULL DEFAULT 'no',
  PRIMARY KEY (`id_user`)
) ENGINE=MyISAM;
INSERT INTO `users` (`username`, `class`, `secret`, `passhash`, `editsecret`, `added`, `phonemy`) VALUES ('testuser', '1', 'testuser', 'testuser', 'testuser', now(), 'testuser') ON DUPLICATE KEY UPDATE username = 'testuser', class = 'testuser' , secret = 'testuser', passhash = 'testuser', editsecret = 'testuser', added = 'testuser', phonemy = 'testuser';

