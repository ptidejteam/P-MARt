/*
 * (c) Copyright 2001-2003 Yann-Ga�l Gu�h�neuc,
 * �cole des Mines de Nantes and Object Technology International, Inc.
 * 
 * Use and copying of this software and preparation of derivative works
 * based upon this software are permitted. Any copy of this software or
 * of any derivative work must include the above copyright notice of
 * the author, this paragraph and the one after it.
 * 
 * This software is made available AS IS, and THE AUTHOR DISCLAIMS
 * ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, AND NOT WITHSTANDING ANY OTHER PROVISION CONTAINED HEREIN,
 * ANY LIABILITY FOR DAMAGES RESULTING FROM THE SOFTWARE OR ITS USE IS
 * EXPRESSLY DISCLAIMED, WHETHER ARISING IN CONTRACT, TORT (INCLUDING
 * NEGLIGENCE) OR STRICT LIABILITY, EVEN IF THE AUTHOR IS ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * All Rights Reserved.
 */
package padl.creator;

import padl.util.repository.constituent.ConstituentRepository;

/**
 * @author Yann-Ga�l Gu�h�neuc
 * @since  2004/07/30
 * 
 * This idiom-level creator analyses Java class files to create
 * a model of a Java programs, including method invocations
 * between classes.
 */
public class CompleteClassFileCreator extends AbstractClassFileCreator {
	public CompleteClassFileCreator(
		final ConstituentRepository aConsituentRepository,
		final String[] someClassFiles) {

		this(aConsituentRepository, someClassFiles, false);
	}
	public CompleteClassFileCreator(
		final ConstituentRepository aConsituentRepository,
		final String[] someClassFiles,
		final boolean recurseIntoDirectories) {

		// Yann 2004/07/30: Object-orientation.
		// The flag "storeMethodInvocation" is *not* object-oriented
		// programming. However, it helps in increasing performances
		// by avoiding to recompute many variables to compute then
		// method invocations.
		super(
			aConsituentRepository,
			someClassFiles,
			recurseIntoDirectories,
			true);
	}
}
