/**
 * Java大作业————网络爬虫之豆瓣书籍
 * 成员：陈树银 舟泊湖 黄冠华
 * 功能实现：
 * 	1.爬虫爬取到的书籍信息包括 书名|评分|作者|出版社|价格|作品简介|作者简介
 *  2.界面化显示所有书籍封面，书名以及起其豆瓣评分
 *  3.点击书籍可以显示书籍具体简介
 *  4.列表模式|网格模式
 *  5.书籍数量尽量多
 *  6.按豆瓣评分筛选书籍
 *  7.按作者|书名搜索书籍 
 *  8.上一页|下一页|跳转页面
 */

package doubanCrawl;

import java.io.*;
import java.net.*;
import java.util.regex.*;
import java.util.*; 
import java.awt.*;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;   
import javax.swing.*;
import java.awt.event.*;
import javax.swing.JList;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.IOException; 
import javax.imageio.ImageIO;  


public class Crawl extends JFrame {
	
	//static private String url = "https://book.douban.com/";
	static private String url = "https://book.douban.com/tag/?view=type&icn=index-sorttags-all";
	static private Set<String> bookSet = new HashSet<String>();
	static private Set<String> bookTagURLSet = new HashSet<String>();
	static private Book[] booklist;
	static private int numOfBook = 3000;    //需要爬取的大概书籍
	static private int[] bookToShow = new int[numOfBook];
	static private int showMode = 1;
	static private String[] gradeList = new String[]{"全部", ">=9.5", ">=9.0", ">=8.5", ">=8.0", ">=7.5", ">=7.0"};
	static private int maxPage = 0;
	static private int time = 1000;		// 爬虫防封IP

	JButton btnListMode = new JButton("列表模式");
	JButton btnGridMode = new JButton("网格模式");
	JButton btnSearch = new JButton("查找书籍");
	JButton btnFirst = new JButton("首页");
	JButton btnPrevious = new JButton("上一页");
	JButton btnNext = new JButton("下一页");
	JButton btnLast = new JButton("尾页");
	JButton btnJump = new JButton("跳转");
	JLabel lblBookNum = new JLabel();
	JTextField jtfSearch = new JTextField("请输入要查找的书名");
	JTextField jtfCurrentPage = new JTextField();
	JTextField jtfJumpPage = new JTextField("跳转页面");
	JScrollPane jspBook = new JScrollPane(); 
	JPanel jpBook;
	JComboBox<String> jbGrade;
	
	public void init(){
		numOfBook = 500;	// 设置显示的书籍数量
		for(int i = 0; i < numOfBook; i++) bookToShow[i] = 1;
		lblBookNum.setText("共" + String.valueOf(numOfBook) + "本书");
		// 先将boolist恢复需要的数量 再写界面
		loadBookListFromFile(numOfBook);

		JLabel lblGrade = new JLabel("评分:");
        jbGrade = new JComboBox<String>(gradeList);
        jbGrade.setSelectedIndex(0);

		setSize( 1080,820 );
		setLayout(null);
		getContentPane().add(btnGridMode);
		getContentPane().add(btnListMode);
		getContentPane().add(btnSearch);
		getContentPane().add(lblBookNum);
		getContentPane().add(jspBook);
		getContentPane().add(jbGrade);
		getContentPane().add(lblGrade);
		getContentPane().add(jtfSearch);
		getContentPane().add(btnFirst);
		getContentPane().add(btnLast);
		getContentPane().add(btnPrevious);
		getContentPane().add(btnNext);
		getContentPane().add(jtfCurrentPage);
		getContentPane().add(btnJump);
		getContentPane().add(jtfJumpPage);


		btnGridMode.setBounds(440,0,100,50);
		btnListMode.setBounds(540,0,100,50);
		btnSearch.setBounds(1000,10,80,30);
		lblBookNum.setBounds(30,750,100,30);
		jspBook.setBounds(0,50,1080,700);
		jbGrade.setBounds(50,0,100,50);
		lblGrade.setBounds(20,0,30,50);
		jtfSearch.setBounds(860,10,140,30);
		btnFirst.setBounds(340,750,80,30);
		btnLast.setBounds(660,750,80,30);
		btnPrevious.setBounds(420,750,80,30);
		btnNext.setBounds(580,750,80,30);
		jtfCurrentPage.setBounds(500,750,80,30);
		jtfJumpPage.setBounds(920,750,80,30);
		btnJump.setBounds(1000,750,80,30);
		jtfCurrentPage.setHorizontalAlignment(JTextField.RIGHT);
		jtfJumpPage.setHorizontalAlignment(JTextField.RIGHT);

		gridMode();		// 最开始是网格模式
		//listMode();

		setLocationRelativeTo(null);
		setTitle("豆瓣书籍"); 
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}	
	
