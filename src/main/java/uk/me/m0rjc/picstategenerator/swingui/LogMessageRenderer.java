package uk.me.m0rjc.picstategenerator.swingui;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Render the Message field in the table.
 *
 * @author Richard Corfield <m0rjc@raynet-uk.net>
 */
public class LogMessageRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;

	private static final Color COLOUR_DEBUG = Color.BLACK;
	private static final Color COLOUR_INFO = Color.BLUE;
	private static final Color COLOUR_WARNING = Color.ORANGE;
	private static final Color COLOUR_SEVERE = Color.RED;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{

		if(value instanceof LogDataModel.Message)
		{
			LogDataModel.Message message = (LogDataModel.Message) value;
			Component renderer = super.getTableCellRendererComponent(table, message.getText(), isSelected, hasFocus, row, column);

			Level level = message.getLevel();
			if(Level.INFO.equals(level))
			{
				renderer.setForeground(COLOUR_INFO);
			}
			else if(Level.WARNING.equals(level))
			{
				renderer.setForeground(COLOUR_WARNING);
			}
			else if(Level.SEVERE.equals(level))
			{
				renderer.setForeground(COLOUR_SEVERE);
			}
			else
			{
				renderer.setForeground(COLOUR_DEBUG);
			}
			return renderer;
		}

		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}
