-- Drop the database if it already exists
DROP DATABASE IF EXISTS skindb;

-- Create the database
CREATE DATABASE skindb;

-- Use the database
USE skindb;


-- Create Weapon Table
CREATE TABLE weapon (
                        weapon_id INT AUTO_INCREMENT PRIMARY KEY,
                        weapon_name VARCHAR(255) NOT NULL UNIQUE,
                        icon VARCHAR(255)
);

-- Insert weapons
INSERT INTO weapon (weapon_name, icon) VALUES
                                           ('classic', 'https://media.valorant-api.com/weapons/29a0cfab-485b-f5d5-779a-b59f85e204a8/displayicon.png'),
                                           ('shorty', 'https://media.valorant-api.com/weapons/42da8ccc-40d5-affc-beec-15aa47b42eda/displayicon.png'),
                                           ('frenzy', 'https://media.valorant-api.com/weapons/44d4e95c-4157-0037-81b2-17841bf2e8e3/displayicon.png'),
                                           ('ghost', 'https://media.valorant-api.com/weapons/1baa85b4-4c70-1284-64bb-6481dfc3bb4e/displayicon.png'),
                                           ('sheriff', 'https://media.valorant-api.com/weapons/e336c6b8-418d-9340-d77f-7a9e4cfe0702/displayicon.png'),
                                           ('stinger', 'https://media.valorant-api.com/weapons/f7e1b454-4ad4-1063-ec0a-159e56b58941/displayicon.png'),
                                           ('spectre', 'https://media.valorant-api.com/weapons/462080d1-4035-2937-7c09-27aa2a5c27a7/displayicon.png'),
                                           ('bucky', 'https://media.valorant-api.com/weapons/910be174-449b-c412-ab22-d0873436b21b/displayicon.png'),
                                           ('judge', 'https://media.valorant-api.com/weapons/ec845bf4-4f79-ddda-a3da-0db3774b2794/displayicon.png'),
                                           ('bulldog', 'https://media.valorant-api.com/weapons/ae3de142-4d85-2547-dd26-4e90bed35cf7/displayicon.png'),
                                           ('guardian', 'https://media.valorant-api.com/weapons/4ade7faa-4cf1-8376-95ef-39884480959b/displayicon.png'),
                                           ('phantom', 'https://media.valorant-api.com/weapons/ee8e8d15-496b-07ac-e5f6-8fae5d4c7b1a/displayicon.png'),
                                           ('vandal', 'https://media.valorant-api.com/weapons/9c82e19d-4575-0200-1a81-3eacf00cf872/displayicon.png'),
                                           ('marshal', 'https://media.valorant-api.com/weapons/c4883e50-4494-202c-3ec3-6b8a9284f00b/displayicon.png'),
                                           ('outlaw', 'https://media.valorant-api.com/weapons/5f0aaf7a-4289-3998-d5ff-eb9a5cf7ef5c/displayicon.png'),
                                           ('operator', 'https://media.valorant-api.com/weapons/a03b24d3-4319-996d-0f8c-94bbfba1dfc7/displayicon.png'),
                                           ('ares', 'https://media.valorant-api.com/weapons/55d8a0f4-4274-ca67-fe2c-06ab45efdf58/displayicon.png'),
                                           ('odin', 'https://media.valorant-api.com/weapons/63e6c2b6-4a8e-869c-3d4c-e38355226584/displayicon.png'),
                                           ('melee', 'https://media.valorant-api.com/weapons/2f59173c-4bed-b6c3-2191-dea9b58be9c7/displayicon.png');

-- Create Skin Table
CREATE TABLE skin (
                      skin_id INT AUTO_INCREMENT PRIMARY KEY,
                      skin_name VARCHAR(255) NOT NULL,
                      weapon_id INT NOT NULL,
                      win_num INT DEFAULT 0,
                      vote_count INT NOT NULL DEFAULT 0,
                      icon VARCHAR(255),
                      FOREIGN KEY (weapon_id) REFERENCES weapon(weapon_id)
);

-- Create Rank Table
CREATE TABLE `rank` (
                        rank_id INT AUTO_INCREMENT PRIMARY KEY,
                        skin_id INT NOT NULL,
                        vote_count INT NOT NULL DEFAULT 0,
                        rank_position INT,
                        FOREIGN KEY (skin_id) REFERENCES skin(skin_id)
);