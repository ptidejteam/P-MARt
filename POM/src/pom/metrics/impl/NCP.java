/**
 * NCP - Number of Classes in a Package
 * 
 * The following metric is related to packages, and is based
 * on the paper "Butterflies: A Visual Approach to Characterize Packages",
 * by Ducasse, Lanza and Ponisio.
 * 
 * @author Karim DHAMBRI
 * @since  2005/??/?? 
 * 
 * @author Duc-Loc Huynh
 * @since  2005/08/18
 * 
 * Modifications made to fit the new architecture
 */

package pom.metrics.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import padl.IFileRepository;
import padl.kernel.IAbstractLevelModel;
import padl.kernel.IClass;
import padl.kernel.IFirstClassEntity;
import padl.kernel.IInterface;
import pom.metrics.IMetric;
import pom.metrics.IUnaryMetric;

public class NCP extends AbstractMetric implements IMetric, IUnaryMetric {
	public NCP(
		final IFileRepository aFileRepository,
		final IAbstractLevelModel anAbstractLevelModel) {

		super(aFileRepository, anAbstractLevelModel);
	}

	protected double concretelyCompute(final IFirstClassEntity firstClassEntity) {
		// String packageName = super.classPrimitives.extractPackageName(entity);
		// String key = packageName + " " + "NCP";
		//
		// if (!super.cachedResults.containsKey(key)) {
		// this.listOfElements(entity);
		// }
		//
		// final double result =
		// ((Double) super.cachedResults.get(key)).doubleValue();
		return new Double(this.listOfElements(firstClassEntity).size()).doubleValue();
	}

	public String getDefinition() {
		String def = "the number of classes package containing entity.";
		System.out.println(def);
		return def;
	}

	private List listOfElements(IFirstClassEntity firstClassEntity) {
		final List classesOfAnalysedPackage = new ArrayList();
		final String packageName =
			super.classPrimitives.extractPackageName(firstClassEntity);
		// final String key = packageName + " " + "NCP";

		final Iterator iterator =
			super.classPrimitives.getIteratorOnTopLevelEntities();
		while (iterator.hasNext()) {
			final IFirstClassEntity d = (IFirstClassEntity) iterator.next();
			if ((d instanceof IClass) || (d instanceof IInterface)) {
				if (super.classPrimitives.extractPackageName(d).equals(
					packageName)) {
					classesOfAnalysedPackage.add(d);
				}
			}
		}

		// super.cachedResults.put(
		// key,
		// new Double(classesOfAnalysedPackage.size()));

		return classesOfAnalysedPackage;
	}
}
