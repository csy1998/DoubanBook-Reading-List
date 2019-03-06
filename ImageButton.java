package doubanCrawl;

import java.io.*;
import java.util.*; 
import java.awt.*;  
import javax.swing.*;

/**
 *
 * 
 * 自己封装的ImageButton类
 * 用来显示书籍封面，并实现点按操作
 */

public class ImageButton extends JButton {
	public Image img;
	public String bookName;
	public int bookNumber;
 	public int gap;	// 不同模式Button之间的gap不同

 	/**
 	 *
 	 *	 
 	 * 
 	 * @param offset 区别网格模式和列表模式
 	 */
 	public ImageButton(Image img, int bookNumber, int offset){
		this.img = img;
		if(offset == 1) this.gap = 30;
		else this.gap = 0;
		this.bookNumber = bookNumber;
		Dimension size = new Dimension(150, 250);
		setSize(size);
		setPreferredSize(size);
		setLayout(null);
 	}

 	public void setBookInfo(JLabel lblBookName, JLabel lblGrade){
 		this.bookName = lblBookName.getText();
		add(lblBookName);
		add(lblGrade);
		lblBookName.setBounds(0, 220, 200, 15);
		lblGrade.setBounds(0, 235, 200, 15);
 	}
 	
	public String getBookName() {
		return bookName;
	}

	@Override
	protected void paintComponent(Graphics g) {
		//Dimension size = this.getParent().getSize();
		g.drawImage(img, 0, 0, 200, 220+this.gap, this);
		//g.drawImage(img, 0, 0, size.width, size.height, this);//此方法中的图片大小可随屏幕的改变而改变
	}
	 	
	public static void main(String[] args) {

	}
}