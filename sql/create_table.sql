-- Drop the database if it already exists
DROP DATABASE IF EXISTS skindb;

-- Create the database
CREATE DATABASE skindb;

-- Use the database
USE skindb;


-- Create Weapon Table
CREATE TABLE weapon (
                        weapon_id INT AUTO_INCREMENT PRIMARY KEY,
                        weapon_name VARCHAR(255) NOT NULL UNIQUE
);

-- Create Skin Table
CREATE TABLE skin (
                      skin_id INT AUTO_INCREMENT PRIMARY KEY,
                      skin_name VARCHAR(255) NOT NULL,
                      weapon_id INT NOT NULL,
                      price INT NOT NULL DEFAULT 0, -- 0 for free skins
                      vote_count INT DEFAULT 0,
                      icon VARCHAR(255), -- URL for skin image
                      FOREIGN KEY (weapon_id) REFERENCES weapon(weapon_id)
);

-- Create Rank Table (Rank is a reserved keyword, so it needs backticks)
CREATE TABLE `rank` (
                        rank_id INT AUTO_INCREMENT PRIMARY KEY,
                        skin_id INT NOT NULL,
                        vote_count INT NOT NULL DEFAULT 0,
                        rank_position INT, -- Optional, for dynamic ranking
                        FOREIGN KEY (skin_id) REFERENCES skin(skin_id)
);