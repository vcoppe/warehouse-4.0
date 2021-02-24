package observer;

import java.util.LinkedList;

public class Observable {

    private final LinkedList<Observer> observers;

    public Observable() {
        this.observers = new LinkedList<>();
    }

    public void attach(Observer observer) {
        this.observers.add(observer);
    }

    protected void changed() {
        for (Observer observer : this.observers) {
            observer.update(this);
        }
    }

}
