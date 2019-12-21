# elestirorg_backend
elestirorg

/login  method POST
Params : "username", "password".

Returns : JSON with "status" and "message"(if error occurred).

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
Params : "token", "title" , "categoryID".

Returns : "status" and "message"(if error occurred).
