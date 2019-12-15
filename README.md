# elestirorg_backend
elestirorg

/login  method POST
Params : "username", "password"

Returns : if successfuly login. -> phoneNumber, creationTime, avatar, email, username, token 
          not successful login return(s) error text.

/signup method POST
Params : "email", "username", "password", "phoneNumber"

Returns : if successfuly signup -> username, "signup succesful"
          not successful signup return(s) error text.
