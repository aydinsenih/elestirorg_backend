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
Params : "count", "offset". No Params required for last 5 questions.

Returns : JSON with "status", "data" and "message"(if error occurred).

/createquestion method POST
Params : "token", "question" , "category" , "answers".

"answers" must be json format as shown below
 {"1":"some answer", "2":"some other answer"}
 "answers" answers must be between 2 and 5 (2 and 5 include).
 
 "category" must be one of the following
 "Siyaset" , "Eglence" , "General"

Returns : "status" and "message"(if error occurred).
