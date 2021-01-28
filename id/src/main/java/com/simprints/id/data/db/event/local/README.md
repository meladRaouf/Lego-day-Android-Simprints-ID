# Troubleshooting Room db with events
The events db is room based and encrypted, so to explore the content of the db you need to created a db with a fixed password:

* Uninstall SID
* Set a fixed password in DbEventDatabaseProvider (e.g. `val key = "test".toCharArray()`)  
* Add the password in android.buildTypes.debug `resValue("string", "DB_PASSWORD_DBEVENTS", "test")`  
* Add `debugImplementation 'com.amitshekhar.android:debug-db:1.0.6'` and `debugImplementation 'com.amitshekhar.android:debug-db-encrypt:1.0.6'`  
* Install SID  
* Access the https://phone_ip:8080/  
  
Note: you can't access a db that was already encrypted with a password that was generated by SID  
  
The added library will create a web server showing the room data:   
  
![Web view with room content](./troubleshooting_roob_db_view.png)

