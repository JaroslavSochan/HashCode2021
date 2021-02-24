package sk.google;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Main {

  public static void main(String[] args) throws IOException, URISyntaxException {

    List<String> inputs = Arrays
        .asList(
            "a_example",
            "b_little_bit_of_everything",
            "c_many_ingredients",
            "d_many_pizzas",
            "e_many_teams"
        );

    for (String input : inputs) {
      solve(input);
      System.out.println("Solved: " + input);
    }
  }

  private static void solve(String input) throws IOException, URISyntaxException {

    File file = new File(Main.class.getResource("resources/" + input + ".in").toURI());
    BufferedReader br = new BufferedReader(new FileReader(file));

    String line = br.readLine();

    List<Integer> firstLine = Arrays.stream(line.split(" ")).map(Integer::valueOf).collect(
        Collectors.toList());

    int team2 = firstLine.get(1);
    int team3 = firstLine.get(2);
    int team4 = firstLine.get(3);

    List<Pizza> pizzas = new ArrayList<>();

    int counter = 0;

    while ((line = br.readLine()) != null) {
      Pizza pizza = new Pizza(Arrays.stream(line.split(" ")).skip(1).collect(Collectors.toList()),
          counter);
      counter++;
      pizzas.add(pizza);
    }

    pizzas.sort((o1, o2) -> Integer.compare(o2.getIng().size(), o1.getIng().size()));
    ConcurrentHashMap<Integer, Pizza> allPizza = new ConcurrentHashMap<>();

    for (int i = 0; i < pizzas.size(); i++) {
      allPizza.put(pizzas.get(i).getId(), pizzas.get(i));
    }

    List<List<Pizza>> pizzaFor4 = new ArrayList<>();
    Set<Integer> idx = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    List<List<Pizza>> pizzaFor3 = new ArrayList<>();
    List<List<Pizza>> pizzaFor2 = new ArrayList<>();

    calculate(team4, pizzas, pizzaFor4, idx, 4, allPizza);
    calculate(team3, pizzas, pizzaFor3, idx, 3, allPizza);
    calculate(team2, pizzas, pizzaFor2, idx, 2, allPizza);

    StringBuffer buff = new StringBuffer();

    for (int i = 0; i < pizzaFor4.size(); i++) {
      buff.append("4 ");
      for (int j = 0; j < pizzaFor4.get(i).size(); j++) {
        buff.append(pizzaFor4.get(i).get(j).getId() + " ");
      }
      buff.append("\n");
    }

    for (int i = 0; i < pizzaFor3.size(); i++) {
      buff.append("3 ");
      for (int j = 0; j < pizzaFor3.get(i).size(); j++) {
        buff.append(pizzaFor3.get(i).get(j).getId() + " ");
      }
      buff.append("\n");
    }

    for (int i = 0; i < pizzaFor2.size(); i++) {
      buff.append("2 ");
      for (int j = 0; j < pizzaFor2.get(i).size(); j++) {
        buff.append(pizzaFor2.get(i).get(j).getId() + " ");
      }
      buff.append("\n");
    }

    int outCounter = pizzaFor4.size() + pizzaFor3.size() + pizzaFor2.size();

    buff.insert(0, +outCounter + "\n");

    File outFile = new File(Main.class.getResource("resources/" + input + ".out").toURI());
    BufferedWriter outBR = new BufferedWriter(new FileWriter(outFile));
    outBR.write(buff.toString());
    outBR.close();
  }

  private static void calculate(int team, List<Pizza> pizzas, List<List<Pizza>> pizzaFor,
      Set<Integer> idx, int teamSize,
      ConcurrentHashMap<Integer, Pizza> allPizza) {
    for (int i = 0; i < team; i++) {
      List<Pizza> current = new ArrayList<>();
      HashSet<String> ind = new HashSet<>();
      for (int k = 0; k < teamSize; k++) {

        AtomicReference<Integer> initBestNextId1 = new AtomicReference<>(-1);
        AtomicReference<Integer> initBestNextId2 = new AtomicReference<>(-1);
        AtomicReference<Integer> initBestNextValue = new AtomicReference<>(-1);

        if (current.isEmpty()) {

          List<Pizza> unusedPizza = pizzas.parallelStream().filter(p -> !idx.contains(p.getId()))
              .collect(
                  Collectors.toList());

          unusedPizza.parallelStream().forEach(p -> {
            for (Pizza pizza : unusedPizza) {
              if (p.getId() != pizza.getId()) {
                HashSet<String> newInd = new HashSet<>(pizza.getIng());
                newInd.addAll(p.getIng());
                if (newInd.size() > initBestNextValue.get()) {
                  initBestNextValue.set(newInd.size());
                  initBestNextId1.set(p.getId());
                  initBestNextId2.set(pizza.getId());
                }
              }
            }
          });

          if (initBestNextId1.get() == -1) {
            idx.removeAll(current.stream().map(c -> c.getIng()).collect(Collectors.toList()));
            break;
          }

          current.add(allPizza.get(initBestNextId1.get()));
          current.add(allPizza.get(initBestNextId2.get()));
          ind.addAll(allPizza.get(initBestNextId1.get()).getIng());
          ind.addAll(allPizza.get(initBestNextId2.get()).getIng());
          idx.add(initBestNextId1.get());
          idx.add(initBestNextId2.get());
          k++;
          continue;

        }
        AtomicReference<Integer> bestNextId = new AtomicReference<>(-1);
        AtomicReference<Integer> bestNextValue = new AtomicReference<>(-1);

        pizzas.parallelStream().forEach(p -> {
          if (!idx.contains(p.getId())) {
            HashSet<String> newInd = new HashSet<>(ind);
            newInd.addAll(p.getIng());
            if (newInd.size() > bestNextValue.get()) {
              bestNextValue.set(newInd.size());
              bestNextId.set(p.getId());
            }
          }
        });

        if (bestNextId.get() == -1) {
          idx.removeAll(current.stream().map(c -> c.getIng()).collect(Collectors.toList()));
          break;
        }
        current.add(allPizza.get(bestNextId.get()));
        idx.add(bestNextId.get());
      }
      if (current.size() > 1) {
        pizzaFor.add(current);
      } else {
        idx.removeAll(current.stream().map(c -> c.getIng()).collect(Collectors.toList()));
      }

      if (i % 1000 == 0) {
        System.out.println("Created: " + i + "/" + team);
      }
    }
  }
}
