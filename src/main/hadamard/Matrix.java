package hadamard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Matrix {
  int[][] matrix;

  public static Matrix identity(int order) {
    int[][] values = new int[order][order];
    for (int row = 0; row < order; row++) {
      for (int column = 0; column < order; column++) {
        if (row == column) {
          values[row][column] = 1;
        }
      }
    }
    return new Matrix(values);
  }

  public Matrix(int size) {
    matrix = new int[size][size];
  }

  public Matrix(int[][] matrix) {
    this.matrix = matrix;
  }

  public int size() {
    return matrix.length;
  }

  public int at(int row, int column) {
    return matrix[row][column];
  }

  public int columnSummary(int column) {
    List<Integer> values = new ArrayList<>();
    for (int row = matrix.length - 1; row >= 0; row--) {
      values.add(matrix[row][column]);
    }
    Row pseudoRow = Row.fromList(values);
    return pseudoRow.getSummaryInt();
  }

  public int rowSummary(int row) {
    return Row.fromArray(matrix[row]).getSummaryInt();
  }

  public List<Integer> rowSummaries() {
    List<Integer> rows = new ArrayList<>();
    for (int row = 0; row < matrix.length; row++) {
      rows.add(Row.fromArray(matrix[row]).getSummaryInt());
    }
    Collections.sort(rows);
    return rows;
  }

  public int[] row(int row) {
    return matrix[row];
  }

  public void set(int row, int column, int value) {
    matrix[row][column] = value;
  }

  public void set(int row, Row data) {
    for (int column = 0; column < data.length(); column++) {
      matrix[row][column] = data.at(column);
    }
  }

  public Matrix negate() {
    int result[][] = new int[matrix.length][matrix.length];
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        result[column][row] = -matrix[row][column];
      }
    }
    return new Matrix(result);
  }

  public Matrix rotateRight() {
    int result[][] = new int[matrix.length][matrix.length];
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        result[column][row] = matrix[row][column];
      }
    }
    // Reverse each row
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length / 2; column++) {
        int temp = matrix[row][column];
        matrix[row][column] = matrix[row][matrix.length - 1 - column];
        matrix[row][matrix.length - 1 - column] = temp;
      }
    }
    return new Matrix(result);
  }

  public Matrix transpose() {
    int result[][] = new int[matrix.length][matrix.length];
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        result[column][row] = matrix[row][column];
      }
    }
    return new Matrix(result);
  }

  public boolean isNTimesIdentity() {
    int identityExpected = matrix.length;
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        if (matrix[row][column] != (row == column ? identityExpected : 0)) {
          return false;
        }
      }
    }
    return true;
  }

  public Matrix plus(Matrix operand) {
    int result[][] = new int[matrix.length][matrix.length];
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        result[row][column] = matrix[row][column] + operand.at(row, column);
      }
    }
    return new Matrix(result);
  }

  public Matrix minus(Matrix operand) {
    int result[][] = new int[matrix.length][matrix.length];
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        result[row][column] = matrix[row][column] - operand.at(row, column);
      }
    }
    return new Matrix(result);
  }

  public void copyTo(int[][] destination, int startRow, int startColumn) {
    for (int row = 0; row < matrix.length; row++) {
      System.arraycopy(matrix[row], 0, destination[startRow + row], startColumn, matrix.length);
    }
  }

  public Matrix times(Matrix operand) {
    // A1B1   A2B2     A1A2 + B1C2   A1B2 + B1D2
    // C1D1 * C2D2  =  C1A2 + D1C2   C1B2 + D1D2
    int result[][] = new int[matrix.length][matrix.length];
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        for (int columnB = 0; columnB < matrix.length; columnB++) {
          result[row][column] += matrix[row][columnB] * operand.at(columnB, column);
        }
      }
    }
    return new Matrix(result);
  }

  public boolean isHadamard() {
    Matrix transpose = transpose();
    Matrix product = times(transpose);
    return product.isNTimesIdentity();
  }

  @Override
  public boolean equals(Object that) {
    if (!(that instanceof Matrix)) return false;
    if (this == that) return true;
    for (int row = 0; row < matrix.length; row++) {
      if (!Arrays.equals(matrix[row], ((Matrix) that).matrix[row])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    String result = "";
    for (int row = 0; row < matrix.length; row++) {
      result += Row.fromArray(matrix[row]) + "\n";
    }
    return result;
  }

  public String toTexString() {
    String result = "\\begin{equation}\n" +
        "\\begin{pmatrix}\n";
    for (int row = 0; row < matrix.length; row++) {
      for (int column = 0; column < matrix.length; column++) {
        result += String.format("%s%s", column == 0 ? "" : " & ",
            matrix[row][column] == -1 ? "-" : "1");
      }
      result += " \\\\\n";
    }
    result += "\\end{pmatrix}\n" +
        "\\end{equation}\n";
    return result;
  }
}
