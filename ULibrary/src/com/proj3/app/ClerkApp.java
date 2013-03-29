package com.proj3.app;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.lang.Integer;

import com.proj3.database.Database;
import com.proj3.model.Book;
import com.proj3.model.BookCopy;
import com.proj3.model.Borrower;
import com.proj3.model.BorrowerType;
import com.proj3.model.Borrowing;
import com.proj3.model.CopyStatus;
import com.proj3.model.HoldRequest;

public class ClerkApp {
	private Database db;
	private Borrower currBorrower;

	public ClerkApp(Database db) {
		this.db = db;
	}

	public Borrower login(int bid, String password) throws SQLException {
		ResultSet rs = db.selectBorrowerByIdAndPassword(bid, password);
		currBorrower = null;
		while (rs.next()) {
			currBorrower = Borrower.getInstance(rs);
		}
		return currBorrower;
	}

	public void addBorrower() throws SQLException {
		if (currBorrower == null) {
			return;
		}

		String password;
		String name;
		String address;
		String phone;
		String email;
		String sinOrStNo;
		Date expiryDate;
		BorrowerType type;
		Boolean result;

		/*
		 * TODO: GUI inserts values for function
		result = insertBorrower(password, name, address,
				phone, email, sinOrStNo, expiryDate,
				type);
		 */
	}

	public void checkOutItems(int bid, String[] callNumbers) throws SQLException {
		int numbooks;

		if (currBorrower == null) {
			return;
		}
		Borrower aBorrower = null;

		ResultSet rs = db.selectBorrowerById(bid);

		while (rs.next()) {
			aBorrower = Borrower.getInstance(rs);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//Automatically initialized to current date
		Date currDate = new Date();
		//Format dates
		sdf.format(currDate);
		sdf.format(aBorrower.getExpiryDate());

		if (currDate.after(aBorrower.getExpiryDate())){
			//Borrower id is expired
			return;
		}


		////Determine if books are borrowable
		//TOFINISH
		numbooks = callNumbers.length;

		BookCopy bc = null;

		for (int i = 0; i < numbooks; i++) {
			rs = db.selectBookCopiesByCallNumber(callNumbers[i]);
			while (rs.next()) {
				bc = BookCopy.getInstance(rs, null);
				
			}
		}
		////

		//Add book to borrowing

		/*
		 * TODO: GUI checks out item
		 * for (int i = 0; i < numbooks, i++) {
		insertBorrowing(bid, callNumber[i],
			copyNo, outDate, inDate);
			}
		 */
	}

	public void proessReturn(int borid) throws SQLException {
		if (currBorrower == null) {
			return;
		}
		
		ResultSet rs = db.searchBorrowingsByClerk(borid);
		while (rs.next()) {

			Borrowing b = Borrowing.getInstance(rs, null, null);

			String callNumber = b.getCallNumber();
			Date outDate = b.getOutDate();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			//Automatically initialized to current date
			Date currDate = new Date();
			
			//Format dates
			sdf.format(currDate);
			sdf.format(outDate);

			/*TODO: Compare the dates, apply fine if necessary
		updateFineAmountField(fid,amount);
			 */
			
			ResultSet holdRs = db.selectHoldRequestsByCall(callNumber);
			ResultSet copyRs = db.selectBookCopiesByCallNumber(callNumber);
			if (holdRs.next()) {
				//There is a hold
				if(copyRs.next()) {
					BookCopy bc = BookCopy.getInstance(rs, null);
					if(bc.getStatus().equals("out")){
						if(!db.updateFirstCopyStatus("on-hold", callNumber))
						{
							return;
						}
						//TODO: Send message to Borrower
						
					}
				}
			}else {
				//There is no hold
				if(copyRs.next()) {
					BookCopy bc = BookCopy.getInstance(rs, null);
					if(bc.getStatus().equals("out")){
						if(!db.updateFirstCopyStatus("in", callNumber))
						{
							return;
						}
					}
				}
			}
			
		}
	}
	
	public Borrowing[] checkOverdueItems() throws SQLException {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
		//Automatically initialized to current date	
		Calendar c = Calendar.getInstance();
		
		//Due date for current books are a week before
		c.add(Calendar.DATE, -7);
		
		sdf.format(c.getTime());
		
		ResultSet rs = db.searchOverDueByDate(c.getTime());
		Map<Integer, Borrowing> borrows = new HashMap<Integer, Borrowing>();
		while(rs.next()) {
			Borrowing borrow;
			String borid = rs.getString("borid");
			if (!borrows.containsKey(borid)) {
				borrow = Borrowing.getInstance(rs,null,null);
				borrows.put(borrow.getBorid(), borrow);
			} else {
				borrow = borrows.get(borid);
			}

			int bid = rs.getInt("bid");
			if (!rs.wasNull()) {
				borrow.setBid(bid);
			}

			String callNumber = rs.getString("callNumber");
			if (!rs.wasNull()) {
				borrow.setCallNumber(callNumber);
			}
		}
	
		return (Borrowing[]) borrows.values().toArray();
		
		}
	}
