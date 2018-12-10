# MySQL4Firebase
MySQL4Firebase is a server side Java program which connects a MySQL database to a Firebase app. It is a Firebase Adapter for MySQL or
MariaDB but you can use any database for which a JDBC driver is available. It allows you to call SQL statements from a mobile device. 


![image]( ./docs/abb_2.png) 

#### 1. Firebase

  * Go to the [Firebase homepage](https://firebase.google.com) to get a basic understanding of Google Firebase
  
  * Get a Firebase account   

#### 2. Installation Gude

 * Create a new Firebase project 
 
 * Create a service account JSON file for your project and download it
 
 * update the file `config/firebase.config.template.xml` file. Change the location of your service account
   JSON file and change the database URL. Then rename the file to `firebase.config.xml`.
   
 * Install the [northwind sample database](https://github.com/dalers/mywind) in your MySQL or 
   MariaDB database. 
  
 * In the `config` directory edit the `mysql.config.xml` file and update the following entries to your correct values:
      
     `<entry key="password">bitnami</entry>`
     
     `<entry key="database">northwind</entry>`
     
     `<entry key="username">root</entry>`
     
     `<entry key="serverName">192.168.188.92:3306</entry> `
   
 * Copy the security rules from the file `config/security_rules.json` to the Firebase database
 
 * Execute the main method of class FirebaseMySQLClient
 
 * Now you can use the demo app from  [MySQL4FirebaseApp](https://github.com/cordjastram/MySQL4FirebaseApp) to call 
   SQL statements from your mobile app.
   
   
##### SQL Statement
   
   ![image]( ./docs/abb_4_a.png )  
   
##### Stored Procedure
      
   ![image]( ./docs/abb_4_b.png )
