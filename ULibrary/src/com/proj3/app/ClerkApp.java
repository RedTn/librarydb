package com.proj3.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.proj3.database.Database;
import com.proj3.model.Book;
import com.proj3.model.BookCopy;
import com.proj3.model.Borrower;
import com.proj3.model.Borrowing;
import com.proj3.model.CopyStatus;
import com.proj3.model.Fine;
import com.proj3.model.HoldRequest;

public class ClerkApp {
	private Database db;
	private Calendar cal;
	private Date currDate;
	private Calendar expCal;
	private String note;
	private String email;

	public ClerkApp(Database db) {
		this.db = db;
		this.note = null;
		this.email = null;

		// Automatically initialized to current date
		cal = Calendar.getInstance();
		expCal = Calendar.getInstance();
		currDate = new Date();

		// Due date for current books are a week before
		cal.add(Calendar.DATE, -7);
		expCal.add(Calendar.DATE, 7);
	}


	public String checkOutItems(int bid, String bookline) {
		if(("").equals(bookline)) {
			return "Please enter callNumbers";
		}
		List<String> books = new ArrayList<String>();
		Scanner scan = new Scanner(bookline);

		//WINDOWS
		//scan.useDelimiter(",|\\r\n");

		//UNIX
		scan.useDelimiter(",|\\n");
		try {
			while(scan.hasNext()){
				books.add(scan.next());
			}
		}finally {
			scan.close();
		}

		Borrower aBorrower = db.selectBorrowerById(bid);
		if (aBorrower == null) {
			return "No Borrower found.";
		}
		if (currDate.after(aBorrower.getExpiryDate())){
			return "Borrower is expired.";
		}
		int limit = aBorrower.getType().getBorrowingLimit();
		Calendar expirycal = Calendar.getInstance();
		expirycal.add(Calendar.DATE, limit);
		Integer[] checkborrows = db.selectAllBorrowingsByBid(bid);
		
		for(int j = 0; j<checkborrows.length; j++) {
			Fine fine = db.selectFineByBorid(checkborrows[j].intValue());
			if (fine != null) {
				return "Borrower must pay fine of : $" + String.format("%.2f", fine.getAmount());
			}
		}

		StringBuilder record = new StringBuilder();
		for (int i=0; i<books.size(); i++) {
			Book book = new Book();
			book.setCallNumber(books.get(i));
			HoldRequest bidHR = db.selectHoldRequestsByBidAndCall(bid, books.get(i));
			if(bidHR != null) {
				BookCopy holdbc = db.selectCopyByCallAndStatus(book.getCallNumber(), CopyStatus.onhold);
				if(holdbc == null) {
					return "Error, bid's hold request copy null";
				}
				if(!db.updateCopyStatus(CopyStatus.out, holdbc.getCopyNo(), book.getCallNumber())){
					return "Error, bookcopy not checked out.";
				}
				record.append("COPYNO: ");
				record.append(holdbc.getCopyNo());
				record.append("; CALLNUMBER: ");
				record.append(book.getCallNumber());
				record.append("; CHECKEDOUT, DUE: ");
				SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
				String formatedDate = sdf.format(expirycal.getTime());
				record.append(formatedDate);
				record.append("\n");
				if(!db.deleteHoldRequest(bidHR.getHid())) {
					return "Error, hold request not deleted";
				}
				else {
					record.append("Hold Request deleted");
				}
			}
			else {
				//HoldRequest[] hr = db.selectHoldRequestsByCall(book);
			
					
					//This book is available
					Book checkbook = db.selectBookByCallNumber(book.getCallNumber());
					if(checkbook == null) {
						return "Error, Book: " + book.getCallNumber() + " is not in database, please check spelling.";
					}
					BookCopy bc = db.selectCopyByCallAndStatus(book.getCallNumber(), CopyStatus.in);
				
					if(bc == null) {
						/*
						int count = db.getCopyCountByCallNumber(book.getCallNumber());
						count++;
						if(!db.insertBookCopy(book.getCallNumber(), count, CopyStatus.out)){
							return "Error, new bookcopy not inserted.";
						}
						if(!db.insertBorrowing(bid, book.getCallNumber(),  count, 
								currDate, null)){
							return "Error, borrowing record not created(1).";
						}
						*/
						record.append("There are no available copies of: " + book.getCallNumber() + ", left");
					}
					else {
						
						if(!db.updateCopyStatus(CopyStatus.out, bc.getCopyNo(), book.getCallNumber())){
							return "Error, bookcopy not checked out.";
						}

						if(!db.insertBorrowing(bid, book.getCallNumber(),  bc.getCopyNo(), 
								currDate, null)){
							return "Error, borrowing record not created(2).";
						}
						
						record.append("BORID: ");
						record.append((db.getBorrowingCount() + 3000));
						record.append("; COPYNO: ");
						record.append(bc.getCopyNo());
						record.append("; CALLNUMBER: ");
						record.append(book.getCallNumber());
						record.append("; CHECKEDOUT, DUE: ");
						SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
						String formatedDate = sdf.format(expirycal.getTime());
						record.append(formatedDate);
					}
			}
		}
		setNote(record.toString());
		return "Done check-out";
	}

