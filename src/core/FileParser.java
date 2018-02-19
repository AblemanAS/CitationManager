package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ui.MainFrame;

public class FileParser
{
	private String		dateString;
	private String 		papersStr;
	private MainFrame 	ui;

	public FileParser(String papersStr, MainFrame ui)
	{		
		this.papersStr = papersStr;
		this.ui = ui;
	}
	
	private String preprocess(String paperName)
	{ return paperName.toLowerCase().replaceAll("[^A-Za-z]+", ""); }
	
	public void parse(String[] filePaths) throws Exception
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		dateString = " (" + dateFormat.format(cal.getTime()) + ")";
		
		// Get Paper list
		String[] papersStrArr = papersStr.split("\n");
		ArrayList<String> paperList = new ArrayList<String>();
		for(String eString : papersStrArr)
		{
			String paper = eString.trim();
			if(paper.length() > 5) paperList.add(preprocess(paper));
		}
		
		// Read and Parse Documents
		List<Paper> parsedPapers = new ArrayList<Paper>();
		for(String eString : filePaths)
		{
			// Read
			BufferedReader reader = new BufferedReader(new FileReader(eString));
			StringBuffer strBuf = new StringBuffer();
			while(true)
			{
				String str = reader.readLine();
				if(str == null) break;
				strBuf.append(str + "\n");
			}
			reader.close();
			String fileString = strBuf.toString();

			// Parse
			Document doc = Jsoup.parse(fileString);
			Elements eList = doc.getElementById("gs_res_ccl_mid").children();
			for(Element item : eList)
			{
				item = item.getElementsByClass("gs_ri").get(0);
				
				// Paper Title
				String paperName = preprocess(item.getElementsByClass("gs_rt").get(0).text());
				String citeGoogle = "0회";
				String citeWoS = "0회";
				
				// Parse Citations
				Element citeElement = item.getElementsByClass("gs_fl").get(0);
				for(Element child : citeElement.children())
				{
					String elemText = child.text();
					// Google Scholar Citation
					if(elemText.contains("회 인용"))
					{
						elemText = elemText.replace(" 인용", "");
						citeGoogle = elemText;
					}

					// Web of Science Citation
					if(elemText.contains("Web of Science: "))
					{
						elemText = elemText.replace("Web of Science: ", "");
						citeWoS = elemText + "회";
					}
				}
				
				// Append
				parsedPapers.add(new Paper(paperName, citeGoogle, citeWoS));
			}
		}

		
		for(String ePaper : paperList)
			System.out.println(ePaper);
		
		for(Paper ePaper : parsedPapers)
			System.out.println(ePaper.title + " : " + ePaper.citeGoogle + ", " + ePaper.citeWoS);
		
		// Get Citations
		for(String eString : paperList)
		{
			boolean found = false;
			
			for(Paper ePaper : parsedPapers)
			{
				if(ePaper.title.contains(eString))
				{
					ui.appendGoogle(ePaper.citeGoogle + dateString);
					ui.appendWoS(ePaper.citeWoS + dateString);
					found = true;
					break;
				}
			}
			
			if(!found)
			{
				ui.appendGoogle("미등록");
				ui.appendWoS("미등록");
			}
		}
	}
}
