package hadamard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;

/**
 * Tool to generate chart and code of construction parameters for Order 4-200 matrices
 * using either Paley construction.
 */
public class PaleyConstructionParameters {
  public static void main(String args[]) {
    new PaleyConstructionParameters().generateConstructions();
  }

  static int MAX_POWER = 10;
  static int MAX_ORDER = 200;

  static class ConstructionParameters {
    enum ConstructionMethod { ONE, TWO };
    ConstructionMethod constructionMethod;
    int p;
    int k;

    public ConstructionParameters(ConstructionMethod constructionMethod, int p, int k) {
      this.constructionMethod = constructionMethod;
      this.p = p;
      this.k = k;
    }
  }

  public void generateConstructions() {
    Multimap<Integer, ConstructionParameters> params = ArrayListMultimap.create();
    for (int p : Paley.ODD_PRIME_BELOW_TWO_HUNDRED) {
      for (int k = 1; k <= MAX_POWER; k++) {
        double qDouble = Math.pow(p, k);
        if (qDouble > 10 * MAX_ORDER) {
          continue;
        }
        int q = (int) qDouble;
        if (q % 4 == 3) {
          params.put(q + 1,
              new ConstructionParameters(ConstructionParameters.ConstructionMethod.ONE, p, k));
        } else if (q % 4 == 1) {
          params.put(2 * (q + 1),
              new ConstructionParameters(ConstructionParameters.ConstructionMethod.TWO, p, k));
        }
      }
    }

    List<Integer> orders = params.keys().elementSet().stream()
        .filter(order -> order <= MAX_ORDER)
        .sorted()
        .toList();

    for (int order : orders) {
      for (ConstructionParameters param : params.get(order)) {
        System.out.println(getOrderJava(order, param));
      }
    }

    for (int i = 0; i < (orders.size() + 1) / 2; i++) {
      int leftOrder = orders.get(i);
      System.out.printf("    %s ", getOrderTex(leftOrder, params.get(leftOrder)));
      int rightIndex = (orders.size() / 2) + i;
      if (rightIndex < orders.size()) {
        int rightOrder = orders.get(rightIndex);
        System.out.printf(" & %s ",
            getOrderTex(rightOrder, params.get(rightOrder)));
        System.out.println("\\\\");
      }
    }
  }

  private String getOrderJava(int order, ConstructionParameters param) {
    return String.format("    check(%d, %d, %d, %d);",
        param.constructionMethod == ConstructionParameters.ConstructionMethod.ONE ? 1 : 2,
        param.p, param.k, order);
  }

  private String getOrderTex(int order, Collection<ConstructionParameters> params) {
    String typeI = "";
    String typeII = "";
    for (ConstructionParameters param : params) {
      if (param.constructionMethod == ConstructionParameters.ConstructionMethod.ONE) {
        Preconditions.checkState(typeI.isEmpty());
        typeI = getPkText(param);
      } else {
        Preconditions.checkState(param.constructionMethod == ConstructionParameters.ConstructionMethod.TWO);
        Preconditions.checkState(typeII.isEmpty());
        typeII = getPkText(param);
      }
    }
    return String.format(" %3d & %s & %s", order, typeI, typeII);
  }

  private String getPkText(ConstructionParameters param) {
    if (param.k == 1) {
      return Integer.toString(param.p);
    } else {
      return String.format("$%d^%d$", param.p, param.k);
    }
  }
}
