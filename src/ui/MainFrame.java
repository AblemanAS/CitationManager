package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import core.CopyToClipboard;
import core.Crawler;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wb.swt.SWTResourceManager;

public class MainFrame
{
	public static final String VERSION = "0.5a";
	
	protected Shell shell;
	private Text txtGoogle;
	private Text txtWoS;
	private Text txtPapers;
	private Button btnRun;
	private ProgressBar progressBar;
	private Label lblStatus;
	
	private Crawler crawler;
	private int txtCountGoogle;
	private int txtCountWoS;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void open()
	{
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while(!shell.isDisposed())
		{
			if(!display.readAndDispatch())
				display.sleep();
		}
	}
	
	private void copyToClipboard(Text btn)
	{
		shell.getDisplay().syncExec(()->
		{
			CopyToClipboard.getInstance().setClipboardContents(btn.getText());
	        MessageBox dia = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
	        dia.setText("알림");
	        dia.setMessage("클립보드로 복사되었습니다."); 
	        dia.open();
		});
	}
	
	private void toggleCrawl()
	{
		if(crawler == null)
		{
			shell.getDisplay().syncExec(()->
			{
				btnRun.setEnabled(false);
				btnRun.setText("정지");
				crawler = new Crawler(txtPapers.getText(), this);
				crawler.start();
				btnRun.setEnabled(true);
			});
		}
		else
		{
			shell.getDisplay().syncExec(()->
			{
				btnRun.setEnabled(false);
				btnRun.setText("크롤링 시작");
				crawler.turnOff();
				crawler = null;
			});
		}
	}
	

	/***
	 * @param progress 0~100
	 */
	public void setProgress(int progress)
	{
		shell.getDisplay().asyncExec(()-> { progressBar.setSelection(progress);});
	}
	
	public void appendGoogle(String str)
	{
		if(txtCountGoogle != 0)
			shell.getDisplay().asyncExec(()-> { txtGoogle.append("\n"); });
		shell.getDisplay().asyncExec(()-> { txtGoogle.append(str); });
		txtCountGoogle++;
	}

	public void appendWoS(String str)
	{
		if(txtCountWoS != 0)
			shell.getDisplay().asyncExec(()-> { txtWoS.append("\n"); });
		shell.getDisplay().asyncExec(()-> { txtWoS.append(str); });
		txtCountWoS++;
	}
	
	
	public void clear()
	{
		shell.getDisplay().syncExec(()-> { txtGoogle.setText(""); });
		shell.getDisplay().syncExec(()-> { txtWoS.setText(""); });
		txtCountGoogle = 0;
		txtCountWoS = 0;
	}
	
	public void showError(String str)
	{
		shell.getDisplay().syncExec(()->
		{
			lblStatus.setText("오류 발생");
	        MessageBox dia = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
	        dia.setText("오류");
	        dia.setMessage(str); 
	        dia.open();
		});
	}
	
	public void setStatusString(String str)
	{ shell.getDisplay().asyncExec(()-> { lblStatus.setText(str); }); }
	
	public void onCrawlEnd()
	{
		shell.getDisplay().syncExec(()->
		{
			btnRun.setText("크롤링 시작");
			btnRun.setEnabled(true);
			crawler = null;
		});
	}
	
	
	protected void createContents()
	{
		shell = new Shell();
		shell.setSize(779, 645);
		shell.setText("Citation Manager (ver " + VERSION + ")");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite compMaster = new Composite(shell, SWT.NONE);
		GridLayout gl_compMaster = new GridLayout(1, false);
		compMaster.setLayout(gl_compMaster);
		
		Composite compInformation = new Composite(compMaster, SWT.BORDER);
		compInformation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		FillLayout fl_compInformation = new FillLayout(SWT.HORIZONTAL);
		fl_compInformation.marginWidth = 5;
		fl_compInformation.marginHeight = 5;
		compInformation.setLayout(fl_compInformation);
		
		Composite compPapers = new Composite(compInformation, SWT.NONE);
		GridLayout gl_compPapers = new GridLayout(1, false);
		compPapers.setLayout(gl_compPapers);
		
		Label lblPapers = new Label(compPapers, SWT.CENTER);
		lblPapers.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblPapers.setText("논문 목록");
		
		txtPapers = new Text(compPapers, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtPapers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compCites = new Composite(compInformation, SWT.NONE);
		compCites.setLayout(new GridLayout(2, false));
		
		Label lblGoogle = new Label(compCites, SWT.NONE);
		lblGoogle.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblGoogle.setText("Google");
		
		Label lblWoS = new Label(compCites, SWT.NONE);
		lblWoS.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblWoS.setText("WoS");
		
		txtGoogle = new Text(compCites, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		txtGoogle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		
		txtWoS = new Text(compCites, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		txtWoS.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		
		Button btnGoogle = new Button(compCites, SWT.NONE);
		btnGoogle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) { copyToClipboard(txtGoogle); }
		});
		btnGoogle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnGoogle.setText("클립보드로 복사");
		
		Button btnWoS = new Button(compCites, SWT.NONE);
		btnWoS.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) { copyToClipboard(txtWoS); }
		});
		btnWoS.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnWoS.setText("클립보드로 복사");
		
		btnRun = new Button(compMaster, SWT.NONE);
		btnRun.setFont(SWTResourceManager.getFont("맑은 고딕", 20, SWT.NORMAL));
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) { toggleCrawl(); }
		});
		btnRun.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnRun.setText("크롤링 시작");
		
		progressBar = new ProgressBar(compMaster, SWT.NONE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		lblStatus = new Label(compMaster, SWT.RIGHT);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblStatus.setText("상태");
	}
}
