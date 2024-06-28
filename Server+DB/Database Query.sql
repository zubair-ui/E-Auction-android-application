CREATE DATABASE db_e_auction_app;

USE db_e_auction_app;

CREATE TABLE `User` (
    userEmail VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(50)
);

CREATE TABLE BidItem (
    bidId INT AUTO_INCREMENT PRIMARY KEY,
    ownerEmail VARCHAR(50),
    title VARCHAR(50),
    description VARCHAR(100),
    image LONGTEXT,
    startingBid INT,
    highestBid INT,
    startDate VARCHAR(20),
    endDate VARCHAR(20),
    highestBidderEmail VARCHAR(50), 
    FOREIGN KEY (highestBidderEmail) REFERENCES `User` (userEmail)
);
