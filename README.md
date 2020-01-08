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
With "token" return contains user\`s previous answers for each question("choice" in "data").

Returns : JSON with "status", "data" and "message"(if error occurred).

/createquestion method POST
Params : "token", "question" , "category" , "answers".

"answers" must be an array
 "answers" answers count must be between 2 and 5 (2 and 5 include).
 
 "category" must be one of the following:
 "Siyaset" , "Eglence" , "General"

Returns : "status" and "message"(if error occurred).

/setchoice method POST
Params: "token", "questionID", "choice"
"questionID" must be valid.
"choice"(integer) must be between 1 and 5 (1 and 5 include).

Returns : JSON with "status", "data" and "message"(if error occurred).

/createcomment method POST
Params: "token", "questionID", "comment", "emoji".
"questionID" must be valid.

Returns : JSON with "status", "message"(if error occurred).

/getcomments/{questionid} method GET
Params: "count"(not required), "offset"(not required).

Returns:  JSON with "status", "data" and "message"(if error occurred).

/user/{userid} method GET
Params: none

Returns: Json with "status", "data" and "message"(if error occurred).

/getquestionsbyuserid/{userid} method GET
Params: "offset"(not required) and "count"(not required).

Returns: Json with "status", "data" and "message"(if error occurred).

/getcommentsbyuserid/{userid} method GET
Params: "offset"(not required) and "count"(not required).

Returns: Json with "status", "data" and "message"(if error occurred).