	public String processReturn(int borid) {
		Borrowing b = db.searchBorrowingsByClerkNull(borid);
		if (b == null) {
			return "Borid is invalid, or book is already returned.";
		}

		Borrower borrower = db.selectBorrowerById(b.getBid());
		if(borrower == null){
			return "Error, borrower BID not found.";
		}
		String callNumber = b.getCallNumber();
		Date borrowDate = new Date();
		borrowDate = b.getOutDate();

		StringBuilder sb = new StringBuilder();


		Calendar checkFine = Calendar.getInstance();

		checkFine.add(Calendar.DATE, -(borrower.getType().getBorrowingLimit()));

		if (borrowDate.before(checkFine.getTime())) {

			Date curDate = checkFine.getTime();
			Date bookDate = borrowDate;
			long curTime = curDate.getTime();
			long bookTime = bookDate.getTime();
			long diffTime = curTime - bookTime;
			long diffDays = diffTime / (1000 * 60 * 60 * 24);
			float amount = diffDays * 1;
			if (!db.insertFine(amount, checkFine.getTime(), null, borid)) {
				return "Fine not inserted.";
			}
			sb.append("Overdue, Fine of $");
			sb.append(String.format("%.2f", amount));
			sb.append(".\n");
		}

		BookCopy bc = db.selectCopy(callNumber, CopyStatus.out,  b.getCopy().getCopyNo());
		HoldRequest hold = db.selectHoldRequestByCall(b.getBook());
		if((hold == null) && (bc != null)) {
			if(!db.updateCopyStatus(CopyStatus.in, b.getCopy().getCopyNo(), callNumber)) {
				return "Error, bookcopy not updated(1).";
			}
			if(!db.updateBorrowingByIndate(borid,currDate)){
				return "Error, borrowing record not inserted(1).";
			}
			sb.append("Book " + callNumber + " returned.");
		}else if ((hold != null) && (bc != null)){
			if(!db.updateCopyStatus(CopyStatus.onhold, b.getCopy().getCopyNo(), callNumber)) {
				return "Error, bookcopy not updated(2).";
			}
			if(!db.updateBorrowingByIndate(borid,currDate)){
				return "Error, borrowing record not inserted(2).";
			}
			sb.append("Returned.\nBOOK: " + callNumber + "\nis available for holder\nBID:" + hold.getBid());

			setEmail(borrower.getEmail());

		}
		else if ((hold == null) && (bc == null)){
			int inbooks = db.getCopyCountByCallNumber(callNumber);
			inbooks++;
			if(!db.insertBookCopy(callNumber, inbooks, CopyStatus.in)) {
				return "Insert new bookcopy failed.";
			}
			if(!db.updateBorrowingByIndate(borid,currDate)){
				return "Error, borrowing record not inserted(2).";
			}
			sb.append("New bookcopy inserted\ncopyNo: " + inbooks);
		}
		else {
			int inbooks = db.getCopyCountByCallNumber(callNumber);
			inbooks++;
			if(!db.insertBookCopy(callNumber, inbooks, CopyStatus.onhold)) {
				return "Insert new bookcopy failed.";
			}
			if(!db.updateBorrowingByIndate(borid,currDate)){
				return "Error, borrowing record not inserted(2).";
			}
			sb.append("New bookcopy inserted\ncopyNo: " + inbooks + "\nBOOK: "+ callNumber + "\nis available for holder\nBID:" + hold.getBid());


			setEmail(borrower.getEmail());

		}
		sb.append("\nEND");
		return sb.toString();
	}

	public Borrowing[] checkOverdueItems() {
		return db.selectOverDueBorrowingByDateByClerk(db);

	}
	private void setNote(String note) {
		this.note = note;
	}
	public String getNote() {
		return note;
	}
	private void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
}
