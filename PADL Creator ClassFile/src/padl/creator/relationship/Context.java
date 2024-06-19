/*
 * (c) Copyright 2000-2002 Yann-Ga�l Gu�h�neuc,
 * Ecole des Mines de Nantes and Object Technology International, Inc.
 * 
 * Use and copying of this software and preparation of derivative works
 * based upon this software are permitted. Any copy of this software or
 * of any derivative work must include the above copyright notice of
 * Yann-Ga�l Gu�h�neuc, this paragraph and the one after it.
 * 
 * This software is made available AS IS, and THE AUTHOR DISCLAIMS
 * ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE, AND NOT WITHSTANDING ANY OTHER PROVISION CONTAINED HEREIN, ANY
 * LIABILITY FOR DAMAGES RESULTING FROM THE SOFTWARE OR ITS USE IS
 * EXPRESSLY DISCLAIMED, WHETHER ARISING IN CONTRACT, TORT (INCLUDING
 * NEGLIGENCE) OR STRICT LIABILITY, EVEN IF YANN-GAEL GUEHENEUC IS ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * All Rights Reserved.
 */
package padl.creator.relationship;

import java.util.List;
import java.util.Map;
import padl.creator.util.ExtendedMethodInfo;
import padl.creator.util.ExtendedMethodInvocation;
import padl.creator.util.Utils;
import padl.event.IModelListener;
import padl.event.MessageInvocationEvent;
import padl.kernel.Constants;
import padl.kernel.IAbstractLevelModel;
import padl.kernel.IOperation;
import padl.kernel.IFirstClassEntity;
import com.ibm.toad.cfparse.ConstantPool;
import com.ibm.toad.cfparse.attributes.AttrInfoList;
import com.ibm.toad.cfparse.attributes.CodeAttrInfo;

/**
 * @version	0.1
 * @author 	Yann-Ga�l Gu�h�neuc
 */
public final class Context implements Cloneable {
	private static Map MapOfIDsEntities;
	public static void initialise(final Map aMapOfIDsEntities) {
		Context.MapOfIDsEntities = aMapOfIDsEntities;
	}
	private int cardinality = Constants.CARDINALITY_ONE;
	private ConstantPool constantPool;
	private IFirstClassEntity currentEntity;
	private final IAbstractLevelModel currentModel;
	private boolean internalFieldDeclaration = false;
	private char[] invocationSiteName = null;
	private boolean invocationSiteStatic = false;
	private List messageTypes;
	private ExtendedMethodInfo methodInfo;
	private char[] methodName;

	private byte[] rawCode;
	public Context(final Context oldContext) {
		this.currentEntity = oldContext.currentEntity;
		this.currentModel = oldContext.currentModel;
		this.methodInfo = oldContext.methodInfo;
		this.constantPool = oldContext.constantPool;
		this.messageTypes = oldContext.messageTypes;
		this.rawCode = oldContext.rawCode;
		this.methodName = oldContext.methodName;
	}
	public Context(final IAbstractLevelModel anAbstractLevelModel) {
		this.currentModel = anAbstractLevelModel;
	}
	//	public void addEntity(final IEntity entity) {
	//		try {
	//			this.currentModel.addConstituent(entity);
	//		}
	//		catch (final ModelDeclarationException mde) {
	//			// Yann 2006/02/10: Duplicate...
	//			// I solved most of the problems of duplicate
	//			// actors by reversing the loops in
	//			// createElements()
	//			mde.printStackTrace(Output.getInstance().errorOutput());
	//		}
	//	}
	//	public void addGhost(final String aName) {
	//		try {
	//			this.currentModel
	//				.addConstituent(Factory.getInstance().createGhost(aName));
	//		}
	//		catch (final ModelDeclarationException mde) {
	//			// Yann 2006/02/10: Duplicate...
	//			// I solved most of the problems of duplicate
	//			// actors by reversing the loops in
	//			// createElements()
	//			mde.printStackTrace(Output.getInstance().errorOutput());
	//		}
	//	}
	public void addMethodInvocation(
		final ExtendedMethodInvocation methodInvocation) {
		if (!this.messageTypes.contains(methodInvocation)) {
			this.currentModel.fireModelChange(
				IModelListener.MESSAGE_SEND_RECOGNIZED,
				new MessageInvocationEvent(
					methodInvocation.getOriginEntity(),
					methodInvocation.getTargetEntity()));
			this.messageTypes.add(methodInvocation);
		}
	}
	public IFirstClassEntity getEntityFromID(final char[] anID) {
		//	final IEntity entity = (IEntity) this.mapOfIDsEntities.get(anID);
		//	final IEntity entity2 =
		//		Utils.searchForEntity(this.currentModel, anID);
		return Utils.getEntityOrCreateGhost(
			this.currentModel,
			anID,
			Context.MapOfIDsEntities);
	}
	public int getCardinality() {
		return this.cardinality;
	}
	public ConstantPool getConstantPool() {
		return this.constantPool;
	}
	public IFirstClassEntity getCurrentEntity() {
		return this.currentEntity;
	}
	public IOperation getCurrentMethod() {
		return (IOperation) this.currentEntity.getConstituentFromID(this
			.getMethodName());
	}
	public IAbstractLevelModel getCurrentModel() {
		return this.currentModel;
	}
	public char[] getInvocationSiteName() {
		return this.invocationSiteName;
	}
	public int getMethodAccess() {
		return this.methodInfo.getVisibility();
	}
	public List getMethodInvocations() {
		return this.messageTypes;
	}
	public char[] getMethodName() {
		return this.methodName;
	}
	public byte[] getRawCode() {
		return this.rawCode;
	}
	public void initialise(
		final IFirstClassEntity anEntity,
		final ExtendedMethodInfo aMethodInfo,
		final List someMessageTypes) {

		this.currentEntity = anEntity;
		this.methodInfo = aMethodInfo;
		this.messageTypes = someMessageTypes;

		this.constantPool = this.methodInfo.getDeclaringClassConstantPool();
		//	if (Utils.isSpecialMethod(this.methodInfo.getName())) {
		this.methodName = Utils.computeSignature(this.methodInfo);
		//	}
		//	else {
		//		this.methodName = this.methodInfo.getName();
		//	}

		final AttrInfoList attributeInfoList = this.methodInfo.getAttributes();
		final CodeAttrInfo codeAttributeInfo =
			(CodeAttrInfo) attributeInfoList.get("Code");
		if (codeAttributeInfo != null) {
			this.rawCode = codeAttributeInfo.getCode();
		}
		else {
			this.rawCode = new byte[0];
		}
	}
	public boolean isInternalFieldDeclaration() {
		return this.internalFieldDeclaration;
	}
	public boolean isInvocationSiteStatic() {
		return this.invocationSiteStatic;
	}
	public void setCardinality(final int cardinality) {
		this.cardinality = cardinality;
	}
	public void setInternalFieldDeclaration(final boolean isInternal) {
		this.internalFieldDeclaration = isInternal;
	}
	public void setInvocationSiteName(final char[] invocationSiteName) {
		this.invocationSiteName = invocationSiteName;
	}
	public void setInvocationSiteStatic(final boolean isStatic) {
		this.invocationSiteStatic = isStatic;
	}
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		return buffer.toString();
	}
}
