package hadamard;

import cc.redberry.rings.poly.FiniteField;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;

import java.util.*;

import static cc.redberry.rings.Rings.GF;

/** Galois field based on p^k where k > 1. */
public class GaloisField {
  private final int p;
  private final int exponent;
  private final int q;
  private final FiniteField<UnivariatePolynomialZp64> gf;
  private final Polynomial divisor;
  private final List<Polynomial> fieldValues;

  public GaloisField(int p, int exponent) {
    this.p = p;
    this.exponent = exponent;
    this.q = (int) Math.pow(p, exponent);
    // I'm not sure the best way to determine the minimum irreducible polynomial,
    // so we use rings to get it.
    this.gf = GF(p, exponent);
    this.divisor = Polynomial.parse(gf.getMinimalPolynomial().toString());
    this.fieldValues = generateFieldValues();
  }

  public List<Polynomial> getFieldValues() {
    return fieldValues;
  }

  private List<Polynomial> generateFieldValues() {
    Iterator<UnivariatePolynomialZp64> it = gf.iterator();
    List<Polynomial> fieldPolynomials = new ArrayList<>();
    while (it.hasNext()) {
      // The elements of GF(p^k) are derived by using GF(p) as all possible combinations
      // of coefficients.  For example, for GF(3^2), GF(3) = 0, 1, 2, so the elements of
      // GF(p^k) are 0*x+0, 0*x+1, 0*x+2, 1*x+0, 1*x+1, 1*x+2, 2*x+0, 2*x+1, 2*x+2
      //   which are     0,     1,     2,   x,     x+1,   x+2, 2*x,   2*x+1, and 2*x+2.
      // If you use a base-3 notation (because GF(3) has three elements), these can be written as
      //                 0,     1,     2,   3,       4,     5,   6,       7,        8
      // where each power of 3 is a coefficient (a or b) of a*x + b.
      // We use rings to enumerate these, but we could do it ourselves.
      fieldPolynomials.add(Polynomial.parse(it.next().toString()));
    }
    return fieldPolynomials;
  }

  public Set<Polynomial> getQuadraticResidues() {
    Set<Polynomial> galoisFieldQuadraticResidues = new HashSet<>();
    for (Polynomial fieldPolynomial : fieldValues) {
      Polynomial squaredMod = fieldPolynomial.square().modulo(divisor).coefficientModulo(p);
      if (!squaredMod.isZero() && fieldValues.contains(squaredMod)) {
        galoisFieldQuadraticResidues.add(squaredMod);
      }
    }
    return galoisFieldQuadraticResidues;
  }
}
