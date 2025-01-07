package hadamard;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Iterator;

public class Polynomial {
  private static final int MAX_NUM_COEFFICIENTS_SUPPORTED = 10;

  /**
   * Parses a polynomial with terms like +2*x^4.
   * Not super robust, but functional.
   * Doesn't handle arbitrary spaces.
   * Only handles a single variable named 'x'.
   */
  public static Polynomial parse(String str) {
    int[] coefficients = new int[MAX_NUM_COEFFICIENTS_SUPPORTED];
    Iterator<ParsedTerm> it = new ParsedTermIterator(str);
    while (it.hasNext()) {
      ParsedTerm term = it.next();
      int exponent = 0;
      int coefficient = 1;
      if (term.indexOf('x') != -1) {
        int times = term.indexOf('*');
        if (times != -1) {
          Preconditions.checkState(times > 0);
          coefficient = Integer.parseInt(term.termString.substring(0, times));
        }
        int caret = term.indexOf('^');
        if (caret != -1) {
          exponent = Integer.parseInt(term.termString.substring(caret + 1));
        } else {
          exponent = 1;
        }
      } else {
        coefficient = Integer.parseInt(term.termString);
      }

      if (term.isNegative) {
        coefficient *= -1;
      }

      coefficients[exponent] = coefficient;
    }
    return new Polynomial(coefficients);
  }

  /**
   * A substring of an input polynomial string that should be parsed.
   */
  private static class ParsedTerm {
    final boolean isNegative;
    final String termString;

    ParsedTerm(boolean isNegative, String termString) {
      this.isNegative = isNegative;
      this.termString = termString;
    }

    int indexOf(char c) {
      return termString.indexOf(c);
    }
  }

  /**
   * Splits an input polynomial string into terms to be parsed.
   */
  private static class ParsedTermIterator implements Iterator<ParsedTerm> {
    private final String input;
    private int nextTermStartIndex;
    private int nextTermEndIndex;
    private boolean isNextTermNegative;

    ParsedTermIterator(String input) {
      this.input = input;
    }

    @Override
    public boolean hasNext() {
      isNextTermNegative = false;
      if (nextTermStartIndex == input.length()) {
        return false;
      }
      if (input.charAt(nextTermStartIndex) == '-') {
        isNextTermNegative = true;
        nextTermStartIndex++;
      } else if (input.charAt(nextTermStartIndex) == '+') {
        nextTermStartIndex++;
      }
      int plus = input.indexOf('+', nextTermStartIndex);
      int minus = input.indexOf('-', nextTermStartIndex);
      Preconditions.checkState(plus != 0 && minus != 0);

      if (plus == -1 && minus != -1) {
        // only negative terms remain.
        nextTermEndIndex = minus;
      } else if (plus != -1 && minus == -1) {
        // only positive terms remain.
        nextTermEndIndex = plus;
      } else if (plus == -1 && minus == -1) {
        // only this term remains.
        nextTermEndIndex = input.length();
      } else {
        // negative and positive terms remain.
        Preconditions.checkState(plus > 0 && minus > 0);
        nextTermEndIndex = Math.min(plus, minus);
      }
      return true;
    }

    @Override
    public ParsedTerm next() {
      ParsedTerm result = new ParsedTerm(isNextTermNegative,
          input.substring(nextTermStartIndex, nextTermEndIndex));
      nextTermStartIndex = nextTermEndIndex;
      return result;
    }
  }

  private final int[] coefficients;

  /**
   * Polynomial is a immutable representation of a polynomial of degree up to
   * MAX_NUM_COEFFICIENTS_SUPPORTED - 1.
   */
  private Polynomial(int[] coefficients) {
    this.coefficients = coefficients;
  }

  public int getDecimalRepresentationBase10() {
    int sum = 0;
    for (int exponent = 0; exponent < MAX_NUM_COEFFICIENTS_SUPPORTED; exponent++) {
      // This representation doesn't capture negative coefficients higher than exponent 0.
      Preconditions.checkState(exponent == 0 || coefficients[exponent] >= 0);
      int multiplier = (int) Math.pow(10, exponent);
      sum += multiplier * coefficients[exponent];
    }
    return sum;
  }

  public int getDecimalRepresentationBase(int base) {
    int sum = 0;
    Polynomial coefficientMod = coefficientModulo(base);
    for (int exponent = 0; exponent < MAX_NUM_COEFFICIENTS_SUPPORTED; exponent++) {
      int multiplier = (int) Math.pow(base, exponent);
      sum += multiplier * coefficients[exponent];
    }
    return sum;
  }

