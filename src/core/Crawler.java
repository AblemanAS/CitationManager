package core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import ui.MainFrame;


public class Crawler extends Thread
{
	private String					dateString;
	private CloseableHttpClient		httpclient;
	private ResponseHandler<String> handler;
	private String 					papersStr;
	
	private boolean					on;
	private MainFrame 				ui;
	
	public Crawler(String papersStr, MainFrame ui)
	{		
		this.papersStr = papersStr;
		this.ui = ui;
	}
	
	@Override
	public void run()
	{
		httpclient = HttpClients.createDefault();
		handler = new BasicResponseHandler();
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		dateString = " (" + dateFormat.format(cal.getTime()) + ")";
		
		on = true;
		ui.setProgress(0);
		ui.clear();
		
		String[] papersStrArr = papersStr.split("\n");
		ArrayList<String> paperList = new ArrayList<String>();
		for(String eString : papersStrArr)
		{
			String paper = eString.trim();
			if(paper.length() > 5) paperList.add(paper);
		}
		
		try
		{
			getCitations(paperList);
		} catch(Exception e) { ui.showError(e.toString()); }
		ui.onCrawlEnd();
	}
	
	public void turnOff() { on = false; }
		
	public void getCitations(List<String> papers) throws Exception
	{
		HttpGet 				httpget;
		CloseableHttpResponse	response = null;
		String					responseString;
		long					lastQuery = 0;
		
		httpget = new HttpGet("http://google.com");
		response = httpclient.execute(httpget);
		int count = 0;
		
		for(String eString : papers)
		{
			count++;
			ui.setStatusString(eString + " (" + count + " / " + papers.size() + ")");
			
			long currentQuery = System.currentTimeMillis();
			long reverseTimeGap = lastQuery - currentQuery + 1500;
			if(reverseTimeGap > 0) try { Thread.sleep(reverseTimeGap); } catch(Exception e) {}
			lastQuery = currentQuery;
			
			httpget = new HttpGet("http://scholar.google.co.kr/scholar?hl=ko&q=" + eString.replaceAll(" ", "%20"));
			response = httpclient.execute(httpget);
			responseString = handler.handleResponse(response);
			int find;
			
			// Google Scholar Citation
			find = responseString.indexOf("회 인용");
			if(find == -1) ui.appendGoogle("0회" + dateString);
			else
			{
				int first	= find;
				int last	= find + 1;
				while(responseString.charAt(--first) != '>');
				ui.appendGoogle(responseString.substring(first+1, last) + dateString);
			}

			// Web of Science Citation
			find = responseString.indexOf("Web of Science: ");
			if(find == -1) ui.appendWoS("0회" + dateString);
			else
			{
				int first	= find + 16;
				int last	= first;
				while(responseString.charAt(++last) != '<');
				ui.appendWoS(responseString.substring(first, last) + "회" + dateString);
			}
			
			ui.setProgress((count * 100) / papers.size());
			if(!on) break;
		}
		
		response.close();
		ui.setStatusString(count == papers.size() ? "완료" : "중지됨");
	}
}