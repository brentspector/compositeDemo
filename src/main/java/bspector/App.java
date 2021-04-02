package bspector;

import java.util.ArrayList;
import java.util.Arrays;
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
        SettingsOptionFactory.setSettingsList(settings);
        SettingsOptionComposite soc1 = SettingsOptionFactory.createSettingsOption(false);
        SettingsOptionComposite soc2 = SettingsOptionFactory.createSettingsOption(false, new PredicatePair(soc1, PredicatePair.BOOLEAN_TRUE));
        SettingsOptionComposite soc3 = SettingsOptionFactory.createSettingsOption(true, new PredicatePair(soc2, PredicatePair.BOOLEAN_TRUE));
        //SettingsOptionComposite soc4 = SettingsOptionFactory.createSettingsOption(1);
        for(int i = 0; i < 2; i++) {
            System.out.println("\n====== " + i + " ========\n");
            settings.stream().filter(SettingsOption::isParent).forEach((option) -> {
                option.listAllItems();
                option.randomValue(new Random());
            });  
        }

    }
}

interface SettingsOption<T> {
    public T getItem();
    public void setItem(T item);
    public Boolean isParent();
    public void setIsParent(Boolean value);
    public void randomValue(Random random);
    public void attemptRandomValue(Random random, SettingsOption item);
    public void listAllItems();
}

class BooleanSettingsOption implements SettingsOption<Boolean> {

    Boolean defaultValue;
    Boolean value;
    Boolean isParent;
    ArrayList<PredicatePair> matches;

    public BooleanSettingsOption(Boolean value, PredicatePair... matches) {
        this.defaultValue = value;
        this.value = value;
        this.isParent = false;
        this.matches = new ArrayList<PredicatePair>(Arrays.asList(matches));
    }

    @Override
    public Boolean getItem() {
        return value;
    }

    @Override
    public void setItem(Boolean item) {
        value = item;
    }

    @Override
    public Boolean isParent() {
        System.out.println("Boolean - isParent - " + isParent);
        return isParent;
    }

    @Override
    public void setIsParent(Boolean value) {
        isParent = value;
    }

    @Override
    public void randomValue(Random random) {
        setItem(random.nextInt(2) > 0 ? true : false);    
    }    

    @Override
    public void attemptRandomValue(Random random, SettingsOption item) {
        System.out.println("Item is " + item);
        if (matches.stream().anyMatch((match) -> match.test(item))) {
            System.out.println("Matches is true");
            randomValue(random);
        } else {
            System.out.println("Matches is false");
            setItem(defaultValue);
        }
    }

    @Override
    public void listAllItems() {
        System.out.println(getItem());
    }
}

class SettingsOptionComposite<T> implements SettingsOption<T> {
    private ArrayList<SettingsOption> childOptions = new ArrayList<SettingsOption>();
    
    SettingsOption<T> value;

    public SettingsOptionComposite(SettingsOption<T> value) {
        this.value = value;
    }

    @Override
    public T getItem() {
        return value.getItem();
    }

    @Override
    public void setItem(T item) {
        value.setItem(item);
    }

    @Override
    public Boolean isParent() {
        System.out.println("Composite - isParent");
        return value.isParent();
    }

    @Override
    public void setIsParent(Boolean isParent) {
        value.setIsParent(isParent);
    }
    
    @Override
    public void randomValue(Random random) {
        value.randomValue(random);
        childOptions.forEach((option) -> option.attemptRandomValue(random, this));
    }

    @Override
    public void attemptRandomValue(Random random, SettingsOption item) {
        value.attemptRandomValue(random, item);
        childOptions.forEach((option) -> option.attemptRandomValue(random, this));
    }

    @Override
    public void listAllItems() {
        System.out.println(value.getItem());
        childOptions.forEach((option) -> option.listAllItems());
    }

    public void add(SettingsOption childOption) {
        childOptions.add(childOption);
    }
}

class PredicatePair {
    public static final Predicate<Object> NO_REQUIREMENT = (item) -> true;
    public static final Predicate<Boolean> BOOLEAN_TRUE = (item) -> item.equals(true);

    public static final PredicatePair NONE = new PredicatePair(null, NO_REQUIREMENT);

    private SettingsOptionComposite parent;
    private Predicate parentState;
    
    public PredicatePair(SettingsOptionComposite parent, Predicate parentState) {
        this.parent = parent;
        this.parentState = parentState;
    }

    public Boolean test(SettingsOption item) {
        System.out.println("parent - " + parent + ", item is parent " + (item == parent));
        if (parent != null && item != parent) {
            return false;
        }
        return parentState.test(item.getItem());
    }

    public SettingsOptionComposite getParent() {
        return parent;
    }
}

class SettingsOptionFactory {
    private static SettingsOptionFactory instance;
    private static ArrayList<SettingsOption> settingsList;

    private SettingsOptionFactory() {}
    public static SettingsOptionFactory getInstance() {
        if (instance == null) {
            instance = new SettingsOptionFactory();
        }
        return instance;
    }

    public static void setSettingsList(ArrayList<SettingsOption> refList) {
        settingsList = refList;
    }

    /**
     * Creates a parent
     * @param value
     * @return
     */
    public static SettingsOptionComposite createSettingsOption(Object value) {
        SettingsOptionComposite soc = createComposite(value, PredicatePair.NONE);
        soc.setIsParent(true);
        settingsList.add(soc);
        return soc;
    }

    /**
     * Creates a child and attaches to parent
     * @param value
     * @param matches
     * @return
     */
    public static SettingsOptionComposite createSettingsOption(Object value, PredicatePair matches) {
        SettingsOptionComposite soc = createComposite(value, matches);
        soc.setIsParent(false);
        matches.getParent().add(soc);
        settingsList.add(soc);
        return soc;
    }

    private static SettingsOptionComposite createComposite(Object value, PredicatePair matches) {
        if (value instanceof Boolean) {
            return new SettingsOptionComposite<Boolean>(new BooleanSettingsOption((Boolean)value, matches));
        } else {
            throw new RuntimeException(value.getClass() + " has no supported factory.");
        }
    }
}