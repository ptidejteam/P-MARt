package pom.metrics.impl;

import java.util.Collection;
import java.util.Iterator;
import padl.IFileRepository;
import padl.kernel.IAbstractLevelModel;
import padl.kernel.IClass;
import padl.kernel.IConstructor;
import padl.kernel.IFirstClassEntity;

abstract class AbstractLCOM extends AbstractMetric {
	public AbstractLCOM(
		final IFileRepository aFileRepository,
		final IAbstractLevelModel anAbstractLevelModel) {

		super(aFileRepository, anAbstractLevelModel);
	}
	public String getDefinition() {
		return "";
	}
	protected double pairsOfMethodNotSharingFields(final IFirstClassEntity firstClassEntity) {
		if (!(firstClassEntity instanceof IClass)) {
			return 0;
		}
		// LCOM is the size of a set where are couples of method...
		// Why not just increment a local variable?
		double numberOfCouples = 0;
		// So we consider methods at the class level .
		final Collection methodsOfClass =
			super.classPrimitives.listOfOverriddenAndConcreteMethods(firstClassEntity);
		// For every method...
		for (final Iterator iterMethod1 = methodsOfClass.iterator(); iterMethod1
			.hasNext();) {
			final IConstructor method1 = (IConstructor) iterMethod1.next();
			// For a given method, we analyze other methods.
			// We can then make couples.
			for (final Iterator iterMethod2 = methodsOfClass.iterator(); iterMethod2
				.hasNext();) {
				final IConstructor method2 = (IConstructor) iterMethod2.next();
				// If the methods are different, we pass... (m1,m1)
				if (!method1.equals(method2)) {
					// If the intersection between AR(m1), AR(m2) and
					// allAttributes(class) is empty,
					// the condition is satisfied.
					if (super.operators.intersection(
						super.methodPrimitives.listOfFieldsUsedByMethod(
							firstClassEntity,
							method1),
						super.operators.intersection(
							super.methodPrimitives.listOfFieldsUsedByMethod(
								firstClassEntity,
								method2),
							super.classPrimitives
								.listOfImplementedFields(firstClassEntity))).size() == 0) {
						// Incrementation of an index instead of
						// storing the couple in a set.
						numberOfCouples++;
					}
				}
			}
		}
		return numberOfCouples;
	}
}
