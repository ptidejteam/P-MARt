/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.print.*;
import javax.swing.text.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import javax.swing.ProgressMonitor;
import java.io.*;
import java.util.*;
import java.net.ServerSocket;

import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.awt.font.TextLayout;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.*;

import edu.rice.cs.util.swing.FindReplaceMachine;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import edu.rice.cs.util.*;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.config.BooleanOption;
import edu.rice.cs.drjava.model.print.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.platform.PlatformFactory;


/**
 * Handles the bulk of DrJava's program logic.
 * The UI components interface with the GlobalModel through its public methods,
 * and GlobalModel responds via the GlobalModelListener interface.
 * This removes the dependency on the UI for the logical flow of the program's
 * features.  With the current implementation, we can finally test the compile
 * functionality of DrJava, along with many other things.
 *
 * @version $Id: DefaultGlobalModel.java,v 1.1 2005/08/05 12:45:06 guehene Exp $
 */
public class DefaultGlobalModel implements GlobalModel, OptionConstants,
    DocumentIterator {

  static final String DOCUMENT_OUT_OF_SYNC_MSG =
    "Current document is out of sync with the Interactions Pane and should be recompiled!\n";

  // ----- FIELDS -----

  /**
   * Keeps track of all listeners to the model, and has the ability
   * to notify them of some event.  Originally used a Command Pattern style,
   * but this has been replaced by having EN directly implement all listener
   * interfaces it supports.  Set in constructor so that subclasses can install
   * their own notifier with additional methods.
   */
  final GlobalEventNotifier _notifier = new GlobalEventNotifier();

  // ---- Definitions fields ----

  /**
   * Factory for new definitions documents and views.
   */
  private final DefinitionsEditorKit _editorKit = new DefinitionsEditorKit(_notifier);

  /**
   * ListModel for storing all OpenDefinitionsDocuments.
   */
  private final DefaultListModel _definitionsDocs = new DefaultListModel();


  // ---- Interpreter fields ----

  /**
   * RMI interface to the Interactions JVM.
   * Package private so we can access it from test cases.
   */
  final MainJVM _interpreterControl = new MainJVM();

  /**
   * Interface between the InteractionsDocument and the JavaInterpreter,
   * which runs in a separate JVM.
   */
  protected DefaultInteractionsModel _interactionsModel;

  private CompilerListener _clearInteractionsListener =
    new CompilerListener() {
      public void compileStarted() {}

      public void compileEnded() {
        // Only clear interactions if there were no errors
        if ((_compilerModel.getNumErrors() == 0)
              // reset even when the interpreter is not used.
              //&& _interactionsModel.interpreterUsed()
              && _resetAfterCompile) {
          resetInteractions();
        }
      }

      public void saveBeforeCompile() {}
    };


  // ---- Compiler Fields ----

  /**
   * CompilerModel manages all compiler functionality.
   */
  private final CompilerModel _compilerModel = new DefaultCompilerModel(this);

  /**
   * Whether or not to reset the interactions JVM after compiling.
   * Should only be false in test cases.
   */
  private boolean _resetAfterCompile = true;


  // ---- JUnit Fields ----

  /**
   * JUnitModel manages all JUnit functionality.
   * TODO: remove dependence on GlobalModel
   */
  private final DefaultJUnitModel _junitModel =
    new DefaultJUnitModel(this, _interpreterControl, _compilerModel, this);


  // ---- Javadoc Fields ----

  /**
   * Manages all Javadoc functionality.
   */
  protected JavadocModel _javadocModel = new DefaultJavadocModel(this);

  // ---- Debugger Fields ----

  /**
   * Interface to the integrated debugger.  If the JPDA classes are not
   * available, this is set NoDebuggerAvailable.ONLY.
   */
  private Debugger _debugger = NoDebuggerAvailable.ONLY;

  /**
   * Port used by the debugger to connect to the Interactions JVM.
   * Uniquely created in getDebugPort().
   */
//  private int _debugPort = -1;


  // ---- Input/Output Document Fields ----

  /**
   * The document adapter used in the Interactions model.
   */
  private final SwingDocumentAdapter _interactionsDocAdapter;

  /**
   * The document used to display System.out and System.err,
   * and to read from System.in.
   */
  private final ConsoleDocument _consoleDoc;

  /**
   * The document adapter used in the console document.
   */
  private final SwingDocumentAdapter _consoleDocAdapter;

  /**
   * A lock object to prevent print calls to System.out or System.err
   * from flooding the JVM, ensuring the UI remains responsive.
   */
  private final Object _systemWriterLock = new Object();

  /**
   * Number of milliseconds to wait after each println, to prevent
   * the JVM from being flooded with print calls.
   * TODO: why is this here, and why is it public?
   */
  public static final int WRITE_DELAY = 50;

  /**
   * A PageFormat object for printing to paper.
   */
  private PageFormat _pageFormat = new PageFormat();

  /**
   * Listens for requests from System.in.
   */
  private InputListener _inputListener;


  // ----- CONSTRUCTORS -----

  /**
   * Constructs a new GlobalModel.
   * Creates a new MainJVM and starts its Interpreter JVM.
   */
  public DefaultGlobalModel() {

    _interactionsDocAdapter = new SwingDocumentAdapter();
    _interactionsModel =
      new DefaultInteractionsModel(this, _interpreterControl,
                                   _interactionsDocAdapter);

    _interpreterControl.setInteractionsModel(_interactionsModel);
    _interpreterControl.setJUnitModel(_junitModel);

    _consoleDocAdapter = new SwingDocumentAdapter();
    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);

    _inputListener = NoInputListener.ONLY;

    _createDebugger();

    _registerOptionListeners();

    // Chain notifiers so that all events also go to GlobalModelListeners.
    _interactionsModel.addListener(_notifier);
    _compilerModel.addListener(_notifier);
    _junitModel.addListener(_notifier);
    _javadocModel.addListener(_notifier);

    // Listen to compiler to clear interactions appropriately.
    // XXX: The tests need this to be registered after _notifier, sadly.
    //      This is obnoxiously order-dependent, but it works for now.
    _compilerModel.addListener(_clearInteractionsListener);

    // Perhaps do this in another thread to allow startup to continue...
    _interpreterControl.startInterpreterJVM();
  }

  /**
   * Constructor.  Initializes all the documents, but take the interpreter
   * from the given previous model. This is used only for test cases,
   * since there is substantial overhead to initializing the interpreter.
   *
   * Reset the interpreter for good measure since it's an old one.
   * (TODO: I'm not sure this is still correct or effective any more,
   *   now that we're always restarting the JVM.  Needs to be looked at...)
   * This has been commented out because it is never used and has bitrot.
   */
//  public DefaultGlobalModel(DefaultGlobalModel other) {
//    this(other._interpreterControl);
//
//    _interpreterControl.reset();
//    try {
//      _interactionsModel.setDebugPort(other.getDebugPort());
//      _interactionsModel.setWaitingForFirstInterpreter(false);
//    }
//    catch (IOException ioe) {
//      // Other model should already have a port, or it should be -1.
//      //  We shouldn't ever get an IOException here.
//      throw new UnexpectedException(ioe);
//    }
//  }

  /**
   * Constructs a new GlobalModel with the given MainJVM to act as an
   * RMI interface to the Interpreter JVM.  Does not attempt to start
   * the InterpreterJVM.
   * TODO: This has been commented out because it is never used and has bitrot.
   * @param control RMI interface to the Interpreter JVM
   */
