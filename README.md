# README #

Project uses Play! Framework 2.6

### How do I get set up? ###

`sbt run`

## Task ##

You have to create tool to calculate Dribbble stats using dribble public api:
1. For given Dribbble user find all followers
2. For each follower find all shots
3. For each shot find all "likers"
4. Calculate Top10 «likers». People with greater like count descending. 
Implement an api endpoint where user login is a parameter. Ex. http://0.0.0.0:9000/top10?login=alagoon
Output the results as json.

Think about:
How to deal with Dribbble API Limits? Try solve this problem in your solution.