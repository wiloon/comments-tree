-- default admin user, password: 0oVHFEqB
REPLACE INTO users (id,name,email,password) VALUES ('c31f5e0e-0e0c-4731-97dc-9c6675a0068c','admin','admin@admin.com','$2a$10$AR7t1b/kgGS2oiTrlrW2C.JkVAOT3ZviKj.2zvWZIm0lBnsOrTuX2');
-- test data
-- comments
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (1, 'msg1', 1, '2022-01-19 22:43:24', '2022-01-19 22:43:25');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (2, 'msg2', 1, '2022-01-19 23:13:08', '2022-01-19 23:13:09');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (3, 'msg3', 2, '2022-01-19 23:13:10', '2022-01-19 23:13:11');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (4, 'msg1.1', 1, '2022-01-19 23:13:12', '2022-01-19 23:13:12');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (5, 'msg1.2', 1, '2022-01-19 23:13:13', '2022-01-19 23:13:14');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (6, 'msg1.3', 2, '2022-01-19 23:13:15', '2022-01-19 23:13:15');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (7, 'msg1.1.1', 1, '2022-01-19 23:13:16', '2022-01-19 23:13:17');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (8, 'msg1.1.2', 2, '2022-01-19 23:13:18', '2022-01-19 23:13:19');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (9, 'msg2.1', 1, '2022-01-19 23:13:20', '2022-01-19 23:13:20');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (10, 'msg2.1.1', 1, '2022-01-19 23:13:21', '2022-01-19 23:13:22');
REPLACE INTO `comments` (`id`, `content`, `user_id`, `create_time`, `update_time`) VALUES (11, 'msg3.1', 1, '2022-01-19 23:13:23', '2022-01-19 23:13:24');

-- comments_tree_path
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (0, 1);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (0, 2);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (0, 3);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (1, 4);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (1, 5);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (1, 6);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (2, 9);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (3, 11);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (4, 8);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (4, 7);
REPLACE INTO `comments_tree_path` (`parent_id`, `child_id`) VALUES (9, 10);
