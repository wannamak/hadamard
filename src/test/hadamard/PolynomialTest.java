package hadamard;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PolynomialTest {
  @Test
  public void testParseDegreeTwo() {
    Polynomial poly = Polynomial.parse("x^2");
    assertEquals(100, poly.getDecimalRepresentationBase10());
    assertEquals("x^2", poly.toString());
  }

  @Test
  public void testParseDegreeZeroOneTwo() {
    Polynomial poly = Polynomial.parse("1+2*x+x^2");
    assertEquals(121, poly.getDecimalRepresentationBase10());
    assertEquals("x^2+2*x+1", poly.toString());
  }

  @Test
  public void testParseDegreeZeroOneTwoOnes() {
    Polynomial poly = Polynomial.parse("1+x+x^2");
    assertEquals(111, poly.getDecimalRepresentationBase10());
    assertEquals("x^2+x+1", poly.toString());
  }

  @Test
  public void testParseThreeConstant() {
    Polynomial poly = Polynomial.parse("x^2+3");
    assertEquals(103, poly.getDecimalRepresentationBase10());
    assertEquals("x^2+3", poly.toString());
  }

  @Test
  public void testParseNegative() {
    Polynomial poly = Polynomial.parse("-1");
    assertEquals(-1, poly.getDecimalRepresentationBase10());
    assertEquals("-1", poly.toString());
  }

  @Test
  public void testSubtract() {
    Polynomial a = Polynomial.parse("3*x^2+2*x+1");
    Polynomial b = Polynomial.parse("x^2+3");
    assertEquals("2*x^2+2*x-2", a.minus(b).toString());
  }

  @Test
  public void testModulo() {
    Polynomial dividend = Polynomial.parse("x^3-2*x^2-4");
    Polynomial divisor = Polynomial.parse("x-3");
    assertEquals("5", dividend.modulo(divisor).toString());
  }

  @Test
  public void testModuloThreeSquared() {
    Polynomial dividend = Polynomial.parse("x^2");
    Polynomial divisor = Polynomial.parse("x^2+1");
    assertEquals("-1", dividend.modulo(divisor).toString());
  }

  @Test
  public void testModuloSquares() {
    Polynomial dividend = Polynomial.parse("2*x^2+x");
    Polynomial divisor = Polynomial.parse("x^2+1");
    assertEquals("x-2", dividend.modulo(divisor).toString());
    assertEquals("x+1", dividend.modulo(divisor).coefficientModulo(3).toString());
  }

  @Test
  public void testSquare() {
    Polynomial a = Polynomial.parse("3*x^2+2*x-1");
    assertEquals("9*x^4+12*x^3-2*x^2-4*x+1", a.square().toString());
  }
}