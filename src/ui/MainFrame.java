package ui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wb.swt.SWTResourceManager;

import core.CopyToClipboard;
import core.FileParser;

public class MainFrame
{
	public static final String VERSION = "0.7";
	
	protected Shell shell;
	private Text txtGoogle;
	private Text txtWoS;
	private Text txtPapers;
	private Button btnRun;
	private Label lblStatus;
	
	private String[] filePaths;
	
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
	
	private void chooseFile()
	{
        FileDialog fd = new FileDialog(shell, SWT.MULTI);
        fd.setText("저장된 HTML 문서 선택");
        fd.setFilterPath("C:/");
        String[] filterExt = {"*.html", "*.htm", "*.txt"};
        fd.setFilterExtensions(filterExt);
        if(fd.open() != null)
        {
        	String filterPath = fd.getFilterPath();
        	filePaths = fd.getFileNames();
        	for(int i = 0; i < filePaths.length; i++)
        		filePaths[i] = filterPath + File.separator + filePaths[i];
    		btnRun.setEnabled(true);
        }
	}
	
	
	private void parseFiles()
	{
		btnRun.setEnabled(false);
		clear();
		try
		{
			String[] result = new FileParser(txtPapers.getText()).parse(filePaths);
			shell.getDisplay().syncExec(()-> { txtGoogle.append(result[0]); });
			shell.getDisplay().syncExec(()-> { txtWoS.append(result[1]); });
		}
		catch(Exception e)
		{
			lblStatus.setText("오류 발생");
	        MessageBox dia = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
	        dia.setText("오류");
	        dia.setMessage(e.getMessage()); 
	        dia.open();
		}
		
		btnRun.setEnabled(true);
	}
	
	public void clear()
	{
		shell.getDisplay().syncExec(()-> { txtGoogle.setText(""); });
		shell.getDisplay().syncExec(()-> { txtWoS.setText(""); });
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
		
		Button btnNewButton = new Button(compMaster, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) { chooseFile(); }
		});
		btnNewButton.setFont(SWTResourceManager.getFont("맑은 고딕", 16, SWT.NORMAL));
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setText("파일 선택");
		
		btnRun = new Button(compMaster, SWT.NONE);
		btnRun.setEnabled(false);
		btnRun.setFont(SWTResourceManager.getFont("맑은 고딕", 16, SWT.NORMAL));
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) { parseFiles(); }
		});
		btnRun.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnRun.setText("파일에서 읽기");
		
		lblStatus = new Label(compMaster, SWT.RIGHT);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblStatus.setText("상태");
	}
}
