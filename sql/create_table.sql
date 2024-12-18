-- Drop the database if it already exists
DROP DATABASE IF EXISTS skindb;

-- Create the database
CREATE DATABASE skindb;

-- Use the database
USE skindb;

-- Create Weapon Table
CREATE TABLE Weapon (
                        weapon_id INT AUTO_INCREMENT PRIMARY KEY,
                        weapon_name VARCHAR(255) NOT NULL UNIQUE
);

-- Create Level Table
CREATE TABLE Level (
                       level_id INT AUTO_INCREMENT PRIMARY KEY,
                       level_name VARCHAR(255) NOT NULL UNIQUE,
                       price INT, -- NULL for variable pricing
                       icon VARCHAR(255) -- URL for level icon
);

-- Create Skin Table
CREATE TABLE Skin (
                      skin_id INT AUTO_INCREMENT PRIMARY KEY,
                      skin_name VARCHAR(255) NOT NULL,
                      weapon_id INT NOT NULL,
                      level_id INT NOT NULL,
                      price INT NOT NULL DEFAULT 0, -- 0 for free skins
                      vote_count INT DEFAULT 0,
                      icon VARCHAR(255) NOT NULL, -- URL for skin image
                      FOREIGN KEY (weapon_id) REFERENCES Weapon(weapon_id),
                      FOREIGN KEY (level_id) REFERENCES Level(level_id)
);

-- Create Rank Table (Rank is a reserved keyword, so it needs backticks)
CREATE TABLE `Rank` (
                        rank_id INT AUTO_INCREMENT PRIMARY KEY,
                        skin_id INT NOT NULL,
                        vote_count INT NOT NULL DEFAULT 0,
                        rank_position INT, -- Optional, for dynamic ranking
                        FOREIGN KEY (skin_id) REFERENCES Skin(skin_id)
);
