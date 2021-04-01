package bspector;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ArrayList<SettingsOption> settings = new ArrayList<SettingsOption>();
        SettingsOptionComposite soc1 = new SettingsOptionComposite(new BooleanSettingsOption(false, (item) -> false));
        settings.add(soc1);
        SettingsOptionComposite soc2 = new SettingsOptionComposite(new BooleanSettingsOption(false, (item) -> item.equals(true)));
        //SettingsOptionComposite soc2 = new SettingsOptionComposite(new BooleanOverride(false, (item) -> item.equals(true)));
        soc1.add(soc2);
        settings.add(soc2);
        for(int i = 0; i < 2; i++) {
            settings.forEach((option) -> {
                option.listAllItems();
                option.randomValue(new Random());
            });  
            System.out.println("\n====== " + i + " ========\n");
        }

    }
}

interface SettingsOption<T> {
    public T getItem();
    public void setItem(T item);
    public void randomValue(Random random);
    public void attemptRandomValue(Random random, Object item);
    public void listAllItems();
}

class BooleanSettingsOption implements SettingsOption<Boolean> {

    Boolean value;
    Predicate matches;

    public BooleanSettingsOption(Boolean value, Predicate matches) {
        this.value = value;
        this.matches = matches;
    }

    @Override
    public Boolean getItem() {
        System.out.println("BooleanSettingsOption - getItem");
        return value;
    }

    @Override
    public void setItem(Boolean item) {
        System.out.println("BooleanSettingsOption - setItem");
        value = item;
    }

    @Override
    public void randomValue(Random random) {
        System.out.println("BooleanSettingsOption - randomValue");
        setItem(random.nextInt(2) > 0 ? true : false);    
    }    

    @Override
    public void attemptRandomValue(Random random, Object item) {
        System.out.println("BooleanSettingsOption - attemptRandomItem");
        if (matches.test(item)) {
            randomValue(random);
        }
    }

    @Override
    public void listAllItems() {
        System.out.println("BooleanSettingsOption - listAllItems");
        System.out.println(getItem());
    }
}

// class BooleanOverride extends BooleanSettingsOption {

//     public BooleanOverride(Boolean value, Predicate matches) {
//         super(value, matches);
//         //TODO Auto-generated constructor stub
//     }
    
//     @Override
//     public void randomValue(Random random) {
//         System.out.println("BooleanOverride - randomValue");
//     }
// }

class SettingsOptionComposite<T> implements SettingsOption<T> {
    private ArrayList<SettingsOption> childOptions = new ArrayList<SettingsOption>();
    
    SettingsOption<T> value;

    public SettingsOptionComposite(SettingsOption<T> value) {
        this.value = value;
    }

    @Override
    public T getItem() {
        System.out.println("Composite - getItem");
        return value.getItem();
    }

    @Override
    public void setItem(T item) {
        System.out.println("Composite - setItem");
        value.setItem(item);
    }
    
    @Override
    public void randomValue(Random random) {
        System.out.println("Composite - randomValue");
        value.randomValue(random);
        childOptions.forEach((option) -> option.attemptRandomValue(random, getItem()));
    }

    @Override
    public void attemptRandomValue(Random random, Object item) {
        System.out.println("Composite - attemptRandomValue");
        value.attemptRandomValue(random, item);
    }

    @Override
    public void listAllItems() {
        System.out.println("Composite - listAllItems");
        System.out.println(value.getItem());
        childOptions.forEach((option) -> System.out.println(option.getItem()));
    }

    public void add(SettingsOption childOption) {
        childOptions.add(childOption);
    }
}