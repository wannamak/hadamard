package hadamard;

import cc.redberry.rings.Rational;
import cc.redberry.rings.Ring;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.io.Coder;
import cc.redberry.rings.poly.FiniteField;
import cc.redberry.rings.poly.univar.UnivariatePolynomial;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

import static cc.redberry.rings.Rings.GF;
import static cc.redberry.rings.Rings.Q;



public class Paley {
  public static void main(String args[]) throws Exception {
    new Paley().run();
  }

  public void run() throws Exception {
    generateConstructionParameters();
    check(1, 3, 1, 4);
    check(1, 7, 1, 8);
    check(1, 11, 1, 12);
    check(1, 19, 1, 20);
    check(1, 23, 1, 24);
    check(2, 13, 1, 28);
    check(1, 31, 1, 32);
    check(2, 17, 1, 36);
    System.out.println("All valid.");
  }

  public void check(int constructionType, int p, int exponent, int expectedOrder) {
    Matrix matrix = constructionType == 1
        ? paleyConstructionOne(p, exponent)
        : paleyConstructionTwo(p, exponent);
    Preconditions.checkState(
        matrix.isHadamard() && expectedOrder == matrix.size(),
        String.format("Invalid %d^%d construction %d, expected order %d\n%s",
          p, exponent, constructionType, expectedOrder, matrix));
  }

  static class ConstructionMethod {
    enum Method { ONE, TWO };
    Method method;
    int p;
    int k;
    public ConstructionMethod(Method method, int p, int k) {
      this.method = method;
      this.p = p;
      this.k = k;
    }
  }

  public void generateConstructionParameters() {
    Multimap<Integer, ConstructionMethod> params = ArrayListMultimap.create();
    for (int p : ODD_PRIME_BELOW_ONE_HUNDRED) {
      for (int k = 1; k < 10; k++) {
        int q = (int) Math.pow(p, k);
        if (q > 2000) {
          continue;
        }
        if (q % 4 == 3) {
          params.put(q + 1, new ConstructionMethod(ConstructionMethod.Method.ONE, p, k));
        } else if (q % 4 == 1) {
          params.put(2 * (q+1), new ConstructionMethod(ConstructionMethod.Method.TWO, p, k));
        }
      }
    }
    List<Integer> orders = new ArrayList<>(params.keys().elementSet());
    Collections.sort(orders);
    for (int order : orders) {
      String pk = "";
      String methods = "";
      for (ConstructionMethod method : params.get(order)) {
        if (!pk.isEmpty()) {
          pk += ", ";
          methods += ", ";
        }
        if (method.k == 1) {
          pk += Integer.toString(method.p);
        } else {
          pk += String.format("%d^%d", method.p, method.k);
        }
        methods += method.method == ConstructionMethod.Method.ONE ? "I" : "II";
      }
      System.out.printf("     %2d &  %s &  %s \\\\\n", order, pk, methods);
    }
  }

  Set<Integer> ODD_PRIME_BELOW_ONE_HUNDRED = Set.of(3, 5, 7, 11,
      13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
      73, 79, 83, 89, 97,
      101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157,
      163, 167, 173, 179, 181, 191, 193, 197, 199);

  /**
   * Construction One = q congruent 3 (mod 4) be the power of an odd prime.
   * We have possible qs of 3, 7, 11, 19, 23, 31, 43 ...
   * Produces a H of order q + 1 (4, 8, 12, 20, 24, 32, 44 ...)
   * Skew matrix (X = -X^T) (?)
   */
  public Matrix paleyConstructionOne(int p, int exponent) {
    Preconditions.checkState(ODD_PRIME_BELOW_ONE_HUNDRED.contains(p), "p not prime?");
    int q = (int) Math.pow(p, exponent);
    Preconditions.checkState(q % 4 == 3, "q mod 4 != 3");

    int[][] values = new int[q+1][q+1];
    Arrays.fill(values[0], 1);
    for (int row = 0; row < q + 1; row++) {
      values[row][0] = 1;
    }

    fillJacobsthalMatrix(p, exponent, values, 1, 1);
    // Subtract identity, but just from the Jacobsthal.
    for (int row = 1; row < q + 1; row++) {
      values[row][row] -= 1;
    }
    return new Matrix(values);
  }

  /**
   * Construction Two = q congruent 1 (mod 4) be the power of an odd prime.
   * Possible qs of 1, 5, 9, 13, 17 ...
   * Produces a H of order 2(q + 1) (4, 12, 20, 28, ...)
   * Symmetric matrix (X = X^T) (?)
   */
  public Matrix paleyConstructionTwo(int p, int exponent) {
    Preconditions.checkState(ODD_PRIME_BELOW_ONE_HUNDRED.contains(p), "p not prime?");
    int q = (int) Math.pow(p, exponent);
    Preconditions.checkState(q % 4 == 1, "q mod 4 != 1");

    int[][] conferenceValues = new int[q+1][q+1];
    Arrays.fill(conferenceValues[0], 1);
    for (int row = 1; row < q + 1; row++) {
      conferenceValues[row][0] = 1;
    }
    conferenceValues[0][0] = 0;

    fillJacobsthalMatrix(p, exponent, conferenceValues, 1, 1);
    Matrix conference = new Matrix(conferenceValues);

    int[][] values = new int[2*q+2][2*q+2];
    Matrix identity = Matrix.identity(q+1);
    conference.plus(identity).copyTo(values, 0, 0);
    conference.minus(identity).copyTo(values, 0, q+1);
    conference.minus(identity).copyTo(values, q+1, 0);
    conference.negate().minus(identity).copyTo(values, q+1, q+1);

    return new Matrix(values);
  }

  private void fillJacobsthalMatrix(int p, int exponent, int[][] values, int startRow, int startColumn) {
    int q = (int) Math.pow(p, exponent);

    if (exponent == 1) {
      Set<Integer> galoisFieldQuadraticResidues = getPrimeGaloisFieldQuadraticResidues(q);
      for (int row = 0; row < q; row++) {
        for (int column = 0; column < q; column++) {
          int operand = ((row - column) + q) % q;
          int value = operand == 0 ? 0 : (galoisFieldQuadraticResidues.contains(operand) ? 1 : -1);
          values[startRow + row][startColumn + column] = value;
        }
      }
    } else {
      GaloisField gf = new GaloisField(p, exponent);
      List<Polynomial> fieldValues = gf.getFieldValues();
      Set<Polynomial> galoisFieldQuadraticResidues = gf.getQuadraticResidues();

      for (int row = 0; row < q; row++) {
        for (int column = 0; column < q; column++) {
          Polynomial difference = fieldValues.get(row).minus(fieldValues.get(column))
              .coefficientModulo(p);
          int value = difference.isZero() ? 0 :
              (galoisFieldQuadraticResidues.contains(difference) ? 1 : -1);
          values[startRow + row][startColumn + column] = value;
        }
      }
    }
  }

  private Set<Integer> getPrimeGaloisFieldQuadraticResidues(int q) {
    Set<Integer> galoisFieldQuadraticResidues = new HashSet<>();
    for (int i = 1; i < (q + 1) / 2; i++) {
      int square = ((int) Math.pow(i, 2)) % q;
      if (square != 0) {
        galoisFieldQuadraticResidues.add(square);
      }
    }
    return galoisFieldQuadraticResidues;
  }
}
