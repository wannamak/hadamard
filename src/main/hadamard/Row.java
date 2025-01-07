package hadamard;

import java.util.ArrayList;
import java.util.List;

public class Row {
  public static Row length(int length) {
    return new Row(length);
  }

  public static Row fromBits(int length, int x) {
    return new Row(populateFromBits(length, x));
  }

  public static Row fromArray(int[] values) {
    return new Row(values);
  }

  public static Row fromList(List<Integer> values) {
    return new Row(values);
  }

  private final int[] cells;

  private Row(int length) {
    cells = new int[length];
  }

  private Row(int[] values) {
    cells = values;
  }

  private Row(List<Integer> values) {
    cells = new int[values.size()];
    for (int i = 0; i < values.size(); i++) {
      cells[i] = values.get(i);
    }
  }

  public int length() {
    return cells.length;
  }

  public int at(int column) {
    return cells[column];
  }

  private static int[] populateFromBits(int length, int x) {
    int[] result = new int[length];
    for (int shift = 0; shift < result.length; shift++) {
      int value = (x >> shift) & 1;
      result[result.length - 1 - shift] = value == 1 ? 1 : -1;
    }
    return result;
  }

  public int dot(Row that) {
    int result = 0;
    for (int column = 0; column < cells.length; column++) {
      result += cells[column] * that.at(column);
    }
    return result;
  }

  public int diffDescriptor(Row that) {
    int result = 0;
    for (int column = 0; column < cells.length; column++) {
      int bit = cells[column] == that.at(column) ? 0 : 1;
      result |= (bit << (cells.length - 1 - column));
    }
//    System.out.println("Row " + this.toString() + " col0=" + cells[0]);
//    System.out.println("Row " + that.toString() + " col0=" + that.at(0));
//    System.out.println("  = " + result + "  (" + Integer.toBinaryString(result) + ")");
    return result;
  }

  public int numDifferences(Row that) {
    int numDifferences = 0;
    for (int column = 0; column < cells.length; column++) {
      if (cells[column] != that.at(column)) {
        numDifferences++;
      }
    }
    return numDifferences;
  }

  public int getSummaryInt() {
    int shift = 0;
    int result = 0;
    for (int column = cells.length - 1; column >= 0; column--) {
      result += (cells[column] == -1 ? 0 : 1) << shift++;
    }
    return result;
  }

  public boolean hasHomogeneousSign() {
    int expected = cells[0];
    for (int column = 1; column < cells.length; column++) {
      if (cells[column] != expected) {
        return false;
      }
    }
    return true;
  }

  public Row negate() {
    int[] newCells = new int[cells.length];
    for (int column = 0; column < cells.length; column++) {
      newCells[column] = -cells[column];
    }
    return new Row(newCells);
  }

  @Override
  public String toString() {
    String result = "";
    for (int column = 0; column < cells.length; column++) {
      result += String.format("%2d ", cells[column]);
    }
    return result;
  }
}
