package core;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;

/***
 * @reference http://www.stechstar.com/user/zbxe/index.php?document_srl=24299&mid=AlgorithmJava
 */
public class CopyToClipboard implements ClipboardOwner
{
	private static CopyToClipboard self;
	public static CopyToClipboard getInstance()
	{ return new CopyToClipboard(); }
	
	public void setClipboardContents(String aString)
	{
	    StringSelection stringSelection = new StringSelection(aString);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(stringSelection, this);
    }

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {}
}
