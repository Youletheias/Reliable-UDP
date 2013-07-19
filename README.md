Purpose: 
This client/server application allows files exchange over UDP. 

In other words, it implements a reliable UDP that supports fragmentation, reassembly, fragments ordering, and all the necessary features that ensure a trustworthy file exchange between a client and a server. 

The following is the protocol design that was implemented in this application.

Protocol: 

1. The client sends a datagram to inform the server whether it wants to download or upload a file using a header.


2. If the client wants to download a file then 

		2.1 The header must be the following:

        		[Download][One space][File name][Linefeed]

    	2.2 Once the server receives the header it searches for the specified file

		2.3 If the file is not found then the server replies using the following header:

        		[File][One space][NOT][One space][Found][Linefeed]

    	2.4 If the file is found and its size exceeds 9 Mbytes then the server replies using the following header: 

				[File][One space][too][One space][large][Linefeed]

    	2.5 If the file is found and its size is less than 9 Mbytes the server replies using the following header: 

				[You][One space][want][One space][to][One space][download][One space][File name][One space][of][One space][size][One space][File size][Linefeed]

		2.6 After sending the header, the server sends the actual bytes that represent the requested file


3. If the client wants to upload a file then

		3.1 The header must be the following:

        		[Upload][One space][File name][Linefeed]

    	3.2 The client searches for the specified file

		3.3 If the file is not found the client sends the following header:

        		[File][One space][NOT][One space][Found][Linefeed]

    	3.4 If the file is found and its size exceeds 9 Mbytes, the client sends the following header: 

				[File][One space][too][One space][large][Linefeed]

		3.5 If the file is found and its size is less than 9 Mbytes the client sends the following header: 

        		[Upload][One space][File name][File size][Linefeed]

    	3.6 Once the server receives the header, it replies using:

        		[You][One space][want][One space][to][One space][upload][One space][File name][One space][of][One space][size][One space][File size][Linefeed]

		3.7 After receiving the header, the client sends the actual bytes that represent the specified file.

