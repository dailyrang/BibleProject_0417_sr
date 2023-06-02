import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.ScrollPaneConstants;
import java.awt.Color;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextPane;

public class WinBible extends JDialog {

	private String abbr[] = {"창","출","레","민","신","수","삿","룻","삼상","삼하","왕상","왕하","대상","대하","스","느","에","욥","시","잠","전","아","사","렘","애","겔","단","호","욜","암","옵","욘","미","나","합","습","학","슥","말","마","막","눅","요","행","롬","고전","고후","갈","엡","빌","골","살전","살후","딤전","딤후","딛","몬","히","약","벧전","벧후","요일","요이","요삼","유","계"};
	private String full[] = {"창세기","출애굽기","레위기","민수기","신명기","여호수아","사사기","룻기","사무엘상","사무엘하","열왕기상","열왕기하","역대상","역대하","에스라","느헤미야","에스더","욥기","시편","잠언","전도서","아가","이사야","예레미야","예레미야애가","에스겔","다니엘","호세아","요엘","아모스","오바댜","요나","미가","나훔","하박국","스바냐","학개","스가랴","말라기","마태복음","마가복음","누가복음","요한복음","사도행전","로마서","고린도전서","고린도후서","갈라디아서","에베소서","빌립보서","골로새서","데살로니가전서","데살로니가후서","디모데전서","디모데후서","디도서","빌레몬서","히브리서","야고보서","베드로전서","베드로후서","요한일서","요한이서","요한삼서","유다서","요한계시록"};
	private JComboBox cbBook;
	private JComboBox cbChapter;
	private JComboBox cbVerse;
	private JTextField tfSearchWord;
	private JTextField tfAbbr;
	private JTextPane taContents;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WinBible dialog = new WinBible();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public WinBible() {
		setTitle("성경 프로젝트");
		setBounds(100, 100, 1018, 534);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		
		JButton btnMerge = new JButton("파일 합치기");
		btnMerge.setEnabled(false);
		btnMerge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// C:/FileIO/BibleTxts 폴더안의 텍스트파일들을 하나로(bible.txt) 합치시오.
				File file = new File("C:/FileIO/BibleTxts/");
				String flist[] = file.list();
				FileWriter fw;
				try {
					fw = new FileWriter("C:/FileIO/bible2.txt");
					
					for(int i=0; i<flist.length;i++) {
						try {							
							FileReader fr = new FileReader("C:/FileIO/BibleTxts/" + flist[i]);
							int ch;
							while((ch=fr.read()) != -1)
								fw.write((char)ch);
							fr.close();
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}					
					}
					fw.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
		});
		panel.add(btnMerge);
		
