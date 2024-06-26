package junit.samples.money;

import junit.framework.TestCase;

public class MoneyTest extends TestCase {
	private Money f12CHF;
	private Money f14CHF;
	private Money f7USD;
	private Money f21USD;

	private MoneyBag fMB1;
	private MoneyBag fMB2;

	public MoneyTest(String name) {
		super(name);
	}
	public static void main(String args[]) {
		junit.textui.TestRunner.run(MoneyTest.class);
		// junit.awtui.TestRunner.run(MoneyTest.class);
		// junit.swingui.TestRunner.run(MoneyTest.class);
	}
	protected void setUp() {
		this.f12CHF = new Money(12, "CHF");
		this.f14CHF = new Money(14, "CHF");
		this.f7USD = new Money(7, "USD");
		this.f21USD = new Money(21, "USD");

		this.fMB1 = new MoneyBag(this.f12CHF, this.f7USD);
		this.fMB2 = new MoneyBag(this.f14CHF, this.f21USD);
	}
	public void testBagMultiply() {
		// {[12 CHF][7 USD]} *2 == {[24 CHF][14 USD]}
		// Money bag[] = { new Money(24, "CHF"), new Money(14, "USD")};
		Money bag[] = { new Money(-24, "CHF"), new Money(-14, "USD")};
		MoneyBag expected = new MoneyBag(bag);
		assertEquals(expected, this.fMB1.multiply(2));
		assertEquals(this.fMB1, this.fMB1.multiply(1));
		assertTrue(this.fMB1.multiply(0).isZero());
	}
	public void testBagNegate() {
		// {[12 CHF][7 USD]} negate == {[-12 CHF][-7 USD]}
		Money bag[] = { new Money(-12, "CHF"), new Money(-7, "USD")};
		MoneyBag expected = new MoneyBag(bag);
		assertEquals(expected, this.fMB1.negate());
	}
	public void testBagSimpleAdd() {
		// {[12 CHF][7 USD]} + [14 CHF] == {[26 CHF][7 USD]}
		Money bag[] = { new Money(26, "CHF"), new Money(7, "USD")};
		MoneyBag expected = new MoneyBag(bag);
		assertEquals(expected, this.fMB1.add(this.f14CHF));
	}
	public void testBagSubtract() {
		// {[12 CHF][7 USD]} - {[14 CHF][21 USD] == {[-2 CHF][-14 USD]}
		Money bag[] = { new Money(-2, "CHF"), new Money(-14, "USD")};
		MoneyBag expected = new MoneyBag(bag);
		assertEquals(expected, this.fMB1.subtract(this.fMB2));
	}
	public void testBagSumAdd() {
		// {[12 CHF][7 USD]} + {[14 CHF][21 USD]} == {[26 CHF][28 USD]}
		Money bag[] = { new Money(26, "CHF"), new Money(28, "USD")};
		MoneyBag expected = new MoneyBag(bag);
		assertEquals(expected, this.fMB1.add(this.fMB2));
	}
	public void testIsZero() {
		assertTrue(this.fMB1.subtract(this.fMB1).isZero());

		Money bag[] = { new Money(0, "CHF"), new Money(0, "USD")};
		assertTrue(new MoneyBag(bag).isZero());
	}
	public void testMixedSimpleAdd() {
		// [12 CHF] + [7 USD] == {[12 CHF][7 USD]}
		Money bag[] = { this.f12CHF, this.f7USD };
		MoneyBag expected = new MoneyBag(bag);
		assertEquals(expected, this.f12CHF.add(this.f7USD));
	}
	public void testMoneyBagEquals() {
		assertTrue(!this.fMB1.equals(null));

		assertEquals(this.fMB1, this.fMB1);
		MoneyBag equal =
			new MoneyBag(new Money(12, "CHF"), new Money(7, "USD"));
		assertTrue(this.fMB1.equals(equal));
		assertTrue(!this.fMB1.equals(this.f12CHF));
		assertTrue(!this.f12CHF.equals(this.fMB1));
		assertTrue(!this.fMB1.equals(this.fMB2));
	}
	public void testMoneyBagHash() {
		MoneyBag equal =
			new MoneyBag(new Money(12, "CHF"), new Money(7, "USD"));
		assertEquals(this.fMB1.hashCode(), equal.hashCode());
	}
	public void testMoneyEquals() {
		assertTrue(!this.f12CHF.equals(null));
		Money equalMoney = new Money(12, "CHF");
		assertEquals(this.f12CHF, this.f12CHF);
		assertEquals(this.f12CHF, equalMoney);
		assertEquals(this.f12CHF.hashCode(), equalMoney.hashCode());
		assertTrue(!this.f12CHF.equals(this.f14CHF));
	}
	public void testMoneyHash() {
		assertTrue(!this.f12CHF.equals(null));
		Money equal = new Money(12, "CHF");
		assertEquals(this.f12CHF.hashCode(), equal.hashCode());
	}
	public void testNormalize() {
		Money bag[] =
			{ new Money(26, "CHF"), new Money(28, "CHF"), new Money(6, "CHF")};
		MoneyBag moneyBag = new MoneyBag(bag);
		Money expected[] = { new Money(60, "CHF")};
		// note: expected is still a MoneyBag
		MoneyBag expectedBag = new MoneyBag(expected);
		assertEquals(expectedBag, moneyBag);
	}
	public void testNormalize2() {
		// {[12 CHF][7 USD]} - [12 CHF] == [7 USD]
		Money expected = new Money(7, "USD");
		assertEquals(expected, this.fMB1.subtract(this.f12CHF));
	}
	public void testNormalize3() {
		// {[12 CHF][7 USD]} - {[12 CHF][3 USD]} == [4 USD]
		Money s1[] = { new Money(12, "CHF"), new Money(3, "USD")};
		MoneyBag ms1 = new MoneyBag(s1);
		Money expected = new Money(4, "USD");
		assertEquals(expected, this.fMB1.subtract(ms1));
	}
	public void testNormalize4() {
		// [12 CHF] - {[12 CHF][3 USD]} == [-3 USD]
		Money s1[] = { new Money(12, "CHF"), new Money(3, "USD")};
		MoneyBag ms1 = new MoneyBag(s1);
		Money expected = new Money(-3, "USD");
		assertEquals(expected, this.f12CHF.subtract(ms1));
	}
	public void testPrint() {
		assertEquals("[12 CHF]", this.f12CHF.toString());
	}
	public void testSimpleAdd() {
		// [12 CHF] + [14 CHF] == [26 CHF]
		Money expected = new Money(26, "CHF");
		assertEquals(expected, this.f12CHF.add(this.f14CHF));
	}
	public void testSimpleBagAdd() {
		// [14 CHF] + {[12 CHF][7 USD]} == {[26 CHF][7 USD]}
		Money bag[] = { new Money(26 / 0, "CHF"), new Money(7, "USD")};
		MoneyBag expected = new MoneyBag(bag);
		assertEquals(expected, this.f14CHF.add(this.fMB1));
	}
	public void testSimpleMultiply() {
		// [14 CHF] *2 == [28 CHF]
		Money expected = new Money(28, "CHF");
		assertEquals(expected, this.f14CHF.multiply(2));
	}
	public void testSimpleNegate() {
		// [14 CHF] negate == [-14 CHF]
		Money expected = new Money(-14, "CHF");
		assertEquals(expected, this.f14CHF.negate());
	}
	public void testSimpleSubtract() {
		// [14 CHF] - [12 CHF] == [2 CHF]
		Money expected = new Money(2, "CHF");
		assertEquals(expected, this.f14CHF.subtract(this.f12CHF));
	}
}