  public Polynomial modulo(Polynomial divisor) {
    Polynomial remainder = copy();

    while (!remainder.isZero() && remainder.getDegree() >= divisor.getDegree()) {
      int remainderHighestDegree = remainder.getDegree();
      int divisorHighestDegree = divisor.getDegree();

      int quotientCoefficient = remainder.getCoefficient(remainderHighestDegree)
          / divisor.getCoefficient(divisorHighestDegree);
      int quotientExponent = remainderHighestDegree - divisorHighestDegree;

      int[] productCoefficients = new int[MAX_NUM_COEFFICIENTS_SUPPORTED];
      for (int divisorExponent = 0; divisorExponent <= divisorHighestDegree; divisorExponent++) {
        int productExponent = quotientExponent + divisorExponent;
        Preconditions.checkState(productExponent < MAX_NUM_COEFFICIENTS_SUPPORTED);
        productCoefficients[productExponent] +=
            divisor.getCoefficient(divisorExponent) * quotientCoefficient;
      }
      Polynomial product = new Polynomial(productCoefficients);
      remainder = remainder.minus(product);
    }

    return remainder;
  }

  /** Returns a new polynomial with all coefficients modulo the argument. */
  public Polynomial coefficientModulo(int modulo) {
    int[] modCoefficients = Arrays.copyOf(coefficients, MAX_NUM_COEFFICIENTS_SUPPORTED);
    for (int exponent = 0; exponent < MAX_NUM_COEFFICIENTS_SUPPORTED; exponent++) {
      modCoefficients[exponent] = Math.floorMod(coefficients[exponent], modulo);
    }
    return new Polynomial(modCoefficients);
  }

  public Polynomial minus(Polynomial subtrahend) {
    int[] minuendCoefficients = Arrays.copyOf(coefficients, MAX_NUM_COEFFICIENTS_SUPPORTED);
    for (int exponent = 0; exponent < MAX_NUM_COEFFICIENTS_SUPPORTED; exponent++) {
      minuendCoefficients[exponent] -= subtrahend.getCoefficient(exponent);
    }
    return new Polynomial(minuendCoefficients);
  }

  public boolean isZero() {
    for (int i = MAX_NUM_COEFFICIENTS_SUPPORTED - 1; i >= 0; i--) {
      if (coefficients[i] != 0) {
        return false;
      }
    }
    return true;
  }

  /** Returns the highest exponent present in the polynomial. */
  public int getDegree() {
    for (int exponent = MAX_NUM_COEFFICIENTS_SUPPORTED - 1; exponent >= 0; exponent--) {
      if (coefficients[exponent] != 0) {
        return exponent;
      }
    }
    return -1;
  }

  public int getCoefficient(int exponent) {
    return coefficients[exponent];
  }

  public Polynomial copy() {
    int[] newCoefficients = Arrays.copyOf(coefficients, MAX_NUM_COEFFICIENTS_SUPPORTED);
    return new Polynomial(newCoefficients);
  }

  public Polynomial square() {
    int[] squaredCoefficients = new int[MAX_NUM_COEFFICIENTS_SUPPORTED];
    for (int i = 0; i <= getDegree(); i++) {
      for (int j = 0; j <= getDegree(); j++) {
        squaredCoefficients[i + j] += coefficients[i] * coefficients[j];
      }
    }
    return new Polynomial(squaredCoefficients);
  }

  @Override
  public String toString() {
    if (isZero()) {
      return "0";
    }
    StringBuilder result = new StringBuilder();
    for (int exponent = MAX_NUM_COEFFICIENTS_SUPPORTED - 1; exponent >= 0; exponent--) {
      if (coefficients[exponent] != 0) {
        if (!result.isEmpty()) {
          if (coefficients[exponent] > 0) {
            result.append("+");
          } else if (exponent > 0) {
            // No need to add - for the constant, it will be printed by termToString.
            result.append("-");
          }
        }
        result.append(termToString(coefficients[exponent], exponent));
      }
    }
    return result.toString();
  }

  private String termToString(int coefficient, int exponent) {
    String result = "";
    int absCoefficient = Math.abs(coefficient);
    if (absCoefficient > 1 && exponent > 0) {
      result += String.format("%d*", absCoefficient);
    }
    if (exponent > 1) {
      result += String.format("x^%d", exponent);
    } else if (exponent == 1) {
      result += "x";
    } else if (exponent == 0) {
      result += String.format("%d", coefficient);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(coefficients);
  }

  @Override
  public boolean equals(Object that) {
    if (!(that instanceof Polynomial)) {
      return false;
    }
    return Arrays.equals(coefficients, ((Polynomial) that).coefficients);
  }
}
