package doubanCrawl;	

import java.io.*;
import java.util.*;

/**
 *
 * 
 *  
 * Book类封装书籍的所有信息
 */

public class Book implements Serializable{
		int bookNum;
		String bookName;
		String grade;
		String author;
		String press;
		String price;
		String contentIntro;
		String authorIntro;
		String image;  // 书籍封面网址
		public void setBookNum(int bookNum){
			this.bookNum = bookNum;
		}
		public void setBookName(String bookName){
			this.bookName = bookName;
		}
		public void setGrade(String grade){
			this.grade = grade;
			if( this.grade == null ) this.grade = "暂无评分";
		}
		public void setAuthor(String author){
			this.author = author;
			//if(this.author == null) this.author = "无作者"; 
		}
		public void setPress(String press){
			this.press = press;
			//if(this.press == null) this.press = "无出版社"; 
		}
		public void setPrice(String price){
			this.price = price;
			//if(this.price == null) this.price = "暂无价格"; 
		}
		public void setImage(String image){
			this.image = image;
		}
		public void setContentIntro(String contentIntro){
			this.contentIntro = contentIntro;
			//if(this.contentIntro == null) this.contentIntro = "暂无内容简介"; 
		}
		public void setAuthorIntro(String authorIntro){
			this.authorIntro = authorIntro;
			//if(this.authorIntro  == null) this.authorIntro  = "暂无作者简介";
		}
		// String形式返回书籍所有信息
		public String printInfo(){
			StringBuffer info = new StringBuffer();
			info.append("#书号: " + bookNum + "\n");
			info.append("#书名: " + bookName + "\n"); 
			info.append("#豆瓣评分: " + grade + "\n");
			info.append("#作者: " + author + "\n");
			info.append("#出版社: " + press + "\n");
			info.append("#定价:  " + price + "\n");
			info.append("#内容简介: \n" + "" + contentIntro + "\n");
			info.append("#作者简介: \n" + "" + authorIntro + "\n");
			info.append("*************************************************************\n\n");
			return info.toString();
		}
		public static void main(String[] args) {
				
		}
	}
	