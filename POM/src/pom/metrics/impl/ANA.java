/*
 * (c) Copyright 2001-2004 Yann-Ga�l Gu�h�neuc,
 * University of Montr�al.
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
package pom.metrics.impl;

import java.util.Iterator;
import java.util.List;
import padl.IFileRepository;
import padl.kernel.IAbstractLevelModel;
import padl.kernel.IClass;
import padl.kernel.IFirstClassEntity;
import padl.kernel.IGhost;
import padl.kernel.IInterface;
import pom.metrics.IMetric;
import pom.metrics.IUnaryMetric;

/**
 * @author Foutse Khomh
 * @since 2007/03/01
 */
public class ANA extends AbstractMetric implements IMetric, IUnaryMetric {
	public ANA(
		final IFileRepository aFileRepository,
		final IAbstractLevelModel anAbstractLevelModel) {

		super(aFileRepository, anAbstractLevelModel);
	}
	public double concretelyCompute(final IFirstClassEntity firstClassEntity) {
		this.abstractLevelModel.getNumberOfTopLevelEntities(IGhost.class);
		final Iterator iter =
			this.abstractLevelModel.getIteratorOnTopLevelEntities();

		// we need just classes
		double nba = 0;
		double nbofClassInterf = 0;
		while (iter.hasNext()) {
			final IFirstClassEntity anElement = (IFirstClassEntity) iter.next();

			if (anElement instanceof IClass) {
				nbofClassInterf = nbofClassInterf + 1;
				final List parent = listOfElements(anElement);
				nba = nba + parent.size();
				//System.out.println(anElement.getDisplayName()+":  "+  parent.size());
			}
			else if (anElement instanceof IInterface) {
				nbofClassInterf = nbofClassInterf + 1;
			}
		}
		return (nba / nbofClassInterf);
		// return (nba / (double) (this.anAbstractLevelModel
		// .getNumberOfConstituents(IClass.class) + this.anAbstractLevelModel
		// .getNumberOfConstituents(IInterface.class)));
	}
	public String getDefinition() {
		final String def =
			"Average number of classes from which a class inherits informations";
		return def;
	}
	private List listOfElements(final IFirstClassEntity firstClassEntity) {
		return this.classPrimitives.listOfAllDirectParents(firstClassEntity);
	}
}
