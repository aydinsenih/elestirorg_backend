# elestirorg_backend
elestirorg API v0.1

/login  method POST
Params : "username", "password".

Returns : JSON with "status", "data" and "message"(if error occurred).

/signup method POST
Params : "email", "username", "password", "phoneNumber".

Returns : JSON with "status" and "message"(if error occurred).

/isloggedin method POST
Params : "token"

Returns : JSON with "status" and "message"(if error occurred).

/getquestions method GET
Params : "count"(not required), "offset"(not required), "token"
"token" must be in request\`s body
with "token" return contains user\`s previous answers for each question("choice" in "data").

Returns : JSON with "status", "data" and "message"(if error occurred).

/createquestion method POST
Params : "token", "question" , "category" , "answers".

"answers" must be json format as shown below
"answers":["c1", "c2", "c3", "c4"]
 "answers" answers count must be between 2 and 5 (2 and 5 include).
 
 "category" must be one of the following:
 "Siyaset" , "Eglence" , "General"

Returns : "status" and "message"(if error occurred).

/setchoice method POST
Params: "token", "questionID", "choice"
"questionID" must be valid.
"choice"(integer) must be between 1 and 1 (1 and 5 include).
Returns : JSON with "status", "data" and "message"(if error occurred).