		JButton btnFile2DB = new JButton("File2DB");
		btnFile2DB.setEnabled(false);
		btnFile2DB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 창1:1 태초에~~ => 창   1    1    태초에~~ 	// 삼상3:2 ~~~
				FileReader fr;
				try {
					fr = new FileReader("C:/FileIO/bible.txt");
					BufferedReader br = new BufferedReader(fr);
					String temp;
					int idx=0;
					while((temp=br.readLine()) != null) {
						String sBook = new String();
						if(temp.charAt(1) >= '0' && temp.charAt(1) <= '9') { // 두번째 문자가 숫자면
							idx=1;
							sBook = temp.substring(0,idx);   //한글자만 출력(창,출,...)
						}else {
							idx=2;
							sBook = temp.substring(0,idx);   //두글자만 출력(왕상, 삼하,...)
						}
						int cnt=0;
						for(int i=0;i<abbr.length;i++)
							if(abbr[i].equals(sBook)) {
								cnt = i;
								break;
							}
						sBook = full[cnt];
						int colon=temp.indexOf(':');
						String sChapter = temp.substring(idx, colon);  // 장 번호						
						int space=temp.indexOf(' ');
						String sVerse = temp.substring(colon+1,space);   // 절 번호						
						String sContents = temp.substring(space+1);   // 성경구절		
						
						// DB연결
						Class.forName("com.mysql.cj.jdbc.Driver");
						Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
						Statement stmt = con.createStatement();
						
						String sql = "insert into bibleTBL values(null,'" + sBook + "',";
						sql = sql + sChapter + "," + sVerse + ",'" + sContents.replaceAll("'", "@") + "')";
						System.out.println(sql);
						
						if(stmt.executeUpdate(sql) < 1)
							System.out.println("입력 오류");
						stmt.close();
						con.close();
					}
					br.close();
					fr.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		panel.add(btnFile2DB);
		
		cbBook = new JComboBox();		
		cbBook.addActionListener(new ActionListener() { // 성경책을 선택하면 선택한 책의 1장부터 N장까지가 콤보박스에 추가된다.
			public void actionPerformed(ActionEvent e) {
				String sBook = (String) cbBook.getSelectedItem();								
				cbChapter.removeAllItems();
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
					Statement stmt = con.createStatement();
					
					String sql = "select distinct chapter from bibleTBL where book='" + sBook + "'";
					ResultSet rs = stmt.executeQuery(sql);
					while(rs.next()) {
						cbChapter.addItem(rs.getString("chapter"));
					}
				} catch (ClassNotFoundException | SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		panel.add(cbBook);
		
		cbChapter = new JComboBox();
		cbChapter.addActionListener(new ActionListener() { // 성경책, 장을 선택하면 선택한 장의 1절부터 N절까지가 콤보박스에 추가된다.
			public void actionPerformed(ActionEvent e) {
				String sBook = (String) cbBook.getSelectedItem();
				String sChapter = (String) cbChapter.getSelectedItem();
				
				cbVerse.removeAllItems();
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
					Statement stmt = con.createStatement();
					
					String sql = "select verse from bibleTBL where book='" + sBook + "' and chapter=" + sChapter ;
					ResultSet rs = stmt.executeQuery(sql);
					while(rs.next()) {
						cbVerse.addItem(rs.getString("verse"));
					}
				} catch (ClassNotFoundException | SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				
			}
		});
		panel.add(cbChapter);
		
		cbVerse = new JComboBox();
		panel.add(cbVerse);
		
		JButton btnShow = new JButton("읽기");
		btnShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sBook = (String) cbBook.getSelectedItem();
				String sChapter = (String) cbChapter.getSelectedItem();
				String sVerse = (String) cbVerse.getSelectedItem();
				taContents.setText("");
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
					Statement stmt = con.createStatement();
					
					String sql = "select verse, contents from bibleTBL where book='" + sBook + "' and chapter=" + sChapter;
												
					ResultSet rs = stmt.executeQuery(sql);
					
					String temp="<span style='font: bold 2em Nanum Godic; color:green;'>";
					temp = temp + "[" + sBook + " " + sChapter + ":" + sVerse + "]</span><br><br>";
					
					while(rs.next()) {						
						if(sVerse.equals(rs.getString("verse")))
							temp = temp + "<span style='font: 1.5em bold Nanum Godic; color:red;'>";
						else
							temp = temp + "<span style='font: 1.5em Nanum Godic; color:black;'>";
						
						temp = temp + rs.getString("verse") + "   " + rs.getString("contents");
						
						temp = temp + "</span><br>";
							
					}
					taContents.setText("<html><body>" + temp + "</body></html>");
					taContents.setCaretPosition(0);
					
				} catch (ClassNotFoundException | SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
			}
		});
		panel.add(btnShow);
		
		tfSearchWord = new JTextField();
		panel.add(tfSearchWord);
		tfSearchWord.setColumns(10);
		
		JButton btnSearch = new JButton("검색");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchWord = tfSearchWord.getText();
				taContents.setText("");
				
				try {
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
					Statement stmt = con.createStatement();
					
					String sql = "select * from bibleTBL where contents like '%" + searchWord + "%'";
					ResultSet rs = stmt.executeQuery(sql);
					String temp="";
					int count=0;
					
					while(rs.next()) {
						temp = temp + "<span style='font: 1.5em bold Nanum Godic; color:red;'>";
						temp = temp + "[" + rs.getString("book") + " ";						
						if(rs.getString("book").equals("시편"))
							temp = temp + rs.getString("chapter") + "편 " + rs.getString("verse") + "절]</span><br>";
						else
							temp = temp + rs.getString("chapter") + "장 " + rs.getString("verse") + "절]</span><br>";
							
						String sContent = rs.getString("contents");
						
						String ArrContents[] = sContent.split(searchWord);
						for(int i=0;i<ArrContents.length;i++) {
							temp = temp + "<span style='font: 1.5em Nanum Godic; color:black'>";
							temp = temp + ArrContents[i] + "</span>";	
							if(i < ArrContents.length-1) {
								temp = temp + "<span style='font: bold 1.5em Nanum Godic; color:blue;'>";
								temp = temp  + searchWord + "</span>";
							}
						}
						temp = temp + "<br>";
						count++;
					}
					taContents.setText("<html><body>" + temp + "</body></html>");
					taContents.setCaretPosition(0);
					
					setTitle(count + "회 출현");
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				
				
			}
		});
		panel.add(btnSearch);
		
		tfAbbr = new JTextField();
		tfAbbr.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					String text = tfAbbr.getText(); //창 1:5-9
					String arrText[] = text.split(" |:|-");
					
					//int idxSpace = text.indexOf(' ');
					//int idxColon = text.indexOf(':');
					//String sBook = text.substring(0, idxSpace).trim();
					//String sChapter = text.substring(idxSpace+1, idxColon).trim();
					//String sVerse = text.substring(idxColon+1).trim();
					if(arrText.length == 2) {
						String sBook = arrText[0];
						String sChapter = arrText[1];
						sBook = abbr2full(sBook);
						
						try {
							showResult(sBook, sChapter);
						} catch (ClassNotFoundException | SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}else if(arrText.length == 3) {
						String sBook = arrText[0];
						String sChapter = arrText[1];
						String sStart = arrText[2];
						sBook = abbr2full(sBook);
						
						try {
							showResult(sBook, sChapter, sStart);
						} catch (ClassNotFoundException | SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}else {
						String sBook = arrText[0];
						String sChapter = arrText[1];
						String sStart = arrText[2];
						String sEnd = arrText[3];
						sBook = abbr2full(sBook);
						try {
							showResult(sBook, sChapter, sStart, sEnd);
						} catch (ClassNotFoundException | SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		panel.add(tfAbbr);
		tfAbbr.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		taContents = new JTextPane();
		taContents.setEditable(false);
		taContents.setFont(new Font("나눔고딕", Font.BOLD, 28));
		taContents.setContentType("text/html");
		scrollPane.setViewportView(taContents);
		
		// cbBook에 66권의 책이름을 추가하시오.(full 배열)
		//for(int i=0;i<full.length;i++)
		//	cbBook.addItem(full[i]);
		
		// cbBook에 66권의 책이름을 추가하시오.(DB-bibleTBL)
		try {
			addBooks();
		} catch (ClassNotFoundException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected void showResult(String sBook, String sChapter) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
		Statement stmt = con.createStatement();
		
		tfAbbr.setText(tfAbbr.getText() + ":1");
		cbBook.setSelectedItem(sBook);
		cbChapter.setSelectedIndex(Integer.parseInt(sChapter)-1);
		
		String sql = "select * from bibleTBL where book='" + sBook + "' and chapter=" + sChapter ;
		ResultSet rs = stmt.executeQuery(sql);
		String temp = "";
		while(rs.next()) {
			temp = temp + "<span style='font: 1.5em Nanum Godic; color:black'>";
			temp = temp + rs.getString("verse") + " " + rs.getString("contents") + "</span><br>"; 
		}		
		taContents.setText(temp);
		taContents.setCaretPosition(0);
	}

	protected void showResult(String sBook, String sChapter, String sStart, String sEnd) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
		Statement stmt = con.createStatement();
		
		cbBook.setSelectedItem(sBook);
		cbChapter.setSelectedIndex(Integer.parseInt(sChapter)-1);
		cbVerse.setSelectedItem(sStart);
		
		String sql = "select * from bibleTBL where book='" + sBook + "' and chapter=" + sChapter ;
		sql = sql + " and verse >=" + sStart +" and verse <=" + sEnd;
		ResultSet rs = stmt.executeQuery(sql);
		String temp = "";
		while(rs.next()) {
			temp = temp + "<span style='font: 1.5em Nanum Godic; color:black'>";
			temp = temp + rs.getString("verse") + " " + rs.getString("contents") + "</span><br>"; 
		}		
		taContents.setText(temp);
		taContents.setCaretPosition(0);
	}

	protected void showResult(String sBook, String sChapter, String sVerse) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
		Statement stmt = con.createStatement();
		
		cbBook.setSelectedItem(sBook);
		cbChapter.setSelectedIndex(Integer.parseInt(sChapter)-1);
		cbVerse.setSelectedItem(sVerse);
		
		String sql = "select * from bibleTBL where book='" + sBook + "' and chapter=" + sChapter + " and verse=" + sVerse;
		ResultSet rs = stmt.executeQuery(sql);
		
		String temp = "";
		while(rs.next()) {
			temp = temp + "<span style='font: 1.5em Nanum Godic; color:black'>";
			temp = temp + rs.getString("verse") + " " + rs.getString("contents") + "</span><br>"; 
		}		
		taContents.setText(temp);	
		taContents.setCaretPosition(0);
	}

	protected String abbr2full(String sBook) {
		for(int i=0;i<abbr.length;i++) {
			if(sBook.equals(abbr[i])) {
				sBook = full[i];
				break;
			}
		}		
		return sBook;
	}

	private void addBooks() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sqlDB","root","1234");						
		Statement stmt = con.createStatement();
		
		String sql = "select distinct book from bibleTBL";
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			cbBook.addItem(rs.getString("book"));
		}
	}

}
