CREATE SEQUENCE bid_counter
START WITH 1
INCREMENT BY 2
NOCYCLE;

CREATE SEQUENCE borid_counter
START WITH 2
INCREMENT BY 2
NOCYCLE;

CREATE TABLE BorrowerType(
	type CHAR(3) PRIMARY KEY,
	bookTimeLimit INT NOT NULL
);


INSERT INTO BorrowerType VALUES('stu', 14);
INSERT INTO BorrowerType VALUES('fal', 84);
INSERT INTO BorrowerType VALUES('sta', 42);

CREATE TABLE Borrower(
	bid INT PRIMARY KEY,
	password VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	address VARCHAR(255) NOT NULL,
	phone VARCHAR(16) NOT NULL,
	emailAddress VARCHAR(255) NOT NULL,
	sinOrStNo VARCHAR(20) UNIQUE NOT NULL,
	expiryDate DATE NOT NULL,
	type CHAR(3) NOT NULL,
	CONSTRAINT fk_BorrowType_Borrower FOREIGN KEY(type) 
		REFERENCES BorrowerType(type)
		ON DELETE CASCADE
);

CREATE TABLE Book(
	callNumber VARCHAR(255) PRIMARY KEY,
	isbn VARCHAR(13) UNIQUE NOT NULL,
	title VARCHAR(255) NOT NULL,
	mainAuthor VARCHAR(255) NOT NULL,
	publisher VARCHAR(255) NOT NULL,
	year CHAR(4) NOT NULL
);

CREATE TABLE HasAuthor(
	callNumber VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	CONSTRAINT pk_HasAuthorID PRIMARY KEY(callNumber, name),
	CONSTRAINT fk_Book_HasAuthor FOREIGN KEY(callNumber)
		REFERENCES Book(callNumber)
		ON DELETE CASCADE
);

CREATE TABLE HasSubject(
	callNumber VARCHAR(255) NOT NULL,
	subject VARCHAR(255) NOT NULL,
	CONSTRAINT pk_HasSubjectID PRIMARY KEY(callNumber, subject),
	CONSTRAINT fk_Book_HasSubject FOREIGN KEY(callNumber)
		REFERENCES Book(callNumber)
		ON DELETE CASCADE
);

CREATE TABLE BookCopy(
	callNumber VARCHAR(255) NOT NULL,
	copyNo INT NOT NULL,
	status VARCHAR(7) NOT NULL,
	CONSTRAINT pk_BookCopyID PRIMARY KEY(callNumber, copyNo),
	CONSTRAINT fk_Book_BookCopy FOREIGN KEY(callNumber)
		REFERENCES Book(callNumber)
		ON DELETE CASCADE
);

CREATE TABLE HoldRequest(
	hid INT PRIMARY KEY,
	bid INT NOT NULL,
	callNumber VARCHAR(255) NOT NULL,
	issuedDate TIMESTAMP,
	CONSTRAINT fk_HoldRequest_Borrower FOREIGN KEY(bid)
		REFERENCES Borrower(bid)
		ON DELETE CASCADE,
	CONSTRAINT fk_HoldRequest_Book FOREIGN KEY(callNumber)
		REFERENCES Book(callNumber)
		ON DELETE CASCADE
);

CREATE TABLE Borrowing(
	borid INT PRIMARY KEY,
	bid INT NOT NULL,
	callNumber VARCHAR(255) NOT NULL,
	copyNo INT NOT NULL,
	outDate TIMESTAMP NOT NULL,
	inDate TIMESTAMP,
	CONSTRAINT fk_Borrowing_Borrower FOREIGN KEY(bid)
		REFERENCES Borrower(bid)
		ON DELETE CASCADE,
	CONSTRAINT fk_Borrowing_BookCopy FOREIGN KEY(callNumber, copyNo)
		REFERENCES BookCopy(callNumber, copyNo)
		ON DELETE CASCADE
);

CREATE TABLE Fine(
	fid INT PRIMARY KEY,
	amount FLOAT NOT NULL,
	issuedDate TIMESTAMP NOT NULL,
	paidDate TIMESTAMP,
	borid INT NOT NULL,
	CONSTRAINT fk_Fine_Borrowing FOREIGN KEY(borid)
		REFERENCES Borrowing(borid)
		ON DELETE CASCADE
);

