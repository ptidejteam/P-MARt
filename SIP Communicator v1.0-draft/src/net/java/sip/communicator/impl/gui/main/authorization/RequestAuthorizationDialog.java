/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.gui.main.authorization;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;

import net.java.sip.communicator.impl.gui.main.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.swing.*;

/**
 * The <tt>RequestAuthorisationDialog</tt> is a <tt>JDialog</tt> that is
 * shown when user is trying to add a contact, which requires authorization.
 * 
 * @author Yana Stamcheva
 */
public class RequestAuthorizationDialog
    extends SIPCommDialog
    implements ActionListener
{
    public static final int OK_RETURN_CODE = 1;
    
    public static final int CANCEL_RETURN_CODE = 0;
    
    private JTextArea infoTextArea = new JTextArea();
    
    private JLabel requestLabel = new JLabel(
        GuiActivator.getResources()
            .getI18NString("service.gui.TYPE_YOUR_REQUEST") + ": ");
    
    private JTextField requestField = new JTextField();

    private JPanel requestPanel = new TransparentPanel(new BorderLayout());

    private JPanel buttonsPanel =
        new TransparentPanel(new FlowLayout(FlowLayout.RIGHT));

    private String cancelString
        = GuiActivator.getResources().getI18NString("service.gui.CANCEL");
    
    private JButton requestButton = new JButton(
        GuiActivator.getResources().getI18NString("service.gui.REQUEST"));
    
    private JButton cancelButton = new JButton(cancelString);
    
    private JPanel mainPanel = new TransparentPanel(new BorderLayout(10, 10));
    
    private JPanel northPanel = new TransparentPanel(new BorderLayout());
    
    private JPanel titlePanel = new TransparentPanel(new GridLayout(0, 1));
    
    private JLabel iconLabel = new JLabel(new ImageIcon(
            ImageLoader.getImage(ImageLoader.AUTHORIZATION_ICON)));
    
    private JLabel titleLabel = new JLabel();
    
    private String title = GuiActivator.getResources()
        .getI18NString("service.gui.REQUEST_AUTHORIZATION");
    
    private Object lock = new Object();
    
    private int returnCode;
    
    /**
     * Constructs the <tt>RequestAuthorisationDialog</tt>.
     * 
     * @param contact The <tt>Contact</tt>, which requires authorisation.
     * @param request The <tt>AuthorizationRequest</tt> that will be sent.
     */
    public RequestAuthorizationDialog(MainFrame mainFrame, Contact contact,
            AuthorizationRequest request)
    {
        super(mainFrame);
        
        this.setModal(false);
        
        this.setTitle(title);
        
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setText(title);

        Font font = titleLabel.getFont();
        titleLabel.setFont(font.deriveFont(Font.BOLD, font.getSize2D() + 6));
        
        this.mainPanel.setPreferredSize(new Dimension(400, 230));
        
        infoTextArea.setText(GuiActivator.getResources().getI18NString(
            "service.gui.REQUEST_AUTHORIZATION_MSG", 
            new String[]{contact.getDisplayName()}));
        
        this.infoTextArea.setFont(Constants.FONT.deriveFont(Font.BOLD, 12f));
        this.infoTextArea.setLineWrap(true);
        this.infoTextArea.setOpaque(false);
        this.infoTextArea.setWrapStyleWord(true);
        this.infoTextArea.setEditable(false);
        
        this.titlePanel.add(titleLabel);
        this.titlePanel.add(infoTextArea);
        
        this.northPanel.add(iconLabel, BorderLayout.WEST);
        this.northPanel.add(titlePanel, BorderLayout.CENTER);
        
        this.requestPanel.add(requestLabel, BorderLayout.WEST);
        this.requestPanel.add(requestField, BorderLayout.CENTER);
        
        this.requestButton.setName("request");
        this.cancelButton.setName("cancel");
        
        this.requestButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
        
        this.buttonsPanel.add(requestButton);
        this.buttonsPanel.add(cancelButton);
        
        this.getRootPane().setDefaultButton(requestButton);
        this.requestButton.setMnemonic(
            GuiActivator.getResources().getI18nMnemonic("service.gui.REQUEST"));
        this.cancelButton.setMnemonic(
            GuiActivator.getResources().getI18nMnemonic("service.gui.CANCEL"));
        
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.mainPanel.add(northPanel, BorderLayout.NORTH);
        this.mainPanel.add(requestPanel, BorderLayout.CENTER);
        this.mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        this.getContentPane().add(mainPanel);
    }

    /**
     * Shows this modal dialog and returns the result of the user choice.
     * @return if the "Request" button was pressed returns OK_RETURN_CODE,
     * otherwise CANCEL_RETURN_CODE is returned
     */
    public int showDialog()
    {
        this.setVisible(true);
        
        this.requestField.requestFocus();
        
        synchronized (lock) {
            try {                    
                lock.wait();
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return returnCode;
    }
    
    /**
     * Handles the <tt>ActionEvent</tt> triggered when one user clicks
     * on one of the buttons.
     */
    public void actionPerformed(ActionEvent e)
    {
        JButton button = (JButton)e.getSource();
        String name = button.getName();
        
        if(name.equals("request")) {
            returnCode = OK_RETURN_CODE;
        }
        else if(name.equals("cancel")) {
            returnCode = CANCEL_RETURN_CODE;
        }
        
        synchronized (lock) {
            lock.notify();
        }
        
        this.dispose();
    }
    
    /**
     * The text entered as a resuest reason.
     * @return the text entered as a resuest reason
     */
    public String getRequestReason()
    {
        return requestField.getText();
    }

    protected void close(boolean isEscaped)
    {
        this.cancelButton.doClick();
    }   
}
