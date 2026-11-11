
  Agricultural Marketplace System.
  Android application connecting farmers and buyers.

PART A

	------------------------------------
	| Full Name       | Student Number |
	|-----------------|----------------|
	| Witness Mudenda | 2521886        |
	------------------------------------
  


1.  .Problem Statement
Most farmers rely on traditional methods of selling produce such as open markets and brokers. 
These methods often result in delayed transactions, reduced profits, and limited market reach. 
Buyers also face difficulties in identifying reliable suppliers and obtaining current information on agricultural products. Consequently, there is a need for a mobile system that connects farmers directly with buyers.

2. .Login Screen
Used to enter Phone number and password to authenticate the user.

 .Registration Screen.
Used to enter user details to allow login into the system

 .Home Screen.
Conatins the Add Product button, View Products button and the Menu.

 .Menu Screen.
Contains the Make Payments, Track Goods, Orders, My Profile and the Logout buttons.

 .My Profile Screen.
Contains the fields where the user views their details and make adjustments when need be. it has a back and logout buttons.

 .Order Management Screen.
Where orders are placed and viewed with a delete, new order and back to home buttons.

 .Track Goods Screen
This is where the users are able to track the movement of their orders abd know if the ordered goods have been paid.

 .Wallet Screen
This is where payments are made or done during an order and means of payments are done through mobile money or bank transaction.

 .Add Product Screen
This is an interface through which enables users to add and save new products.

 .View Products Screen
This is where customers view the products they have ordered have the provision to make payments.


3. .Clone the repository called AgroMarketPlace. Open App in Android folder in Android Studio. Wait for Gradle sync to finish. Press Run within android while the phone connected to .the android phone or emulator.

4.

Concept 1: Local Data Persistence Using Android Room Database

 • What It Is:
Android Room is an object-relational mapping (ORM) persistence library that sits on top of SQLite. It uses compile-time verified annotations—specifically @Entity to define database tables, @Dao (Data Access Objects) to define SQL queries, and @Database to manage the database connection—alongside Kotlin Coroutines and Flow for reactive, asynchronous data updates.

 • Why the AgroMarket App Needs It:
Initially, the AgroMarket system relied on temporary in-memory lists (ProductRepository) to hold user accounts, farm produce listings, orders, and wallet balances. Every time the application closed or restarted, all registered farmers, buyers, posted crops, and order records disappeared. Room was essential to permanently store user profiles, product catalogs, order delivery states (Pending, Confirmed, In Transit, Delivered), and wallet ledger histories locally on the Android device so offline records remain intact across sessions.

 • Where I Learned It:
	• Official Android Developers Documentation: Save data in a local database using Room (://android.com).
	• Official Android Developers Documentation: Kotlin Symbol Processing (KSP) migration guide (://android.com).
	• Practical code walkthroughs and debugging sessions with an AI coding assistant while configuring Room entities and DAOs.

 • What Went Wrong Along the Way, and How I Fixed It:
	• Runtime Implementation Crash: When opening AddProductScreen, the app crashed immediately because AgroMarketDatabase_Impl was not generated at compile time. I fixed this by adding the com.google.devtools.ksp plugin and ksp("androidx.room:room-compiler:2.7.2") in build.gradle.kts (:app).
	• AGP 9.0 Built-In Kotlin Conflict: Enabling KSP triggered a Gradle build failure (Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin). I resolved this by upgrading KSP to version 2.3.2 and adding android.disallowKotlinSourceSets=false inside gradle.properties.
	• Schema Migration & Redeclaration Errors: Adding new fields to Order and User caused duplicate class errors (Order.kt vs. Model.kt) and Room schema version mismatches. I deleted the duplicate entity file and incremented the @Database version number (version = 5) with .fallbackToDestructiveMigration() enabled.

Concept 2: REST API Communication & Mobile Money Integration Using Retrofit and Node.js

 • What It Is:
Retrofit is a type-safe HTTP client library for Android that converts REST API endpoints into Kotlin interfaces using GsonConverterFactory to serialize JSON requests and responses. In the AgroMarket architecture, Retrofit communicates over HTTP with a custom Node.js and Express backend server (server.js), which securely holds merchant API keys and communicates with a Zambian payment gateway (Lenco) to initiate Mobile Money USSD push collections and disbursements.

 • Why the AgroMarket App Needs It:
An Android mobile application cannot securely store secret payment gateway keys or directly trigger Airtel Money, MTN MoMo, or Zamtel Kwacha SIM PIN prompts on its own. Without a backend and HTTP client, wallet deposits were purely simulated in local memory without verifying actual funds. AgroMarket needed Retrofit and a Node.js backend so buyers could deposit real Zambian Kwacha (ZMW) from Mobile Money into their AgroMarket online account, transfer verified escrow payments to farmers, and allow farmers to withdraw earnings back to their mobile wallets.

 • Where I Learned It:
	• Official Android Developers Documentation: Connect to the network and Network security configuration (://android.com).
	• Square Open Source Documentation: Retrofit — A type-safe HTTP client for Android and Java (square.github.io/retrofit).
	• Lenco Zambia API Documentation: Initiate Collection from Mobile Money & Transfers (lenco-api.readme.io).
	• Express.js Official Documentation (expressjs.com) and iterative debugging with an AI coding assistant.

 • What Went Wrong Along the Way, and How I Fixed It:
	• Project Placement Confusion: I initially placed Server.java inside the Android Studio source folder, causing hundreds of compilation errors. I fixed this by deleting Server.java from Android Studio and creating a separate Node.js project directory (AgroMarketServer) on my PC.
	• Cleartext HTTP Security Block: Android 9+ blocks unencrypted http:// traffic by default, throwing a CLEARTEXT communication not permitted error when connecting to my local server (10.15.15.245:3000). I fixed this by adding <uses-permission android:name="android.permission.INTERNET" /> and android:usesCleartextTraffic="true" in AndroidManifest.xml.
	• URL Formatting & HTTP 404 Routing Errors: Missing the port number (:3000/) in BASE_URL caused UnknownHostException, and an outdated server.js process still listening on port 3000 returned HTTP 404 Not Found for /api/wallet/deposit. I corrected BASE_URL in PaymentApiService.kt, terminated stale node.exe background processes, and restarted the updated server.js.
Would you like me to adjust the word count or academic tone of this Research Note to match a specific university rubric or project report template?