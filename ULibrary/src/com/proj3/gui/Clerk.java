//src --> pacakge "com" --> folder "proj3"
package com.proj3.gui;

//Gui package
import javax.swing.*;
//We need to import the java.sql package to use JDBC
import java.sql.*;

//for reading from the command line
import java.io.*;

//for the login window
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.awt.*;
import java.awt.event.*;

public class Clerk extends Gui {
	private JLabel label;
	private JTextField add_text,check_text,process_text,overdue_text;
	private JButton add_but,check_but,process_but,overdue_but;
	
	//Instantiate new buffereader 'in'
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    
	private Connection con;
    
	public Clerk(JFrame frame) {
		//super(frame, "Clerk Mode", true);
		setLayout(new FlowLayout());
		
		label = new JLabel("Select operation");
		add(label);
		
		add_but = new JButton("Add a new borrower");
		add(add_but);
		
		check_but = new JButton("Check-out borrower's items");
		add(check_but);
		
		process_but = new JButton("Process a return");
		add(process_but);
		
		overdue_but = new JButton("Check overdue items");
		add(overdue_but);
		
		addBorrower _add = new addBorrower();
		//Button event for Add Borrower
		add_but.addActionListener(_add);
		
		
	}
	public void HideDialog()
	{
		add_but.setVisible(false);
		check_but.setVisible(false);
		process_but.setVisible(false);
		overdue_but.setVisible(false);
		label.setVisible(false);
	}
	
	public class addBorrower implements ActionListener {
		public void actionPerformed(ActionEvent _add) {
			//JLabel bid,password,name, address, phone,  emailAddress, sinOrStNo, expiryDate, type;
			//JTextField _bid,_password,_name, _address, _phone,  _emailAddress, _sinOrStNo, _expiryDate, _type;
			PreparedStatement  ps;
			
			String bpassword;
			String bname;
			String baddress;
			String bphone;
			String bemail;
			String bsin;
			String bdate;
			String btype;
			
			HideDialog();
			setLayout(new GridLayout(10,2));
			
			con = getCon();
			
			try
			{
			  ps = con.prepareStatement("INSERT INTO Borrower VALUES (bid_counter.nextval,?,?,?,?,?,?,?,?)");

			  System.out.print("\nBorrower Password: ");
			  bpassword = in.readLine();
			  ps.setString(1, bpassword);
			  
			  System.out.print("\nBorrower Name: ");
			  bname = in.readLine();
			  ps.setString(2, bname);

			  System.out.print("\nBorrower Address: ");
			  baddress = in.readLine();
			  ps.setString(3, baddress);
		
			  System.out.print("\nBorrower Phone: ");
			  bphone = in.readLine();
			  ps.setString(4, bphone);
			  
			  System.out.print("\nBorrower Email: ");
			  bemail = in.readLine();
			  ps.setString(5, bemail);
			  
			  System.out.print("\nBorrower Sin: ");
			  bsin = in.readLine();
			  ps.setString(6, bsin);
			  
			  bdate = ("TO_DATE('01-JAN-1975','DD-MON-YYYY')");
			  System.out.print("\nBorrower Expiry Date: " + bdate);
			  ps.setString(7, bdate);
			  
			  System.out.print("\nBorrower Type: ");
			  btype = in.readLine();
			  ps.setString(8, btype);

			  ps.executeUpdate();

			  // commit work 
			  con.commit();

			  ps.close();
			}
			catch (IOException e)
			{
			    System.out.println("IOException!");
			}
			catch (SQLException ex)
			{
			    System.out.println("Message: " + ex.getMessage());
			    try 
			    {
				// undo the insert
				con.rollback();	
			    }
			    catch (SQLException ex2)
			    {
				System.out.println("Message: " + ex2.getMessage());
				System.exit(-1);
			    }
			}
		    
		    
		}
	}
}