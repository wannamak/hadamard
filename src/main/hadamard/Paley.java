package hadamard;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Paley {
  public static void main(String args[]) throws Exception {
    new Paley().run();
  }

  public void run() throws Exception {
    int[][] four = new int[][] {
        {1,1,1,1},
        {1,1,-1,-1},
        {1,-1,-1,1},
        {1,-1,1,-1}
    };
    Matrix hadamard = new Matrix(four);
    System.out.println(hadamard);
    System.out.println(hadamard.transpose());
    System.out.println(hadamard.times(hadamard.transpose()));
  }

  public void runToCheckTableOne() throws Exception {
    check(1, 3, 1, 4);
    check(1, 7, 1, 8);
    check(1, 11, 1, 12);
    check(2, 5, 1, 12);
    check(1, 19, 1, 20);
    check(2, 3, 2, 20);
    check(1, 23, 1, 24);
    check(2, 13, 1, 28);
    check(1, 3, 3, 28);
    check(1, 31, 1, 32);
    check(2, 17, 1, 36);
    check(1, 43, 1, 44);
    check(1, 47, 1, 48);
    check(2, 5, 2, 52);
    check(1, 59, 1, 60);
    check(2, 29, 1, 60);
    check(1, 67, 1, 68);
    check(1, 71, 1, 72);
    check(2, 37, 1, 76);
    check(1, 79, 1, 80);
    check(2, 41, 1, 84);
    check(1, 83, 1, 84);
    check(2, 7, 2, 100);
    check(1, 103, 1, 104);
    check(2, 53, 1, 108);
    check(1, 107, 1, 108);
    check(2, 61, 1, 124);
    check(1, 127, 1, 128);
    check(1, 131, 1, 132);
    check(1, 139, 1, 140);
    check(2, 73, 1, 148);
    check(1, 151, 1, 152);
    check(2, 3, 4, 164);
    check(1, 163, 1, 164);
    check(1, 167, 1, 168);
    check(1, 179, 1, 180);
    check(2, 89, 1, 180);
    check(1, 191, 1, 192);
    check(2, 97, 1, 196);
    check(1, 199, 1, 200);
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

  public static Set<Integer> ODD_PRIME_BELOW_TWO_HUNDRED = Set.of(3, 5, 7, 11,
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
    Preconditions.checkState(ODD_PRIME_BELOW_TWO_HUNDRED.contains(p), "p not prime?");
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
    Preconditions.checkState(ODD_PRIME_BELOW_TWO_HUNDRED.contains(p), "p not prime?");
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
