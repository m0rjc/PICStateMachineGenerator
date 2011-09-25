package m0rjc.ax25.generator.swingui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.LogRecord;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A JFrame that allow the user to select a file and run it throuh the processor.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class SwingRunner extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JTextField m_txtFileName;
	private AbstractAction m_goAction;
	private AbstractAction m_openFileAction;
	private LogDataModel m_logModel = new LogDataModel();
	private JFileChooser m_fileChooser = new JFileChooser();
	private ProcessWorker m_processWorker;
	
	public SwingRunner() throws HeadlessException
	{
		super("Micrcontroller State Machine Generator");
		layoutComponent();
		pack();
		
		m_fileChooser.setCurrentDirectory(new File("."));
	}

	private void layoutComponent()
	{
		setLayout(new BorderLayout());
		
		JPanel filePanel = new JPanel();
		add(filePanel, BorderLayout.PAGE_START);
		filePanel.setLayout(new BorderLayout());
		JLabel fileLabel = new JLabel("Input file:");
		filePanel.add(fileLabel, BorderLayout.LINE_START);

		m_openFileAction = new AbstractAction("...") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				openFile();
			}
		};
		
		m_goAction = new AbstractAction("Run") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				run();
			}
		};
		
		m_txtFileName = new JTextField();
		m_txtFileName.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				setGreyStates();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				setGreyStates();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				setGreyStates();
			}
		});
		
		filePanel.add(m_txtFileName, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		filePanel.add(buttonPanel, BorderLayout.LINE_END);
		buttonPanel.add(new JButton(m_openFileAction));
		buttonPanel.add(new JButton(m_goAction));
		
		JTable table = new JTable(m_logModel);
		table.setDefaultRenderer(LogDataModel.Message.class, new LogMessageRenderer());
		JScrollPane scroller = new JScrollPane(table);
		add(scroller, BorderLayout.CENTER);
		
		setGreyStates();
	}

	/**
	 * Pop up a file dialog to allow the user to select a file.
	 */
	void openFile()
	{
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "XML Files", "xml");
	    m_fileChooser.setFileFilter(filter);
	    int returnVal = m_fileChooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
	       m_txtFileName.setText(m_fileChooser.getSelectedFile().getPath());
	       setGreyStates();
	    }
	}

	/**
	 * Run the process
	 */
	void run()
	{
		m_processWorker = new ProcessWorker(m_txtFileName.getText(), new ProcessWorkerListener() {
			@Override
			public void process(LogRecord entry)
			{
				m_logModel.addRecord(entry);
			}
			
			@Override
			public void done(boolean succeeded)
			{
				m_processWorker = null;
				setGreyStates();
			}
		});
		m_logModel.startLog();
		m_processWorker.execute();
	}
	
	/**
	 * Set the availability of controls based on the current state of the program.
	 */
	void setGreyStates()
	{
		boolean isRunning = m_processWorker != null;
		m_txtFileName.setEnabled(!isRunning);
		m_openFileAction.setEnabled(!isRunning);
		
		m_goAction.setEnabled(m_txtFileName.getText().length() > 0);
	}
	
	
}
