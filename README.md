# stakxtest
This is simple project to cache the binance data and placing deals with that

How to run the project ---
1. You must have mongodb , git , and maven installed in your PC
2. mongod and mongo must have started and a db must get created with the name (stakx) (command - use stakx)
3. Clone https://github.com/binance-exchange/binance-java-api.git in your IDE as a git import project
4. After that you need to configure it as a maven project (Right click on the project and go to configure and then choose convert to maven project)
4. Clone https://github.com/sumanjitmoshat/stakxtest.git in your IDE as a git import project
5. repeat stage 4
6. Run the TestApplication File as a java application


Note  - 
1. I have the run the Dealhandler thread 3 minutes after the cacheloader gets initiated as it must have some data for the deal handler to compute
2. I have taken the quantity as a fixed value as no information was provided
3. The API-Key and Secret-Key  have been removed for obvious reasons.