	public void start(){
		new Thread(()->{
			try{
				// 列表模式
				btnListMode.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						jpBook.removeAll();
						listMode();
						showMode = 1 - showMode;
					}
				});
				// 网格模式
				btnGridMode.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						jpBook.removeAll();
						gridMode();
						showMode = 1 - showMode;
					}
				});
				// 搜索书籍
				btnSearch.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						if(bookSearch() == false) return;
						if(showMode == 0) listMode();
                		else gridMode();
					}
				});
				// 筛选书籍
				jbGrade.addItemListener(new ItemListener() {
           			@Override
            		public void itemStateChanged(ItemEvent e) {
                		if (e.getStateChange() == ItemEvent.SELECTED) {
                			bookFilter(gradeList[jbGrade.getSelectedIndex()]);
                			if(showMode == 0) listMode();
                			else gridMode();
                		}
            		}
        		});
        		// 首页
				btnFirst.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						jspBook.getVerticalScrollBar().setValue(jspBook.getVerticalScrollBar().getMinimum());
					}
				});
				// 尾页
				btnLast.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						jspBook.getVerticalScrollBar().setValue(jspBook.getVerticalScrollBar().getMaximum());
					}
				});
				// 上一页
				btnPrevious.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						int current = jspBook.getVerticalScrollBar().getValue();
						if(current + jspBook.getHeight() >= jspBook.getVerticalScrollBar().getMaximum()) {
							jspBook.getVerticalScrollBar().setValue(jspBook.getHeight()*(maxPage-2));
							return;	
						}
						current = (current > jspBook.getHeight())? (current-jspBook.getHeight()) : jspBook.getVerticalScrollBar().getMinimum();
						jspBook.getVerticalScrollBar().setValue(current);
					}
				});
				// 下一页
				btnNext.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						int current = jspBook.getVerticalScrollBar().getValue();
						if(current+jspBook.getHeight()<=jspBook.getVerticalScrollBar().getMaximum()) 
							jspBook.getVerticalScrollBar().setValue(current+jspBook.getHeight());
						else jspBook.getVerticalScrollBar().setValue(jspBook.getVerticalScrollBar().getMaximum());

					}	
				});
				// 跳转页面
				btnJump.addActionListener(new ActionListener() {	
					@Override
					public void actionPerformed(ActionEvent e) {
						pageJump();
					}
				});
			}catch(Exception ex){}
			// 实时刷新当前页面页号
			new javax.swing.Timer(time ,(e)->{			
				int current = jspBook.getVerticalScrollBar().getValue();
				int page = 0;
				while(current >= jspBook.getHeight()) {
					current -= jspBook.getHeight();
					page ++;
				}
				page ++;
				if(jspBook.getVerticalScrollBar().getValue() + jspBook.getHeight() >= ((int)jpBook.getPreferredSize().getHeight()))
					page = maxPage;
				jtfCurrentPage.setText(page + "/" + maxPage);
			}).start();
		}).start();
	}

	/**
     *
	 *
	 *
	 * 网格模式显示书籍
	 */
	public void gridMode() {
		jpBook = new JPanel(new GridLayout(0,5,10,10));
		
		for(int i = 0; i < numOfBook; i++){
			if(bookToShow[i] == 0) continue;
			JLabel lblBookName = new JLabel(booklist[i].bookName, JLabel.CENTER);
			JLabel lblGrade = new JLabel("豆瓣评分：" + booklist[i].grade, JLabel.CENTER);
			
			String imagePath;
			if(booklist[i].image == null) imagePath = "icons//find.png";
			else imagePath = "bookImage//" + String.valueOf(i) + ".jpg";
			ImageButton ipBook = new ImageButton(new ImageIcon(imagePath).getImage(), i ,0);
			ipBook.setBookInfo(lblBookName, lblGrade);
			
			// 设置点击事件——显示书籍具体信息
			ipBook.addActionListener(new ActionListener() {	
				@Override
				public void actionPerformed(ActionEvent e) {
					JFrame intro = new JFrame(((ImageButton)e.getSource()).bookName); 
					Dimension size = new Dimension(500, 400);
					intro.setSize(size);
					// intro 居中显示
					intro.setLocation(getBounds().x + getBounds().width/2  - size.width/2 ,
									  getBounds().y + getBounds().height/2 - size.height/2); 
					
					JTextArea textIntro = new JTextArea(booklist[((ImageButton)e.getSource()).bookNumber].printInfo());
					//textIntro.setFont(new Font("楷体", Font.BOLD, 12)); 
					textIntro.setLineWrap(true);
					JScrollPane textScroll = new JScrollPane(); 
					textScroll.setViewportView(textIntro);
					textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
					intro.add(textScroll); 
					intro.setVisible(true);
				}
			});
			jpBook.add(ipBook);
		}
		maxPage = ((int)jpBook.getPreferredSize().getHeight()+1)/ jspBook.getHeight() + 1;
		jpBook.repaint();
		jspBook.repaint();
		jspBook.setViewportView(jpBook);
		repaint();
	} 

	/**
	 *
	 *
	 *
	 * 列表模式显示书籍 
	 */
	public void listMode() {
		jpBook = new JPanel(new GridLayout(0,1,10,30));

		for(int i = 0 ; i < numOfBook; i++) {
			if(bookToShow[i] == 0) continue;
			JTextArea lblBookInfo = new JTextArea();
			JTextArea lblAuthorIntro = new JTextArea();
			JTextArea lblContentIntro = new JTextArea();
			ImageButton jpBookPic = new ImageButton(
				new ImageIcon("bookImage//" + String.valueOf(i) + ".jpg").getImage(), i, 1);

			// 点击图片显示大图
			jpBookPic.addActionListener(new ActionListener() {	
				@Override
				public void actionPerformed(ActionEvent e) {
					try{
						JFrame pic = new JFrame(((ImageButton)e.getSource()).bookName); 
						int i = ((ImageButton)e.getSource()).bookNumber;
						String imagePath;
						if(booklist[i].image == null) imagePath = "icons//find.png";
						else imagePath = "bookImage//" + String.valueOf(i) + ".jpg";
						File picture = new File(imagePath);  
        				BufferedImage img = ImageIO.read(new FileInputStream(picture));
        				Dimension size = new Dimension(img.getWidth(), img.getHeight());
						pic.setSize(size);

	       				JPanel jpPic = new JPanel() {
	        				@Override
	            			public void paint(Graphics g) {
	            				super.paint(g);
	            				Dimension _size = this.getParent().getSize();
	            				Image img = ((ImageButton)e.getSource()).img;
	                			g.drawImage(img, 0, 0, _size.width, _size.height, this);
	            			}
	        			};
	       				pic.add(jpPic);
						pic.setVisible(true);
					}catch(Exception ex) {
						System.out.println("图片路径不存在!"); 
						ex.printStackTrace();
					}
				}
			});

			JPanel jpItem = new JPanel(new GridLayout(1,4,10,10));
			jpItem.setPreferredSize(new Dimension(1080, 250));

			String _bookInfo = "#书号: " + i + "\n" + 
							   "#书名: " + booklist[i].bookName + "\n" + 
							   "#豆瓣评分: " + booklist[i].grade + "\n" +
							   "#作者: " + booklist[i].author + "\n" +
							   "#出版社: " + booklist[i].press + "\n" + 
							   "#定价: " + booklist[i].price;
			lblBookInfo.setText(_bookInfo);
			lblAuthorIntro.setText("#作者简介: \n" + booklist[i].authorIntro);
			lblContentIntro.setText("#内容简介: \n" + booklist[i].contentIntro);
			lblBookInfo.setLineWrap(true);
			lblAuthorIntro.setLineWrap(true);
			lblContentIntro.setLineWrap(true); 

			JScrollPane jspBookInfo = new JScrollPane(lblBookInfo); 
			JScrollPane jspAuthorIntro = new JScrollPane(lblAuthorIntro);  
			JScrollPane jspContentIntro = new JScrollPane(lblContentIntro);

			jpItem.add(jpBookPic);
			jpItem.add(jspBookInfo);
			jpItem.add(jspAuthorIntro);
			jpItem.add(jspContentIntro);
			jpItem.setVisible(true);

			jpBook.add(jpItem);
		}
		maxPage = ((int)jpBook.getPreferredSize().getHeight()+1)/ jspBook.getHeight() + 1;
		jpBook.repaint();
		jspBook.repaint();
		jspBook.setViewportView(jpBook);
		repaint();
	}

	/**
	 *
	 *
	 *
	 * 按书名|作者搜索书籍
	 */
	public boolean bookSearch() {
		String name = jtfSearch.getText();
		if(name.equals("") || name.equals("请输入要查找的书名")) return false;

		int counter = 0;
		for(int i = 0; i < numOfBook; i++) {
			bookToShow[i] = 0;
			String book_name = booklist[i].bookName;
			String book_author = booklist[i].author;
			if(book_name.contains(name)) {
				counter ++;
				bookToShow[i] = 1;
			}
			else if(book_author != null) {
				if(book_author.contains(name)) {
					counter ++;
					bookToShow[i] = 1;
				}
			}
		}
		lblBookNum.setText("共" + String.valueOf(counter) + "本书");
		return true;
	} 

	/**
	 *
	 *
	 *
	 * 按豆瓣评分筛选书籍
	 */
	public void bookFilter(String rank) {
		int counter = 0;
		if(rank == "全部") {
			for(int i = 0; i < numOfBook; i++) bookToShow[i] = 1;
			counter = numOfBook;
		}
		try{
			double grade = Double.parseDouble(rank.substring(2));
			for(int i = 0; i < numOfBook; i++) {
				bookToShow[i] = 0;
				if(booklist[i].grade == null) continue;
				double book_grade = Double.parseDouble(booklist[i].grade);
				if(book_grade >= grade) {
					counter ++;
					bookToShow[i] = 1;
				}
			}
		}catch(NumberFormatException ex){
   			System.out.println("浮点转换错误");
 		}
 		lblBookNum.setText("共" + String.valueOf(counter) + "本书");
	} 

	/**
	 * 
	 *
	 * 跳转页面
	 */
	public void pageJump() {
		try{
			int page = Integer.parseInt(jtfJumpPage.getText());
			if(page <= maxPage) {
				int current = jspBook.getHeight() * (page-1);
				jspBook.getVerticalScrollBar().setValue(current);
			}
			jtfCurrentPage.setText(page + "/" + maxPage);
		}catch(NumberFormatException ex){
   			System.out.println("页面整数转换错误");
 		}
	}

	/**
	 * 
	 *
	 * 从网页上获取内容
	 */
	public static String getContentFromUrl( String strUrl ) {
		try { 
			URL url = new URL(strUrl);  
			InputStream stream = url.openStream();
			String content = readAll( stream,"UTF-8" ); //常见的编码包括 GB2312, UTF-8
			return content;
		}catch(MalformedURLException mue) { 
			System.out.println("URL格式有错！"); 
		}catch(IOException ioe) {
			System.out.println("IO异常："+ioe ); 
		}
		return "";
	}

	/**
	 * 
	 *
	 * 读取所有内容
	 */
	public static String readAll( InputStream stream, String charcode ) throws IOException {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(stream, charcode)); 
		StringBuilder sb = new StringBuilder();
		String line; 
		while ((line = reader.readLine()) != null) { 
			sb.append(line+"\n"); 
		} 
		return sb.toString();
	}

	/**
	 * 
	 *
	 * 正则表达式匹配
	 */	
  	static Set<String> RegexString(String target, String patternString) {
		Set<String> set = new HashSet<String>();
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(target);
		while (matcher.find()) {
			set.add(matcher.group());
		}
		return set;  
 	}

	/**
	 * 
	 *
	 * 获取内容简介和作者简介的Regex函数 因为顺序是 内容简介 --> 作者简介
	 */	
  	static ArrayList<String> RegexStringOfAbstract(String target, String patternString) {
		ArrayList<String> array = new ArrayList<String>();
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(target);
		while (matcher.find()) {
			array.add(matcher.group());
		}
		return array;  
 	}
	
	/**
	 * 
	 *
	 * 豆瓣爬虫 爬取书籍
	 */
	public static void getBookInfo(){
		getAllBookURL(url);		// 获取所有书籍网址
		getContentOfBook();		// 获取所有书籍信息
		saveBookListToFile();	// Book类序列化之后保存书籍信息到本地booklist.ser文件中
		try {
			saveBookImage();	// 下载所有书籍封面图片到本地文件夹中
		}catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/**
	 *  
	 *
	 * 获取所有书籍网址
	 */
	public static void getAllBookURL(String srcURL) {
		addTagURL(srcURL);
		for(Iterator it = bookTagURLSet.iterator() ; it.hasNext();) {
			String tagURL = (String)(it.next());
			int page = 5;
			for(int i = 1; i <= page; i++) {					// 爬取各个标签下前page页的书籍
				addBookURL(tagURL + "?start=" + 20*i + "&type=T");
			}
			if(bookSet.size() > numOfBook) break;         // 此处改变所需书籍总数
		}
		booklist = new Book[bookSet.size()];
		for(int i = 0 ; i < bookSet.size() ; i++){
			booklist[i] = new Book();
		}
	}

	/**
	 *  
	 *
	 * 获取所有标签网址
	 */	
	public static void addTagURL(String tagURL) {
		String tagURLContent, tag, suffix;
		try {
			System.out.println("开始爬取标签信息");
			tagURLContent = getContentFromUrl(tagURL);
			Set<String> tagSet = RegexString(tagURLContent, "/tag/[\\u4e00-\\u9fa5]*");			
			if(!tagSet.isEmpty()) {
				//System.out.println(tagSet.size());
				for( Iterator it = tagSet.iterator() ; it.hasNext();) {
					tag = (String)(it.next());
					if(tag.length() <= 5) continue;
			    	int index = tag.lastIndexOf("/");
			    	suffix = URLEncoder.encode(tag.substring(index+1), "UTF-8");  // 汉字编码后得到正确网址
					System.out.println(tag + "    https://book.douban.com/tag/" + suffix);
			    	bookTagURLSet.add("https://book.douban.com/tag/" + suffix);
				}
			} 
			System.out.println("爬取标签信息完毕");
		}catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 *
	 * 获取所有书籍网址
	 */
  	static public void addBookURL(String bookURL) {
  		String urlContent = new String();  
  		try{
  			int counter = 0;
			urlContent = getContentFromUrl(bookURL);			
			Set<String> _bookSet = RegexString(urlContent, "(https?|ftp|file)://book.douban.com/subject[-A-Za-z0-9\\u4e00-\\u9fa5+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");	
			for( Iterator it = _bookSet.iterator();  it.hasNext();) {
				// 爬虫防封IP
				if(counter % 10 == 0) {
	  				try { 
						Thread.sleep(1000); 
					}catch(InterruptedException e) { 
						e.printStackTrace(); 
					}
	  			}
				counter++;
				String _bookURL = (String)(it.next());
				if(_bookURL.indexOf("buylinks") != -1)
					continue;
				//System.out.println(_bookURL);
				bookSet.add(_bookURL);
			}
  		}catch(Exception ex) {
  			ex.printStackTrace();
  		}
  	}

  	/**
	 * 
	 *
	 * 在书籍网页获取书籍所有信息, 并将书籍文本内容保存到本地"豆瓣书籍.txt"文件
	 */
  	public static void getContentOfBook() { 
		int bookCounter = -1;
		int beginIdx , endIdx;
		String url , urlContent , title , grade , keywords , price , image;
		try{
			System.out.println("开始爬取书籍信息 共" + bookSet.size() + "本书");
			File file = new File("豆瓣书籍.txt"); 
			FileWriter fileWriter = new FileWriter(file);          
			fileWriter.write("豆瓣书籍信息汇总: (共" + bookSet.size() + "本)\n\n");
	 
	  		for( Iterator it = bookSet.iterator();  it.hasNext();) {
	  			// 每爬取10本书就间隔1s再爬 防止被封IP
	  			if(bookCounter % 10 == 0) {
	  				try { 
						Thread.sleep(1000); 
					}catch(InterruptedException e) { 
						e.printStackTrace(); 
					}
	  			}
				bookCounter++;
				System.out.println("已经爬取了" + bookCounter + "本书籍信息");
				fileWriter.flush();
				booklist[bookCounter].setBookNum(bookCounter);
	  			url = new String((String)it.next());			  
	  			urlContent = getContentFromUrl(url);
	  			
	  			// 获取书名
	  			Set<String> titleSet = RegexString(urlContent, "<title>(.)*</title>");  			
	  			if(!titleSet.isEmpty()) {
	  				title = titleSet.iterator().next();
	  				beginIdx = title.indexOf(">");  
	  				endIdx = title.lastIndexOf("<");
					booklist[bookCounter].setBookName(title.substring(beginIdx+1, endIdx-5));
				} 			
	  			
				// 获取豆瓣评分
	  			Set<String> gradeSet = RegexString(urlContent,"<strong class=\"ll rating_num \" property=\"v:average\"> \\d\\.\\d </strong>");
	  			if(!gradeSet.isEmpty()) {
	  				grade = gradeSet.iterator().next();
	  				beginIdx = grade.indexOf(">");  
	  				endIdx = grade.lastIndexOf("<");
					booklist[bookCounter].setGrade(grade.substring(beginIdx+1, endIdx));
				}
				
	  			// 获取作者以及出版社信息
	  			Set<String> keywordSet = RegexString(urlContent, "<meta name=\"keywords\" content=\"(.)*\">");
	  			if(!keywordSet.isEmpty()) {
	  				keywords = keywordSet.iterator().next();
	  				beginIdx = keywords.indexOf(","); 
					String _keywords = keywords.substring(beginIdx+1);
	  				endIdx = _keywords.indexOf(",");
					booklist[bookCounter].setAuthor(_keywords.substring(0, endIdx));

					beginIdx = _keywords.indexOf(","); 
					_keywords = _keywords.substring(beginIdx+1);
					endIdx = _keywords.indexOf(",");
					booklist[bookCounter].setPress(_keywords.substring(0, endIdx));
				} 
				
				// 获取定价
	  			Set<String> priceSet = RegexString(urlContent,"<span class=\"pl\">定价:</span>(.*)<br/>");
	  			if(!priceSet.isEmpty()) {
	  				price = priceSet.iterator().next();
	  				beginIdx = price.indexOf("</span>");  
	  				endIdx = price.lastIndexOf("<");
					booklist[bookCounter].setPrice(price.substring(beginIdx+8, endIdx));
				}else booklist[bookCounter].setPrice("无价格信息");
				
				// 获取封面图片链接
				Set<String> imageSet = RegexString(urlContent,"<img src=\"(.*)\" title=\"点击看大图\"");
				if(!imageSet.isEmpty()) {
					image = imageSet.iterator().next();
					beginIdx = image.indexOf("src");
					endIdx = image.lastIndexOf(" title");
					booklist[bookCounter].setImage(image.substring(beginIdx+5, endIdx-1));
				}
								
				// 获取内容简介 作者简介
	  			ArrayList<String> intro = RegexStringOfAbstract(urlContent,"<div class=\"intro\">(\\s)*(.)*</p></div>");
	  			if(!intro.isEmpty()) {
	  				int cnt = 1;
	  				for(Iterator introIterator = intro.iterator(); introIterator.hasNext();) {
	  					String para = (String)introIterator.next();
	  					beginIdx = para.indexOf("<p>");
	  					endIdx = para.lastIndexOf("</p>");
	  					if(para.indexOf("展开全部") == -1) {
	  						String _para = para.substring(beginIdx+3, endIdx-1);
	
	  						_para = _para.replace("<p>", "");
	  						_para = _para.replace("</p>", "\n");
	  						_para.trim();
							if( cnt == 1 ) booklist[bookCounter].setContentIntro("    " + _para);
							if( cnt == 2) booklist[bookCounter].setAuthorIntro("    " + _para);
	  						cnt += 1;
	  					}else continue;
	  				}
				}
				fileWriter.write(booklist[bookCounter].printInfo());
	  		}
			fileWriter.flush();
			fileWriter.close();
			System.out.println("爬取书籍信息完毕");
		} catch (IOException e) {   
			e.printStackTrace();  
		}
  	}
  	/**
	 * 
	 *
	 * 将Book类序列化 保存Book类信息到本地
	 */
	public static void saveBookListToFile(){
		try{
			FileOutputStream outFile = new FileOutputStream("booklist.ser");
			ObjectOutputStream out = new ObjectOutputStream(outFile);
			for(Book item : booklist){
				out.writeObject(item);	
			}
			out.close();
			outFile.close();
			System.out.println("Save Books Successfully!");
		}catch(IOException i){
			i.printStackTrace();
		}
	}

	/**
	 * 
	 *
	 * 将Book类反序列化 将本地Book类信息加载进来
	 */
	public static void loadBookListFromFile(int bookNum){
		booklist = new Book[bookNum];
		for(int i = 0 ; i < bookNum ; i++){
			booklist[i] = new Book();
		}
		try{
			FileInputStream inFile = new FileInputStream("booklist.ser");
			ObjectInputStream in = new ObjectInputStream(inFile);
			for( int i = 0 ; i < numOfBook ; i++){
				booklist[i] = (Book)(in.readObject());
			}		
			in.close();
			inFile.close();
		}catch(IOException i){
			i.printStackTrace();
		}catch(ClassNotFoundException c){
			System.out.println("Not Found");
			c.printStackTrace();
		}	
		System.out.println("Load Books Successfully!");
	}

	/**
	 * 
	 *
	 * 下载书籍封面并保存在本地文件夹中
	 */
	public static void saveBookImage() throws Exception {
		System.out.println("开始下载图片!");
		File file = new File("bookImage");
		URLConnection imageConnection = null;
		InputStream imageInputStream = null;
		for(int i = 0 ; i < bookSet.size() ; i++) {
			if(booklist[i].image == null) {
				continue;
			}
			System.out.println("正在下载第 " + i + " 张图片 " + booklist[i].image);
			URL url = new URL(booklist[i].image);
			try {
				imageConnection = url.openConnection();
				imageInputStream = imageConnection.getInputStream();
			}catch(Exception e) {
				System.out.println("图片下载失败!");
				continue;
			}
			OutputStream imageOutputStream = new FileOutputStream(new File("bookImage//" + i + ".jpg"));
			byte[] b = new byte[2048];
			int len = 0;
			while((len = imageInputStream.read(b)) != -1) {
				imageOutputStream.write(b, 0, len);
			}
		}
		System.out.println("图片下载完成");
	}

 	public static void main(String[] args) {
		// 先预处理爬取豆瓣书籍信息并保存 
		// 这里已经保存好了书籍所有不用再运行getBookInfo()函数
		//getBookInfo();
		
		// 再界面化将书籍信息显示出来 
		Crawl crawl = new Crawl();
		crawl.init();
		crawl.start();
 	}
}