//  public DefaultGlobalModel(MainJVM control) {
//    _interpreterControl = control;
//    _interactionsDocAdapter = new SwingDocumentAdapter();
//    _interactionsModel =
//      new DefaultInteractionsModel(this, control, _interactionsDocAdapter);
//    _interpreterControl.setInteractionsModel(_interactionsModel);
//    _interpreterControl.setJUnitModel(this);  // to be replaced by JUnitModel
//
//    _consoleDocAdapter = new SwingDocumentAdapter();
//    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);
//
//    _inputListener = NoInputListener.ONLY;
//
//    _createDebugger();
//
//    _registerOptionListeners();
//  }



  // ----- METHODS -----

  /**
   * Add a listener to this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void addListener(GlobalModelListener listener) {
    _notifier.addListener(listener);
  }

  /**
   * Remove a listener from this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void removeListener(GlobalModelListener listener) {
    _notifier.removeListener(listener);
  }

  // getter methods for the private fields

  public DefinitionsEditorKit getEditorKit() {
    return _editorKit;
  }

  /**
   * @return the interactions model.
   */
  public DefaultInteractionsModel getInteractionsModel() {
    return _interactionsModel;
  }

  /**
   * @return SwingDocumentAdapter in use by the InteractionsDocument.
   */
  public SwingDocumentAdapter getSwingInteractionsDocument() {
    return _interactionsDocAdapter;
  }

  public InteractionsDocument getInteractionsDocument() {
    return _interactionsModel.getDocument();
  }

  public ConsoleDocument getConsoleDocument() {
    return _consoleDoc;
  }

  public SwingDocumentAdapter getSwingConsoleDocument() {
    return _consoleDocAdapter;
  }

  public PageFormat getPageFormat() {
    return _pageFormat;
  }

  public void setPageFormat(PageFormat format) {
    _pageFormat = format;
  }

  /**
   * Gets the CompilerModel, which provides all methods relating to compilers.
   */
  public CompilerModel getCompilerModel() {
    return _compilerModel;
  }

  /**
   * Gets the JUnitModel, which provides all methods relating to JUnit testing.
   */
  public JUnitModel getJUnitModel() {
    return _junitModel;
  }

  /**
   * Gets the JavadocModel, which provides all methods relating to Javadoc.
   */
  public JavadocModel getJavadocModel() {
    return _javadocModel;
  }

  /**
   * Creates a new definitions document and adds it to the list.
   * @return The new open document
   */
  public OpenDefinitionsDocument newFile() {
    final OpenDefinitionsDocument doc = _createOpenDefinitionsDocument();
    doc.getDocument().setFile(null);
    _definitionsDocs.addElement(doc);
    _notifier.newFileCreated(doc);
    return doc;
  }

  /**
   * Creates a new junit test case.
   * @param name the name of the new test case
   * @param makeSetUp true iff an empty setUp() method should be included
   * @param makeTearDown true iff an empty tearDown() method should be included
   * @return the new open test case
   */
  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown) {
    StringBuffer buf = new StringBuffer();
    buf.append("import junit.framework.TestCase;\n\n");
    buf.append("/**\n");
    buf.append("* A JUnit test case class.\n");
    buf.append("* Every method starting with the word \"test\" will be called when running\n");
    buf.append("* the test with JUnit.\n");
    buf.append("*/\n");
    buf.append("public class ");
    buf.append(name);
    buf.append(" extends TestCase {\n\n");
    if (makeSetUp) {
      buf.append("/**\n");
      buf.append("* This method is called before each test method, to perform any common\n");
      buf.append("* setup if necessary.\n");
      buf.append("*/\n");
      buf.append("public void setUp() {\n}\n\n");
    }
    if (makeTearDown) {
      buf.append("/**\n");
      buf.append("* This method is called after each test method, to perform any common\n");
      buf.append("* clean-up if necessary.\n");
      buf.append("*/\n");
      buf.append("public void tearDown() {\n}\n\n");
    }
    buf.append("/**\n");
    buf.append("* A test method.\n");
    buf.append("* (Replace \"X\" with a name describing the test.  You may write as\n");
    buf.append("* many \"testSomething\" methods in this class as you wish, and each\n");
    buf.append("* one will be called when running JUnit over this class.)\n");
    buf.append("*/\n");
    buf.append("public void testX() {\n}\n\n");
    buf.append("}\n");
    String test = buf.toString();

    OpenDefinitionsDocument openDoc = newFile();
    DefinitionsDocument doc = openDoc.getDocument();
    try {
      doc.insertString(0, test, null);
      doc.indentLines(0, test.length());
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    return openDoc;
  }

  //---------------------- Specified by ILoadDocuments ----------------------//

  /**
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return a canonical path, as this method makes
   * it canonical.  This is necessary to ensure proper package detection.
   * (Also see bug 774896 and 707734)
   * @see ILoadDocuments
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {
    // This code is duplicated in MainFrame._setCurrentDirectory(File) for safety.
    final File file = (com.getFiles())[0].getCanonicalFile();
    OpenDefinitionsDocument odd = _openFile(file);

    // Make sure this is on the classpath
    try {
      File classpath = odd.getSourceRoot();
      _interactionsModel.addToClassPath(classpath.getAbsolutePath());
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }

    return odd;
  }

  /**
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return an absolute path, as this method makes
   * it absolute.
   * @see ILoadDocuments
   */
  public OpenDefinitionsDocument openFiles(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {
    final File[] files = com.getFiles();
    OpenDefinitionsDocument retDoc = null;

    if (files == null) {
      throw new IOException("No Files returned from FileSelector");
    }

    AlreadyOpenException storedAOE = null;

    for (int i=0; i < files.length; i++) {
      if (files[i] == null) {
        throw new IOException("File name returned from FileSelector is null");
      }

      try {
        //always return last opened Doc
        retDoc = _openFile(files[i].getAbsoluteFile());
      }
      catch (AlreadyOpenException aoe) {
        retDoc = aoe.getOpenDocument();
        //Remember the first AOE
        if (storedAOE == null) {
          storedAOE = aoe;
        }
      }
    }

    if (storedAOE != null) {
      throw storedAOE;
    }

    if (retDoc != null) {
      return retDoc;
    }
    else {
      //if no OperationCanceledException, then getFiles should
      //have at least one file.
      throw new IOException("No Files returned from FileChooser");
    }
  }

  //----------------------- End ILoadDocuments Methods -----------------------//

  /**
   * Saves all open files, prompting for names if necessary.
   * When prompting (ie, untitled document), set that document as active.
   * @param com a selector that picks the file name, used for each
   * @exception IOException
   */
  public void saveAllFiles(FileSaveSelector com) throws IOException {
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.getElementAt(i);
      aboutToSaveFromSaveAll(doc);
      doc.saveFile(com);
    }
  }

  /**
   * Does nothing in default model.
   * @param doc the document which is about to be saved by a save all
   *            command
   */
  public void aboutToSaveFromSaveAll(OpenDefinitionsDocument doc) {}

  /**
   * Closes an open definitions document, prompting to save if
   * the document has been changed.  Returns whether the file
   * was successfully closed.
   * @return true if the document was closed
   */
  public boolean closeFile(OpenDefinitionsDocument doc) {
    boolean canClose = doc.canAbandonFile();
    final OpenDefinitionsDocument closedDoc = doc;
    if (canClose) {
      doc.removeFromDebugger();
      // Only fire event if doc exists and was removed from list
      if (_definitionsDocs.removeElement(doc)) {
        _notifier.fileClosed(closedDoc);
        return true;
      }
    }
    return false;
  }

  /**
   * Attempts to close all open documents.
   * @return true if all documents were closed
   */
  public boolean closeAllFiles() {
    boolean keepClosing = true;
    while (!_definitionsDocs.isEmpty() && keepClosing) {
      OpenDefinitionsDocument openDoc = (OpenDefinitionsDocument)
        _definitionsDocs.get(0);
      keepClosing = closeFile(openDoc);
    }
    return keepClosing;
  }

  /**
   * Reverts all open files.
   * Not working yet: causes an exception in the reduced model if a
   * non-active document is reverted...?
   *
  public void revertAllFiles() throws IOException {
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.getElementAt(i);
      if (!doc.isUntitled()) {
        doc.revertFile();
      }
    }
  }*/

  /**
   * Exits the program.
   * Only quits if all documents are successfully closed.
   */
  public void quit() {
    if (closeAllFiles()) {
      dispose();  // kills the interpreter

      if (DrJava.getSecurityManager() != null) {
        DrJava.getSecurityManager().exitVM(0);
      }
      else {
        // If we are being debugged by another copy of DrJava,
        //  then we have no security manager.  Just exit cleanly.
        System.exit(0);
      }

    }
  }

  /**
   * Prepares this model to be thrown away.  Never called in
   * practice outside of quit(); only used in tests.
   */
  public void dispose() {
    // Kill the interpreter
    _interpreterControl.killInterpreter(false);

    _notifier.removeAllListeners();
    _definitionsDocs.clear();
  }

  //----------------------- Specified by IGetDocuments -----------------------//

  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException
  {
    // Check if this file is already open
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null) {
      // If not, open and return it
      final File f = file;

      // TODO: Is this class construction overhead really necessary?
      FileOpenSelector selector = new FileOpenSelector() {
        public File getFile() throws OperationCanceledException {
          return f;
        }

        public File[] getFiles() throws OperationCanceledException {
          return new File[] {f};
        }
      };
      try {
        doc = openFile(selector);
      }
      catch (AlreadyOpenException aoe) {
        doc = aoe.getOpenDocument();
      }
      catch (OperationCanceledException oce) {
        // Cannot happen, since we don't throw it in our selector
        throw new UnexpectedException(oce);
      }
    }
    return doc;
  }

  /**
   * Iterates over OpenDefinitionsDocuments, looking for this file.
   * TODO: This is not very efficient!
   */
  public boolean isAlreadyOpen(File file) {
    return (_getOpenDocument(file) != null);
  }

  /**
   * Simply returns a reference to our internal ListModel.
   * TODO: Protect this object from untrusted code!
   * @deprecated Use getDefinitionsDocuments().
   */
  public ListModel getDefinitionsDocs() {
    return _definitionsDocs;
  }

  /**
   * Returns the OpenDefinitionsDocument corresponding to the document
   * passed in.
   * @param doc the searched for Document
   * @return its corresponding OpenDefinitionsDocument
   */
  public OpenDefinitionsDocument getODDForDocument(Document doc) {
    int index = _getIndexOfDocument(doc);
    if (index == -1) {
      throw new UnexpectedException(new IllegalStateException("Could not get the OpenDefinitionsDocument for Document: " + doc));
    }
    else {
      return (OpenDefinitionsDocument) _definitionsDocs.elementAt(index);
    }
  }

  /**
   * Gets a DocumentIterator to allow navigating through open Swing Documents.
   */
  public DocumentIterator getDocumentIterator() {
    return this;
  }

  /**
   * Given a Document, returns the Document corresponding to the next
   * OpenDefinitionsDocument in the document list.
   * @param doc the current Document
   * @return the next Document
   */
  public Document getNextDocument(Document doc) {
    int index = _getIndexOfDocument(doc);
    if (index == -1) {
      throw new UnexpectedException(new IllegalStateException("Could not get the next Document for Document: " + doc));
    }
    else if (index == _definitionsDocs.size() - 1) {
      return ((OpenDefinitionsDocument)_definitionsDocs.elementAt(0)).getDocument();
    }
    else {
      return ((OpenDefinitionsDocument)_definitionsDocs.elementAt(index+1)).getDocument();
    }
  }

  /**
   * Given a Document, returns the Document corresponding to the previous
   * OpenDefinitionsDocument in the document list.
   * @param doc the current Document
   * @return the previous Document
   */
  public Document getPrevDocument(Document doc) {
    int index = _getIndexOfDocument(doc);
    if (index == -1) {
      throw new UnexpectedException(new IllegalStateException("Could not get the previous Document for Document: " + doc));
    }
    else if (index == 0) {
      return ((OpenDefinitionsDocument)_definitionsDocs.elementAt(_definitionsDocs.size()-1)).getDocument();
    }
    else {
      return ((OpenDefinitionsDocument)_definitionsDocs.elementAt(index-1)).getDocument();
    }
  }

  private int _getIndexOfDocument(Document doc) {
    int index = 0;
    Enumeration en = _definitionsDocs.elements();
    while (en.hasMoreElements()) {
      if (doc == ((OpenDefinitionsDocument)en.nextElement()).getDocument()) {
        return index;
      }
      else {
        index++;
      }
    }
    return -1;
  }

  /**
   * Returns a collection of all documents currently open for editing.
   * This is equivalent to the results of getDocumentForFile for the set
   * of all files for which isAlreadyOpen returns true.
   * @return a random-access List of the open definitions documents.
   */
  public List<OpenDefinitionsDocument> getDefinitionsDocuments() {
    ArrayList<OpenDefinitionsDocument> docs =
      new ArrayList<OpenDefinitionsDocument>(_definitionsDocs.size());
    Enumeration en = _definitionsDocs.elements();

    while (en.hasMoreElements()) {
      docs.add((OpenDefinitionsDocument) en.nextElement());
    }

    return docs;
  }

  //----------------------- End IGetDocuments Methods -----------------------//

  /**
   * Set the indent tab size for all definitions documents.
   * @param indent the number of spaces to make per level of indent
   */
  void setDefinitionsIndent(int indent) {
    for (int i = 0; i < _definitionsDocs.size(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.get(i);
      doc.setDefinitionsIndent(indent);
    }
  }

  /**
   * Clears and resets the interactions pane. Also clears the console
   * if the option is indicated (on by default).
   * Bug #576179 pointed out that this needs to end any threads that were
   * running in the interactions JVM, so we completely restart the JVM now.
   * Ideally, we'd like a way to end any running threads and cleanly reset
   * the interpreter (to speed up this method), but that might be too complex...
   * <p>
   * (Old approach:
   * First it makes sure it's in the right package given the
   * package specified by the definitions.  If it can't,
   * the package for the interactions becomes the defualt
   * top level. In either case, this method calls a helper
   * which fires the interactionsReset() event.)
   */
  public void resetInteractions() {
    if ((_debugger.isAvailable()) && (_debugger.isReady())) {
      _debugger.shutdown();
    }

    _interactionsModel.resetInterpreter();
    if (DrJava.getConfig().getSetting(OptionConstants.RESET_CLEAR_CONSOLE).booleanValue()) {
      resetConsole();
    }
    //_restoreInteractionsState();

    /* Old approach.  (Didn't kill leftover interactions threads)
    _interpreterControl.reset();
    _restoreInteractionsState();
    */
  }


  /**
   * Resets the console.
   * Fires consoleReset() event.
   */
  public void resetConsole() {
    _consoleDoc.reset();

    _notifier.consoleReset();
  }

  /**
   * Interprets the current given text at the prompt in the interactions
   * pane.
   */
  public void interpretCurrentInteraction() {
    _interactionsModel.interpretCurrentInteraction();
  }

  /**
   * Interprets the file selected in the FileOpenSelector. Assumes all strings
   * have no trailing whitespace. Interprets the array all at once so if there are
   * any errors, none of the statements after the first erroneous one are processed.
   */
  public void loadHistory(FileOpenSelector selector) throws IOException {
    _interactionsModel.loadHistory(selector);
  }

  /**
   * Loads the history/histories from the given selector.
   */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException
  {
    return _interactionsModel.loadHistoryAsScript(selector);
  }

  /**
   * Clears the interactions history
   */
  public void clearHistory() {
    _interactionsModel.getDocument().clearHistory();
  }

  /**
   * Saves the unedited version of the current history to a file
   * @param selector File to save to
   */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector);
  }

  /**
   * Saves the edited version of the current history to a file
   * @param selector File to save to
   * @param editedVersion Edited verison of the history which will be
   * saved to file instead of the lines saved in the history. The saved
   * file will still include any tags needed to recognize it as a saved
   * interactions file.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion)
    throws IOException
  {
    _interactionsModel.getDocument().saveHistory(selector, editedVersion);
  }

  /**
   * Returns the entire history as a String with semicolons as needed
   */
  public String getHistoryAsStringWithSemicolons() {
    return _interactionsModel.getDocument().getHistoryAsStringWithSemicolons();
  }

  /**
   * Returns the entire history as a String
   */
  public String getHistoryAsString() {
    return _interactionsModel.getDocument().getHistoryAsString();
  }

  /**
   * Registers OptionListeners.  Factored out code from the two constructors
   */
  private void _registerOptionListeners(){
    // Listen to any relevant config options
    DrJava.getConfig().addOptionListener(EXTRA_CLASSPATH,
                                         new ExtraClasspathOptionListener());
    
    DrJava.getConfig().addOptionListener(BACKUP_FILES,
                                         new BackUpFileOptionListener());
    Boolean makeBackups = DrJava.getConfig().getSetting(BACKUP_FILES);
    FileOps.DefaultFileSaver.setBackupsEnabled(makeBackups.booleanValue());
    
    DrJava.getConfig().addOptionListener(ALLOW_PRIVATE_ACCESS,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        getInteractionsModel().setPrivateAccessible(oce.value.booleanValue());
      }
    });
  }

  /**
   * Appends a string to the given document using a particular attribute set.
   * Also waits for a small amount of time (WRITE_DELAY) to prevent any one
   * writer from flooding the model with print calls to the point that the
   * user interface could become unresponsive.
   * @param doc Document to append to
   * @param s String to append to the end of the document
   * @param style the style to print with
   */
  private void _docAppend(ConsoleDocument doc, String s, String style) {
    synchronized(_systemWriterLock) {
      try {
        doc.insertBeforeLastPrompt(s, style);

        // Wait to prevent being flooded with println's
        _systemWriterLock.wait(WRITE_DELAY);
      }
      catch (InterruptedException e) {
        // It's ok, we'll go ahead and resume
      }
    }
  }


  /**
   * Prints System.out to the DrJava console.
   */
  public void systemOutPrint(String s) {
    _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_OUT_STYLE);
  }

  /**
   * Prints System.err to the DrJava console.
   */
  public void systemErrPrint(String s) {
    _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_ERR_STYLE);
  }

  /** Called when the repl prints to System.out.
  public void replSystemOutPrint(String s) {
    systemOutPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_OUT_STYLE);
  } */

  /** Called when the repl prints to System.err.
  public void replSystemErrPrint(String s) {
    systemErrPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_ERR_STYLE);
  } */

  /** Called when the debugger wants to print a message.  Inserts a newline. */
  public void printDebugMessage(String s) {
    _interactionsModel.getDocument().
      insertBeforeLastPrompt(s + "\n", InteractionsDocument.DEBUGGER_STYLE);
  }


  /**
   * Blocks until the interpreter has registered.
   */
  public void waitForInterpreter() {
    _interpreterControl.ensureInterpreterConnected();
  }

  /**
   * Returns the current classpath in use by the Interpreter JVM.
   */
  public String getClasspathString() {
    return _interpreterControl.getClasspathString();
  }

  /**
   * Returns the current classpath in use by the Interpreter JVM.
   */
  public Vector<String> getClasspath() {
    return _interpreterControl.getClasspath();
  }

  /**
   * Gets an array of all sourceRoots for the open definitions
   * documents, without duplicates. Note that if any of the open
   * documents has an invalid package statement, it won't be added
   * to the source root set. On 8.7.02 changed the sourceRootSet such that
   * the directory DrJava was executed from is now after the sourceRoots
   * of the currently open documents in order that whatever version the user
   * is looking at corresponds to the class file the interactions window
   * uses.
   * TODO: Fix out of date comment, possibly remove this here?
   */
  public File[] getSourceRootSet() {
    LinkedList<File> roots = new LinkedList<File>();

    for (int i = 0; i < _definitionsDocs.size(); i++) {
      OpenDefinitionsDocument doc
        = (OpenDefinitionsDocument) _definitionsDocs.get(i);

      try {
        File root = doc.getSourceRoot();

        // Don't add duplicate Files, based on path
        if (!roots.contains(root)) {
          roots.add(root);
        }
      }
      catch (InvalidPackageException e) {
        // oh well, invalid package statement for this one
        // can't add it to roots
      }
    }

//    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
//
//    if (workDir == FileOption.NULL_FILE) {
//      workDir = new File( System.getProperty("user.dir"));
//    }
//    if (workDir.isFile() && workDir.getParent() != null) {
//      workDir = workDir.getParentFile();
//    }
//    roots.add(workDir);

    return roots.toArray(new File[0]);
  }

  /**
   * Return the name of the file, or "(untitled)" if no file exists.
   * Does not include the ".java" if it is present.
   * TODO: move to a static utility class?
   */
  public String getDisplayFilename(OpenDefinitionsDocument doc) {

    String filename = doc.getFilename();

    // Remove ".java" if at the end of name
    if (filename.endsWith(".java")) {
      int extIndex = filename.lastIndexOf(".java");
      if (extIndex > 0) {
        filename = filename.substring(0, extIndex);
      }
    }

    // Mark if modified
    if (doc.isModifiedSinceSave()) {
      filename = filename + " *";
    }

    return filename;
  }

  /**
   * Return the absolute path of the file, or "(untitled)" if no file exists.
   * TODO: move to a static utility class?
   */
  public String getDisplayFullPath(OpenDefinitionsDocument doc) {

    String path = "(untitled)";
    try {
      File file = doc.getFile();
      path = file.getAbsolutePath();
    }
    catch (IllegalStateException ise) {
      // No file, filename stays "Untitled"
    }
    catch (FileMovedException fme) {
      // Recover, even though file was deleted
      File file = fme.getFile();
      path = file.getAbsolutePath();
    }

    // Mark if modified
    if (doc.isModifiedSinceSave()) {
      path = path + " *";
    }

    return path;
  }

  /**
   * Return the absolute path of the file with the given index,
   * or "(untitled)" if no file exists.
   */
  public String getDisplayFullPath(int index) {
    OpenDefinitionsDocument doc =
      getDefinitionsDocuments().get(index);
    if (doc == null) {
      throw new RuntimeException(
        "Document not found with index " + index);
    }
    return getDisplayFullPath(doc);
  }

  /**
   * Sets whether or not the Interactions JVM will be reset after
   * a compilation succeeds.  This should ONLY be used in tests!
   * @param shouldReset Whether to reset after compiling
   */
  void setResetAfterCompile(boolean shouldReset) {
    _resetAfterCompile = shouldReset;
  }

  /**
   * Gets the Debugger used by DrJava.
   */
  public Debugger getDebugger() {
    return _debugger;
  }

  /**
   * Returns an available port number to use for debugging the interactions JVM.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException {
    return _interactionsModel.getDebugPort();
  }

  /**
   * Checks if any open definitions documents have been modified
   * since last being saved.
   * @return whether any documents have been modified
   */
  public boolean hasModifiedDocuments() {
    boolean modified = false;
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc =
        (OpenDefinitionsDocument)_definitionsDocs.getElementAt(i);
      if (doc.isModifiedSinceSave()) {
        modified = true;
        break;
      }
    }
    return modified;
  }

  /**
   * Searches for a file with the given name on the current source roots and the
   * augmented classpath.
   * @param filename Name of the source file to look for
   * @return the file corresponding to the given name, or null if it cannot be found
   */
  public File getSourceFile(String filename) {
    File[] sourceRoots = getSourceRootSet();
    for (int i = 0; i < sourceRoots.length; i++) {
      File f = _getSourceFileFromPath(filename, sourceRoots[i]);
      if (f != null) {
        return f;
      }
    }
    Vector<File> sourcepath = DrJava.getConfig().getSetting(OptionConstants.DEBUG_SOURCEPATH);
    return getSourceFileFromPaths(filename, sourcepath);
  }

  /**
   * Searches for a file with the given name on the provided paths.
   * Returns null if the file is not found.
   * @param filename Name of the source file to look for
   * @param paths An array of directories to search
   * @return the file if it is found, or null otherwise
   */
  public File getSourceFileFromPaths(String filename, Vector<File> paths) {
    File f = null;
    for (int i = 0; i < paths.size(); i++) {
      f = _getSourceFileFromPath(filename, paths.get(i));
      if (f != null) {
        return f;
      }
    }
    return null;
  }

  /**
   * Gets the file named filename from the given path, if it exists.
   * Returns null if it's not there.
   * @param filename the file to look for
   * @param path the path to look for it in
   * @return the file if it exists
   */
  private File _getSourceFileFromPath(String filename, File path) {
    String root = path.getAbsolutePath();
    File f = new File(root + System.getProperty("file.separator") + filename);
    return f.exists() ? f : null;
  }

  /**
   * Returns the document currently being tested (with JUnit) if there is
   * one, otherwise null.
   *
  public OpenDefinitionsDocument getDocBeingTested() {
    return _docBeingTested;
  }*/

  // ---------- DefinitionsDocumentHandler inner class ----------

  /**
   * Inner class to handle operations on each of the open
   * DefinitionsDocuments by the GlobalModel.
   */
  private class DefinitionsDocumentHandler implements OpenDefinitionsDocument {
    private final DefinitionsDocument _doc;
    // TODO: Should these be document-specific?  They aren't used as such now.
//    private CompilerErrorModel _errorModel;
//    private JUnitErrorModel _junitErrorModel;
    private DrJavaBook _book;
    private Vector<Breakpoint> _breakpoints;

//    boolean _shouldRun;
//    private GlobalModelListener _notifyListener = new DummySingleDisplayModelListener() {
//      public synchronized void interpreterReady() {
//        notify();
//      }
//      public synchronized void interperterResetting() {
//        notify();
//        _shouldRun = false;
//      }
//      public synchronized void interactionEnded() {
//        notify();
//      }
//    };

    /**
     * Constructor.  Initializes this handler's document.
     * @param doc DefinitionsDocument to manage
     */
    DefinitionsDocumentHandler(DefinitionsDocument doc) {
      _doc = doc;
//      _errorModel = new CompilerErrorModel<CompilerError> (new CompilerError[0], null);
//      _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);
      _breakpoints = new Vector<Breakpoint>();
    }

    /**
     * Gets the definitions document being handled.
     * @return document being handled
     */
    public DefinitionsDocument getDocument() {
      return _doc;
    }

    /**
     * Returns the name of the top level class, if any.
     * @throws ClassNameNotFoundException if no top level class name found.
     */
    public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
      return _doc.getFirstTopLevelClassName();
    }

    /**
     * Returns whether this document is currently untitled
     * (indicating whether it has a file yet or not).
     * @return true if the document is untitled and has no file
     */
    public boolean isUntitled() {
      return _doc.isUntitled();
    }

    /**
     * Returns the file for this document.  If the document
     * is untitled and has no file, it throws an IllegalStateException.
     * @return the file for this document
     * @exception IllegalStateException if no file exists
     */
    public File getFile() throws IllegalStateException, FileMovedException {
      return _doc.getFile();
    }

    /**
     * Returns the name of this file, or "(untitled)" if no file.
     */
    public String getFilename() {
      return _doc.getFilename();
    }

    // TODO: Move this to where it can be static.
    private class TrivialFSS implements FileSaveSelector {
      private File _file;
      private TrivialFSS(File file) {
        _file = file;
      }
      public File getFile() throws OperationCanceledException {
        return _file;
      }
      public void warnFileOpen() {}
      public boolean verifyOverwrite() {
        return true;
      }
      public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc,
                                              File oldFile) {
        return true;
      }
    }

    /**
     * Saves the document with a FileWriter.  If the file name is already
     * set, the method will use that name instead of whatever selector
     * is passed in.
     * @param com a selector that picks the file name if the doc is untitled
     * @exception IOException
     * @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFile(FileSaveSelector com) throws IOException {
      FileSaveSelector realCommand;
      final File file;

      if (!isModifiedSinceSave() && !isUntitled()) {
        // Don't need to save.
        //  Return true, since the save wasn't "canceled"
        return true;
      }

      try {
        if (_doc.isUntitled()) {
          realCommand = com;
        }
        else {
          try {
            file = _doc.getFile();
            realCommand = new TrivialFSS(file);
          }
          catch (FileMovedException fme) {
            // getFile() failed, prompt the user if a new one should be selected
            if (com.shouldSaveAfterFileMoved(this, fme.getFile())) {
              realCommand = com;
            }
            else {
              // User declines to save as a new file, so don't save
              return false;
            }
          }
        }

        return saveFileAs(realCommand);
      }
      catch (IllegalStateException ise) {
        // No file--  this should have been caught by isUntitled()
        throw new UnexpectedException(ise);
      }
    }

    /**
     * Saves the document with a FileWriter.  The FileSaveSelector will
     * either provide a file name or prompt the user for one.  It is
     * up to the caller to decide what needs to be done to choose
     * a file to save to.  Once the file has been saved succssfully,
     * this method fires fileSave(File).  If the save fails for any
     * reason, the event is not fired.
     * @param com a selector that picks the file name.
     * @throws IOException if the save fails due to an IO error
     * @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFileAs(FileSaveSelector com) throws IOException {
      try {
        final OpenDefinitionsDocument openDoc = this;
        final File file = com.getFile();
        final OpenDefinitionsDocument otherDoc = _getOpenDocument(file);

        // Check if file is already open in another document
        if ( otherDoc != null && openDoc != otherDoc ) {
          // Can't save over an open document
          com.warnFileOpen();
        }

        // If the file exists, make sure it's ok to overwrite it
        else if (!file.exists() || com.verifyOverwrite()) {

          // Correct the case of the filename (in Windows)
          if (! file.getCanonicalFile().getName().equals(file.getName())) {
            file.renameTo(file);
          }
          
          // Check for # in the path of the file because if there
          // is one, then the file cannot be used in the Interactions Pane
          if (file.getAbsolutePath().indexOf("#") != -1) {
            _notifier.filePathContainsPound();
          }

          // have FileOps save the file
          FileOps.saveFile(new FileOps.DefaultFileSaver(file){
            public void saveTo(OutputStream os) throws IOException {
              try {
                _editorKit.write(os, _doc, 0, _doc.getLength());
              } catch (BadLocationException docFailed){
                // We don't expect this to happen
                throw new UnexpectedException(docFailed);
              }
            }
          });

          _doc.resetModification();
          _doc.setFile(file);
          _doc.setCachedClassFile(null);
          checkIfClassFileInSync();
          _notifier.fileSaved(openDoc);

          // Make sure this file is on the classpath
          try {
            File classpath = getSourceRoot();
            _interactionsModel.addToClassPath(classpath.getAbsolutePath());
          }
          catch (InvalidPackageException e) {
            // Invalid package-- don't add to classpath
          }
        }

        return true;

      }
      catch (OperationCanceledException oce) {
        // Thrown by com.getFile() if the user cancels.
        //   We don't save if this happens.
        return false;
      }
    }

    /**
     * This method tells the document to prepare all the DrJavaBook
     * and PagePrinter objects.
     */
    public void preparePrintJob() throws BadLocationException,
      FileMovedException {

      String filename = "(untitled)";
      try {
        filename = _doc.getFile().getAbsolutePath();
      }
      catch (IllegalStateException e) {
      }

      _book = new DrJavaBook(_doc.getText(0, _doc.getLength()), filename, _pageFormat);
    }

    /**
     * Prints the given document by bringing up a
     * "Print" window.
     */
    public void print() throws PrinterException, BadLocationException,
      FileMovedException
    {
      preparePrintJob();
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPageable(_book);
      if (printJob.printDialog()) {
        printJob.print();
      }
      cleanUpPrintJob();
    }

    /**
     * Returns the Pageable object for printing.
     * @return A Pageable representing this document.
     */
    public Pageable getPageable() throws IllegalStateException {
      return _book;
    }

    public void cleanUpPrintJob() {
      _book = null;
    }

    public void startCompile() throws IOException {
      _compilerModel.compile(DefinitionsDocumentHandler.this);
    }

    /**
     * Runs the main method in this document in the interactions pane.
     * Demands that the definitions be saved and compiled before proceeding.
     * Fires an event to signal when execution is about to begin.
     * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
     * @exception IOException propagated from GlobalModel.compileAll()
     */
    public void runMain() throws ClassNameNotFoundException, IOException {
      try {
        // First, get the class name to use.  This relies on Java's convention of
        // one top-level class per file.
        String className = _doc.getQualifiedClassName();
        /*  Do not compile in any case.
        // Prompt to save and compile if any document is modified.
        if (hasModifiedDocuments()) {
          _notifier.saveBeforeRun();

          // If the user chose to cancel, abort the run.
          if (hasModifiedDocuments()) {
            return;
          }
        }
        // If no document is modified, still compile the current doc.
        // compile only if class file out of sync
        if (!checkIfClassFileInSync()) {
          startCompile();
        }

        // Make sure that the compiler is done before continuing.
        synchronized(_compilerModel) {
          // If the compile had errors, abort the run.
          if (!_compilerErrorModel.hasOnlyWarnings()) {
            return;
          }
        }
        */
        // Then clear the current interaction and replace it with a "java X" line.
        InteractionsDocument iDoc = _interactionsModel.getDocument();
//        if (iDoc.inProgress()) {
//          addListener(_notifyListener);
//          _shouldRun = true;
//          synchronized(_notifyListener) {
//            try {
//              _notifyListener.wait();
//            }
//            catch(InterruptedException ie) {
//            }
//          }
//          removeListener(_notifyListener);
//          if (!_shouldRun) {
//            // The interactions pane was reset during another interaction.
//            //  Don't run the main method.
//            return;
//          }
//        }
        synchronized (_interpreterControl) {
          iDoc.clearCurrentInput();
          if (!checkIfClassFileInSync()) {
            iDoc.insertBeforeLastPrompt(DOCUMENT_OUT_OF_SYNC_MSG, InteractionsDocument.ERROR_STYLE);
          }
          iDoc.insertText(iDoc.getDocLength(), "java " + className, null);

          // Notify listeners that the file is about to be run.
          _notifier.runStarted(this);

          // Finally, execute the new interaction.
          _interactionsModel.interpretCurrentInteraction();
        }
      }
      catch (DocumentAdapterException e) {
        // This was thrown by insertText - and shouldn't have happened.
        throw new UnexpectedException(e);
      }
    }

    /**
     * Runs JUnit on the current document. Used to compile all open documents
     * before testing but have removed that requirement in order to allow the
     * debugging of test cases. If the classes being tested are out of
     * sync, a message is displayed.
     */
    public void startJUnit() throws ClassNotFoundException, IOException {
      _junitModel.junit(DefinitionsDocumentHandler.this);
    }

    /**
     * Generates Javadoc for this document, saving the output to a temporary
     * directory.  The location is provided to the javadocEnded event on
     * the given listener.
     * @param saver FileSaveSelector for saving the file if it needs to be saved
     */
    public void generateJavadoc(FileSaveSelector saver) throws IOException {
      // Use the model's classpath, and use the EventNotifier as the listener
      _javadocModel.javadocDocument(this, saver, getClasspath());
    }

    /**
     * Determines if the definitions document has changed since the
     * last save.
     * @return true if the document has been modified
     */
    public boolean isModifiedSinceSave() {
      return _doc.isModifiedSinceSave();
    }

    /**
     * Determines if the definitions document has changed on disk
     * since the last time the document was read.
     * @return true if the document has been modified on disk
     */
    public boolean isModifiedOnDisk() {
      return _doc.isModifiedOnDisk();
    }

    /**
     * Checks if the document is modified. If not, searches for the class file
     * corresponding to this document and compares the timestamps of the
     * class file to that of the source file.
     */
    public boolean checkIfClassFileInSync() {
      // If modified, then definitely out of sync
      if(isModifiedSinceSave()) {
        _doc.setClassFileInSync(false);
        return false;
      }

      // Look for cached class file
      File classFile = _doc.getCachedClassFile();
      if (classFile == null) {
        // Not cached, so locate the file
        classFile = _locateClassFile();
        _doc.setCachedClassFile(classFile);

        if (classFile == null) {
          // couldn't find the class file
          _doc.setClassFileInSync(false);
          return false;
        }
      }

      // compare timestamps
      File sourceFile = null;
      try {
        sourceFile = getFile();
      }
      catch (IllegalStateException ise) {
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        _doc.setClassFileInSync(false);
        return false;
      }
      if (sourceFile.lastModified() > classFile.lastModified()) {
        _doc.setClassFileInSync(false);
        return false;
      }
      else {
        _doc.setClassFileInSync(true);
        return true;
      }
    }

    /**
     * Returns the class file for this source document, if one could be found.
     * Looks in the source root directories of the open documents, the
     * system classpath, and the "extra.classpath".  Returns null if the
     * class file could not be found.
     */
    private File _locateClassFile() {
      try {
        String className = _doc.getQualifiedClassName();
        String ps = System.getProperty("file.separator");
        // replace periods with the System's file separator
        className = StringOps.replace(className, ".", ps);
        String filename = className + ".class";

        // Check source root set (open files)
        File[] sourceRoots = getSourceRootSet();
        Vector<File> roots = new Vector<File>();
        // Add the current document to the beginning of the roots Vector
        try {
          roots.add(getSourceRoot());
        }
        catch (InvalidPackageException ipe) {
          try {
            File f = getFile().getParentFile();
            if (f != null) {
              roots.add(f);
            }
          }
          catch (IllegalStateException ise) {
            // No file, don't add to source root set
          }
          catch (FileMovedException fme) {
            // Moved, but we'll add the old file to the set anyway
            File root = fme.getFile().getParentFile();
            if (root != null) {
              roots.add(root);
            }
          }
        }

        for (int i=0; i < sourceRoots.length; i++) {
          roots.add(sourceRoots[i]);
        }
        File classFile = getSourceFileFromPaths(filename, roots);

        if (classFile == null) {
          // Class not on source root set, check system classpath
          String cp = System.getProperty("java.class.path");
          String pathSeparator = System.getProperty("path.separator");
          Vector<File> cpVector = new Vector<File>();
          for (int i = 0; i < cp.length();) {
            int nextSeparator = cp.indexOf(pathSeparator, i);
            if (nextSeparator == -1) {
              cpVector.add(new File(cp.substring(i, cp.length())));
              break;
            }
            cpVector.add(new File(cp.substring(i, nextSeparator)));
            i = nextSeparator + 1;
          }
          classFile = getSourceFileFromPaths(filename, cpVector);
        }

        if (classFile == null) {
          // not on system classpath, check interactions classpath
          classFile = getSourceFileFromPaths(filename, DrJava.getConfig().getSetting(EXTRA_CLASSPATH));
        }

        return classFile;
      }
      catch (ClassNameNotFoundException cnnfe) {
        // No class name found, so we can't find a class file
        return null;
      }
    }


    /**
     * Determines if the defintions document has been changed
     * by an outside program. If the document has changed,
     * then asks the listeners if the GlobalModel should
     * revert the document to the most recent version saved.
     * @return true if document has been reverted
     */
    public boolean revertIfModifiedOnDisk() throws IOException{
      final OpenDefinitionsDocument doc = this;
      if (isModifiedOnDisk()) {

        boolean shouldRevert = _notifier.shouldRevertFile(doc);
        if (shouldRevert) {
          doc.revertFile();
        }
        return shouldRevert;
      }
      else {
        return false;
      }
    }

    public void revertFile() throws IOException {

      //need to remove old, possibly invalid breakpoints
      removeFromDebugger();

      final OpenDefinitionsDocument doc = this;

      try {
        File file = doc.getFile();
        //this line precedes the .remove() so that a document with an invalid
        // file is not cleared before this fact is discovered.

        FileReader reader = new FileReader(file);
        DefinitionsDocument tempDoc = doc.getDocument();

        tempDoc.remove(0,tempDoc.getLength());


        _editorKit.read(reader, tempDoc, 0);
        reader.close(); // win32 needs readers closed explicitly!

        tempDoc.resetModification();
        doc.checkIfClassFileInSync();

        syncCurrentLocationWithDefinitions(0);

        _notifier.fileReverted(doc);
      }
      catch (IllegalStateException docFailed) {
        //cant revert file if doc has no file
        throw new UnexpectedException(docFailed);
      }
      catch (BadLocationException docFailed) {
        throw new UnexpectedException(docFailed);
      }
    }

    /**
     * Asks the listeners if the GlobalModel can abandon the current document.
     * Fires the canAbandonFile(File) event if isModifiedSinceSave() is true.
     * @return true if the current document may be abandoned, false if the
     * current action should be halted in its tracks (e.g., file open when
     * the document has been modified since the last save).
     */
    public boolean canAbandonFile() {
      final OpenDefinitionsDocument doc = this;
      if (isModifiedSinceSave()) {
        return _notifier.canAbandonFile(doc);
      }
      else {
        return true;
      }
    }

    /**
     * Moves the definitions document to the given line, and returns
     * the character position in the document it's gotten to.
     * @param line Number of the line to go to. If line exceeds the number
     *             of lines in the document, it is interpreted as the last line.
     * @return Index into document of where it moved
     */
    public int gotoLine(int line) {
      _doc.gotoLine(line);
      return _doc.getCurrentLocation();
    }

    /**
     * Forwarding method to sync the definitions with whatever view
     * component is representing them.
     */
    public void syncCurrentLocationWithDefinitions(int location) {
      _doc.setCurrentLocation(location);
    }

    /**
     * Get the location of the cursor in the definitions according
     * to the definitions document.
     */
    public int getCurrentDefinitionsLocation() {
      return _doc.getCurrentLocation();
    }

    /**
     * Forwarding method to find the match for the closing brace
     * immediately to the left, assuming there is such a brace.
     * @return the relative distance backwards to the offset before
     *         the matching brace.
     */
    public int balanceBackward() {
      return _doc.balanceBackward();
    }

    /**
     * Forwarding method to find the match for the open brace
     * immediately to the right, assuming there is such a brace.
     * @return the relative distance forwards to the offset after
     *         the matching brace.
     */
    public int balanceForward() {
      return _doc.balanceForward();
    }

    /**
     * Set the indent tab size for this document.
     * @param indent the number of spaces to make per level of indent
     */
    public void setDefinitionsIndent(int indent) {
      _doc.setIndent(indent);
    }

    /**
     * A forwarding method to indent the current line or selection
     * in the definitions.
     */
    public void indentLinesInDefinitions(int selStart, int selEnd,
                                         int reason, ProgressMonitor pm)
        throws OperationCanceledException {
      _doc.indentLines(selStart, selEnd, reason, pm);
    }

    /**
     * A forwarding method to comment out the current line or selection
     * in the definitions.
     */
    public void commentLinesInDefinitions(int selStart, int selEnd) {
      _doc.commentLines(selStart, selEnd);
    }

    /**
     * A forwarding method to un-comment the current line or selection
     * in the definitions.
     */
    public void uncommentLinesInDefinitions(int selStart, int selEnd) {
      _doc.uncommentLines(selStart, selEnd);
    }

    /**
     * Create a find and replace mechanism starting at the current
     * character offset in the definitions.
     * NOT USED.
     */
