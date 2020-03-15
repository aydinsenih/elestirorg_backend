# elestirorg_backend
elestirorg API v0.1

/login  method POST
Body : "username", "password".

Returns : JSON with "status", "data" and "message"(if error occurred).

/signup method POST
Body : "email", "username", "password", "phoneNumber".

Returns : JSON with "status" and "message"(if error occurred).

/isloggedin method POST
Header : "AuthToken"

Returns : JSON with "status" and "message"(if error occurred).

/getquestions method POST
Body : "count"(not required), "offset"(not required)
Header : "AuthToken"
With "AuthToken" return contains user\`s previous answers for each question("choice" in "data").

Returns : JSON with "status", "data" and "message"(if error occurred).

/createquestion method POST
Body : "question" , "category" , "answers".
Header : "AuthToken"

"answers" must be an array
 "answers" answers count must be between 2 and 5 (2 and 5 include).
 
 "category" must be one of the following:
 "Siyaset" , "Eglence" , "General"

Returns : "status" and "message"(if error occurred).

/setchoice method POST
Body: "questionID", "choice"
Header : "AuthToken"
"questionID" must be valid.
"choice"(integer) must be between 1 and 5 (1 and 5 include).

Returns : JSON with "status", "data" and "message"(if error occurred).

/createcomment method POST
Body: "questionID", "comment", "emoji".
Header : "AuthToken"
"questionID" must be valid.

Returns : JSON with "status", "message"(if error occurred).

/getcomments/{questionid} method GET
Body: "count"(not required), "offset"(not required).

Returns:  JSON with "status", "data" and "message"(if error occurred).

/user/{userid} method GET or POST
Body: 
Header : "AuthToken"(not required).

Returns: Json with "status", "data" and "message"(if error occurred).

/getquestionsbyuserid/{userid} method GET
Body: "offset"(not required) and "count"(not required).

Returns: Json with "status", "data" and "message"(if error occurred).

/getcommentsbyuserid/{userid} method GET
Body: "offset"(not required) and "count"(not required).

Returns: Json with "status", "data" and "message"(if error occurred).

/question/{questionid} method POST or GET
Header : "AuthToken" (with "AuthToken" method must be POST. not required).

Returns: Json with "status", "data" and "message"(if error occurred).