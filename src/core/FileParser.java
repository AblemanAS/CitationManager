package core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class FileParser
{
	private String		dateString;
	private String 		papersStr;

	public FileParser(String papersStr)
	{ this.papersStr = papersStr; }
	
	public String[] parse(String[] filePaths) throws Exception
	{
		StringBuilder sbGoogle = new StringBuilder();
		StringBuilder sbWoS = new StringBuilder();
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		dateString = " (" + dateFormat.format(cal.getTime()) + ")\n";
		
		// Get Paper list
		String[] papersStrArr = papersStr.split("\n");
		ArrayList<String> paperList = new ArrayList<String>();
		for(String eString : papersStrArr)
		{
			String paper = eString.trim();
			if(paper.length() > 5) paperList.add(paper);
		}
		// Read and Parse Documents
		List<Paper> parsedPapers = new ArrayList<Paper>();
		for(String eString : filePaths)
		{
			// Parse
			Document doc = Jsoup.parse(new File(eString), "UTF-8");
			Elements eList = doc.getElementById("gs_res_ccl_mid").children();
			for(Element item : eList)
			{
				item = item.getElementsByClass("gs_ri").get(0);
				
				// Paper Title
				String paperName = item.getElementsByClass("gs_rt").get(0).child(0).text();
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

		System.out.println("===== 논문 목록 =====");
		for(String ePaper : paperList)
			System.out.println(ePaper);

		System.out.println("\n\n===== 문서에서 읽은 리스트 =====");
		for(Paper ePaper : parsedPapers)
			System.out.println(ePaper.title + " : " + ePaper.citeGoogle + ", " + ePaper.citeWoS);

		// Get Citations
		System.out.println("\n\n===== 유사도 매칭 =====");
		LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();
		for(String eString : paperList)
		{
			int leastScore = Integer.MAX_VALUE;
			String citeStrGoogle = "";
			String citeStrWoS = "";
			String strNearest = "";
			
			for(Paper ePaper : parsedPapers)
			{
				int score = ld.apply(ePaper.title.toLowerCase(), eString.toLowerCase());
				if(score < leastScore)
				{
					leastScore = score;
					strNearest = ePaper.title;
					citeStrGoogle = ePaper.citeGoogle;
					citeStrWoS = ePaper.citeWoS;
				}
			}

			System.out.println(eString + " : " + leastScore + " of " + eString.length() + " (" + (float)leastScore / (float)eString.length() + ")");
			System.out.println(strNearest);
			
			if(leastScore < eString.length() / 5)
			{
				sbGoogle.append(citeStrGoogle);
				sbGoogle.append(dateString);
				sbWoS.append(citeStrWoS);
				sbWoS.append(dateString);
			}
			else
			{
				sbGoogle.append("미등록\n");
				sbWoS.append("미등록\n");
			}
		}
		
		sbGoogle.deleteCharAt(sbGoogle.length() - 1);
		sbWoS.deleteCharAt(sbWoS.length() - 1);
		String[] result = new String[2];
		result[0] = sbGoogle.toString();
		result[1] = sbWoS.toString();
		return result;
	}
}