//    public FindReplaceMachine createFindReplaceMachine() {
      //try {
      //return new FindReplaceMachine(_doc, _doc.getCurrentLocation());
//      return new FindReplaceMachine();
      //}
      //catch (BadLocationException e) {
      //throw new UnexpectedException(e);
      //}
//    }

    /**
     * Returns the first Breakpoint in this OpenDefinitionsDocument whose region
     * includes the given offset, or null if one does not exist.
     * @param offset an offset at which to search for a breakpoint
     * @return the Breakpoint at the given lineNumber, or null if it does not exist.
     */
    public Breakpoint getBreakpointAt( int offset) {
      //return _breakpoints.get(new Integer(lineNumber));

      for (int i =0; i<_breakpoints.size(); i++) {
        Breakpoint bp = _breakpoints.get(i);
        if (offset >= bp.getStartOffset() && offset <= bp.getEndOffset()) {
          return bp;
        }
      }
      return null;
    }

    /**
     * Inserts the given Breakpoint into the list, sorted by region
     * @param breakpoint the Breakpoint to be inserted
     */
    public void addBreakpoint( Breakpoint breakpoint) {
      //_breakpoints.put( new Integer(breakpoint.getLineNumber()), breakpoint);

      for (int i=0; i<_breakpoints.size();i++) {
        Breakpoint bp = _breakpoints.get(i);
        int oldStart = bp.getStartOffset();
        int newStart = breakpoint.getStartOffset();

        if ( newStart < oldStart) {
          // Starts before, add here
          _breakpoints.add(i, breakpoint);
          return;
        }
        if ( newStart == oldStart) {
          // Starts at the same place
          int oldEnd = bp.getEndOffset();
          int newEnd = breakpoint.getEndOffset();

          if ( newEnd < oldEnd) {
            // Ends before, add here
            _breakpoints.add(i, breakpoint);
            return;
          }
        }
      }
      _breakpoints.add(breakpoint);
    }

    /**
     * Remove the given Breakpoint from our list (but not the debug manager)
     * @param breakpoint the Breakpoint to be removed.
     */
    public void removeBreakpoint(Breakpoint breakpoint) {
      _breakpoints.remove(breakpoint);
    }

    /**
     * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects
     * in this document.
     */
    public Vector<Breakpoint> getBreakpoints() {
      return _breakpoints;
    }

    /**
     * Tells the document to remove all breakpoints (without removing them
     * from the debug manager).
     */
    public void clearBreakpoints() {
      _breakpoints.clear();
    }

    /**
     * Called to indicate the document is being closed, so to remove
     * all related state from the debug manager.
     */
    public void removeFromDebugger() {
      if (_debugger.isAvailable() && (_debugger.isReady())) {
        try {
          while (_breakpoints.size() > 0) {
            _debugger.removeBreakpoint(_breakpoints.get(0));
          }
        }
        catch (DebugException de) {
          // Shouldn't happen if debugger is active
          throw new UnexpectedException(de);
        }
      }
      else {
        clearBreakpoints();
      }
    }

    /**
     * Finds the root directory of the source files.
     * @return The root directory of the source files,
     *         based on the package statement.
     * @throws InvalidPackageException If the package statement is invalid,
     *                                 or if it does not match up with the
     *                                 location of the source file.
     */
    public File getSourceRoot() throws InvalidPackageException
    {
      return _getSourceRoot(_doc.getPackageName());
    }

    /**
     * Gets the name of the package this source file claims it's in (with the
     * package keyword). It does this by minimally parsing the source file
     * to find the package statement.
     *
     * @return The name of package this source file declares itself to be in,
     *         or the empty string if there is no package statement (and thus
     *         the source file is in the empty package).
     *
     * @exception InvalidPackageException if there is some sort of a
     *                                    <TT>package</TT> statement but it
     *                                    is invalid.
     */
    public String getPackageName() throws InvalidPackageException {
      return _doc.getPackageName();
    }

    /**
     * Finds the root directory of the source files.
     * @param packageName Package name, already fetched from the document
     * @return The root directory of the source files,
     *         based on the package statement.
     * @throws InvalidPackageException If the package statement is invalid,
     *                                 or if it does not match up with the
     *                                 location of the source file.
     */
    private File _getSourceRoot(String packageName)
      throws InvalidPackageException
    {
      File sourceFile;
      try {
        sourceFile = _doc.getFile();
      }
      catch (IllegalStateException ise) {
        throw new InvalidPackageException(-1, "Can not get source root for " +
                                          "unsaved file. Please save.");
      }
      catch (FileMovedException fme) {
        throw new InvalidPackageException(-1, "File has been moved or deleted " +
                                          "from its previous location. Please save.");
      }

      if (packageName.equals("")) {
        return sourceFile.getParentFile();
      }

      Stack<String> packageStack = new Stack<String>();
      int dotIndex = packageName.indexOf('.');
      int curPartBegins = 0;

      while (dotIndex != -1)
      {
        packageStack.push(packageName.substring(curPartBegins, dotIndex));
        curPartBegins = dotIndex + 1;
        dotIndex = packageName.indexOf('.', dotIndex + 1);
      }

      // Now add the last package component
      packageStack.push(packageName.substring(curPartBegins));

      // Must use the canonical path, in case there are dots in the path
      //  (which will conflict with the package name)
      try {
        File parentDir = sourceFile.getCanonicalFile();
        while (!packageStack.empty()) {
          String part = packageStack.pop();
          parentDir = parentDir.getParentFile();

          if (parentDir == null) {
            throw new RuntimeException("parent dir is null?!");
          }

          // Make sure the package piece matches the directory name
          if (! part.equals(parentDir.getName())) {
            String msg = "The source file " + sourceFile.getAbsolutePath() +
              " is in the wrong directory or in the wrong package. " +
              "The directory name " + parentDir.getName() +
              " does not match the package component " + part + ".";

            throw new InvalidPackageException(-1, msg);
          }
        }

        // OK, now parentDir points to the directory of the first component of the
        // package name. The parent of that is the root.
        parentDir = parentDir.getParentFile();
        if (parentDir == null) {
          throw new RuntimeException("parent dir of first component is null?!");
        }

        return parentDir;
      }
      catch (IOException ioe) {
        String msg = "Could not locate directory of the source file: " + ioe;
        throw new InvalidPackageException(-1, msg);
      }
    }
  }

  /**
   * Creates a DefinitionsDocumentHandler for a new DefinitionsDocument,
   * using the DefinitionsEditorKit.
   * @return OpenDefinitionsDocument object for a new document
   */
  private OpenDefinitionsDocument _createOpenDefinitionsDocument() {
    DefinitionsDocument doc = (DefinitionsDocument)
      _editorKit.createNewDocument();
    return new DefinitionsDocumentHandler(doc);
  }


  /**
   * Returns the OpenDefinitionsDocument corresponding to the given
   * File, or null if that file is not open.
   * @param file File object to search for
   * @return Corresponding OpenDefinitionsDocument, or null
   */
  private OpenDefinitionsDocument _getOpenDocument(File file) {
    OpenDefinitionsDocument doc = null;

    for (int i=0; ((i < _definitionsDocs.size()) && (doc == null)); i++) {
      OpenDefinitionsDocument thisDoc =
        (OpenDefinitionsDocument) _definitionsDocs.get(i);
      try {
        File thisFile = null;
        try {
          thisFile = thisDoc.getFile();
        }
        catch (FileMovedException fme) {
          // Ok, file is invalid, but compare anyway
          thisFile = fme.getFile();
        }
        finally {
          // Always do the comparison
          if (thisFile != null) {
            try {
              // Compare canonical paths if possible
              if (thisFile.getCanonicalFile().equals(file.getCanonicalFile())) {
                doc = thisDoc;
              }
            }
            catch (IOException ioe) {
              // Can be thrown from getCanonicalFile.
              //  If so, compare the files themselves
              if (thisFile.equals(file)) {
                doc = thisDoc;
              }
            }
          }
        }
      }
      catch (IllegalStateException ise) {
        // No file in thisDoc
      }
    }

    return doc;
  }

  /**
   * Returns true if a document corresponding to the given
   * file is open, or false if that file is not open.
   * @param file File object to search for
   * @return boolean whether file is open
   */
  private boolean _docIsOpen(File file) {
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null)
      return false;
    else
      return true;
  }

  /**
   * Creates a document from a file.
   * @param file File to read document from
   * @return openened document
   */
  private OpenDefinitionsDocument _openFile(File file)
    throws IOException, AlreadyOpenException
  {
    try {
      OpenDefinitionsDocument openDoc = _getOpenDocument(file);
      if (openDoc != null) {
        throw new AlreadyOpenException(openDoc);
      }
      DefinitionsDocument tempDoc = (DefinitionsDocument) _editorKit.createNewDocument();

      FileReader reader = new FileReader(file);
      _editorKit.read(reader, tempDoc, 0);
      reader.close(); // win32 needs readers closed explicitly!

      tempDoc.setFile(file);
      tempDoc.resetModification();
      tempDoc.setCurrentLocation(0);

      final OpenDefinitionsDocument doc = new DefinitionsDocumentHandler(tempDoc);
      _definitionsDocs.addElement(doc);
      //doc.checkIfClassFileInSync();

      // Make sure this is on the classpath
      try {
        File classpath = doc.getSourceRoot();
        _interactionsModel.addToClassPath(classpath.getAbsolutePath());
      }
      catch (InvalidPackageException e) {
        // Invalid package-- don't add it to classpath
      }

      _notifier.fileOpened(doc);

      return doc;
    }
    catch (BadLocationException docFailed) {
      throw new UnexpectedException(docFailed);
    }
  }

  /**
   * Instantiates the integrated debugger if the "debugger.enabled"
   * config option is set to true.  Leaves it at null if not.
   */
  private void _createDebugger() {
    try {
      _debugger = new JPDADebugger(this);
      _interpreterControl.setDebugModel((JPDADebugger) _debugger);
    }
    catch( NoClassDefFoundError ncdfe ){
      // JPDA not available, so we won't use it.
      _debugger = NoDebuggerAvailable.ONLY;
    }
    catch( UnsupportedClassVersionError ucve ) {
      // Wrong version of JPDA, so we won't use it.
      _debugger = NoDebuggerAvailable.ONLY;
    }
    catch( Throwable t ) {
      // Something went wrong in initialization, don't use debugger
      _debugger = NoDebuggerAvailable.ONLY;
    }
  }


  /**
   * Adds the source roots for all open documents and the paths on the
   * "extra classpath" config option to the interpreter's classpath.
   */
  public void resetInteractionsClasspath() {
    // Ideally, we'd like to put the open docs before the config option,
    //  but this is inconsistent with how the classpath was defined
    //  as it was built up.  (The config option is inserted on startup,
    //  and docs are added as they are opened.  It shouldn't switch after
    //  a reset.)

    Vector<File> cp = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if(cp!=null) {
      Enumeration<File> en = cp.elements();
      while(en.hasMoreElements()) {
        _interactionsModel.addToClassPath(en.nextElement().getAbsolutePath());
      }
    }

    File[] sourceRoots = getSourceRootSet();
    for (int i = 0; i < sourceRoots.length; i++) {
      _interactionsModel.addToClassPath(sourceRoots[i].getAbsolutePath());
    }
  }

  /**
   * Sets the listener for any type of single-source input event.
   * The listener can only be changed with the changeInputListener method.
   * @param listener a listener that reacts to input requests
   * @throws IllegalStateException if the input listener is locked
   */
  public void setInputListener(InputListener listener) {
    if (_inputListener == NoInputListener.ONLY) {
      _inputListener = listener;
    }
    else {
      throw new IllegalStateException("Cannot change the input listener until it is released.");
    }
  }

  /**
   * Changes the input listener. Takes in the old listener to ensure that the owner
   * of the original listener is aware that it is being changed. It is therefore
   * important NOT to include a public accessor to the input listener on the model.
   * @param oldListener the listener that was installed
   * @param newListener the listener to be installed
   */
  public void changeInputListener(InputListener oldListener, InputListener newListener) {
    // syncrhonize to prevent concurrent modifications to the listener
    synchronized(NoInputListener.ONLY) {
      if (_inputListener == oldListener) {
        _inputListener = newListener;
      }
      else {
        throw new IllegalArgumentException("The given old listener is not installed!");
      }
    }
  }

  /**
   * Gets input from the console through the currently installed input listener.
   * @return the console input
   */
  public String getConsoleInput() {
    _consoleDoc.insertPrompt();
    return _inputListener.getConsoleInput();
  }

  /**
   * Singleton InputListener which should never be asked for input.
   */
  private static class NoInputListener implements InputListener {
    public static final NoInputListener ONLY = new NoInputListener();
    private NoInputListener() {
    }

    public String getConsoleInput() {
      throw new IllegalStateException("No input listener installed!");
    }
  }

  private class ExtraClasspathOptionListener implements OptionListener<Vector<File>> {
    public void optionChanged (OptionEvent<Vector<File>> oce) {
      Vector<File> cp = oce.value;
      if(cp!=null) {
        Enumeration<File> en = cp.elements();
        while(en.hasMoreElements()) {
          _interactionsModel.addToClassPath(en.nextElement().getAbsolutePath());
        }
      }
    }
  }

  private static class BackUpFileOptionListener implements OptionListener<Boolean> {
    public void optionChanged (OptionEvent<Boolean> oe){
      Boolean value = oe.value;
      FileOps.DefaultFileSaver.setBackupsEnabled(value.booleanValue());
    }
  }
}

