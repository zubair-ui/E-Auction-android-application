# E-Auction-android-application

A very basic Android Studio Application in kotlin, with PHP as a backend, mysqlight(XAMPP) as the database and is hosted locally using XAMPP
before running the application you would have to make changes in the res/xm/network-configuration.xml file and the com.example.e-auction.util.COnfiguration.kt
you would have to change the ip address to your machines local address before running the app, aslo dont forget to setup xampp and run xampp server with the php code, you would need to run the database query on phpmyadmin to create the schema of your database

**Screens:**
•	Splash Screen
•	Log In (Startup Screen) 
•	Sign Up
•	Home Screen (List of auctions with the ability to filter them by current auctions, past auctions , future auctions)
•	Add User Auction 
•	View Users Auction
•	Auctions which user has bided on

Screens which will have a bottom navigation bar are Home screen, View users Auction, Auction which user has bided on

**Flow of the App:**
1.	User will login to the app using their email and password, if they do not have an account they will have to signup first using their email, name and password.
2.	After logging in the user will see the home screen where the currently ongoing options can be seen with the ability to see past auctions and future auctions using a filter.
3.	The user can place a bid on an item which should be greater than the starting bid and the current highest bid.
4.	The user can use the bottom navigation bar to go to his auctions which he has added, there he can delete or update his auction or add a new auction
5.	If the user decides to add an auction they will have to provide the title, description, picture, start date, end date and the starting bid for the item.
6.	Using the bottom navigation bar, the user can also see the items he has bid on only and only if he is the highest bidder on that item.





**Database Schema:**
**•	User**
1)	User email (Primary key) (string)
2)	User name (String)
3)	Password (Hashed password) (string)

**•	Bid**
1)	bidId (Primary key) (long)
2)	title (String)
3)	description (String)
4)	image (String in the form of base64)
5)	starting bid (long)
6)	start date (String)
7)	end date (String)
8)	Highest bid (long)
9)	Bidder email (Foreign key from user) (String)

**Server Requirements:**
**•	Sign Up:**
The app will send the new user’s username, email and password. The server will check whether the user exists using his email, if it exists the server will send a message saying “user already exists” otherwise it will add the user and send the message “user has been added” 
**•	Log In:**
The app will send the user’s email and password, the server will check and return whether the user is correct or incorrect
**•	Bids:**
The server should get all bids from the database and filter them by the following:
1.	Bids which user has added
2.	Bids in which the user is the highest bidder
3.	Previous bids
4.	Current ongoing bids
5.	Future Bids
**•	Add new Auctions**
The server should cater the addition of new auctions
**•	Delete Auctions**
The server should cater the deletion of previous auction items placed by user
**•	Update Highest Bidder**
The server should be able to update the highest bidder on an item
**•	Update Auction**
The server should be able to update the user’s auction item




