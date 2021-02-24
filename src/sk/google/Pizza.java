package sk.google;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pizza {

  private List<String> ing = new ArrayList<>();
  private int id;

  public Pizza(List<String> ing, int counter) {
    this.ing.addAll(ing);
    this.id = counter;

  }

  public int points(Pizza pizza) {
    Set set = new HashSet(pizza.getIng());
    set.addAll(ing);
    return set.size();
  }

  public List<String> getIng() {
    return ing;
  }

  public void setIng(ArrayList<String> ing) {
    this.ing = ing;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
